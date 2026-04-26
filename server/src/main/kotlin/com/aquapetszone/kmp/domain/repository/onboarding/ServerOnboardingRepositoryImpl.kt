package com.aquapetszone.kmp.domain.repository.onboarding

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.domain.repository.ServerBaseRepository
import com.aquapetszone.kmp.domain.model.request.OnboardingAccountRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingBusinessRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingCatalogRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingComplianceRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingLogisticsRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingPaymentRequest
import com.aquapetszone.kmp.domain.model.request.OnboardingStoreRequest
import com.aquapetszone.kmp.utils.CryptoUtil
import com.aquapetszone.kmp.utils.PasswordUtil
import com.mongodb.client.model.Filters
import org.bson.types.ObjectId

class ServerOnboardingRepositoryImpl : ServerOnboardingRepository, ServerBaseRepository() {

    private val collection = db.getCollection<OnboardingMongo>("onboarding")

    override suspend fun createAccountInformation(
        request: OnboardingAccountRequest
    ): OnboardingOperationResult {

        if (request.fullName?.isBlank() == true) throw Exception("Full name is required")
        if (request.email?.isBlank() == true) throw Exception("Email is required")
        if (request.mobile?.isBlank() == true) throw Exception("Mobile number is required")
        if ((request.password?.length ?: 0) < 6) throw Exception("Password must be at least 6 characters")
        if (request.password != request.confirmPassword) throw Exception("Passwords do not match")

        val now = System.currentTimeMillis()

        val normalizedEmail = request.email?.trim()?.lowercase()
        val normalizedMobile = request.mobile?.trim()

        val emailEnc = CryptoUtil.encrypt(normalizedEmail)
        val mobileEnc = CryptoUtil.encrypt(normalizedMobile)

        // 1️⃣ Exact match check
        val existing = collection.find(
            Filters.and(
                Filters.eq("account.email", emailEnc),
                Filters.eq("account.mobile", mobileEnc)
            )
        ).first()

        if (existing != null) {

            val storedHash = existing.account?.password
                ?: throw Exception("Invalid email, mobile number, or password")

            if (!PasswordUtil.verify(request.password, storedHash)) {
                throw Exception("Invalid email, mobile number, or password")
            }

            val updatedAccount = request.copy(
                email = emailEnc,
                mobile = mobileEnc,
                password = storedHash,
                confirmPassword = ""
            )

            if (isSameAccount(existing.account, updatedAccount)) {
                return OnboardingOperationResult(
                    onboarding = existing,
                    operation = OperationType.NO_CHANGE
                )
            }

            val updated = existing.copy(
                account = updatedAccount,
                updatedAt = now
            )

            collection.replaceOne(
                Filters.eq("_id", existing.id),
                updated
            )

            return OnboardingOperationResult(
                onboarding = updated,
                operation = OperationType.UPDATED
            )
        }

        // 2️⃣ Partial mismatch detection
        val emailExists = collection.find(
            Filters.eq("account.email", emailEnc)
        ).first()

        val mobileExists = collection.find(
            Filters.eq("account.mobile", mobileEnc)
        ).first()

        if (emailExists != null || mobileExists != null) {
            throw Exception("Email, mobile number, or password does not match.")
        }

        // 3️⃣ Create new onboarding document
        val onboarding = OnboardingMongo(
            userId = ObjectId(),
            currentStep = 2,
            account = request.copy(
                email = emailEnc,
                mobile = mobileEnc,
                password = PasswordUtil.hash(request.password),
                confirmPassword = ""
            ),
            createdAt = now,
            updatedAt = now
        )

        collection.insertOne(onboarding)

        return OnboardingOperationResult(
            onboarding = onboarding,
            operation = OperationType.CREATED
        )
    }

