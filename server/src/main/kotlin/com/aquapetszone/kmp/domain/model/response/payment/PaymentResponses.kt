package com.aquapetszone.kmp.domain.model.response.payment

import kotlinx.serialization.Serializable

@Serializable
data class CreateOrderResponse(
    val orderId: String,
    val razorpayOrderId: String? = null,
    val amount: Double,
    val amountPaise: Long,
    val currency: String,
    val keyId: String
)

@Serializable
data class PaymentVerificationResponse(
    val success: Boolean,
    val orderId: String,
    val paymentId: String,
    val orderStatus: String,
    val paymentStatus: String,
    val invoiceNumber: String? = null,
    val message: String? = null
)

@Serializable
data class PaymentStatusResponse(
    val paymentId: String,
    val orderId: String,
    val status: String,
    val amount: Double,
    val currency: String,
    val razorpayPaymentId: String? = null,
    val razorpayOrderId: String? = null,
    val refundedAmount: Double = 0.0,
    val failureReason: String? = null,
    val errorCode: String? = null
)

@Serializable
data class RefundResponse(
    val refundId: String,
    val paymentId: String,
    val orderId: String,
    val razorpayRefundId: String? = null,
    val amount: Double,
    val status: String,
    val isPartial: Boolean = false,
    val reason: String? = null
)

@Serializable
data class WebhookAckResponse(
    val received: Boolean = true
)

@Serializable
data class OrderPaymentResponse(
    val orderId: String,
    val orderStatus: String,
    val paymentStatus: String? = null,
    val refundStatus: String? = null,
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val invoiceNumber: String? = null,
    val invoiceStatus: String? = null,
    val grandTotal: Double
)
