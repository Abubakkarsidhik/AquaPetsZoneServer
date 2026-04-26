package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingAccountResponse(
    val token: String,
    val role: String,
    val currentStep: Int,
)

@Serializable
data class OnboardingStoreResponse(
    val currentStep: Int,
)