    override suspend fun createStoreInformation(
        userId: String,
        request: OnboardingStoreRequest
    ): OnboardingOperationResult {

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        val now = System.currentTimeMillis()

        // 🔐 Encrypt sensitive store fields
        val encryptedRequest = request.copy(
            supportPhone = request.supportPhone?.let { CryptoUtil.encrypt(it) },
            supportEmail = request.supportEmail?.let { CryptoUtil.encrypt(it.trim().lowercase()) }
        )

        val oldStore = existing.store

        if (oldStore != null && isSameStore(oldStore, encryptedRequest)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        val nextStep = if (existing.currentStep < 3) 3 else existing.currentStep

        val updated = existing.copy(
            store = encryptedRequest,
            currentStep = nextStep,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = OperationType.UPDATED
        )
    }

    override suspend fun createBusinessInformation(
        userId: String,
        request: OnboardingBusinessRequest
    ): OnboardingOperationResult {

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        val now = System.currentTimeMillis()

        // 🔐 Encrypt sensitive fields (null-safe)
        val encryptedRequest = request.copy(
            pan = request.pan?.let { CryptoUtil.encrypt(it) },
            aadhar = request.aadhar?.let { CryptoUtil.encrypt(it) },
            businessPan = request.businessPan?.let { CryptoUtil.encrypt(it) }
        )

        val oldBusiness = existing.business

        if (oldBusiness != null && isSameBusiness(oldBusiness, encryptedRequest)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        // Prevent backward step
        val nextStep = if (existing.currentStep < 4) 4 else existing.currentStep

        val updated = existing.copy(
            business = encryptedRequest,
            currentStep = nextStep,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = if (oldBusiness == null) OperationType.CREATED else OperationType.UPDATED
        )
    }

    override suspend fun createPaymentInformation(
        userId: String,
        request: OnboardingPaymentRequest
    ): OnboardingOperationResult {

        if (request.accountHolder.isNullOrBlank()) throw Exception("Account holder name is required")
        if (request.bankName.isNullOrBlank()) throw Exception("Bank name is required")
        if (request.ifsc.isNullOrBlank()) throw Exception("IFSC code is required")
        if (request.accountNumber.isNullOrBlank()) throw Exception("Account number is required")
        if (request.confirmAccountNumber.isNullOrBlank()) throw Exception("Confirm account number is required")

        if (request.accountNumber != request.confirmAccountNumber) {
            throw Exception("Account numbers do not match")
        }

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        val now = System.currentTimeMillis()

        // 🔐 Encrypt sensitive fields
        val encryptedRequest = request.copy(
            accountNumber = CryptoUtil.encrypt(request.accountNumber),
            confirmAccountNumber = "",
            ifsc = CryptoUtil.encrypt(request.ifsc)
        )

        val oldPayment = existing.payment

        if (oldPayment != null && isSamePayment(oldPayment, encryptedRequest)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        val nextStep = if (existing.currentStep < 5) 5 else existing.currentStep

        val updated = existing.copy(
            payment = encryptedRequest,
            currentStep = nextStep,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = if (oldPayment == null) OperationType.CREATED else OperationType.UPDATED
        )
    }

    override suspend fun createCatalogInformation(
        userId: String,
        request: OnboardingCatalogRequest
    ): OnboardingOperationResult {
        val isManufacturer = request.isManufacturer ?: false
        val isReseller = request.isReseller ?: false

        if (request.selectedCategories.isEmpty()) {
            throw Exception("At least one category must be selected")
        }

        if (!isManufacturer && !isReseller) {
            throw Exception("Select at least one seller type")
        }

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        val now = System.currentTimeMillis()

        val oldCatalog = existing.catalog

        if (oldCatalog != null && isSameCatalog(oldCatalog, request)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        val nextStep = if (existing.currentStep < 6) 6 else existing.currentStep

        val updated = existing.copy(
            catalog = request,
            currentStep = nextStep,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = if (oldCatalog == null) OperationType.CREATED else OperationType.UPDATED
        )
    }

    override suspend fun createLogisticsInformation(
        userId: String,
        request: OnboardingLogisticsRequest
    ): OnboardingOperationResult {

        if (request.dispatchTimeInDays == null)
            throw Exception("Dispatch time is required")

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        // If sameAsStore is true, copy the store address as pickup & return address
        val finalRequest = if (request.sameAsStore == true) {
            val storeAddress = existing.store?.address
                ?: throw Exception("Store address not found. Please complete store information first.")
            request.copy(
                pickupAddress = storeAddress,
                returnAddress = storeAddress
            )
        } else {
            if (request.pickupAddress.isNullOrBlank())
                throw Exception("Pickup address is required")
            if (request.returnAddress.isNullOrBlank())
                throw Exception("Return address is required")
            request
        }

        val now = System.currentTimeMillis()

        val oldLogistic = existing.logistic

        if (oldLogistic != null && isSameLogistics(oldLogistic, finalRequest)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        val nextStep = if (existing.currentStep < 7) 7 else existing.currentStep

        val updated = existing.copy(
            logistic = finalRequest,
            currentStep = nextStep,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = if (oldLogistic == null)
                OperationType.CREATED
            else
                OperationType.UPDATED
        )
    }

    override suspend fun createComplianceInformation(
        userId: String,
        request: OnboardingComplianceRequest
    ): OnboardingOperationResult {

        if (request.acceptCommission != true)
            throw Exception("Commission agreement must be accepted")

        if (request.acceptReturnPolicy != true)
            throw Exception("Return policy must be accepted")

        if (request.acceptTerms != true)
            throw Exception("Terms and conditions must be accepted")

        if (Constant.Compliance.ENABLE_GOVERNMENT_PERMIT) {
            if (request.sellsLiveFish == true && request.hasGovernmentPermit != true)
                throw Exception("Government permit required to sell live fish")

            if (request.hasGovernmentPermit == true && request.permitDoc.isNullOrBlank())
                throw Exception("Permit document is required")
        }

        if (Constant.Compliance.ENABLE_MEDICINE_LICENSE) {
            if (request.hasMedicineLicense == true && request.medicineDoc.isNullOrBlank())
                throw Exception("Medicine license document is required")
        }

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        val now = System.currentTimeMillis()

        val oldCompliance = existing.compliance

        if (oldCompliance != null && isSameCompliance(oldCompliance, request)) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        // FINAL STEP → Mark onboarding completed
        val updated = existing.copy(
            compliance = request,
            currentStep = 8,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = if (oldCompliance == null)
                OperationType.CREATED
            else
                OperationType.UPDATED
        )
    }

    override suspend fun getOnboardingReview(
        userId: String
    ): OnboardingReviewResponse {

        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Onboarding not found")


        // 🔓 Decrypt sensitive fields for display (masked on client)
        val decryptedAccount = existing.account?.copy(
            email = CryptoUtil.decrypt(existing.account.email),
            mobile = CryptoUtil.decrypt(existing.account.mobile),
            password = "••••••", // Never expose password hash
            confirmPassword = ""
        )
        val decryptedStore = existing.store?.let {
            it.copy(
                supportPhone = CryptoUtil.decrypt(it.supportPhone),
                supportEmail = CryptoUtil.decrypt(it.supportEmail)
            )
        }
        val decryptedBusiness = existing.business?.let {
            it.copy(
                pan = CryptoUtil.decrypt(it.pan),
                aadhar = CryptoUtil.decrypt(it.aadhar),
                businessPan = CryptoUtil.decrypt(it.businessPan)
            )
        }
        val decryptedPayment = existing.payment?.let {
            it.copy(
                accountNumber = CryptoUtil.decrypt(it.accountNumber),
                ifsc = CryptoUtil.decrypt(it.ifsc),
                confirmAccountNumber = ""
            )
        }

        return OnboardingReviewResponse(
            currentStep = existing.currentStep,
            isOnboardingCompleted = existing.onboardingStatus == OnboardingStatus.APPROVED,
            onboardingStatus = existing.onboardingStatus,
            rejectionReason = existing.rejectionReason,

            data = OnboardingReviewData(
                account = decryptedAccount,
                store = decryptedStore,
                business = decryptedBusiness,
                payment = decryptedPayment,
                catalog = existing.catalog,
                logistics = existing.logistic,
                compliance = existing.compliance
            ),

            createdAt = existing.createdAt,
            updatedAt = existing.updatedAt
        )
    }

    override suspend fun submitForReview(userId: String): OnboardingOperationResult {
        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Unauthorized")

        // Validate all 7 steps are completed
        if (existing.currentStep < 8) {
            throw Exception("Please complete all onboarding steps before submitting for review")
        }

        if (existing.onboardingStatus == OnboardingStatus.PENDING_REVIEW) {
            return OnboardingOperationResult(
                onboarding = existing,
                operation = OperationType.NO_CHANGE
            )
        }

        if (existing.onboardingStatus == OnboardingStatus.APPROVED) {
            throw Exception("Onboarding is already approved")
        }

        val now = System.currentTimeMillis()

        val updated = existing.copy(
            onboardingStatus = OnboardingStatus.PENDING_REVIEW,
            rejectionReason = null,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = OperationType.UPDATED
        )
    }

    override suspend fun approveOnboarding(userId: String): OnboardingOperationResult {
        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Onboarding not found")

        val now = System.currentTimeMillis()

        val updated = existing.copy(
            onboardingStatus = OnboardingStatus.APPROVED,
            rejectionReason = null,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = OperationType.UPDATED
        )
    }

    override suspend fun rejectOnboarding(userId: String, reason: String): OnboardingOperationResult {
        val objectUserId = ObjectId(userId)

        val existing = collection.find(
            Filters.eq("userId", objectUserId)
        ).first() ?: throw Exception("Onboarding not found")

        val now = System.currentTimeMillis()

        val updated = existing.copy(
            onboardingStatus = OnboardingStatus.REJECTED,
            rejectionReason = reason,
            updatedAt = now
        )

        collection.replaceOne(
            Filters.eq("userId", objectUserId),
            updated
        )

        return OnboardingOperationResult(
            onboarding = updated,
            operation = OperationType.UPDATED
        )
    }
}