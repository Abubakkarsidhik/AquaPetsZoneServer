package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpResponse(
    val sessionInfo: String
)