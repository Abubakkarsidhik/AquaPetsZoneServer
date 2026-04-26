package com.aquapetszone.kmp.domain.repository.onboarding

import com.aquapetszone.kmp.domain.model.request.OnboardingAccountRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingBusinessRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingCatalogRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingComplianceRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingLogisticsRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingPaymentRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingStoreRequest
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId


object OnboardingStatus {
    const val IN_PROGRESS = "IN_PROGRESS"
    const val PENDING_REVIEW = "PENDING_REVIEW"
    const val APPROVED = "APPROVED"
    const val REJECTED = "REJECTED"
}

@Serializable
data class OnboardingMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),
    @Transient
    val userId: ObjectId = ObjectId(),
    val currentStep: Int = 1,
    val onboardingStatus: String = OnboardingStatus.IN_PROGRESS,
    val rejectionReason: String? = null,
    val account: OnboardingAccountRequest? = null,
    val store: OnboardingStoreRequest? = null,
    val business: OnboardingBusinessRequest? = null,
    val payment: OnboardingPaymentRequest? = null,
    val catalog: OnboardingCatalogRequest? = null,
    val logistic: OnboardingLogisticsRequest? = null,
    val compliance: OnboardingComplianceRequest? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class OnboardingResponse(
    val currentStep: Int,
    val isOnboardingCompleted: Boolean,
    val onboardingStatus: String,
    val rejectionReason: String? = null,

    val accountCompleted: Boolean,
    val storeCompleted: Boolean,
    val businessCompleted: Boolean,
    val paymentCompleted: Boolean,
    val catalogCompleted: Boolean,
    val logisticCompleted: Boolean,
    val complianceCompleted: Boolean,

    val createdAt: Long,
    val updatedAt: Long,

    // Only populated for account creation (initial auth)
    val token: String? = null,
    val refreshToken: String? = null
)

fun OnboardingMongo.toResponse(token: String? = null, refreshToken: String? = null): OnboardingResponse {
    return OnboardingResponse(
        currentStep = this.currentStep,
        isOnboardingCompleted = this.onboardingStatus == OnboardingStatus.APPROVED,
        onboardingStatus = this.onboardingStatus,
        rejectionReason = this.rejectionReason,

        accountCompleted = this.account != null,
        storeCompleted = this.store != null,
        businessCompleted = this.business != null,
        paymentCompleted = this.payment != null,
        catalogCompleted = this.catalog != null,
        logisticCompleted = this.logistic != null,
        complianceCompleted = this.compliance != null,

        createdAt = this.createdAt,
        updatedAt = this.updatedAt,

        token = token,
        refreshToken = refreshToken
    )
}

enum class OperationType {
    CREATED,
    UPDATED,
    NO_CHANGE
}

data class OnboardingOperationResult(
    val onboarding: OnboardingMongo,
    val operation: OperationType
)

@Serializable
data class OnboardingReviewResponse(
    val currentStep: Int,
    val isOnboardingCompleted: Boolean,
    val onboardingStatus: String,
    val rejectionReason: String? = null,
    val data: OnboardingReviewData? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class OnboardingReviewData(
    val account: OnboardingAccountRequest? = null,
    val store: OnboardingStoreRequest? = null,
    val business: OnboardingBusinessRequest? = null,
    val payment: OnboardingPaymentRequest? = null,
    val catalog: OnboardingCatalogRequest? = null,
    val logistics: OnboardingLogisticsRequest? = null,
    val compliance: OnboardingComplianceRequest? = null
)

fun isSameAccount(
    old: OnboardingAccountRequest,
    new: OnboardingAccountRequest
): Boolean {
    return old.fullName == new.fullName &&
            old.email == new.email &&
            old.mobile == new.mobile &&
            old.profilePhoto == new.profilePhoto &&
            old.password == new.password
}

fun isSameStore(
    old: OnboardingStoreRequest,
    new: OnboardingStoreRequest
): Boolean {
    return old.storeName == new.storeName &&
            old.logo == new.logo &&
            old.banner == new.banner &&
            old.description == new.description &&
            old.address == new.address &&
            old.city == new.city &&
            old.state == new.state &&
            old.pincode == new.pincode &&
            old.latitude == new.latitude &&
            old.longitude == new.longitude &&
            old.supportPhone == new.supportPhone &&
            old.supportEmail == new.supportEmail
}

fun isSameBusiness(
    old: OnboardingBusinessRequest,
    new: OnboardingBusinessRequest
): Boolean {
    return old.type == new.type &&
            old.pan == new.pan &&
            old.aadhar == new.aadhar &&
            old.namePan == new.namePan &&
            old.nameAadhar == new.nameAadhar &&
            old.businessName == new.businessName &&
            old.businessPan == new.businessPan &&
            old.aadharProof == new.aadharProof
}

fun isSamePayment(
    old: OnboardingPaymentRequest,
    new: OnboardingPaymentRequest
): Boolean {
    return old.accountHolder == new.accountHolder &&
            old.bankName == new.bankName &&
            old.ifsc == new.ifsc &&
            old.accountNumber == new.accountNumber &&
            old.chequeImage == new.chequeImage
}

fun isSameCatalog(
    old: OnboardingCatalogRequest,
    new: OnboardingCatalogRequest
): Boolean {
    return old.selectedCategories == new.selectedCategories &&
            old.brands == new.brands &&
            old.isManufacturer == new.isManufacturer &&
            old.isReseller == new.isReseller
}

fun isSameLogistics(
    old: OnboardingLogisticsRequest,
    new: OnboardingLogisticsRequest
): Boolean {
    return old.pickupAddress == new.pickupAddress &&
            old.returnAddress == new.returnAddress &&
            old.dispatchTimeInDays == new.dispatchTimeInDays &&
            old.isSelfShipping == new.isSelfShipping &&
            old.shippingPartners == new.shippingPartners
}

fun isSameCompliance(
    old: OnboardingComplianceRequest,
    new: OnboardingComplianceRequest
): Boolean {
    return old.sellsLiveFish == new.sellsLiveFish &&
            old.hasGovernmentPermit == new.hasGovernmentPermit &&
            old.permitDoc == new.permitDoc &&
            old.animalSafetyDeclaration == new.animalSafetyDeclaration &&
            old.hasMedicineLicense == new.hasMedicineLicense &&
            old.medicineDoc == new.medicineDoc &&
            old.acceptCommission == new.acceptCommission &&
            old.acceptReturnPolicy == new.acceptReturnPolicy &&
            old.acceptTerms == new.acceptTerms
}