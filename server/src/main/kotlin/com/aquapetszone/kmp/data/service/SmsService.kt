package com.aquapetszone.kmp.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*

private const val FIREBASE_API_KEY = "AIzaSyD_qzELhiC7OaSP49MM6C5Djpfz0xiRC-o"

object SmsService {

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
        phone: String,
        recaptchaToken: String
    ): String {

        val formattedPhone = formatIndianPhone(phone)

        val response = client.post(
            "https://identitytoolkit.googleapis.com/v1/accounts:sendVerificationCode?key=$FIREBASE_API_KEY"
        ) {

            contentType(ContentType.Application.Json)

            setBody(
                buildJsonObject {
                    put("phoneNumber", formattedPhone)
                    put("recaptchaToken", recaptchaToken)
                }
            )
        }

        val body = response.bodyAsText()

        println(body)

        if (!response.status.isSuccess()) {
            throw Exception(body)
        }

        return Json.parseToJsonElement(body)
            .jsonObject["sessionInfo"]
            ?.jsonPrimitive
            ?.content
            ?: throw Exception("sessionInfo missing")
    }

    suspend fun verifyOtp(
        sessionInfo: String,
        otp: String
    ): String {

        val response: HttpResponse = client.post(
            "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPhoneNumber?key=$FIREBASE_API_KEY"
        ) {

            contentType(ContentType.Application.Json)

            setBody(
                mapOf(
                    "sessionInfo" to sessionInfo,
                    "code" to otp
                )
            )
        }

        val body = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception(body)
        }

        val json = Json.parseToJsonElement(body).jsonObject

        return json["idToken"]
            ?.jsonPrimitive
            ?.content
            ?: throw Exception("idToken missing")
    }

    private fun formatIndianPhone(phone: String): String {

        val cleaned = phone
            .replace(" ", "")
            .replace("-", "")
            .trim()

        return when {
            cleaned.startsWith("+91") -> cleaned
            cleaned.startsWith("91") -> "+$cleaned"
            cleaned.length == 10 -> "+91$cleaned"
            else -> throw Exception("Invalid mobile number format")
        }
    }

}