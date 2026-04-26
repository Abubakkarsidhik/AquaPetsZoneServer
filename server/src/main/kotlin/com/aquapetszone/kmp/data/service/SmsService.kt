package com.aquapetszone.kmp.data.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

// TODO hide key
private const val FIREBASE_API_KEY = "AIzaSyAbhZDDihTpJXY0MEoLuSlfQL7GVygvq2I"

object SmsService {


    private val client = HttpClient {

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

    suspend fun sendOtp(phone: String, recaptchaToken: String): String {

        val response: HttpResponse = client.post(
            "https://identitytoolkit.googleapis.com/v1/accounts:sendVerificationCode?key=$FIREBASE_API_KEY"
        ) {

            contentType(ContentType.Application.Json)

            setBody(
                mapOf(
                    "phoneNumber" to phone,
                    "recaptchaToken" to recaptchaToken
                )
            )
        }

        val bodyText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception("Firebase error: $bodyText")
        }

        val json = Json.parseToJsonElement(bodyText).jsonObject

        return json["sessionInfo"]?.jsonPrimitive?.content
            ?: throw Exception("sessionInfo missing: $bodyText")
    }

    suspend fun verifyOtp(sessionInfo: String, otp: String): String {

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

        val bodyText = response.bodyAsText()

        if (!response.status.isSuccess()) {
            throw Exception("Firebase error: $bodyText")
        }

        val json = Json.parseToJsonElement(bodyText).jsonObject

        return json["idToken"]?.jsonPrimitive?.content
            ?: throw Exception("idToken missing: $bodyText")
    }
}