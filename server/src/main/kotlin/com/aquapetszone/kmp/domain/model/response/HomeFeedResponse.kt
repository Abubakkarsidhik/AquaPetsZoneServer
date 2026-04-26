package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable


@Serializable
data class HomeFeedResponse(
    val screen: String,
    val version: Int,
    val sections: List<HomeSection>
)

@Serializable
data class HomeSection(
    val id: String?,
    val type: SectionType?,
    val visible: Boolean,
    val order: Int?,
    val api: String?,
    val ui: UiConfig?
)

@Serializable
enum class SectionType {
    BANNER,
    PRODUCT_CAROUSEL,
    PRODUCT_GRID,
    MAGAZINE_LIST,
    ARTICLE_LIST,
    CATEGORY_LIST
}

@Serializable
data class UiConfig(
    // Banner-specific
    val style: String? = null,          // SLIDER
    val autoScroll: Boolean? = null,
    val intervalMs: Long? = null,
    val indicator: Boolean? = null,
    val heightDp: Int? = null,

    // List-specific
    val layout: String? = null,         // HORIZONTAL / GRID
    val itemWidthDp: Int? = null,
    val columns: Int? = null
)
