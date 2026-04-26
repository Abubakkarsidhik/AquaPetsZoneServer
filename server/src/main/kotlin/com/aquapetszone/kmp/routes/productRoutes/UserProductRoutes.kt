package com.aquapetszone.kmp.routes.productRoutes

import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.model.response.PaginatedResponse
import com.aquapetszone.kmp.domain.repository.product.ServerProductRepository
import com.aquapetszone.kmp.domain.repository.product.toCarouselModel
import com.aquapetszone.kmp.domain.repository.product.toResponse
import com.aquapetszone.kmp.helper.handleError
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlin.text.toIntOrNull

fun Route.userProductRoutes(repo: ServerProductRepository){

    // ── Public: Get Product by ID ───────────────────────────────
    get("/detail/{id}") {
        try {
            val id = call.parameters["id"]
                ?: throw Exception("Product ID is required")

            val product = repo.getProductById(id)
                ?: throw Exception("Product not found")

            call.respond(
                HttpStatusCode.OK,
                ApiSuccessResponse(
                    message = "Product fetched successfully",
                    code = 200,
                    data = product.toResponse()
                )
            )
        } catch (e: Exception) {
            call.handleError(e)
        }
    }

    // ── Public: Get All Products (for buyers, with category filter & pagination) ──
    get {
        try {
            val category = call.request.queryParameters["category"]
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

            val (products, totalItems) = repo.getAllProducts(category, page, limit)
            val totalPages = if (totalItems == 0L) 0 else ((totalItems + limit - 1) / limit).toInt()
            val carouselModels = products.map { it.toCarouselModel() }

            call.respond(
                HttpStatusCode.OK,
                ApiSuccessResponse(
                    message = "Products fetched successfully",
                    code = 200,
                    data = PaginatedResponse(
                        items = carouselModels,
                        page = page,
                        limit = limit,
                        totalItems = totalItems,
                        totalPages = totalPages
                    )
                )
            )
        } catch (e: Exception) {
            call.handleError(e)
        }
    }

}