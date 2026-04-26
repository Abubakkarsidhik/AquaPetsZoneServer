package com.aquapetszone.kmp.config

object Constant {

    // Feature flags – flip to true when ready to enable
    object Compliance {
        const val ENABLE_GOVERNMENT_PERMIT = false
        const val ENABLE_MEDICINE_LICENSE = false
    }

    object ROLE {
        const val USER = "USER"
        const val ADMIN = "ADMIN"
        const val SELLER = "SELLER"

    }

}