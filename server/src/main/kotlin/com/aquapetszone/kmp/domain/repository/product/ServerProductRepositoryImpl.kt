package com.aquapetszone.kmp.domain.repository.product

import com.aquapetszone.kmp.domain.model.request.AddProductRequest
import com.aquapetszone.kmp.domain.model.response.Action
import com.aquapetszone.kmp.domain.model.response.ActionType
import com.aquapetszone.kmp.domain.model.response.Badge
import com.aquapetszone.kmp.domain.model.response.Discount
import com.aquapetszone.kmp.domain.model.response.Price
import com.aquapetszone.kmp.domain.repository.ServerBaseRepository
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId

class ServerProductRepositoryImpl : ServerProductRepository, ServerBaseRepository() {

    private val collection = db.getCollection<ProductMongo>("products")

    override suspend fun createProduct(
        sellerId: String,
        request: AddProductRequest
    ): ProductMongo {

        // ── Validation ──────────────────────────────────────────────
        if (request.name.isNullOrBlank()) throw Exception("Product name is required")
        if (request.description.isNullOrBlank()) throw Exception("Product description is required")
        if (request.coverImageUrl.isNullOrBlank()) throw Exception("Cover image URL is required")
        if (request.shopByCategory.isNullOrBlank()) throw Exception("Shop by category is required")
        if (request.sellingPrice == null || (request.sellingPrice ?: 0.0) <= 0)
            throw Exception("Selling price is required and must be greater than 0")
        if (request.originalMRPPrice == null || (request.originalMRPPrice ?: 0.0) <= 0)
            throw Exception("Original MRP price is required and must be greater than 0")

        val now = System.currentTimeMillis()
        val objectSellerId = ObjectId(sellerId)
        val productObjectId = ObjectId()
        val productId = "P-${productObjectId.toHexString()}"

        // ── Build SKU ───────────────────────────────────────────────
        // Format: [Category]-[Product]-[Variant]-[Number]
        // eg. FISH-SALMON-500G-001
        val categoryPrefix = (request.shopByCategory ?: "GEN").take(4).uppercase()
        val productPrefix = (request.name ?: "PROD").take(6).uppercase().replace(" ", "")
        val variantPart = request.sellingUnitUnit?.uppercase() ?: "STD"
        val numberPart = productObjectId.toHexString().takeLast(3).uppercase()
        val sku = request.sku?.takeIf { it.isNotBlank() }
            ?: "$categoryPrefix-$productPrefix-$variantPart-$numberPart"

        // ── Build Price ─────────────────────────────────────────────
        val price = Price(
            originalMRPPrice = request.originalMRPPrice ?: 0.0,
            sellingPrice = request.sellingPrice ?: 0.0,
            maxDiscountPrice = (request.maxDiscountPrice ?: request.sellingPrice) ?: 0.0,
            currency = request.currency ?: "INR"
        )

        // ── Build Discount ──────────────────────────────────────────
        val discount = if (request.discountPercentage != null && (request.discountPercentage ?: 0) > 0) {
            Discount(
                percentage = request.discountPercentage ?: 0,
                label = "${request.discountPercentage}% OFF"
            )
        } else null

        // ── Build Badge ─────────────────────────────────────────────
        val badgeType = request.badgeType
        val badge = if (!badgeType.isNullOrBlank()) {
            Badge(
                type = badgeType,
                text = request.badgeText ?: badgeType.replace("_", " ")
            )
        } else null

        // ── Build Action ────────────────────────────────────────────
        val action = Action(
            type = ActionType.PRODUCT_DETAIL,
            value = productId
        )

        // ── Build Mongo Document ────────────────────────────────────
        val product = ProductMongo(
            productId = productObjectId,
            sellerId = objectSellerId,
            sku = sku,
            name = request.name!!,
            description = request.description!!,
            coverImageUrl = request.coverImageUrl!!,
            otherImageUrls = request.otherImageUrls,
            price = price,
            discount = discount,
            rating = null,
            availability = request.availability ?: "IN_STOCK",
            badge = badge,
            sellingUnitType = request.sellingUnitType ?: "Quantity",
            sellingUnitValue = request.sellingUnitValue ?: "1",
            sellingUnitUnit = request.sellingUnitUnit ?: "PC",
            shopByCategory = request.shopByCategory!!,
            action = action,
            status = "PENDING",
            submissionStatus = request.submissionStatus ?: "DRAFT",
            createdAt = now,
            updatedAt = now,
            lastEdited = now,
            verifiedBy = null
        )

        collection.insertOne(product)

        return product
    }

