package com.aquapetszone.kmp.data.service

import com.aquapetszone.kmp.data.service.Msg91Service
import com.aquapetszone.kmp.data.service.SmsService

object OtpProvider {
    private val useMsg91: Boolean = System.getenv("USE_MSG91_OTP")?.toBooleanStrictOrNull() ?: false

    suspend fun sendOtp(mobile: String, recaptchaToken: String? = null): String {
        return if (useMsg91) {
            Msg91Service.sendOtp(mobile)
        } else {
            if (recaptchaToken == null) throw IllegalArgumentException("recaptchaToken required for Firebase OTP")
            SmsService.sendOtp(mobile, recaptchaToken)
        }
    }

    suspend fun verifyOtp(mobile: String, otp: String, sessionInfo: String? = null): Boolean {
        return if (useMsg91) {
            Msg91Service.verifyOtp(mobile, otp)
        } else {
            if (sessionInfo == null) throw IllegalArgumentException("sessionInfo required for Firebase OTP verification")
            // Firebase returns idToken, treat non-null as success
            SmsService.verifyOtp(sessionInfo, otp).isNotBlank()
        }
    }

    suspend fun resendOtp(mobile: String): String {
        return if (useMsg91) {
            Msg91Service.resendOtp(mobile)
        } else {
            throw UnsupportedOperationException("Resend OTP not supported for Firebase")
        }
    }
}

