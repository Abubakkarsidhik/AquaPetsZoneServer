package com.aquapetszone.kmp.domain.model.request.payment

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
    val couponCode: String? = null,
    val deliveryAddress: String? = null,
    val applyDeliveryFee: Boolean = true,
    val idempotencyKey: String? = null
)

@Serializable
data class OrderItemRequest(
    val productId: String,
    val quantity: Int = 1
)

@Serializable
data class VerifyPaymentRequest(
    val razorpayOrderId: String,
    val razorpayPaymentId: String,
    val razorpaySignature: String,
    val idempotencyKey: String? = null
)

@Serializable
data class PaymentFailedRequest(
    val orderId: String,
    val razorpayOrderId: String? = null,
    val failureReason: String? = null,
    val errorCode: String? = null,
    val gatewayResponse: String? = null
)

@Serializable
data class RefundRequest(
    val paymentId: String,
    val reason: String? = null,
    val idempotencyKey: String? = null
)

@Serializable
data class PartialRefundRequest(
    val paymentId: String,
    val amount: Double,
    val reason: String? = null,
    val idempotencyKey: String? = null
)
