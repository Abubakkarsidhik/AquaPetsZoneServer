package com.aquapetszone.kmp.routes.productRoutes

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.domain.model.request.AddProductRequest
import com.aquapetszone.kmp.domain.model.response.AddProductResponse
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.model.response.PaginatedResponse
import com.aquapetszone.kmp.domain.repository.product.ServerProductRepository
import com.aquapetszone.kmp.domain.repository.product.toCarouselModel
import com.aquapetszone.kmp.domain.repository.product.toResponse
import com.aquapetszone.kmp.helper.authorizeRoles
import com.aquapetszone.kmp.helper.handleError
import com.aquapetszone.kmp.helper.userId
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlin.text.toIntOrNull

fun Route.sellerProductRoutes(repo: ServerProductRepository){

    authorizeRoles(Constant.ROLE.SELLER) {

        // ── Add Product ─────────────────────────────────────────
        post("/add") {
            try {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw Exception("Unauthorized")

                val userId = principal.userId()
                val request = call.receive<AddProductRequest>()

                val product = repo.createProduct(userId, request)
                val response = product.toResponse()

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "Product created successfully",
                        code = 200,
                        data = response
                    )
                )
            } catch (e: Exception) {
                call.handleError(e)
            }
        }

        // ── Get My Products (Paginated) ─────────────────────────
        get("/my-products") {
            try {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw Exception("Unauthorized")

                val userId = principal.userId()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

                println("──── GET /products/my-products ────")
                println("Seller ID: $userId | Page: $page | Limit: $limit")

                val (products, totalItems) = repo.getProductsBySeller(userId, page, limit)
                println("Products found: ${products.size} / total: $totalItems")

                val totalPages = if (totalItems == 0L) 0
                else ((totalItems + limit - 1) / limit).toInt()

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
                println("──── ERROR in /products/my-products ────")
                e.printStackTrace()
                call.handleError(e)
            }
        }

        // ── Update Product ───────────────────────────────────────
        put("/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw Exception("Unauthorized")

                val userId = principal.userId()
                val productId = call.parameters["id"]
                    ?: throw Exception("Product ID is required")
                val request = call.receive<AddProductRequest>()

                val updated = repo.updateProduct(userId, productId, request)
                    ?: throw Exception("Product not found or not owned by you")

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "Product updated successfully",
                        code = 200,
                        data = updated.toResponse()
                    )
                )
            } catch (e: Exception) {
                call.handleError(e)
            }
        }

        // ── Delete Product ──────────────────────────────────────
        delete("/{id}") {
            try {
                val principal = call.principal<JWTPrincipal>()
                    ?: throw Exception("Unauthorized")

                val userId = principal.userId()
                val productId = call.parameters["id"]
                    ?: throw Exception("Product ID is required")

                val deleted = repo.deleteProduct(userId, productId)
                if (!deleted) throw Exception("Product not found or not owned by you")

                call.respond(
                    HttpStatusCode.OK,
                    ApiSuccessResponse(
                        message = "Product deleted successfully",
                        code = 200,
                        data = AddProductResponse(productId = productId)
                    )
                )
            } catch (e: Exception) {
                call.handleError(e)
            }
        }
    }


}
