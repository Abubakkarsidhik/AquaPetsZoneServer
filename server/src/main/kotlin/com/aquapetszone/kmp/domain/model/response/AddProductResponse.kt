package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class AddProductResponse(
    val productId: String? = null,
    val sellerId: String? = null,
    val sku: String? = null,
    val name: String? = null,
    val status: String? = null,
    val submissionStatus: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
    val lastEdited: Long? = null
)

