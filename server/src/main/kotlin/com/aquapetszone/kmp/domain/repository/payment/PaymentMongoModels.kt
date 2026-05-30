package com.aquapetszone.kmp.domain.repository.payment

import com.aquapetszone.kmp.config.InvoiceStatus
import com.aquapetszone.kmp.config.OrderStatus
import com.aquapetszone.kmp.config.PaymentStatus
import com.aquapetszone.kmp.config.RefundStatus
import com.aquapetszone.kmp.domain.model.response.payment.CreateOrderResponse
import com.aquapetszone.kmp.domain.model.response.payment.OrderPaymentResponse
import com.aquapetszone.kmp.domain.model.response.payment.PaymentStatusResponse
import com.aquapetszone.kmp.domain.model.response.payment.RefundResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class OrderLineItem(
    val productId: String,
    val sellerId: String,
    val sku: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val lineTotal: Double
)

@Serializable
data class OrderPricing(
    val subtotal: Double,
    val discount: Double = 0.0,
    val couponDiscount: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val grandTotal: Double
)

@Serializable
data class OrderMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val orderId: ObjectId = ObjectId(),

    @Transient
    val userId: ObjectId = ObjectId(),

    val items: List<OrderLineItem> = emptyList(),
    val pricing: OrderPricing,
    val status: String = OrderStatus.PENDING_PAYMENT,
    val razorpayOrderId: String? = null,
    val couponCode: String? = null,
    val deliveryAddress: String? = null,
    val idempotencyKey: String? = null,
    val invoiceNumber: String? = null,
    val invoiceDate: Long? = null,
    val invoiceStatus: String? = InvoiceStatus.PENDING,
    val paymentId: String? = null,
    val refundStatus: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class PaymentMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val paymentId: ObjectId = ObjectId(),

    @Transient
    val orderId: ObjectId = ObjectId(),

    @Transient
    val userId: ObjectId = ObjectId(),

    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val amount: Double,
    val amountPaise: Long,
    val currency: String = "INR",
    val status: String = PaymentStatus.PENDING,
    val method: String? = null,
    val failureReason: String? = null,
    val errorCode: String? = null,
    val gatewayResponse: String? = null,
    val idempotencyKey: String? = null,
    val refundedAmount: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class PaymentTransactionMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val transactionId: ObjectId = ObjectId(),

    val paymentId: String,
    val orderId: String,
    val fromStatus: String? = null,
    val toStatus: String,
    val source: String,
    val metadata: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class RefundMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val refundId: ObjectId = ObjectId(),

    val paymentId: String,
    val orderId: String,
    val razorpayRefundId: String? = null,
    val razorpayPaymentId: String? = null,
    val amount: Double,
    val amountPaise: Long,
    val reason: String? = null,
    val idempotencyKey: String? = null,
    val status: String = RefundStatus.PENDING,
    val isPartial: Boolean = false,
    val gatewayResponse: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class WebhookEventMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    val eventId: String,
    val eventType: String,
    val payloadHash: String,
    val processed: Boolean = false,
    val processingResult: String? = null,
    val rawPayload: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class AuditLogMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    val action: String,
    val userId: String? = null,
    val orderId: String? = null,
    val paymentId: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis()
)

fun OrderMongo.toCreateOrderResponse(keyId: String): CreateOrderResponse {
    return CreateOrderResponse(
        orderId = orderId.toHexString(),
        razorpayOrderId = razorpayOrderId,
        amount = pricing.grandTotal,
        amountPaise = (pricing.grandTotal * 100).toLong(),
        currency = "INR",
        keyId = keyId
    )
}

fun PaymentMongo.toPaymentStatusResponse(): PaymentStatusResponse {
    return PaymentStatusResponse(
        paymentId = paymentId.toHexString(),
        orderId = orderId.toHexString(),
        status = status,
        amount = amount,
        currency = currency,
        razorpayPaymentId = razorpayPaymentId,
        razorpayOrderId = razorpayOrderId,
        refundedAmount = refundedAmount,
        failureReason = failureReason,
        errorCode = errorCode
    )
}

fun OrderMongo.toOrderPaymentResponse(payment: PaymentMongo?): OrderPaymentResponse {
    return OrderPaymentResponse(
        orderId = orderId.toHexString(),
        orderStatus = status,
        paymentStatus = payment?.status,
        refundStatus = refundStatus,
        razorpayOrderId = razorpayOrderId,
        razorpayPaymentId = payment?.razorpayPaymentId,
        invoiceNumber = invoiceNumber,
        invoiceStatus = invoiceStatus,
        grandTotal = pricing.grandTotal
    )
}

fun RefundMongo.toRefundResponse(): RefundResponse {
    return RefundResponse(
        refundId = refundId.toHexString(),
        paymentId = paymentId,
        orderId = orderId,
        razorpayRefundId = razorpayRefundId,
        amount = amount,
        status = status,
        isPartial = isPartial,
        reason = reason
    )
}
