package com.aquapetszone.kmp.config

import kotlinx.serialization.Serializable


object Config {


    @Serializable
    enum class CarouselType {
        PRODUCT_CAROUSEL,
        FEATURED_PRODUCTS,
        NEW_ARRIVALS,
        BEST_SELLERS,
        TRENDING,
        RECOMMENDED,
        DISCOUNTED_PRODUCTS,
        SIMILAR_PRODUCTS,
        RECENTLY_VIEWED,
        TOP_RATED,
        UNKNOWN
    }
    enum class MenuType {
        NONE,
        THEME,
        LANGUAGE
    }
    object Onboarding {
        object Business {
            const val INDIVIDUAL = "INDIVIDUAL"
            const val COMPANY = "COMPANY"
        }
    }



}

enum class CategoryKey {
    ALL,
    FISH,
    PLANT,
    FOOD,
    WATER_FILTER,
    TANK,
    DECOR,
    MEDICINE,
    TOOLS
}

enum class OnboardingKey {
    ACCOUNT,
    STORE,
    BUSINESS,
    PAYMENT,
    CATALOG,
    LOGISTICS,
    COMPLIANCE,
    REVIEW
}

enum class SEND_OTP_TYPE {
    PHONE_NO,
    EMAIL_ID
}

