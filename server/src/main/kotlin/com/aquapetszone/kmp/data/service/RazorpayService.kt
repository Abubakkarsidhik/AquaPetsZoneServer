package com.aquapetszone.kmp.data.service

import com.aquapetszone.kmp.config.RazorpayConfig
import com.aquapetszone.kmp.utils.PaymentAuditLogger
import com.aquapetszone.kmp.utils.PaymentSecurityUtil
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object RazorpayService {

    private val json = Json {
        prettyPrint = false
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 15_000
            socketTimeoutMillis = 30_000
        }
    }

    suspend fun createOrder(
        amountPaise: Long,
        receipt: String,
        currency: String = RazorpayConfig.CURRENCY_INR,
        notes: Map<String, String> = emptyMap()
    ): RazorpayOrderDto {
        val response = client.post("${RazorpayConfig.BASE_URL}/orders") {
            basicAuth(RazorpayConfig.keyId, RazorpayConfig.keySecret)
            contentType(ContentType.Application.Json)
            setBody(
                RazorpayCreateOrderRequest(
                    amount = amountPaise,
                    currency = currency,
                    receipt = receipt,
                    notes = notes.ifEmpty { null }
                )
            )
        }
        return parseResponse(response.bodyAsText(), response.status.value) {
            json.decodeFromString<RazorpayOrderDto>(it)
        }
    }

    suspend fun fetchOrder(razorpayOrderId: String): RazorpayOrderDto {
        val response = client.get("${RazorpayConfig.BASE_URL}/orders/$razorpayOrderId") {
            basicAuth(RazorpayConfig.keyId, RazorpayConfig.keySecret)
        }
        return parseResponse(response.bodyAsText(), response.status.value) {
            json.decodeFromString<RazorpayOrderDto>(it)
        }
    }

    suspend fun fetchPayment(razorpayPaymentId: String): RazorpayPaymentDto {
        val response = client.get("${RazorpayConfig.BASE_URL}/payments/$razorpayPaymentId") {
            basicAuth(RazorpayConfig.keyId, RazorpayConfig.keySecret)
        }
        return parseResponse(response.bodyAsText(), response.status.value) {
            json.decodeFromString<RazorpayPaymentDto>(it)
        }
    }

    suspend fun refundPayment(
        razorpayPaymentId: String,
        amountPaise: Long? = null,
        notes: Map<String, String> = emptyMap(),
        speed: String = "normal"
    ): RazorpayRefundDto {
        val response = client.post("${RazorpayConfig.BASE_URL}/payments/$razorpayPaymentId/refund") {
            basicAuth(RazorpayConfig.keyId, RazorpayConfig.keySecret)
            contentType(ContentType.Application.Json)
            setBody(
                RazorpayRefundRequest(
                    amount = amountPaise,
                    speed = speed,
                    notes = notes.ifEmpty { null }
                )
            )
        }
        return parseResponse(response.bodyAsText(), response.status.value) {
            json.decodeFromString<RazorpayRefundDto>(it)
        }
    }

    suspend fun capturePayment(
        razorpayPaymentId: String,
        amountPaise: Long,
        currency: String = RazorpayConfig.CURRENCY_INR
    ): RazorpayPaymentDto {
        val response = client.post("${RazorpayConfig.BASE_URL}/payments/$razorpayPaymentId/capture") {
            basicAuth(RazorpayConfig.keyId, RazorpayConfig.keySecret)
            contentType(ContentType.Application.Json)
            setBody(
                RazorpayCaptureRequest(
                    amount = amountPaise,
                    currency = currency
                )
            )
        }
        return parseResponse(response.bodyAsText(), response.status.value) {
            json.decodeFromString<RazorpayPaymentDto>(it)
        }
    }

    fun verifySignature(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ): Boolean {
        return PaymentSecurityUtil.verifyPaymentSignature(
            orderId = razorpayOrderId,
            paymentId = razorpayPaymentId,
            signature = razorpaySignature,
            secret = RazorpayConfig.keySecret
        )
    }

    fun verifyWebhookSignature(rawBody: String, signature: String): Boolean {
        return PaymentSecurityUtil.verifyWebhookSignature(
            rawBody = rawBody,
            signature = signature,
            webhookSecret = RazorpayConfig.webhookSecret
        )
    }

    suspend fun processWebhook(
        rawBody: String,
        signature: String
    ): RazorpayWebhookPayload {
        if (!verifyWebhookSignature(rawBody, signature)) {
            PaymentAuditLogger.log(
                "WEBHOOK_FAILED",
                mapOf("reason" to "invalid_signature")
            )
            throw Exception("Invalid webhook signature")
        }

        PaymentAuditLogger.log(
            "WEBHOOK_RECEIVED",
            mapOf("payloadLength" to rawBody.length.toString())
        )

        return try {
            json.decodeFromString<RazorpayWebhookPayload>(rawBody)
        } catch (e: Exception) {
            PaymentAuditLogger.log(
                "WEBHOOK_FAILED",
                mapOf("reason" to "parse_error", "message" to (e.message ?: "unknown"))
            )
            throw Exception("Invalid webhook payload")
        }
    }

    private inline fun <T> parseResponse(
        body: String,
        statusCode: Int,
        parser: (String) -> T
    ): T {
        println("========== RAZORPAY ==========")
        println("STATUS: $statusCode")
        println("BODY: ${body.take(500)}")
        println("==============================")
        if (statusCode !in 200..299) {
            val errorMessage = extractErrorMessage(body)
            throw Exception(errorMessage)
        }
        return parser(body)
    }

    private fun extractErrorMessage(body: String): String {
        return try {
            val obj = json.parseToJsonElement(body).jsonObject
            obj["error"]?.jsonObject?.get("description")?.jsonPrimitive?.content
                ?: obj["message"]?.jsonPrimitive?.content
                ?: "Razorpay API error"
        } catch (_: Exception) {
            "Razorpay API error"
        }
    }
}

