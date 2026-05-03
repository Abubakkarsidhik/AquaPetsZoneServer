package com.aquapetszone.kmp.plugins

import com.aquapetszone.kmp.data.database.MongoFactory
import com.aquapetszone.kmp.routes.imageRoutes
import com.aquapetszone.kmp.routes.authRoutes
import com.aquapetszone.kmp.routes.onboardingRoutes.onboardingRoute
import com.aquapetszone.kmp.routes.productRoutes.productRoutes
import com.aquapetszone.kmp.routes.webRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
//        module()
//        authRoutes()
        authRoutes()
        imageRoutes()
        productRoutes()
        onboardingRoute()
//        orderRoutes()
        webRoutes()



        get("/test-db") {
            val dbName = MongoFactory.database.name
            call.respondText("Connected to DB: $dbName")
        }
    }
}
