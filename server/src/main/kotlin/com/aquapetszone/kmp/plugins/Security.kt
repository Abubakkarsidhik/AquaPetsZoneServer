package com.aquapetszone.kmp.plugins

import com.aquapetszone.kmp.config.JwtConfig
import com.aquapetszone.kmp.domain.model.response.ApiErrResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    install(Authentication) {
        jwt("auth-jwt") {

            verifier(JwtConfig.verifier())

            validate { credential ->

                println("------ JWT VALIDATE BLOCK HIT ------")
                println("RAW AUTH HEADER: ${request.headers["Authorization"]}")
                println("RAW CLAIMS: ${credential.payload.claims}")
                println("ISSUER FROM TOKEN: ${credential.payload.issuer}")
                println("USER ID: ${credential.payload.getClaim("userId").asString()}")
                println("ROLE: ${credential.payload.getClaim("role").asString()}")
                println("TOKEN TYPE: ${credential.payload.getClaim("type").asString()}")
                println("EXPIRES AT: ${credential.payload.expiresAt}")

                JWTPrincipal(credential.payload)
            }

            challenge { _, _ ->
                println("------ JWT CHALLENGE TRIGGERED ------")
                println("TOKEN FAILED BEFORE PRINCIPAL CREATION")
                println("RAW AUTH HEADER: ${call.request.headers["Authorization"]}")
                val authHeader = call.request.headers["Authorization"]
                if (authHeader.isNullOrBlank()) {
                    println("REASON: No Authorization header sent by client")
                } else if (!authHeader.startsWith("Bearer ")) {
                    println("REASON: Authorization header missing 'Bearer ' prefix")
                } else {
                    println("REASON: Token verification failed (expired/invalid signature/wrong issuer)")
                }

                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(
                        success = false,
                        code = 401,
                        message = "Token validation failed"
                    )
                )
            }
        }
    }


}
