package com.aquapetszone.kmp.utils

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES Encryption utility.
 *
 * 🔐 SECURITY: The SECRET key is loaded from the environment variable `AES_SECRET_KEY`.
 *    - In production, set this via your deployment platform (Docker env, cloud secrets, etc.)
 *    - NEVER commit production keys to source control.
 *    - The fallback "1234567890123456" is only for local development.
 *
 * Uses AES/CBC/PKCS5Padding with a fixed IV for deterministic encryption
 * (needed for DB lookups). For non-lookup fields, consider random IVs.
 */
object CryptoUtil {

    // Load secret from environment variable; fallback for local dev only
    private val SECRET: String = System.getenv("AES_SECRET_KEY") ?: "1234567890123456"

    // Fixed IV for deterministic encryption (needed for email/mobile DB lookups)
    // In production, set via env var AES_IV_KEY
    private val IV: String = System.getenv("AES_IV_KEY") ?: "abcdef9876543210"

    private val keySpec = SecretKeySpec(SECRET.toByteArray(), "AES")
    private val ivSpec = IvParameterSpec(IV.toByteArray())

    fun encrypt(data: String?): String {
        if (data.isNullOrEmpty()) return ""

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(encryptedData: String?): String {
        if (encryptedData.isNullOrEmpty()) return ""

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decryptedBytes = cipher.doFinal(decodedBytes)

        return String(decryptedBytes)
    }
}

fun main() {
    println("encrypt - email: ${CryptoUtil.encrypt("test@gmail.com")}")
    println("decrypt - email: ${CryptoUtil.decrypt(CryptoUtil.encrypt("test@gmail.com"))}")
}