    override suspend fun updateProduct(
        sellerId: String,
        productId: String,
        request: AddProductRequest
    ): ProductMongo? {
        val hexId = productId.removePrefix("P-")
        val filter = Filters.and(
            Filters.eq("sellerId", ObjectId(sellerId)),
            Filters.eq("productId", ObjectId(hexId))
        )

        val now = System.currentTimeMillis()
        val updates = mutableListOf(
            Updates.set("updatedAt", now),
            Updates.set("lastEdited", now)
        )

        request.name?.let { updates.add(Updates.set("name", it)) }
        request.description?.let { updates.add(Updates.set("description", it)) }
        request.coverImageUrl?.let { updates.add(Updates.set("coverImageUrl", it)) }
        request.otherImageUrls?.let { updates.add(Updates.set("otherImageUrls", it)) }
        request.shopByCategory?.let { updates.add(Updates.set("shopByCategory", it)) }
        request.availability?.let { updates.add(Updates.set("availability", it)) }
        request.submissionStatus?.let { updates.add(Updates.set("submissionStatus", it)) }
        request.sellingUnitType?.let { updates.add(Updates.set("sellingUnitType", it)) }
        request.sellingUnitValue?.let { updates.add(Updates.set("sellingUnitValue", it)) }
        request.sellingUnitUnit?.let { updates.add(Updates.set("sellingUnitUnit", it)) }

        // Price — rebuild if any price field is provided
        if (request.sellingPrice != null || request.originalMRPPrice != null) {
            val price = Price(
                originalMRPPrice = request.originalMRPPrice ?: 0.0,
                sellingPrice = request.sellingPrice ?: 0.0,
                maxDiscountPrice = (request.maxDiscountPrice ?: request.sellingPrice) ?: 0.0,
                currency = request.currency ?: "INR"
            )
            updates.add(Updates.set("price", price))
        }

        // Discount
        if (request.discountPercentage != null && (request.discountPercentage ?: 0) > 0) {
            val discount = Discount(
                percentage = request.discountPercentage ?: 0,
                label = "${request.discountPercentage}% OFF"
            )
            updates.add(Updates.set("discount", discount))
        }

        // Badge
        if (!request.badgeType.isNullOrBlank()) {
            val badge = Badge(
                type = request.badgeType!!,
                text = request.badgeText ?: request.badgeType!!.replace("_", " ")
            )
            updates.add(Updates.set("badge", badge))
        }

        collection.updateOne(filter, Updates.combine(updates))

        // Return the updated document
        return collection.find(filter).first()
    }

    override suspend fun getProductsBySeller(sellerId: String, page: Int, limit: Int): Pair<List<ProductMongo>, Long> {
        val objectSellerId = ObjectId(sellerId)
        val filter = Filters.eq("sellerId", objectSellerId)
        val totalItems = collection.countDocuments(filter)
        val skip = (page - 1).coerceAtLeast(0) * limit
        val products = collection.find(filter)
            .skip(skip)
            .limit(limit)
            .toList()
        return Pair(products, totalItems)
    }

    override suspend fun getProductById(productId: String): ProductMongo? {
        // productId comes as "P-hexString", strip the prefix
        val hexId = productId.removePrefix("P-")
        return collection.find(
            Filters.eq("productId", ObjectId(hexId))
        ).first()
    }

    override suspend fun deleteProduct(sellerId: String, productId: String): Boolean {
        val hexId = productId.removePrefix("P-")
        // Fetch product first
        val product = collection.find(
            Filters.and(
                Filters.eq("sellerId", ObjectId(sellerId)),
                Filters.eq("productId", ObjectId(hexId))
            )
        ).first() ?: return false

        // Helper to extract S3 key from URL
        fun extractKeyFromUrl(url: String): String? {
            val regex = Regex("""https://[\w.-]+\.s3[\w.-]*/([\w\-./]+)""")
            val match = regex.find(url)
            return match?.groups?.get(1)?.value
        }

        val s3Keys = mutableListOf<String>()
        extractKeyFromUrl(product.coverImageUrl)?.let { s3Keys.add(it) }
        product.otherImageUrls?.forEach { url: String ->
            extractKeyFromUrl(url)?.let { s3Keys.add(it) }
        }

        // Delete all S3 objects before DB record
        try {
            for (key in s3Keys) {
                try {
                    com.aquapetszone.kmp.data.service.S3Service.deleteObject(key)
                } catch (e: Exception) {
                    // Log and abort
                    println("[deleteProduct] Failed to delete S3 object: $key. Error: ${e.message}")
                    return false
                }
            }
        } catch (e: Exception) {
            println("[deleteProduct] Unexpected error during S3 deletion: ${e.message}")
            return false
        }

        // All S3 deletions succeeded, now delete DB record
        val result = collection.deleteOne(
            Filters.and(
                Filters.eq("sellerId", ObjectId(sellerId)),
                Filters.eq("productId", ObjectId(hexId))
            )
        )
        return result.deletedCount > 0
    }

    override suspend fun getAllProducts(category: String?, page: Int, limit: Int): Pair<List<ProductMongo>, Long> {
        val baseFilters = mutableListOf(
            Filters.eq("status", "APPROVED"),
            Filters.eq("submissionStatus", "APPROVED")
        )
        if (!category.isNullOrBlank() && !category.equals("all", ignoreCase = true)) {
            baseFilters.add(Filters.eq("shopByCategory", category))
        }
        val filter = if (baseFilters.size == 1) baseFilters[0] else Filters.and(baseFilters)
        val totalItems = collection.countDocuments(filter)
        val skip = (page - 1).coerceAtLeast(0) * limit
        val products = collection.find(filter)
            .skip(skip)
            .limit(limit)
            .toList()
        return Pair(products, totalItems)
    }
}
