package com.aquapetszone.kmp.routes

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.config.JwtConfig
import com.aquapetszone.kmp.data.database.MongoFactory
import com.aquapetszone.kmp.domain.model.request.RefreshTokenRequest
import com.aquapetszone.kmp.domain.model.request.LoginRequest
import com.aquapetszone.kmp.domain.model.request.SendOtpRequest
import com.aquapetszone.kmp.domain.model.request.VerifyOtpRequest
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.model.response.ApiErrResponse
import com.aquapetszone.kmp.domain.model.response.LoginResponse
import com.aquapetszone.kmp.domain.model.response.SendOtpResponse
import com.aquapetszone.kmp.domain.repository.auth.ServerAuthRepositoryImpl
import com.aquapetszone.kmp.domain.repository.onboarding.OnboardingMongo
import com.aquapetszone.kmp.domain.repository.onboarding.OnboardingStatus
import com.aquapetszone.kmp.helper.role
import com.aquapetszone.kmp.helper.userId
import com.aquapetszone.kmp.utils.CryptoUtil
import com.aquapetszone.kmp.utils.PasswordUtil
import com.mongodb.client.model.Filters
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes() {

    val collection = MongoFactory.database.getCollection<OnboardingMongo>("onboarding")
    val repository = ServerAuthRepositoryImpl()
    post("/login") {

        // 1) Receive request body
        val request = try {
            call.receive<LoginRequest>()
        } catch (e: Exception) {
            val detail = e.cause?.message ?: e.message ?: "Invalid request"
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrResponse(400, "Invalid request body: $detail")
            )
            return@post
        }

        // 2) Basic validation — empty fields
        if (request.email.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrResponse(400, "Email is required")
            )
            return@post
        }

        if (request.password.isBlank()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ApiErrResponse(400, "Password is required")
            )
            return@post
        }

        // 3) Encrypt email to match stored format in DB
        val normalizedEmail = request.email.trim().lowercase()
        val encryptedEmail = CryptoUtil.encrypt(normalizedEmail)

        // 4) Look up user by encrypted email
        val existing = collection.find(
            Filters.eq("account.email", encryptedEmail)
        ).first()

        if (existing == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiErrResponse(401, "No account found with this email. Please check your email or start onboarding.")
            )
            return@post
        }

        // 5) Verify bcrypt password
        val storedHash = existing.account?.password
        if (storedHash == null) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiErrResponse(401, "Account setup incomplete. Please complete onboarding first.")
            )
            return@post
        }

        if (!PasswordUtil.verify(request.password, storedHash)) {
            call.respond(
                HttpStatusCode.Unauthorized,
                ApiErrResponse(401, "Incorrect password. Please try again.")
            )
            return@post
        }

        // 6) Generate JWT tokens (access + refresh)
        val userId = existing.userId.toHexString()
        val role = Constant.ROLE.SELLER

        val token = JwtConfig.generateAccessToken(
            userId = userId,
            role = role
        )

        val refreshToken = JwtConfig.generateRefreshToken(
            userId = userId,
            role = role
        )

        val isOnboardingCompleted = existing.onboardingStatus == OnboardingStatus.APPROVED

        // 7) Success response
        call.respond(
            HttpStatusCode.OK,
            ApiSuccessResponse(
                message = "Login successful",
                code = 200,
                data = LoginResponse(
                    token = token,
                    refreshToken = refreshToken,
                    userId = userId,
                    role = role,
                    currentStep = existing.currentStep,
                    isOnboardingCompleted = isOnboardingCompleted,
                    onboardingStatus = existing.onboardingStatus,
                    rejectionReason = existing.rejectionReason
                )
            )
        )
    }

    // Validate session — lightweight JWT check returning latest onboarding state

    route("/auth") {

        authenticate("auth-jwt") {
            get("/validate-session") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: run {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ApiErrResponse(401, "Session expired. Please login again.")
                            )
                            return@get
                        }

                    val userId = principal.userId()
                    val role = principal.role()

                    val objectUserId = org.bson.types.ObjectId(userId)
                    val existing = collection.find(
                        Filters.eq("userId", objectUserId)
                    ).first()

                    if (existing == null) {
                        call.respond(
                            HttpStatusCode.Unauthorized,
                            ApiErrResponse(401, "Account not found. Please start onboarding.")
                        )
                        return@get
                    }

                    val isOnboardingCompleted = existing.onboardingStatus == OnboardingStatus.APPROVED

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Session valid",
                            code = 200,
                            data = LoginResponse(
                                token = "", // No need to regenerate token
                                userId = userId,
                                role = role,
                                currentStep = existing.currentStep,
                                isOnboardingCompleted = isOnboardingCompleted,
                                onboardingStatus = existing.onboardingStatus,
                                rejectionReason = existing.rejectionReason
                            )
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ApiErrResponse(401, "Session expired. Please login again.")
                    )
                }
            }
        }

        post("/send-otp") {
            val req = try {
                call.receive<SendOtpRequest>()
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(400, "Invalid request body: ${e.message}")
                )
                return@post
            }

            if (req.data.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(400, "Phone number is required")
                )
                return@post
            }


            try {
                val sessionInfo = repository.sendOtp(
                    mobile = req.data,
                    recaptchaToken = req.recaptchaToken
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "OTP sent successfully",
                        code = 200,
                        data = SendOtpResponse(
                            sessionInfo = sessionInfo
                        )
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiErrResponse(500, "Failed to send OTP. Please try again.")
                )
            }
        }
        post("/verify-otp") {

            val req = try {

                call.receive<VerifyOtpRequest>()

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(
                        400,
                        "Invalid request body: ${e.message}"
                    )
                )

                return@post
            }

            if (req.mobile.isBlank()) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(
                        400,
                        "Mobile number is required"
                    )
                )

                return@post
            }

            if (req.otp.isBlank()) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(
                        400,
                        "OTP is required"
                    )
                )

                return@post
            }

            try {

                val loginResponse = repository.verifyOtp(
                    mobile = req.mobile,
                    otp = req.otp,
                    sessionInfo = req.sessionInfo
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "OTP verified successfully",
                        code = 200,
                        data = loginResponse
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiErrResponse(
                        500,
                        e.message ?: "Failed to verify OTP"
                    )
                )
            }
        }
        post("/resend-otp") {

            val req = try {

                call.receive<SendOtpRequest>()

            } catch (e: Exception) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(
                        400,
                        "Invalid request body"
                    )
                )

                return@post
            }

            if (req.data.isBlank()) {

                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(
                        400,
                        "Mobile number required"
                    )
                )

                return@post
            }

            try {

                repository.resendOtp(
                    mobile = req.data
                )

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        code = 200,
                        message = "OTP resent successfully",
                        data = true
                    )
                )

            } catch (e: Exception) {

                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiErrResponse(
                        500,
                        e.message ?: "Failed to resend OTP"
                    )
                )
            }
        }

        post("/refresh-token") {
            val request = try {
                call.receive<RefreshTokenRequest>()
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(400, "Invalid request body")
                )
                return@post
            }

            if (request.refreshToken.isBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiErrResponse(400, "Refresh token is required")
                )
                return@post
            }

            // Decode the refresh token (allows expired access tokens, but refresh must be valid)
            val decoded = JwtConfig.decodeTokenUnsafe(request.refreshToken)
            if (decoded == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Invalid refresh token. Please login again.")
                )
                return@post
            }

            // Check that this is actually a refresh token (not an access token)
            val tokenType = decoded.getClaim("type")?.asString()
            if (tokenType != "refresh") {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Invalid token type. Please login again.")
                )
                return@post
            }

            // Check if the refresh token itself is expired
            val expiresAt = decoded.expiresAt
            if (expiresAt != null && expiresAt.before(java.util.Date())) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Session expired. Please login again.")
                )
                return@post
            }

            val userId = decoded.getClaim("userId")?.asString()
            if (userId.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Invalid session. Please login again.")
                )
                return@post
            }

            // Verify user still exists in DB
            val objectUserId = try {
                org.bson.types.ObjectId(userId)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Invalid session. Please login again.")
                )
                return@post
            }

            val existing = collection.find(
                Filters.eq("userId", objectUserId)
            ).first()

            if (existing == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ApiErrResponse(401, "Account not found. Please start onboarding.")
                )
                return@post
            }

            // Issue new token pair
            val role = decoded.getClaim("role")?.asString() ?: Constant.ROLE.SELLER
            val newAccessToken = JwtConfig.generateAccessToken(userId = userId, role = role)
            val newRefreshToken = JwtConfig.generateRefreshToken(userId = userId, role = role)

            val isOnboardingCompleted = existing.onboardingStatus == OnboardingStatus.APPROVED

            call.respond(
                HttpStatusCode.OK,
                ApiSuccessResponse(
                    message = "Token refreshed successfully",
                    code = 200,
                    data = LoginResponse(
                        token = newAccessToken,
                        refreshToken = newRefreshToken,
                        userId = userId,
                        role = role,
                        currentStep = existing.currentStep,
                        isOnboardingCompleted = isOnboardingCompleted,
                        onboardingStatus = existing.onboardingStatus,
                        rejectionReason = existing.rejectionReason
                    )
                )
            )
        }
    }
    // ─── Refresh Token (Public — accepts expired access token via refresh token) ───


}