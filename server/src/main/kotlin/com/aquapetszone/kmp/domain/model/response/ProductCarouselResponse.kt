package com.aquapetszone.kmp.domain.model.response

import com.aquapetszone.kmp.config.Config
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Clock

@Serializable
data class ProductCarouselResponse(
    val carouselId: String,
    val showTitle: Boolean,
    val itemUiModel: ItemUiModel,// Unique identifier for the carousel
    val title: String,
    val titleModel: TitleModel?,         // UI title (e.g., "Best Sellers")
    val showTitleCard: Boolean,         // Show title card
    val titleCardModel: TitleCardModel?,  // UI title (e.g., "Best Sellers")
    val type: Config.CarouselType,       // PRODUCT_CAROUSEL
    val displayOrder: Int,               // Order in the home screen
    val products: List<ProductCarouselModel>?,
    val viewAllEnabled: Boolean,          // Show "View All" CTA
    val viewAllAction: Action?,           // Deep link / navigation action
    val tracking: AnalyticsMeta?          // Analytics & impression tracking
)

@Serializable
data class ItemUiModel(
    val bgColor: LinerColor,
)

@Serializable
data class LinerColor(
    val startColor: String,
    val endColor: String
)

@Serializable
data class ProductCarouselModel(
    val productId: String,
    val sellerId: String? = null,

    val sku: String, // eg.FISH-SALMON-500G-001
    val name: String,
    val description: String,
    val coverImageUrl: String,
    val otherImageUrls: List<String>?,

    val price: Price,
    val discount: Discount?,
    val rating: Rating?,

    val availability: String,
    val badge: Badge?,
    val sellingUnit: SellingUnit,

    val shopByCategory: String,
    val action: Action,

    val status: String = "PENDING", // PENDING / APPROVED / REJECTED
    val submissionStatus: String = "DRAFT", // DRAFT / SENT_FOR_REVIEW
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val lastEdited: Long = Clock.System.now().toEpochMilliseconds(),
    val verifiedBy: String? = null
)

@Serializable data class Price(val originalMRPPrice: Double, val sellingPrice: Double, val maxDiscountPrice: Double,val currency: String)
@Serializable data class Discount(val percentage: Int, val label: String)
@Serializable data class Rating(val average: Double, val count: Int)
@Serializable data class Badge(val type: String, val text: String)
//@Serializable data class SellingUnit(val type: String, val value: Int, val unit: String)
//@Serializable data class Action(val type: String, val value: String)


@Serializable
sealed class SellingUnit {

    @Serializable
    @SerialName("Quantity")
    data class Quantity(
        val value: Int,
        val unit: QuantityUnit
    ) : SellingUnit()

    @Serializable
    @SerialName("Weight")
    data class Weight(
        val value: Double,
        val unit: WeightUnit
    ) : SellingUnit()
}

@Serializable
enum class WeightUnit {
    G,     // grams
    KG     // kilograms
}

@Serializable
data class TitleCardModel(
    val title: String,
    val titleColor: String,
    val subTitle: String,
    val subTitleColor: String,
    val iconUrl: String
)

@Serializable
enum class QuantityUnit {
    PC,
    PAIR
}

@Serializable
data class TitleModel(
    val title: String,
    val titleColor: String,
    val action: Action? = null
)

@Serializable
data class PriceInfo(
    val originalPrice: Double,
    val sellingPrice: Double,
    val currency: String                 // INR, USD
)

@Serializable
data class DiscountInfo(
    val percentage: Int,
    val label: String                    // "20% OFF"
)

@Serializable
data class RatingInfo(
    val average: Double,                 // 4.5
    val count: Int                       // Number of reviews
)

enum class AvailabilityStatus {
    IN_STOCK,
    OUT_OF_STOCK,
    LIMITED_STOCK
}

@Serializable
data class ProductBadge(
    val type: BadgeType,
    val text: String                     // "New Arrival"
)

enum class BadgeType {
    NEW,
    BEST_SELLER,
    FEATURED,
    DISCOUNT
}

@Serializable
data class Action(
    val type: ActionType,
    val value: String                    // productId / deeplink URL
)

enum class ActionType {
    PRODUCT_DETAIL,
    CATEGORY,
    DEEPLINK
}

@Serializable
data class AnalyticsMeta(
    val impressionId: String,
    val source: String                   // HOME, SEARCH
)
