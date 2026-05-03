package com.aquapetszone.kmp.data.service

import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
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

object Msg91Service {

    private val dotenv by lazy {
        dotenv {
            ignoreIfMissing = true
        }
    }

    private fun env(key: String): String? {
        return System.getenv(key) ?: dotenv[key]
    }

    private val AUTH_KEY =
        env("MSG91_AUTH_KEY")
            ?: error("MSG91_AUTH_KEY not set")

    private val TEMPLATE_ID =
        env("MSG91_TEMPLATE_ID")
            ?: error("MSG91_TEMPLATE_ID not set")

    private val client = HttpClient(CIO) {

        install(ContentNegotiation) {

            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                }
            )
        }
    }

    suspend fun sendOtp(
        mobile: String
    ): String {

        val response = client.post(
            "https://control.msg91.com/api/v5/otp"
        ) {

            parameter("authkey", AUTH_KEY)
            parameter("template_id", TEMPLATE_ID)
            parameter("mobile", "91$mobile")

            contentType(ContentType.Application.Json)

            setBody(
                Msg91OtpRequest(
                     otpLength = 6
                )
            )
        }

        val body = response.bodyAsText()
        println("========== MSG91 ==========")
        println("STATUS: ${response.status}")
        println("BODY: $body")
        println("===========================")
        if (!response.status.isSuccess()) {
            throw Exception(body)
        }

        return body
    }

    suspend fun verifyOtp(
        mobile: String,
        otp: String
    ): Boolean {

        val response = client.get(
            "https://control.msg91.com/api/v5/otp/verify"
        ) {

            header("authkey", AUTH_KEY)

            parameter("mobile", "91$mobile")
            parameter("otp", otp)
        }

        val body = response.bodyAsText()
        println("========== MSG91 ==========")
        println("STATUS: ${response.status}")
        println("BODY: $body")
        println("===========================")
        return response.status.isSuccess() &&
                body.contains("success", true)
    }

    suspend fun resendOtp(
        mobile: String
    ): String {

        val response = client.get(
            "https://control.msg91.com/api/v5/otp/retry"
        ) {

            header("authkey", AUTH_KEY)

            parameter("mobile", "91$mobile")

            // text = SMS retry
            parameter("retrytype", "text")
        }

        val body = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception(body)
        }

        return body
    }
}

@Serializable
data class Msg91OtpRequest(
    @SerialName("otp_length")
    val otpLength: Int
)
