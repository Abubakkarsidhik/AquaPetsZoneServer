package com.aquapetszone.kmp.domain.repository.payment

import com.aquapetszone.kmp.domain.model.request.payment.CreateOrderRequest
import com.aquapetszone.kmp.domain.model.request.payment.PartialRefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.PaymentFailedRequest
import com.aquapetszone.kmp.domain.model.request.payment.RefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.VerifyPaymentRequest
import com.aquapetszone.kmp.domain.model.response.payment.CreateOrderResponse
import com.aquapetszone.kmp.domain.model.response.payment.OrderPaymentResponse
import com.aquapetszone.kmp.domain.model.response.payment.PaymentStatusResponse
import com.aquapetszone.kmp.domain.model.response.payment.PaymentVerificationResponse
import com.aquapetszone.kmp.domain.model.response.payment.RefundResponse

interface ServerPaymentRepository {

    suspend fun createOrder(userId: String, request: CreateOrderRequest): CreateOrderResponse

    suspend fun verifyPayment(userId: String, request: VerifyPaymentRequest): PaymentVerificationResponse

    suspend fun handlePaymentFailed(userId: String, request: PaymentFailedRequest): PaymentStatusResponse

    suspend fun refund(userId: String, request: RefundRequest): RefundResponse

    suspend fun partialRefund(userId: String, request: PartialRefundRequest): RefundResponse

    suspend fun getPayment(paymentId: String, userId: String): PaymentStatusResponse

    suspend fun getRefund(refundId: String, userId: String): RefundResponse

    suspend fun getOrderPayment(orderId: String, userId: String): OrderPaymentResponse

    suspend fun processWebhook(rawBody: String, signature: String): Boolean
}
