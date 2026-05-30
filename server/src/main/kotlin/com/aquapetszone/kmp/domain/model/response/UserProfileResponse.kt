package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: String,
    val firebaseUid: String,
    val phone: String,
    val email: String? = null,
    val name: String? = null,
    val role: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val lastLoginAt: Long? = null
)

@Serializable
data class FirebaseLoginData(
    val success: Boolean = true,
    val user: UserProfileResponse,
    val token: String,
    val refreshToken: String
)
