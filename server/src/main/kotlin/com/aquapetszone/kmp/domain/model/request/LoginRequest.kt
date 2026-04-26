package com.aquapetszone.kmp.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)
@Serializable
data class ErrorResponse(
    val message: String
)