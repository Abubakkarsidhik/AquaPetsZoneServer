package com.aquapetszone.kmp.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

object JsonUtil {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        isLenient = true
        encodeDefaults = true
        allowTrailingComma = true
        explicitNulls = false
        coerceInputValues = true
    }

    inline fun <reified T> encode(data: T): String {
        return json.encodeToString(data)
    }

    inline fun <reified T> decode(encodedString: String): T {
        return json.decodeFromString(encodedString)
    }


}
