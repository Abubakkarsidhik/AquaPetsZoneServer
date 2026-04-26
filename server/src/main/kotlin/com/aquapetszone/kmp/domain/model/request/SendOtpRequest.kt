package com.aquapetszone.kmp.domain.model.request

import com.aquapetszone.kmp.config.SEND_OTP_TYPE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val value: String,
    val recaptchaToken: String,
    val type: SEND_OTP_TYPE
)