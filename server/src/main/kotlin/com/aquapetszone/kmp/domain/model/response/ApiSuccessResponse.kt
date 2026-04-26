package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiSuccessResponse<T>(
    val message: String? = null,
    val code: Int,
    val data: T? = null
)
