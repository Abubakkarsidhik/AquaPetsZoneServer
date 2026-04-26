package com.aquapetszone.kmp.domain.model.response

import com.aquapetszone.kmp.config.Config
import kotlinx.serialization.Serializable

@Serializable
data class ProductGridResponse(
    val carouselId: String,
    val showTitle: Boolean,
    val itemUiModel: ItemUiModel,
    val title: String,
    val titleModel: TitleModel?,
    val showTitleCard: Boolean,
    val titleCardModel: TitleCardModel?,
    val type: Config.CarouselType,
    val displayOrder: Int,
    val products: List<ProductCarouselModel>?,
    val viewAllEnabled: Boolean,
    val viewAllAction: Action?,
    val tracking: AnalyticsMeta?
)
