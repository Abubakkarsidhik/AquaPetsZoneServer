package com.aquapetszone.kmp.domain.model.request

import kotlinx.serialization.Serializable

@Serializable
data class FirebaseLoginRequest(
    val idToken: String
)
