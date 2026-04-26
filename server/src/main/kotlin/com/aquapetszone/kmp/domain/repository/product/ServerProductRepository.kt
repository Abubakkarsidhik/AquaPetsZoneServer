package com.aquapetszone.kmp.domain.repository.product

import com.aquapetszone.kmp.domain.model.request.AddProductRequest

interface ServerProductRepository {
    suspend fun createProduct(sellerId: String, request: AddProductRequest): ProductMongo
    suspend fun updateProduct(sellerId: String, productId: String, request: AddProductRequest): ProductMongo?
    suspend fun getProductsBySeller(sellerId: String, page: Int, limit: Int): Pair<List<ProductMongo>, Long>
    suspend fun getProductById(productId: String): ProductMongo?
    suspend fun deleteProduct(sellerId: String, productId: String): Boolean

    // New: Get all products for buyers, with optional category filter and pagination
    suspend fun getAllProducts(category: String?, page: Int, limit: Int): Pair<List<ProductMongo>, Long>
}

