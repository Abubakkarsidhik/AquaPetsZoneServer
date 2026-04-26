package com.aquapetszone.kmp.routes.productRoutes

import com.aquapetszone.kmp.config.json.HomeFeedJson
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.repository.product.ServerProductRepositoryImpl
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import com.aquapetszone.kmp.domain.model.response.HomeFeedResponse
import com.aquapetszone.kmp.utils.JsonUtil

fun Route.productRoutes() {

    val repo by lazy {  ServerProductRepositoryImpl() }

    route("/home-feed") {
        get {
            val category = call.request.queryParameters["categoryId"]?.uppercase() ?: "ALL"
            val json = when (category) {
                "ALL" -> HomeFeedJson.homeAll
                "FISH" -> HomeFeedJson.homeFish
                "PLANT" -> HomeFeedJson.homePlants
                "TOOLS" -> HomeFeedJson.homeTools
                else -> HomeFeedJson.homeAll
            }
            val homeFeed = JsonUtil.decode<HomeFeedResponse>(json)
            call.respond(
                ApiSuccessResponse(
                    message = "Home feed fetched successfully",
                    code = HttpStatusCode.OK.value,
                    data = homeFeed
                )
            )
        }
    }

    route("/products") {
        userProductRoutes(repo)
        sellerProductRoutes(repo)
    }

}
