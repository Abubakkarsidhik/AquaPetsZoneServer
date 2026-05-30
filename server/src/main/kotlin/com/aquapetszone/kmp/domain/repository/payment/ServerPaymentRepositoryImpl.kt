package com.aquapetszone.kmp.domain.repository.payment

import com.aquapetszone.kmp.config.InvoiceStatus
import com.aquapetszone.kmp.config.OrderStatus
import com.aquapetszone.kmp.config.PaymentAuditAction
import com.aquapetszone.kmp.config.PaymentStatus
import com.aquapetszone.kmp.config.RazorpayConfig
import com.aquapetszone.kmp.config.RefundStatus
import com.aquapetszone.kmp.data.service.RazorpayService
import com.aquapetszone.kmp.domain.model.request.payment.CreateOrderRequest
import com.aquapetszone.kmp.domain.model.request.payment.PartialRefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.PaymentFailedRequest
import com.aquapetszone.kmp.domain.model.request.payment.RefundRequest
import com.aquapetszone.kmp.domain.model.request.payment.VerifyPaymentRequest
import com.aquapetszone.kmp.domain.model.response.payment.CreateOrderResponse
import com.aquapetszone.kmp.domain.model.response.payment.OrderPaymentResponse
import com.aquapetszone.kmp.domain.model.response.payment.PaymentStatusResponse
import com.aquapetszone.kmp.domain.model.response.payment.PaymentVerificationResponse
import com.aquapetszone.kmp.domain.model.response.payment.RefundResponse
import com.aquapetszone.kmp.domain.repository.ServerBaseRepository
import com.aquapetszone.kmp.domain.repository.product.ProductMongo
import com.aquapetszone.kmp.utils.PaymentAuditLogger
import com.aquapetszone.kmp.utils.PaymentSecurityUtil
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.bson.types.ObjectId
import java.security.MessageDigest
import java.time.YearMonth
import java.util.UUID

class ServerPaymentRepositoryImpl : ServerPaymentRepository, ServerBaseRepository() {

    private val ordersCollection = db.getCollection<OrderMongo>("orders")
    private val paymentsCollection = db.getCollection<PaymentMongo>("payments")
    private val transactionsCollection = db.getCollection<PaymentTransactionMongo>("payment_transactions")
    private val refundsCollection = db.getCollection<RefundMongo>("refunds")
    private val webhookEventsCollection = db.getCollection<WebhookEventMongo>("webhook_events")
    private val auditLogsCollection = db.getCollection<AuditLogMongo>("audit_logs")
    private val productsCollection = db.getCollection<ProductMongo>("products")

