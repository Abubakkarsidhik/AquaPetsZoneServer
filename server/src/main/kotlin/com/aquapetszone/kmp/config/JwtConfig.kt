package com.aquapetszone.kmp.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.*

/**
 * JWT Configuration — Dual Token System (Access + Refresh).
 *
 * 🔐 SECURITY: Secrets are loaded from environment variables:
 *    - JWT_SECRET: The HMAC signing key (min 32 chars recommended for production)
 *    - JWT_ISSUER: The token issuer claim
 *
 *    Set these in your deployment environment (Docker, cloud, .env file).
 *    The fallback values are ONLY for local development.
 *
 * ⏱️ Token lifetimes:
 *    - Access token:  7 days   (reduce to ~15 min in production)
 *    - Refresh token: 30 days  (long-lived, used only to get new access tokens)
 */
object JwtConfig {

    private val secret: String = System.getenv("JWT_SECRET") ?: "aquapets-secret"
    private val issuer: String = System.getenv("JWT_ISSUER") ?: "aquapets"

    /** Access token validity — 7 days (reduce to 15 min in prod) */
    private const val ACCESS_VALIDITY_MS = 1000L * 60 * 60 * 24 * 7

    /** Refresh token validity — 30 days */
    private const val REFRESH_VALIDITY_MS = 1000L * 60 * 60 * 24 * 30

    private val algorithm = Algorithm.HMAC256(secret)

    /**
     * Generate a short-lived access token.
     */
    fun generateAccessToken(
        userId: String,
        role: String
    ): String {
        val now = System.currentTimeMillis()

        println("------ JWT ACCESS TOKEN GENERATE ------")
        println("USER ID: $userId | ROLE: $role")
        println("EXPIRES AT: ${Date(now + ACCESS_VALIDITY_MS)}")

        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withClaim("type", "access")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + ACCESS_VALIDITY_MS))
            .sign(algorithm)
    }

    /**
     * Generate a long-lived refresh token.
     */
    fun generateRefreshToken(
        userId: String,
        role: String
    ): String {
        val now = System.currentTimeMillis()

        println("------ JWT REFRESH TOKEN GENERATE ------")
        println("USER ID: $userId | ROLE: $role")
        println("EXPIRES AT: ${Date(now + REFRESH_VALIDITY_MS)}")

        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withClaim("type", "refresh")
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + REFRESH_VALIDITY_MS))
            .sign(algorithm)
    }

    /**
     * Backward-compatible wrapper — generates an access token.
     */
    fun generateToken(userId: String, role: String): String =
        generateAccessToken(userId, role)

    /**
     * Standard verifier — rejects expired tokens.
     * Used by the `authenticate("auth-jwt")` pipeline.
     */
    fun verifier() =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .build()
            .also {
                println("------ JWT VERIFIER CREATED ------")
                println("EXPECTED ISSUER: $issuer")
                println("ALGORITHM: HMAC256")
                println("----------------------------------")
            }

    /**
     * Decode a token WITHOUT checking expiry.
     * Used for the refresh flow — the refresh token itself may still be valid
     * while the access token is expired.
     *
     * Returns null if the token is fundamentally invalid (bad signature, issuer, etc.)
     */
    fun decodeTokenUnsafe(token: String): DecodedJWT? {
        return try {
            JWT.require(algorithm)
                .withIssuer(issuer)
                .acceptExpiresAt(Long.MAX_VALUE / 1000) // accept any expiry
                .build()
                .verify(token)
        } catch (e: Exception) {
            println("decodeTokenUnsafe FAILED: ${e.message}")
            null
        }
    }
}

