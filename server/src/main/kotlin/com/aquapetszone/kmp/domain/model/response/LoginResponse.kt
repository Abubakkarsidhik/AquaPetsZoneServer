package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val userId: String,
    val role: String,
    val currentStep: Int? = null,
    val isOnboardingCompleted: Boolean? = null,
    val onboardingStatus: String? = null,
    val rejectionReason: String? = null
)
