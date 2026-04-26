package com.aquapetszone.kmp.routes.onboardingRoutes

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.config.JwtConfig
import com.aquapetszone.kmp.domain.repository.onboarding.ServerOnboardingRepositoryImpl
import com.aquapetszone.kmp.domain.model.request.OnboardingAccountRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingBusinessRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingCatalogRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingComplianceRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingLogisticsRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingPaymentRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingStoreRequest
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.repository.onboarding.OperationType
import com.aquapetszone.kmp.domain.repository.onboarding.toResponse
import com.aquapetszone.kmp.helper.authorizeRoles
import com.aquapetszone.kmp.helper.handleError
import com.aquapetszone.kmp.helper.handleOnboardingStep
import com.aquapetszone.kmp.helper.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

fun Route.onboardingRoute() {

    val repo = ServerOnboardingRepositoryImpl()

    route("/onboarding") {

        // Account (Public) — returns tokens since this is initial auth
        route("/createAccount") {
            post {
                try {
                    val request = call.receive<OnboardingAccountRequest>()
                    val result = repo.createAccountInformation(request)

                    val userId = result.onboarding.userId.toHexString()

                    val token = JwtConfig.generateAccessToken(
                        userId = userId,
                        role = Constant.ROLE.SELLER
                    )
                    val refreshToken = JwtConfig.generateRefreshToken(
                        userId = userId,
                        role = Constant.ROLE.SELLER
                    )

                    val response = result.onboarding.toResponse(token, refreshToken)

                    val message = when (result.operation) {
                        OperationType.CREATED -> "Account created successfully"
                        OperationType.UPDATED -> "Account updated successfully"
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

        // Store (Seller Only)
        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createStore") {
                handleOnboardingStep<OnboardingStoreRequest>("Store") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createStoreInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createBusiness") {
                handleOnboardingStep<OnboardingBusinessRequest>("Business") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createBusinessInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createPayment") {
                handleOnboardingStep<OnboardingPaymentRequest>("Payment") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createPaymentInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createCatalog") {
                handleOnboardingStep<OnboardingCatalogRequest>("Catalog") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createCatalogInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createLogistics") {
                handleOnboardingStep<OnboardingLogisticsRequest>("Logistics") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createLogisticsInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/createCompliance") {
                handleOnboardingStep<OnboardingComplianceRequest>("Compliance") { call, request ->

                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")

                    val userId = principal.userId()

                    repo.createComplianceInformation(userId, request)
                }
            }
        }

        authorizeRoles(Constant.ROLE.SELLER) {
            route("/getReview") {
                get {
                    try {

                        val principal = call.principal<JWTPrincipal>()
                            ?: throw Exception("Unauthorized")

                        val userId = principal.userId()

                        val result = repo.getOnboardingReview(userId)

                        call.respond(
                            HttpStatusCode.OK,
                            ApiSuccessResponse(
                                code = 200,
                                message = "Onboarding review fetched successfully",
                                data = result
                            )
                        )

                    } catch (e: Exception) {
                        call.handleError(e)
                    }
                }
            }
        }

        // Submit for Review (Seller)
        authorizeRoles(Constant.ROLE.SELLER) {
            route("/submitForReview") {
                post {
                    try {
                        val principal = call.principal<JWTPrincipal>()
                            ?: throw Exception("Unauthorized")

                        val userId = principal.userId()
                        val result = repo.submitForReview(userId)

                        val response = result.onboarding.toResponse()

                        val message = when (result.operation) {
                            OperationType.CREATED -> "Submitted for review successfully"
                            OperationType.UPDATED -> "Submitted for review successfully"
                            OperationType.NO_CHANGE -> "Already submitted for review"
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
        }

        // Admin: Approve Onboarding
        authorizeRoles(Constant.ROLE.ADMIN) {
            route("/approve/{userId}") {
                post {
                    try {
                        val targetUserId = call.parameters["userId"]
                            ?: throw Exception("User ID is required")

                        val result = repo.approveOnboarding(targetUserId)
                        val response = result.onboarding.toResponse()

                        call.respond(
                            HttpStatusCode.OK,
                            ApiSuccessResponse(
                                message = "Onboarding approved successfully",
                                code = 200,
                                data = response
                            )
                        )
                    } catch (e: Exception) {
                        call.handleError(e)
                    }
                }
            }
        }

        // Admin: Reject Onboarding
        authorizeRoles(Constant.ROLE.ADMIN) {
            route("/reject/{userId}") {
                post {
                    try {
                        val targetUserId = call.parameters["userId"]
                            ?: throw Exception("User ID is required")

                        @Serializable
                        data class RejectRequest(val reason: String)

                        val request = call.receive<RejectRequest>()
                        if (request.reason.isBlank()) throw Exception("Rejection reason is required")

                        val result = repo.rejectOnboarding(targetUserId, request.reason)
                        val response = result.onboarding.toResponse()

                        call.respond(
                            HttpStatusCode.OK,
                            ApiSuccessResponse(
                                message = "Onboarding rejected",
                                code = 200,
                                data = response
                            )
                        )
                    } catch (e: Exception) {
                        call.handleError(e)
                    }
                }
            }
        }


    }
}

