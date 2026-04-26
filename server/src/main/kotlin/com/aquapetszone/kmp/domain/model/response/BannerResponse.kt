package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class BannerResponse(
    val showIndicator: Boolean = false,
    val enableAutoSlide: Boolean = false,
    val slideInterval: Int = 3000,
    val banners: List<Banner> = emptyList(),
)

@Serializable
data class Banner(
    val bannerId: String? = null,
    val bannerName: String? = null,
    val bannerUrl: String? = null,
)