    override suspend fun createOrder(
        userId: String,
        request: CreateOrderRequest
    ): CreateOrderResponse {
        validateUserId(userId)
        PaymentSecurityUtil.sanitize(request.deliveryAddress, 512)

        if (request.items.isEmpty()) {
            throw Exception("At least one item is required")
        }

        val idempotencyKey = resolveIdempotencyKey(request.idempotencyKey)
        checkIdempotencyForOrder(userId, idempotencyKey)

        val lineItems = mutableListOf<OrderLineItem>()
        var subtotal = 0.0

        for (item in request.items) {
            if (item.quantity <= 0) throw Exception("Quantity must be greater than 0")
            val product = loadAndValidateProduct(item.productId, item.quantity)
            val unitPrice = product.price.sellingPrice
            val lineTotal = unitPrice * item.quantity
            subtotal += lineTotal
            lineItems.add(
                OrderLineItem(
                    productId = "P-${product.productId.toHexString()}",
                    sellerId = product.sellerId.toHexString(),
                    sku = product.sku,
                    name = product.name,
                    quantity = item.quantity,
                    unitPrice = unitPrice,
                    lineTotal = lineTotal
                )
            )
        }

        val discount = 0.0
        val couponDiscount = calculateCouponDiscount(
            request.couponCode?.let { PaymentSecurityUtil.sanitize(it, 32) },
            subtotal
        )
        val deliveryFee = if (request.applyDeliveryFee) RazorpayConfig.DEFAULT_DELIVERY_FEE else 0.0
        val taxableAmount = (subtotal - discount - couponDiscount + deliveryFee).coerceAtLeast(0.0)
        val tax = taxableAmount * (RazorpayConfig.TAX_RATE_PERCENT / 100.0)
        val grandTotal = taxableAmount + tax

        if (grandTotal <= 0) throw Exception("Order total must be greater than 0")

        val now = System.currentTimeMillis()
        val orderObjectId = ObjectId()
        val userObjectId = ObjectId(userId)
        val receipt = "${RazorpayConfig.RECEIPT_PREFIX}-${orderObjectId.toHexString().takeLast(8)}"
        val amountPaise = (grandTotal * 100).toLong()

        val razorpayOrder = RazorpayService.createOrder(
            amountPaise = amountPaise,
            receipt = receipt,
            notes = mapOf(
                "order_id" to orderObjectId.toHexString(),
                "user_id" to userId
            )
        )

        val order = OrderMongo(
            orderId = orderObjectId,
            userId = userObjectId,
            items = lineItems,
            pricing = OrderPricing(
                subtotal = subtotal,
                discount = discount,
                couponDiscount = couponDiscount,
                deliveryFee = deliveryFee,
                tax = tax,
                grandTotal = grandTotal
            ),
            status = OrderStatus.PENDING_PAYMENT,
            razorpayOrderId = razorpayOrder.id,
            couponCode = request.couponCode,
            deliveryAddress = request.deliveryAddress,
            idempotencyKey = idempotencyKey,
            createdAt = now,
            updatedAt = now
        )

        ordersCollection.insertOne(order)

        val payment = PaymentMongo(
            paymentId = ObjectId(),
            orderId = orderObjectId,
            userId = userObjectId,
            razorpayOrderId = razorpayOrder.id,
            amount = grandTotal,
            amountPaise = amountPaise,
            status = PaymentStatus.PENDING,
            idempotencyKey = idempotencyKey,
            createdAt = now,
            updatedAt = now
        )
        paymentsCollection.insertOne(payment)

        ordersCollection.updateOne(
            Filters.eq("orderId", orderObjectId),
            Updates.combine(
                Updates.set("paymentId", payment.paymentId.toHexString()),
                Updates.set("updatedAt", now)
            )
        )

        recordTransaction(
            paymentId = payment.paymentId.toHexString(),
            orderId = orderObjectId.toHexString(),
            fromStatus = null,
            toStatus = PaymentStatus.PENDING,
            source = "CREATE_ORDER"
        )

        writeAudit(
            PaymentAuditAction.PAYMENT_CREATE,
            userId,
            orderObjectId.toHexString(),
            payment.paymentId.toHexString(),
            mapOf("amount" to grandTotal.toString(), "razorpayOrderId" to razorpayOrder.id)
        )

        PaymentAuditLogger.log(
            PaymentAuditAction.PAYMENT_CREATE,
            mapOf("orderId" to orderObjectId.toHexString(), "amountPaise" to amountPaise.toString())
        )

        return order.toCreateOrderResponse(RazorpayConfig.keyId)
    }

    override suspend fun verifyPayment(
        userId: String,
        request: VerifyPaymentRequest
    ): PaymentVerificationResponse {
        validateUserId(userId)

        val razorpayOrderId = PaymentSecurityUtil.sanitize(request.razorpayOrderId, 64)
        val razorpayPaymentId = PaymentSecurityUtil.sanitize(request.razorpayPaymentId, 64)
        val razorpaySignature = PaymentSecurityUtil.sanitize(request.razorpaySignature, 256)

        if (razorpayOrderId.isBlank() || razorpayPaymentId.isBlank() || razorpaySignature.isBlank()) {
            throw Exception("razorpay_order_id, razorpay_payment_id and razorpay_signature are required")
        }

        if (!RazorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, razorpaySignature)) {
            writeAudit(
                PaymentAuditAction.PAYMENT_FAILED,
                userId,
                metadata = mapOf("reason" to "invalid_signature", "razorpayOrderId" to razorpayOrderId)
            )
            throw Exception("Invalid payment signature")
        }

