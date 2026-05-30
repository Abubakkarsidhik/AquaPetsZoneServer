package com.aquapetszone.kmp.utils

object PaymentAuditLogger {

    fun log(action: String, details: Map<String, String> = emptyMap()) {
        println("========== PAYMENT AUDIT ==========")
        println("ACTION: $action")
        println("TIMESTAMP: ${System.currentTimeMillis()}")
        details.forEach { (key, value) ->
            if (!isSensitiveKey(key)) {
                println("$key: $value")
            }
        }
        println("===================================")
    }

    private fun isSensitiveKey(key: String): Boolean {
        val lower = key.lowercase()
        return lower.contains("card") ||
            lower.contains("upi") ||
            lower.contains("vpa") ||
            lower.contains("cvv") ||
            lower.contains("secret") ||
            lower.contains("token")
    }
}
