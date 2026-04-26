package com.aquapetszone.kmp.domain.model.response

import com.aquapetszone.kmp.domain.model.request.OnboardingAccountRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingBusinessRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingCatalogRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingComplianceRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingLogisticsRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingPaymentRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingStoreRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingResponse(
    @SerialName("currentStep") var currentStep: Int? = null,
    @SerialName("isOnboardingCompleted") var isOnboardingCompleted: Boolean? = null,
    @SerialName("onboardingStatus") var onboardingStatus: String? = null,
    @SerialName("rejectionReason") var rejectionReason: String? = null,
    @SerialName("accountCompleted") var accountCompleted: Boolean? = null,
    @SerialName("storeCompleted") var storeCompleted: Boolean? = null,
    @SerialName("businessCompleted") var businessCompleted: Boolean? = null,
    @SerialName("paymentCompleted") var paymentCompleted: Boolean? = null,
    @SerialName("catalogCompleted") var catalogCompleted: Boolean? = null,
    @SerialName("logisticCompleted") var logisticCompleted: Boolean? = null,
    @SerialName("complianceCompleted") var complianceCompleted: Boolean? = null,
    @SerialName("createdAt") var createdAt: Long? = null,
    @SerialName("updatedAt") var updatedAt: Long? = null,

    // Only returned by account creation (initial auth)
    @SerialName("token") var token: String? = null,
    @SerialName("refreshToken") var refreshToken: String? = null
)


@Serializable
data class OnboardingReviewResponse(
    @SerialName("currentStep") var currentStep: Int? = null,
    @SerialName("isOnboardingCompleted") var isOnboardingCompleted: Boolean? = null,
    @SerialName("onboardingStatus") var onboardingStatus: String? = null,
    @SerialName("rejectionReason") var rejectionReason: String? = null,
    @SerialName("data") val data: OnboardingData? = null

)

@Serializable
data class OnboardingData(
    @SerialName("account") var account: OnboardingAccountRequest? = null,
    @SerialName("store") var store: OnboardingStoreRequest? = null,
    @SerialName("business") var business: OnboardingBusinessRequest? = null,
    @SerialName("payment") var payment: OnboardingPaymentRequest? = null,
    @SerialName("catalog") var catalog: OnboardingCatalogRequest? = null,
    @SerialName("logistics") var logistics: OnboardingLogisticsRequest? = null,
    @SerialName("compliance") var compliance: OnboardingComplianceRequest? = null,
    @SerialName("createdAt") var createdAt: Long? = null,
    @SerialName("updatedAt") var updatedAt: Long? = null
)