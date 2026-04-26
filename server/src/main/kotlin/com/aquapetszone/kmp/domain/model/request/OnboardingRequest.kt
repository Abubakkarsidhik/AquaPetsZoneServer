package com.aquapetszone.kmp.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class OnboardingAccountRequest(
    val fullName: String? = null,
    val email: String? = null,
    val mobile: String? = null,
    val profilePhoto: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null
)

@Serializable
data class OnboardingStoreRequest(
    val storeName: String? = null,
    val logo: String? = null,
    val banner: String? = null,
    val description: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    val latitude: String? = null,
    val longitude: String? = null,
    val supportPhone: String? = null,
    val supportEmail: String? = null
)

@Serializable
data class OnboardingBusinessRequest(
    val type: String? = null,
    val pan: String? = null,
    val aadhar: String? = null,
    val namePan: String? = null,
    val nameAadhar: String? = null,
    val businessName: String? = null,
    val businessPan: String? = null,
    val aadharProof: String? = null
)

@Serializable
data class OnboardingCatalogRequest(
    val selectedCategories: List<String> = listOf(),
    val brands: String? = null,
    val isManufacturer: Boolean? = false,
    val isReseller: Boolean? = false
)

@Serializable
data class OnboardingComplianceRequest(
    val sellsLiveFish: Boolean? = null,
    val hasGovernmentPermit: Boolean? = null,
    val permitDoc: String? = null,
    val animalSafetyDeclaration: Boolean? = null,
    val hasMedicineLicense: Boolean? = null,
    val medicineDoc: String? = null,
    val acceptCommission: Boolean? = null,
    val acceptReturnPolicy: Boolean? = null,
    val acceptTerms: Boolean? = null
)

@Serializable
data class OnboardingLogisticsRequest(
    val sameAsStore: Boolean? = null,
    val pickupAddress: String? = null,
    val returnAddress: String? = null,
    val radius: String? = null,
    val shippingType: String? = null,
    val dispatchTimeInDays: String? = "0",
    val shippingPartners: List<String> = emptyList(),
    val isSelfShipping: Boolean = false
)

@Serializable
data class OnboardingPaymentRequest(
    val accountHolder: String? = null,
    val bankName: String? = null,
    val ifsc: String? = null,
    val accountNumber: String? = null,
    val confirmAccountNumber: String? = null,
    val chequeImage: String? = null,
    val upiId: String? = null
)