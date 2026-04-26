package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val imageUrl: String
)