@Serializable
data class RazorpayCreateOrderRequest(
    val amount: Long,
    val currency: String,
    val receipt: String,
    val notes: Map<String, String>? = null
)

@Serializable
data class RazorpayCaptureRequest(
    val amount: Long,
    val currency: String
)

@Serializable
data class RazorpayRefundRequest(
    val amount: Long? = null,
    val speed: String? = null,
    val notes: Map<String, String>? = null
)

@Serializable
data class RazorpayOrderDto(
    val id: String,
    val entity: String? = null,
    val amount: Long,
    val amount_paid: Long? = null,
    val amount_due: Long? = null,
    val currency: String,
    val receipt: String? = null,
    val status: String? = null,
    val notes: Map<String, String>? = null,
    val created_at: Long? = null
)

@Serializable
data class RazorpayPaymentDto(
    val id: String,
    val entity: String? = null,
    val amount: Long,
    val currency: String,
    val status: String? = null,
    val order_id: String? = null,
    val method: String? = null,
    val captured: Boolean? = null,
    val error_code: String? = null,
    val error_description: String? = null,
    val created_at: Long? = null
)

@Serializable
data class RazorpayRefundDto(
    val id: String,
    val entity: String? = null,
    val amount: Long,
    val currency: String? = null,
    val payment_id: String? = null,
    val status: String? = null,
    val notes: Map<String, String>? = null,
    val created_at: Long? = null
)

@Serializable
data class RazorpayWebhookPayload(
    val entity: String? = null,
    val account_id: String? = null,
    val event: String,
    val contains: List<String>? = null,
    @SerialName("payload")
    val payload: RazorpayWebhookEventPayload? = null,
    val created_at: Long? = null
)

@Serializable
data class RazorpayWebhookEventPayload(
    val payment: RazorpayWebhookEntityWrapper? = null,
    val order: RazorpayWebhookEntityWrapper? = null,
    val refund: RazorpayWebhookEntityWrapper? = null
)

@Serializable
data class RazorpayWebhookEntityWrapper(
    val entity: JsonObject? = null
)
