package com.aquapetszone.kmp.utils

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object PaymentSecurityUtil {

    fun hmacSha256(secret: String, payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        val hash = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun timingSafeEquals(expected: String, actual: String): Boolean {
        val a = expected.toByteArray(Charsets.UTF_8)
        val b = actual.toByteArray(Charsets.UTF_8)
        if (a.size != b.size) return false
        return MessageDigest.isEqual(a, b)
    }

    fun verifyPaymentSignature(
        orderId: String,
        paymentId: String,
        signature: String,
        secret: String
    ): Boolean {
        val payload = "$orderId|$paymentId"
        val expected = hmacSha256(secret, payload)
        return timingSafeEquals(expected, signature)
    }

    fun verifyWebhookSignature(
        rawBody: String,
        signature: String,
        webhookSecret: String
    ): Boolean {
        val expected = hmacSha256(webhookSecret, rawBody)
        return timingSafeEquals(expected, signature)
    }

    fun sanitize(input: String?, maxLength: Int = 256): String {
        if (input.isNullOrBlank()) return ""
        return input.trim()
            .replace(Regex("[<>\"'\\\\]"), "")
            .take(maxLength)
    }
}
