package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpResponse(
    val firebaseToken: String
)