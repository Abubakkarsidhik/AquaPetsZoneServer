package com.aquapetszone.kmp.data.service

import com.aquapetszone.kmp.config.FirebaseConfig
import com.aquapetszone.kmp.utils.AuthAuditLogger
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

data class FirebaseUserIdentity(
    val firebaseUid: String,
    val phone: String,
    val email: String?,
    val name: String?
)

object FirebaseAuthService {

    fun verifyIdToken(idToken: String): FirebaseUserIdentity {
        if (idToken.isBlank()) {
            throw Exception("Firebase ID token is required")
        }

        FirebaseConfig.initialize()

        return try {
            val decoded = FirebaseAuth.getInstance().verifyIdToken(idToken)

            val uid = decoded.uid
            if (uid.isBlank()) {
                throw Exception("Invalid Firebase token: missing uid")
            }

            val phone = decoded.claims["phone_number"]?.toString()
                ?: throw Exception("Phone number not found in Firebase token")

            val normalizedPhone = normalizePhone(phone)

            val email = decoded.email
                ?: decoded.claims["email"]?.toString()

            val name = decoded.name
                ?: decoded.claims["name"]?.toString()

            AuthAuditLogger.log(
                "FIREBASE_TOKEN_VERIFIED",
                mapOf("firebaseUid" to uid)
            )

            FirebaseUserIdentity(
                firebaseUid = uid,
                phone = normalizedPhone,
                email = email?.trim()?.lowercase()?.takeIf { it.isNotBlank() },
                name = name?.trim()?.takeIf { it.isNotBlank() }
            )
        } catch (e: FirebaseAuthException) {
            AuthAuditLogger.log(
                "FIREBASE_TOKEN_REJECTED",
                mapOf("reason" to (e.message ?: "invalid_token"))
            )
            throw Exception("Invalid or expired Firebase token")
        } catch (e: Exception) {
            if (e.message?.contains("Firebase", true) == true ||
                e.message?.contains("token", true) == true
            ) {
                throw e
            }
            AuthAuditLogger.log(
                "FIREBASE_TOKEN_REJECTED",
                mapOf("reason" to (e.message ?: "verification_failed"))
            )
            throw Exception("Firebase token verification failed")
        }
    }

    private fun normalizePhone(phone: String): String {
        val digits = phone.filter { it.isDigit() }
        return when {
            digits.length == 10 -> digits
            digits.length > 10 && digits.startsWith("91") -> digits.takeLast(10)
            digits.isNotBlank() -> digits
            else -> throw Exception("Invalid phone number in Firebase token")
        }
    }
}
