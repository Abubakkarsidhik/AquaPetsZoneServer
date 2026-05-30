package com.aquapetszone.kmp.utils

object AuthAuditLogger {

    fun log(action: String, details: Map<String, String> = emptyMap()) {
        println("========== AUTH AUDIT ==========")
        println("ACTION: $action")
        println("TIMESTAMP: ${System.currentTimeMillis()}")
        details.forEach { (key, value) ->
            if (!isSensitiveKey(key)) {
                println("$key: $value")
            }
        }
        println("================================")
    }

    private fun isSensitiveKey(key: String): Boolean {
        val lower = key.lowercase()
        return lower.contains("token") ||
            lower.contains("secret") ||
            lower.contains("password") ||
            lower.contains("credential")
    }
}
