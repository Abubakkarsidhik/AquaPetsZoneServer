package com.aquapetszone.kmp.domain.repository.auth

import com.aquapetszone.kmp.domain.model.response.LoginResponse

interface ServerAuthRepository {

    suspend fun sendOtp(
        mobile: String,
        recaptchaToken: String? = null
    ): String

    suspend fun verifyOtp(
        mobile: String,
        otp: String,
        sessionInfo: String? = null
    ): LoginResponse

    suspend fun resendOtp(
        mobile: String
    ): String

}