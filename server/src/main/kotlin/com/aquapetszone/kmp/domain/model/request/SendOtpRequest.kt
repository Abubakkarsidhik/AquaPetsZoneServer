package com.aquapetszone.kmp.domain.model.request

import com.aquapetszone.kmp.config.SEND_OTP_TYPE
import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequest(
    val data: String,
    val type: SEND_OTP_TYPE,
    val recaptchaToken: String? = null
)