        val order = ordersCollection.find(
            Filters.and(
                Filters.eq("userId", ObjectId(userId)),
                Filters.eq("razorpayOrderId", razorpayOrderId)
            )
        ).first() ?: throw Exception("Order not found")

        val payment = paymentsCollection.find(
            Filters.and(
                Filters.eq("orderId", order.orderId),
                Filters.eq("userId", ObjectId(userId))
            )
        ).first() ?: throw Exception("Payment not found")

        if (payment.status == PaymentStatus.SUCCESS) {
            return PaymentVerificationResponse(
                success = true,
                orderId = order.orderId.toHexString(),
                paymentId = payment.paymentId.toHexString(),
                orderStatus = order.status,
                paymentStatus = payment.status,
                invoiceNumber = order.invoiceNumber,
                message = "Payment already verified"
            )
        }

        val gatewayPayment = RazorpayService.fetchPayment(razorpayPaymentId)
        if (gatewayPayment.order_id != razorpayOrderId) {
            throw Exception("Payment does not belong to this order")
        }

        val now = System.currentTimeMillis()
        val invoiceNumber = generateInvoiceNumber(order.orderId)

        finalizeSuccessfulPayment(
            order = order,
            payment = payment,
            razorpayPaymentId = razorpayPaymentId,
            gatewayPaymentMethod = gatewayPayment.method,
            gatewayResponse = gatewayPayment.status,
            invoiceNumber = invoiceNumber,
            now = now
        )

        writeAudit(
            PaymentAuditAction.PAYMENT_SUCCESS,
            userId,
            order.orderId.toHexString(),
            payment.paymentId.toHexString(),
            mapOf("razorpayPaymentId" to razorpayPaymentId)
        )

        PaymentAuditLogger.log(
            PaymentAuditAction.PAYMENT_SUCCESS,
            mapOf("orderId" to order.orderId.toHexString(), "paymentId" to payment.paymentId.toHexString())
        )

