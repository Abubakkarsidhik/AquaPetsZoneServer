package com.aquapetszone.kmp.domain.repository.onboarding

import com.aquapetszone.kmp.domain.model.request.OnboardingAccountRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingBusinessRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingCatalogRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingComplianceRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingLogisticsRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingPaymentRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingStoreRequest

interface ServerOnboardingRepository {
    suspend fun createAccountInformation(request: OnboardingAccountRequest): OnboardingOperationResult
    suspend fun createStoreInformation(userId: String, request: OnboardingStoreRequest): OnboardingOperationResult
    suspend fun createBusinessInformation(userId: String, request: OnboardingBusinessRequest): OnboardingOperationResult
    suspend fun createPaymentInformation(userId: String, request: OnboardingPaymentRequest): OnboardingOperationResult
    suspend fun createCatalogInformation(userId: String, request: OnboardingCatalogRequest): OnboardingOperationResult
    suspend fun createLogisticsInformation(userId: String, request: OnboardingLogisticsRequest): OnboardingOperationResult
    suspend fun createComplianceInformation(userId: String, request: OnboardingComplianceRequest): OnboardingOperationResult
    suspend fun getOnboardingReview(userId: String): OnboardingReviewResponse
    suspend fun submitForReview(userId: String): OnboardingOperationResult
    suspend fun approveOnboarding(userId: String): OnboardingOperationResult
    suspend fun rejectOnboarding(userId: String, reason: String): OnboardingOperationResult
}