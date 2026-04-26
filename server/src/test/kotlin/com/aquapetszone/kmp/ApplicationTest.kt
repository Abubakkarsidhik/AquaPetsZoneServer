package com.aquapetszone.kmp

import com.aquapetszone.kmp.plugins.configureRouting
import com.aquapetszone.kmp.plugins.configureSerialization
import com.aquapetszone.kmp.plugins.configureSecurity
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    @Test
    fun testHomeFeed() = testApplication {
        application {
            configureSerialization()
            configureSecurity()
            configureRouting()
        }
        val response = client.get("/home-feed")
        assertEquals(HttpStatusCode.OK, response.status)
        // Optionally, check the response body for ApiSuccessResponse structure
        // val body = response.bodyAsText()
        // assertTrue(body.contains("Home feed fetched successfully"))
    }
}