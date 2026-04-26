package com.aquapetszone.kmp.domain.model.request

import com.aquapetszone.kmp.domain.model.response.QuantityUnit
import com.aquapetszone.kmp.domain.model.response.SellingUnit
import com.aquapetszone.kmp.domain.model.response.WeightUnit
import kotlinx.serialization.Serializable

@Serializable
data class AddProductRequest(
    val name: String? = null,
    val description: String? = null,
    val sku: String? = null,
    val coverImageUrl: String? = null,
    val otherImageUrls: List<String>? = null,

    // Price fields
    val originalMRPPrice: Double? = null,
    val sellingPrice: Double? = null,
    val maxDiscountPrice: Double? = null,
    val currency: String? = "INR",

    // Discount
    val discountPercentage: Int? = null,

    // Availability & Badge
    val availability: String? = "IN_STOCK",
    val badgeType: String? = null,
    val badgeText: String? = null,

    // Selling Unit
    val sellingUnitType: String? = "Quantity", // "Quantity" | "Weight"
    val sellingUnitValue: String? = "1",
    val sellingUnitUnit: String? = "PC",

    // Category
    val shopByCategory: String? = null,

    // Submission
    val submissionStatus: String? = "DRAFT" // DRAFT | SENT_FOR_REVIEW
)

fun AddProductRequest.buildSellingUnit(): SellingUnit {
    return if (sellingUnitType == "Weight") {
        SellingUnit.Weight(
            value = sellingUnitValue?.toDoubleOrNull() ?: 1.0,
            unit = runCatching { WeightUnit.valueOf(sellingUnitUnit ?: "G") }.getOrDefault(WeightUnit.G)
        )
    } else {
        SellingUnit.Quantity(
            value = sellingUnitValue?.toIntOrNull() ?: 1,
            unit = runCatching { QuantityUnit.valueOf(sellingUnitUnit ?: "PC") }.getOrDefault(QuantityUnit.PC)
        )
    }
}

