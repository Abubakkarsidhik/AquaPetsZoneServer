package com.aquapetszone.kmp.routes

import com.aquapetszone.kmp.domain.model.request.FirebaseLoginRequest
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.repository.user.FirebaseUserAuthService
import com.aquapetszone.kmp.helper.handleError
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.firebaseAuthRoutes() {

    val authService by lazy { FirebaseUserAuthService() }

    route("/api/v1/auth") {
        post("/firebase-login") {
            try {
                val request = call.receive<FirebaseLoginRequest>()

                if (request.idToken.isBlank()) {
                    throw Exception("Firebase ID token is required")
                }

                val result = authService.firebaseLogin(request.idToken.trim())

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "Login successful",
                        code = 200,
                        data = result
                    )
                )
            } catch (e: Exception) {
                call.handleError(e)
            }
        }
    }
}