        return PaymentVerificationResponse(
            success = true,
            orderId = order.orderId.toHexString(),
            paymentId = payment.paymentId.toHexString(),
            orderStatus = OrderStatus.CONFIRMED,
            paymentStatus = PaymentStatus.SUCCESS,
            invoiceNumber = invoiceNumber,
            message = "Payment verified successfully"
        )
    }

    override suspend fun handlePaymentFailed(
        userId: String,
        request: PaymentFailedRequest
    ): PaymentStatusResponse {
        validateUserId(userId)

        val orderId = PaymentSecurityUtil.sanitize(request.orderId, 64)
        val order = ordersCollection.find(
            Filters.and(
                Filters.eq("orderId", ObjectId(orderId)),
                Filters.eq("userId", ObjectId(userId))
            )
        ).first() ?: throw Exception("Order not found")

        val payment = paymentsCollection.find(Filters.eq("orderId", order.orderId)).first()
            ?: throw Exception("Payment not found")

        val now = System.currentTimeMillis()
        val failureReason = PaymentSecurityUtil.sanitize(request.failureReason, 512)
        val errorCode = PaymentSecurityUtil.sanitize(request.errorCode, 64)
        val gatewayResponse = PaymentSecurityUtil.sanitize(request.gatewayResponse, 1024)

        paymentsCollection.updateOne(
            Filters.eq("paymentId", payment.paymentId),
            Updates.combine(
                Updates.set("status", PaymentStatus.FAILED),
                Updates.set("failureReason", failureReason.ifBlank { "Payment failed" }),
                Updates.set("errorCode", errorCode.ifBlank { null }),
                Updates.set("gatewayResponse", gatewayResponse.ifBlank { null }),
                Updates.set("updatedAt", now)
            )
        )

        ordersCollection.updateOne(
            Filters.eq("orderId", order.orderId),
            Updates.combine(
                Updates.set("status", OrderStatus.PAYMENT_FAILED),
                Updates.set("updatedAt", now)
            )
        )

        recordTransaction(
            paymentId = payment.paymentId.toHexString(),
            orderId = order.orderId.toHexString(),
            fromStatus = payment.status,
            toStatus = PaymentStatus.FAILED,
            source = "PAYMENT_FAILED",
            metadata = failureReason
        )

        writeAudit(
            PaymentAuditAction.PAYMENT_FAILED,
            userId,
            order.orderId.toHexString(),
            payment.paymentId.toHexString(),
            mapOf("errorCode" to errorCode, "reason" to failureReason)
        )

        PaymentAuditLogger.log(
            PaymentAuditAction.PAYMENT_FAILED,
            mapOf("orderId" to order.orderId.toHexString())
        )

        val updated = paymentsCollection.find(Filters.eq("paymentId", payment.paymentId)).first()!!
        return updated.toPaymentStatusResponse()
    }

    override suspend fun refund(
        userId: String,
        request: RefundRequest
    ): RefundResponse {
        validateUserId(userId)
        val payment = loadPaymentForUser(request.paymentId, userId)
        if (payment.status != PaymentStatus.SUCCESS &&
            payment.status != PaymentStatus.PARTIALLY_REFUNDED
        ) {
            throw Exception("Only successful payments can be refunded")
        }

        val razorpayPaymentId = payment.razorpayPaymentId
            ?: throw Exception("Razorpay payment id not found")

        val remaining = payment.amount - payment.refundedAmount
        if (remaining <= 0) throw Exception("Payment already fully refunded")

        request.idempotencyKey?.let { checkRefundIdempotency(it) }

        val refundDto = RazorpayService.refundPayment(
            razorpayPaymentId = razorpayPaymentId,
            amountPaise = (remaining * 100).toLong(),
            notes = mapOf("reason" to (request.reason ?: "full_refund"))
        )

        return persistRefund(
            payment = payment,
            amount = remaining,
            amountPaise = (remaining * 100).toLong(),
            reason = request.reason,
            idempotencyKey = request.idempotencyKey,
            isPartial = false,
            razorpayRefund = refundDto
        )
    }

    override suspend fun partialRefund(
        userId: String,
        request: PartialRefundRequest
    ): RefundResponse {
        validateUserId(userId)
        if (request.amount <= 0) throw Exception("Refund amount must be greater than 0")

        val payment = loadPaymentForUser(request.paymentId, userId)
        if (payment.status != PaymentStatus.SUCCESS &&
            payment.status != PaymentStatus.PARTIALLY_REFUNDED
        ) {
            throw Exception("Only successful payments can be refunded")
        }

        val remaining = payment.amount - payment.refundedAmount
        if (request.amount > remaining) {
            throw Exception("Refund amount exceeds remaining refundable amount")
        }

        val razorpayPaymentId = payment.razorpayPaymentId
            ?: throw Exception("Razorpay payment id not found")

        request.idempotencyKey?.let { checkRefundIdempotency(it) }

        val amountPaise = (request.amount * 100).toLong()
        val refundDto = RazorpayService.refundPayment(
            razorpayPaymentId = razorpayPaymentId,
            amountPaise = amountPaise,
            notes = mapOf("reason" to (request.reason ?: "partial_refund"))
        )

        return persistRefund(
            payment = payment,
            amount = request.amount,
            amountPaise = amountPaise,
            reason = request.reason,
            idempotencyKey = request.idempotencyKey,
            isPartial = true,
            razorpayRefund = refundDto
        )
    }

    override suspend fun getPayment(paymentId: String, userId: String): PaymentStatusResponse {
        validateUserId(userId)
        val payment = loadPaymentForUser(paymentId, userId)
        return payment.toPaymentStatusResponse()
    }

    override suspend fun getRefund(refundId: String, userId: String): RefundResponse {
        validateUserId(userId)
        val refund = refundsCollection.find(Filters.eq("refundId", ObjectId(refundId))).first()
            ?: throw Exception("Refund not found")

        val payment = loadPaymentForUser(refund.paymentId, userId)
        if (payment.paymentId.toHexString() != refund.paymentId) {
            throw Exception("Refund not found")
        }
        return refund.toRefundResponse()
    }

    override suspend fun getOrderPayment(orderId: String, userId: String): OrderPaymentResponse {
        validateUserId(userId)
        val order = ordersCollection.find(
            Filters.and(
                Filters.eq("orderId", ObjectId(orderId)),
                Filters.eq("userId", ObjectId(userId))
            )
        ).first() ?: throw Exception("Order not found")

        val payment = paymentsCollection.find(Filters.eq("orderId", order.orderId)).first()
        return order.toOrderPaymentResponse(payment)
    }

    override suspend fun processWebhook(rawBody: String, signature: String): Boolean {
        val payload = RazorpayService.processWebhook(rawBody, signature)
        val eventId = buildWebhookEventId(payload.event, rawBody)
        val payloadHash = sha256(rawBody)

        val existing = webhookEventsCollection.find(Filters.eq("eventId", eventId)).first()
        if (existing != null) {
            if (existing.processed) return true
        } else {
            webhookEventsCollection.insertOne(
                WebhookEventMongo(
                    eventId = eventId,
                    eventType = payload.event,
                    payloadHash = payloadHash,
                    rawPayload = rawBody.take(4096),
                    processed = false
                )
            )
        }

        try {
            when (payload.event) {
                "payment.authorized", "payment.captured", "order.paid" -> {
                    handleWebhookPaymentSuccess(payload, rawBody)
                }
                "payment.failed" -> {
                    handleWebhookPaymentFailed(payload)
                }
                "refund.created", "refund.processed" -> {
                    handleWebhookRefund(payload)
                }
            }

            webhookEventsCollection.updateOne(
                Filters.eq("eventId", eventId),
                Updates.combine(
                    Updates.set("processed", true),
                    Updates.set("processingResult", "OK")
                )
            )
            return true
        } catch (e: Exception) {
            webhookEventsCollection.updateOne(
                Filters.eq("eventId", eventId),
                Updates.set("processingResult", e.message ?: "FAILED")
            )
            PaymentAuditLogger.log(
                PaymentAuditAction.WEBHOOK_FAILED,
                mapOf("event" to payload.event, "error" to (e.message ?: "unknown"))
            )
            throw e
        }
    }

    private suspend fun handleWebhookPaymentSuccess(
        payload: com.aquapetszone.kmp.data.service.RazorpayWebhookPayload,
        rawBody: String
    ) {
        val paymentEntity = payload.payload?.payment?.entity ?: return
        val razorpayOrderId = paymentEntity["order_id"]?.jsonPrimitive?.content ?: return
        val razorpayPaymentId = paymentEntity["id"]?.jsonPrimitive?.content ?: return

        val order = ordersCollection.find(Filters.eq("razorpayOrderId", razorpayOrderId)).first()
            ?: return

        if (order.status == OrderStatus.CONFIRMED) return

        val payment = paymentsCollection.find(Filters.eq("orderId", order.orderId)).first()
            ?: return

        if (payment.status == PaymentStatus.SUCCESS) return

        val now = System.currentTimeMillis()
        val invoiceNumber = order.invoiceNumber ?: generateInvoiceNumber(order.orderId)

        finalizeSuccessfulPayment(
            order = order,
            payment = payment,
            razorpayPaymentId = razorpayPaymentId,
            gatewayPaymentMethod = paymentEntity["method"]?.jsonPrimitive?.content,
            gatewayResponse = paymentEntity["status"]?.jsonPrimitive?.content,
            invoiceNumber = invoiceNumber,
            now = now
        )

        writeAudit(
            PaymentAuditAction.PAYMENT_SUCCESS,
            order.userId.toHexString(),
            order.orderId.toHexString(),
            payment.paymentId.toHexString(),
            mapOf("source" to "webhook", "event" to payload.event)
        )
    }

    private suspend fun handleWebhookPaymentFailed(
        payload: com.aquapetszone.kmp.data.service.RazorpayWebhookPayload
    ) {
        val paymentEntity = payload.payload?.payment?.entity ?: return
        val razorpayOrderId = paymentEntity["order_id"]?.jsonPrimitive?.content ?: return
        val order = ordersCollection.find(Filters.eq("razorpayOrderId", razorpayOrderId)).first()
            ?: return
        val payment = paymentsCollection.find(Filters.eq("orderId", order.orderId)).first()
            ?: return

        val now = System.currentTimeMillis()
        val errorCode = paymentEntity["error_code"]?.jsonPrimitive?.content
        val errorDescription = paymentEntity["error_description"]?.jsonPrimitive?.content

        paymentsCollection.updateOne(
            Filters.eq("paymentId", payment.paymentId),
            Updates.combine(
                Updates.set("status", PaymentStatus.FAILED),
                Updates.set("failureReason", errorDescription ?: "Payment failed"),
                Updates.set("errorCode", errorCode),
                Updates.set("updatedAt", now)
            )
        )
        ordersCollection.updateOne(
            Filters.eq("orderId", order.orderId),
            Updates.combine(
                Updates.set("status", OrderStatus.PAYMENT_FAILED),
                Updates.set("updatedAt", now)
            )
        )
    }

    private suspend fun handleWebhookRefund(
        payload: com.aquapetszone.kmp.data.service.RazorpayWebhookPayload
    ) {
        val refundEntity = payload.payload?.refund?.entity ?: return
        val razorpayRefundId = refundEntity["id"]?.jsonPrimitive?.content ?: return
        val razorpayPaymentId = refundEntity["payment_id"]?.jsonPrimitive?.content ?: return

        val payment = paymentsCollection.find(
            Filters.eq("razorpayPaymentId", razorpayPaymentId)
        ).first() ?: return

        refundsCollection.updateOne(
            Filters.eq("razorpayRefundId", razorpayRefundId),
            Updates.combine(
                Updates.set("status", RefundStatus.PROCESSED),
                Updates.set("updatedAt", System.currentTimeMillis())
            )
        )

        val refundAmountPaise = refundEntity["amount"]?.jsonPrimitive?.content?.toLongOrNull() ?: 0L
        val refundAmount = refundAmountPaise / 100.0
        val newRefundedTotal = payment.refundedAmount + refundAmount
        val newStatus = if (newRefundedTotal >= payment.amount - 0.01) {
            PaymentStatus.REFUNDED
        } else {
            PaymentStatus.PARTIALLY_REFUNDED
        }

        paymentsCollection.updateOne(
            Filters.eq("paymentId", payment.paymentId),
            Updates.combine(
                Updates.set("refundedAmount", newRefundedTotal),
                Updates.set("status", newStatus),
                Updates.set("updatedAt", System.currentTimeMillis())
            )
        )
    }

    private suspend fun finalizeSuccessfulPayment(
        order: OrderMongo,
        payment: PaymentMongo,
        razorpayPaymentId: String,
        gatewayPaymentMethod: String?,
        gatewayResponse: String?,
        invoiceNumber: String,
        now: Long
    ) {
        recheckInventory(order)

        paymentsCollection.updateOne(
            Filters.eq("paymentId", payment.paymentId),
            Updates.combine(
                Updates.set("status", PaymentStatus.SUCCESS),
                Updates.set("razorpayPaymentId", razorpayPaymentId),
                Updates.set("method", gatewayPaymentMethod),
                Updates.set("gatewayResponse", gatewayResponse),
                Updates.set("updatedAt", now)
            )
        )

        ordersCollection.updateOne(
            Filters.eq("orderId", order.orderId),
            Updates.combine(
                Updates.set("status", OrderStatus.CONFIRMED),
                Updates.set("invoiceNumber", invoiceNumber),
                Updates.set("invoiceDate", now),
                Updates.set("invoiceStatus", InvoiceStatus.GENERATED),
                Updates.set("updatedAt", now)
            )
        )

        reduceInventory(order)

        recordTransaction(
            paymentId = payment.paymentId.toHexString(),
            orderId = order.orderId.toHexString(),
            fromStatus = payment.status,
            toStatus = PaymentStatus.SUCCESS,
            source = "VERIFY_PAYMENT"
        )
    }

    private suspend fun persistRefund(
        payment: PaymentMongo,
        amount: Double,
        amountPaise: Long,
        reason: String?,
        idempotencyKey: String?,
        isPartial: Boolean,
        razorpayRefund: com.aquapetszone.kmp.data.service.RazorpayRefundDto
    ): RefundResponse {
        val now = System.currentTimeMillis()
        val refundDoc = RefundMongo(
            refundId = ObjectId(),
            paymentId = payment.paymentId.toHexString(),
            orderId = payment.orderId.toHexString(),
            razorpayRefundId = razorpayRefund.id,
            razorpayPaymentId = payment.razorpayPaymentId,
            amount = amount,
            amountPaise = amountPaise,
            reason = reason,
            idempotencyKey = idempotencyKey,
            status = razorpayRefund.status ?: RefundStatus.PENDING,
            isPartial = isPartial,
            gatewayResponse = razorpayRefund.status,
            createdAt = now,
            updatedAt = now
        )
        refundsCollection.insertOne(refundDoc)

        val newRefunded = payment.refundedAmount + amount
        val paymentStatus = if (newRefunded >= payment.amount - 0.01) {
            PaymentStatus.REFUNDED
        } else {
            PaymentStatus.PARTIALLY_REFUNDED
        }
        val orderStatus = if (paymentStatus == PaymentStatus.REFUNDED) {
            OrderStatus.REFUNDED
        } else {
            OrderStatus.PARTIALLY_REFUNDED
        }

        paymentsCollection.updateOne(
            Filters.eq("paymentId", payment.paymentId),
            Updates.combine(
                Updates.set("refundedAmount", newRefunded),
                Updates.set("status", paymentStatus),
                Updates.set("updatedAt", now)
            )
        )

        ordersCollection.updateOne(
            Filters.eq("orderId", payment.orderId),
            Updates.combine(
                Updates.set("status", orderStatus),
                Updates.set("refundStatus", refundDoc.status),
                Updates.set("updatedAt", now)
            )
        )

        writeAudit(
            PaymentAuditAction.PAYMENT_REFUND,
            payment.userId.toHexString(),
            payment.orderId.toHexString(),
            payment.paymentId.toHexString(),
            mapOf("amount" to amount.toString(), "partial" to isPartial.toString())
        )

        PaymentAuditLogger.log(
            PaymentAuditAction.PAYMENT_REFUND,
            mapOf("paymentId" to payment.paymentId.toHexString(), "amount" to amount.toString())
        )

        return refundDoc.toRefundResponse()
    }

    private suspend fun loadAndValidateProduct(productId: String, quantity: Int): ProductMongo {
        val hexId = productId.removePrefix("P-")
        val product = productsCollection.find(Filters.eq("productId", ObjectId(hexId))).first()
            ?: throw Exception("Product not found: $productId")

        if (product.availability != "IN_STOCK") {
            throw Exception("Product ${product.name} is out of stock")
        }

        val stock = product.stockQuantity ?: 100
        if (quantity > stock) {
            throw Exception("Insufficient stock for ${product.name}")
        }

        return product
    }

    private suspend fun recheckInventory(order: OrderMongo) {
        for (item in order.items) {
            loadAndValidateProduct(item.productId, item.quantity)
        }
    }

    private suspend fun reduceInventory(order: OrderMongo) {
        for (item in order.items) {
            val hexId = item.productId.removePrefix("P-")
            val product = productsCollection.find(Filters.eq("productId", ObjectId(hexId))).first()
                ?: continue
            val currentStock = product.stockQuantity ?: 100
            val newStock = (currentStock - item.quantity).coerceAtLeast(0)
            val availability = if (newStock <= 0) "OUT_OF_STOCK" else "IN_STOCK"
            productsCollection.updateOne(
                Filters.eq("productId", ObjectId(hexId)),
                Updates.combine(
                    Updates.set("stockQuantity", newStock),
                    Updates.set("availability", availability),
                    Updates.set("updatedAt", System.currentTimeMillis())
                )
            )
        }
    }

    private fun calculateCouponDiscount(couponCode: String?, subtotal: Double): Double {
        if (couponCode.isNullOrBlank()) return 0.0
        return when (couponCode.uppercase()) {
            "APZ10" -> (subtotal * 0.10).coerceAtMost(500.0)
            "APZ5" -> (subtotal * 0.05).coerceAtMost(250.0)
            else -> 0.0
        }
    }

    private suspend fun loadPaymentForUser(paymentId: String, userId: String): PaymentMongo {
        return paymentsCollection.find(
            Filters.and(
                Filters.eq("paymentId", ObjectId(paymentId)),
                Filters.eq("userId", ObjectId(userId))
            )
        ).first() ?: throw Exception("Payment not found")
    }

    private fun validateUserId(userId: String) {
        if (userId.isBlank()) throw Exception("Unauthorized")
        if (!ObjectId.isValid(userId)) throw Exception("Invalid user id")
    }

    private fun resolveIdempotencyKey(provided: String?): String {
        return provided?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
    }

    private suspend fun checkIdempotencyForOrder(userId: String, idempotencyKey: String) {
        val cutoff = System.currentTimeMillis() - RazorpayConfig.IDEMPOTENCY_TTL_MS
        val existing = ordersCollection.find(
            Filters.and(
                Filters.eq("userId", ObjectId(userId)),
                Filters.eq("idempotencyKey", idempotencyKey),
                Filters.gte("createdAt", cutoff)
            )
        ).first()
        if (existing != null) {
            throw Exception("Duplicate order request detected")
        }
    }

    private suspend fun checkRefundIdempotency(idempotencyKey: String) {
        val existing = refundsCollection.find(Filters.eq("idempotencyKey", idempotencyKey)).first()
        if (existing != null) throw Exception("Duplicate refund request detected")
    }

    private suspend fun recordTransaction(
        paymentId: String,
        orderId: String,
        fromStatus: String?,
        toStatus: String,
        source: String,
        metadata: String? = null
    ) {
        transactionsCollection.insertOne(
            PaymentTransactionMongo(
                transactionId = ObjectId(),
                paymentId = paymentId,
                orderId = orderId,
                fromStatus = fromStatus,
                toStatus = toStatus,
                source = source,
                metadata = metadata
            )
        )
    }

    private suspend fun writeAudit(
        action: String,
        userId: String? = null,
        orderId: String? = null,
        paymentId: String? = null,
        metadata: Map<String, String> = emptyMap()
    ) {
        auditLogsCollection.insertOne(
            AuditLogMongo(
                action = action,
                userId = userId,
                orderId = orderId,
                paymentId = paymentId,
                metadata = metadata
            )
        )
    }

    private fun generateInvoiceNumber(orderId: ObjectId): String {
        val ym = YearMonth.now()
        return "INV-${ym.year}${ym.monthValue.toString().padStart(2, '0')}-${orderId.toHexString().takeLast(8).uppercase()}"
    }

    private fun buildWebhookEventId(event: String, rawBody: String): String {
        return "$event-${sha256(rawBody).take(32)}"
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }
}
