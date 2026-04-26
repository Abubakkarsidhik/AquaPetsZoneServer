package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    val items: T,
    val page: Int,
    val limit: Int,
    val totalItems: Long,
    val totalPages: Int
)

