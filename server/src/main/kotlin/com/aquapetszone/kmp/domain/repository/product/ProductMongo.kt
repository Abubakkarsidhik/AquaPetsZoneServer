package com.aquapetszone.kmp.domain.repository.product

import com.aquapetszone.kmp.domain.model.response.AddProductResponse
import com.aquapetszone.kmp.domain.model.response.Action
import com.aquapetszone.kmp.domain.model.response.Badge
import com.aquapetszone.kmp.domain.model.response.Discount
import com.aquapetszone.kmp.domain.model.response.Price
import com.aquapetszone.kmp.domain.model.response.ProductCarouselModel
import com.aquapetszone.kmp.domain.model.response.QuantityUnit
import com.aquapetszone.kmp.domain.model.response.Rating
import com.aquapetszone.kmp.domain.model.response.SellingUnit
import com.aquapetszone.kmp.domain.model.response.WeightUnit
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class ProductMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val productId: ObjectId = ObjectId(),

    @Transient
    val sellerId: ObjectId = ObjectId(),

    val sku: String,
    val name: String,
    val description: String,
    val coverImageUrl: String,
    val otherImageUrls: List<String>? = null,

    val price: Price,
    val discount: Discount? = null,
    val rating: Rating? = null,

    val availability: String = "IN_STOCK",
    val badge: Badge? = null,

    // ── Selling Unit (flattened for Jackson/KMongo compatibility) ────
    val sellingUnitType: String = "Quantity",   // "Quantity" | "Weight"
    val sellingUnitValue: String = "1",         // numeric value as string
    val sellingUnitUnit: String = "PC",         // "PC", "PAIR", "G", "KG"

    val shopByCategory: String,
    val action: Action,

    val status: String = "PENDING",
    val submissionStatus: String = "DRAFT",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastEdited: Long = System.currentTimeMillis(),
    val verifiedBy: String? = null
)


fun ProductMongo.toResponse(): AddProductResponse {
    return AddProductResponse(
        productId = "P-${this.productId.toHexString()}",
        sellerId = this.sellerId.toHexString(),
        sku = this.sku,
        name = this.name,
        status = this.status,
        submissionStatus = this.submissionStatus,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastEdited = this.lastEdited
    )
}

fun ProductMongo.toCarouselModel(): ProductCarouselModel {
    // ── Reconstruct SellingUnit sealed class from flat fields ────
    val sellingUnit: SellingUnit = if (sellingUnitType == "Weight") {
        SellingUnit.Weight(
            value = sellingUnitValue.toDoubleOrNull() ?: 1.0,
            unit = runCatching { WeightUnit.valueOf(sellingUnitUnit) }.getOrDefault(WeightUnit.G)
        )
    } else {
        SellingUnit.Quantity(
            value = sellingUnitValue.toIntOrNull() ?: 1,
            unit = runCatching { QuantityUnit.valueOf(sellingUnitUnit) }.getOrDefault(QuantityUnit.PC)
        )
    }

    return ProductCarouselModel(
        productId = "P-${this.productId.toHexString()}",
        sellerId = this.sellerId.toHexString(),
        sku = this.sku,
        name = this.name,
        description = this.description,
        coverImageUrl = this.coverImageUrl,
        otherImageUrls = this.otherImageUrls,
        price = this.price,
        discount = this.discount,
        rating = this.rating,
        availability = this.availability,
        badge = this.badge,
        sellingUnit = sellingUnit,
        shopByCategory = this.shopByCategory,
        action = this.action,
        status = this.status,
        submissionStatus = this.submissionStatus,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        lastEdited = this.lastEdited,
        verifiedBy = this.verifiedBy
    )
}

