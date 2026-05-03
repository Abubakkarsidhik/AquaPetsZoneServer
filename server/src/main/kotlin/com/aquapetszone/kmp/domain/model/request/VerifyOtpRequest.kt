package com.aquapetszone.kmp.domain.model.request

import com.aquapetszone.kmp.config.SEND_OTP_TYPE
import kotlinx.serialization.Serializable

@Serializable
data class VerifyOtpRequest(
    val mobile: String,
    val otp: String,
    val sessionInfo: String? = null
)