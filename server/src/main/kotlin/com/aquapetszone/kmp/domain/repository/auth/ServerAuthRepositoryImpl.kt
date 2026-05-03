package com.aquapetszone.kmp.domain.repository.auth

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.config.JwtConfig
import com.aquapetszone.kmp.data.service.Msg91Service
import com.aquapetszone.kmp.data.service.OtpProvider
import com.aquapetszone.kmp.domain.model.response.LoginResponse
import com.aquapetszone.kmp.domain.repository.ServerBaseRepository
import com.aquapetszone.kmp.domain.repository.onboarding.OnboardingMongo
import com.aquapetszone.kmp.domain.repository.onboarding.OnboardingStatus
import com.aquapetszone.kmp.utils.CryptoUtil
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId

class ServerAuthRepositoryImpl : ServerAuthRepository, ServerBaseRepository() {

    private val collection =
        db.getCollection<OnboardingMongo>("onboarding")

    override suspend fun sendOtp(
        mobile: String,
        recaptchaToken: String?
    ): String {

        if (mobile.isBlank()) {
            throw Exception("Mobile number required")
        }

        return OtpProvider.sendOtp(mobile, recaptchaToken)
    }

    override suspend fun verifyOtp(
        mobile: String,
        otp: String,
        sessionInfo: String?
    ): LoginResponse {

        val verified = OtpProvider.verifyOtp(
            mobile = mobile,
            otp = otp,
            sessionInfo = sessionInfo
        )

        if (!verified) {
            throw Exception("Invalid OTP")
        }

        val encryptedMobile =
            CryptoUtil.encrypt(mobile)

        var existing = collection.find(
            Filters.eq(
                "account.mobile",
                encryptedMobile
            )
        ).first()

        val now = System.currentTimeMillis()

        if (existing == null) {

            val onboarding = OnboardingMongo(
                userId = ObjectId(),
                currentStep = 1,
                createdAt = now,
                updatedAt = now
            )

            collection.insertOne(onboarding)

            existing = onboarding
        }

        val userId = existing.userId.toHexString()

        val role = Constant.ROLE.SELLER

        val accessToken =
            JwtConfig.generateAccessToken(
                userId = userId,
                role = role
            )

        val refreshToken =
            JwtConfig.generateRefreshToken(
                userId = userId,
                role = role
            )

        return LoginResponse(
            token = accessToken,
            refreshToken = refreshToken,
            userId = userId,
            role = role,
            currentStep = existing.currentStep,
            isOnboardingCompleted =
                existing.onboardingStatus ==
                        OnboardingStatus.APPROVED,
            onboardingStatus = existing.onboardingStatus,
            rejectionReason = existing.rejectionReason
        )
    }

    override suspend fun resendOtp(
        mobile: String
    ): String {

        if (mobile.isBlank()) {
            throw Exception("Mobile number required")
        }

        return OtpProvider.resendOtp(mobile)
    }
}