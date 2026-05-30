package com.aquapetszone.kmp.routes

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.domain.model.request.payment.CreateOrderRequest
import com.aquapetszone.kmp.domain.model.request.payment.PartialRefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.PaymentFailedRequest
import com.aquapetszone.kmp.domain.model.request.payment.RefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.VerifyPaymentRequest
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.model.response.payment.WebhookAckResponse
import com.aquapetszone.kmp.domain.repository.payment.ServerPaymentRepositoryImpl
import com.aquapetszone.kmp.helper.authorizeRoles
import com.aquapetszone.kmp.helper.handleError
import com.aquapetszone.kmp.helper.userId
import com.aquapetszone.kmp.utils.PaymentRateLimiter
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.paymentRoutes() {

    val repository by lazy { ServerPaymentRepositoryImpl() }

    route("/api/v1/payment") {

        post("/webhook") {
            try {
                val rawBody = call.receiveText()
                val signature = call.request.headers["X-Razorpay-Signature"]
                    ?: throw Exception("Missing X-Razorpay-Signature header")

                repository.processWebhook(rawBody, signature)

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "Webhook processed",
                        code = 200,
                        data = WebhookAckResponse(received = true)
                    )
                )
            } catch (e: Exception) {
                call.handleError(e)
            }
        }

        authorizeRoles(Constant.ROLE.USER, Constant.ROLE.SELLER, Constant.ROLE.ADMIN) {

            post("/order") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    PaymentRateLimiter.checkLimit("payment-order-$userId")

                    val request = call.receive<CreateOrderRequest>()
                    val result = repository.createOrder(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Order created successfully",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            post("/verify") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    PaymentRateLimiter.checkLimit("payment-verify-$userId")

                    val request = call.receive<VerifyPaymentRequest>()
                    val result = repository.verifyPayment(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = result.message ?: "Payment verified",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            post("/failed") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()

                    val request = call.receive<PaymentFailedRequest>()
                    val result = repository.handlePaymentFailed(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Payment failure recorded",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            post("/refund") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    PaymentRateLimiter.checkLimit("payment-refund-$userId")

                    val request = call.receive<RefundRequest>()
                    val result = repository.refund(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Refund initiated",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            post("/refund/partial") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    PaymentRateLimiter.checkLimit("payment-refund-$userId")

                    val request = call.receive<PartialRefundRequest>()
                    val result = repository.partialRefund(userId, request)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Partial refund initiated",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            get("/refund/{id}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    val refundId = call.parameters["id"]
                        ?: throw Exception("Refund ID is required")

                    val result = repository.getRefund(refundId, userId)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Refund fetched successfully",
                            code = 200,
                            data = result
                        )
                    )
                } catch (e: Exception) {
                    call.handleError(e)
                }
            }

            get("/{paymentId}") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    val paymentId = call.parameters["paymentId"]
                        ?: throw Exception("Payment ID is required")

                    val result = repository.getPayment(paymentId, userId)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Payment status fetched",
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

    route("/api/v1/order") {
        authorizeRoles(Constant.ROLE.USER, Constant.ROLE.SELLER, Constant.ROLE.ADMIN) {
            get("/{orderId}/payment") {
                try {
                    val principal = call.principal<JWTPrincipal>()
                        ?: throw Exception("Unauthorized")
                    val userId = principal.userId()
                    val orderId = call.parameters["orderId"]
                        ?: throw Exception("Order ID is required")

                    val result = repository.getOrderPayment(orderId, userId)

                    call.respond(
                        HttpStatusCode.OK,
                        ApiSuccessResponse(
                            message = "Order payment status fetched",
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
}
