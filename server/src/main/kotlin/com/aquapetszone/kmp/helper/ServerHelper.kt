package com.aquapetszone.kmp.helper

import com.aquapetszone.kmp.domain.model.response.ApiErrResponse
import com.aquapetszone.kmp.domain.repository.onboarding.OnboardingOperationResult
import com.aquapetszone.kmp.domain.repository.onboarding.OperationType
import com.aquapetszone.kmp.domain.repository.onboarding.toResponse
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.request.receive
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.intercept
import io.ktor.server.routing.post
import kotlinx.serialization.SerializationException

fun Route.authorizeRoles(
    vararg roles: String,
    block: Route.() -> Unit
) {
    authenticate("auth-jwt") {

        intercept(ApplicationCallPipeline.Call) {

            println("------ AUTHORIZE ROLES INTERCEPTOR ------")
            println("PATH: ${call.request.uri}")
            println("AUTH HEADER: ${call.request.headers["Authorization"]?.take(30)}...")

            val principal = call.principal<JWTPrincipal>()
                ?: run {
                    println("AUTHORIZE: No principal found — token invalid or missing")
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiErrResponse(401, "Invalid token", false)
                    )
                    finish()   // ✅ STOP PIPELINE
                    return@intercept
                }

            val role = principal.getClaim("role", String::class)

            if (role !in roles) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ApiErrResponse(403, "Forbidden", false)
                )
                finish()   // ✅ STOP PIPELINE
                return@intercept
            }
        }

        block()
    }
}

fun JWTPrincipal.userId(): String =
    payload.getClaim("userId").asString()

fun JWTPrincipal.role(): String =
    payload.getClaim("role").asString()


suspend fun ApplicationCall.handleError(e: Exception) {
    val message = e.message ?: "Unknown error"

    when {
        // ── Request body parsing / serialization failures → 400 ──
        e is BadRequestException ||
        e is SerializationException ||
        e is ContentTransformationException ||
        message.contains("Failed to convert request body", true) ||
        message.contains("Failed to read", true) ||
        message.contains("Illegal input", true) ||
        message.contains("missing", true) ||
        message.contains("field '", true) -> {
            // Extract a user-friendly message from serialization errors
            val friendlyMsg = when {
                message.contains("field '") -> {
                    // e.g. "Field 'upiId' is required for type ..."
                    val fieldName = Regex("field '(\\w+)'").find(message)?.groupValues?.getOrNull(1)
                    if (fieldName != null) "Missing or invalid field: '$fieldName'"
                    else "Invalid request body: $message"
                }
                message.contains("Failed to convert request body") -> {
                    val cause = e.cause?.message
                    if (cause != null) "Invalid request body: $cause"
                    else "Invalid request body. Please check all required fields."
                }
                else -> "Invalid request body: $message"
            }
            respond(HttpStatusCode.BadRequest, ApiErrResponse(400, friendlyMsg))
        }

        // ── Validation / domain errors → 400 ──
        message.contains("required", true) ||
        message.contains("must be", true) ||
        message.contains("do not match", true) ||
        message.contains("at least", true) -> {
            respond(HttpStatusCode.BadRequest, ApiErrResponse(400, message))
        }

        // ── Auth errors → 401 ──
        message.contains("Unauthorized", true) ||
        message.contains("Invalid", true) -> {
            respond(HttpStatusCode.Unauthorized, ApiErrResponse(401, message))
        }

        // ── Forbidden ──
        message.contains("Forbidden", true) -> {
            respond(HttpStatusCode.Forbidden, ApiErrResponse(403, message))
        }

        // ── Not found ──
        message.contains("not found", true) -> {
            respond(HttpStatusCode.NotFound, ApiErrResponse(404, message))
        }

        // ── Catch-all → 500 ──
        else -> {
            e.printStackTrace()
            respond(
                HttpStatusCode.InternalServerError,
                ApiErrResponse(
                    success = false,
                    code = 500,
                    message = "Something went wrong"
                )
            )
        }
    }
}

fun resolveMessage(
    section: String,
    operation: OperationType
): String? {
    return when (operation) {
        OperationType.CREATED -> "$section created successfully"
        OperationType.UPDATED -> "$section updated successfully"
        OperationType.NO_CHANGE -> null
    }
}

inline fun <reified T : Any> Route.handleOnboardingStep(
    sectionName: String,
    crossinline action: suspend (ApplicationCall, T) -> OnboardingOperationResult
) {
    post {
        try {
            val request = call.receive<T>()

            val result = action(call, request)

            val response = result.onboarding.toResponse()

            val message = when (result.operation) {
                OperationType.CREATED -> "$sectionName created successfully"
                OperationType.UPDATED -> "$sectionName updated successfully"
                OperationType.NO_CHANGE -> null
            }

            call.respond(
                HttpStatusCode.OK,
                ApiSuccessResponse(
                    message = message,
                    code = 200,
                    data = response
                )
            )

        } catch (e: Exception) {
            call.handleError(e)
        }
    }
}