package com.aquapetszone.kmp.domain.model.response

import kotlinx.serialization.Serializable

@Serializable
data class FeedResponse(
    val feed: List<String>
)

