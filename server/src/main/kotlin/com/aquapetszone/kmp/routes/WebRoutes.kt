package com.aquapetszone.kmp.routes

import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.routing.Route

fun Route.webRoutes() {
    staticResources(
        remotePath = "/web",
        basePackage = "staticWeb",
        index = "index.html"
    )
    staticResources(
        remotePath = "/assets",
        basePackage = "staticWeb/assets"
    )
}