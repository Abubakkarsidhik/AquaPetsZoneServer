package com.aquapetszone.kmp.config

import io.github.cdimascio.dotenv.dotenv

object RazorpayConfig {

    private val dotenv by lazy {
        dotenv { ignoreIfMissing = true }
    }

    private fun env(key: String): String? =
        System.getenv(key) ?: dotenv[key]

    val keyId: String
        get() = env("RAZORPAY_KEY_ID") ?: error("RAZORPAY_KEY_ID not set")

    val keySecret: String
        get() = env("RAZORPAY_KEY_SECRET") ?: error("RAZORPAY_KEY_SECRET not set")

    val webhookSecret: String
        get() = env("RAZORPAY_WEBHOOK_SECRET") ?: error("RAZORPAY_WEBHOOK_SECRET not set")

    const val BASE_URL = "https://api.razorpay.com/v1"
    const val CURRENCY_INR = "INR"
    const val RECEIPT_PREFIX = "APZ"

    const val TAX_RATE_PERCENT = 18.0
    const val DEFAULT_DELIVERY_FEE = 49.0
    const val IDEMPOTENCY_TTL_MS = 24 * 60 * 60 * 1000L
}

object PaymentStatus {
    const val PENDING = "PENDING"
    const val SUCCESS = "SUCCESS"
    const val FAILED = "FAILED"
    const val REFUNDED = "REFUNDED"
    const val PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED"
}

object OrderStatus {
    const val PENDING_PAYMENT = "PENDING_PAYMENT"
    const val CONFIRMED = "CONFIRMED"
    const val PAYMENT_FAILED = "PAYMENT_FAILED"
    const val CANCELLED = "CANCELLED"
    const val REFUNDED = "REFUNDED"
    const val PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED"
}

object RefundStatus {
    const val PENDING = "PENDING"
    const val PROCESSED = "PROCESSED"
    const val FAILED = "FAILED"
}

object InvoiceStatus {
    const val PENDING = "PENDING"
    const val GENERATED = "GENERATED"
    const val FAILED = "FAILED"
}

object PaymentAuditAction {
    const val PAYMENT_CREATE = "PAYMENT_CREATE"
    const val PAYMENT_SUCCESS = "PAYMENT_SUCCESS"
    const val PAYMENT_FAILED = "PAYMENT_FAILED"
    const val PAYMENT_REFUND = "PAYMENT_REFUND"
    const val WEBHOOK_RECEIVED = "WEBHOOK_RECEIVED"
    const val WEBHOOK_FAILED = "WEBHOOK_FAILED"
}
