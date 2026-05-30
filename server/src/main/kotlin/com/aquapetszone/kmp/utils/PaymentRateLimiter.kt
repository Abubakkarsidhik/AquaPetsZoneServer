package com.aquapetszone.kmp.utils

import java.util.concurrent.ConcurrentHashMap

object PaymentRateLimiter {

    private data class Window(val count: Int, val windowStartMs: Long)

    private val buckets = ConcurrentHashMap<String, Window>()

    private const val MAX_REQUESTS = 30
    private const val WINDOW_MS = 60_000L

    fun checkLimit(key: String) {
        val now = System.currentTimeMillis()
        val existing = buckets[key]
        if (existing == null || now - existing.windowStartMs >= WINDOW_MS) {
            buckets[key] = Window(count = 1, windowStartMs = now)
            return
        }
        if (existing.count >= MAX_REQUESTS) {
            throw Exception("Rate limit exceeded. Please try again later.")
        }
        buckets[key] = Window(count = existing.count + 1, windowStartMs = existing.windowStartMs)
    }
}
