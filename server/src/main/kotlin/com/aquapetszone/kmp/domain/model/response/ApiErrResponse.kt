package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrResponse(
    val code: Int,
    val message: String,
    val success: Boolean = false
)