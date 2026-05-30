package com.aquapetszone.kmp.domain.repository.user

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.config.JwtConfig
import com.aquapetszone.kmp.data.service.FirebaseUserIdentity
import com.aquapetszone.kmp.domain.model.response.FirebaseLoginData
import com.aquapetszone.kmp.domain.repository.ServerBaseRepository
import com.aquapetszone.kmp.utils.AuthAuditLogger
import com.mongodb.MongoWriteException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId

class ServerUserRepositoryImpl : ServerUserRepository, ServerBaseRepository() {

    private val collection = db.getCollection<UserMongo>("users")

    @Volatile
    private var indexesEnsured = false

    override suspend fun ensureIndexes() {
        if (indexesEnsured) return
        collection.createIndex(
            Indexes.ascending("firebaseUid"),
            IndexOptions().unique(true).name("idx_firebase_uid_unique")
        )
        collection.createIndex(
            Indexes.ascending("phone"),
            IndexOptions().unique(true).name("idx_phone_unique")
        )
        collection.createIndex(
            Indexes.ascending("userId"),
            IndexOptions().name("idx_user_id")
        )
        indexesEnsured = true
        println("------ USER INDEXES ENSURED ------")
    }

    override suspend fun loginWithFirebase(identity: FirebaseUserIdentity): FirebaseLoginData {
        ensureIndexes()

        if (identity.firebaseUid.isBlank()) {
            throw Exception("Invalid Firebase identity")
        }
        if (identity.phone.isBlank()) {
            throw Exception("Phone number is required")
        }

        val now = System.currentTimeMillis()
        var user = findByFirebaseUid(identity.firebaseUid)

        if (user == null) {
            user = createUser(identity, now)
            AuthAuditLogger.log(
                "USER_CREATED",
                mapOf("userId" to user.userId.toHexString(), "firebaseUid" to identity.firebaseUid)
            )
        } else {
            user = updateExistingUser(user, identity, now)
            AuthAuditLogger.log(
                "USER_LOGIN",
                mapOf("userId" to user.userId.toHexString(), "firebaseUid" to identity.firebaseUid)
            )
        }

        if (!user.isActive) {
            throw Exception("Account is deactivated. Please contact support.")
        }

        if (user.role != Constant.ROLE.USER) {
            throw Exception("Forbidden: buyer account only")
        }

        val userId = user.userId.toHexString()
        val token = JwtConfig.generateAccessToken(
            userId = userId,
            role = Constant.ROLE.USER,
            firebaseUid = user.firebaseUid
        )
        val refreshToken = JwtConfig.generateRefreshToken(
            userId = userId,
            role = Constant.ROLE.USER,
            firebaseUid = user.firebaseUid
        )

        return FirebaseLoginData(
            success = true,
            user = user.toProfileResponse(),
            token = token,
            refreshToken = refreshToken
        )
    }

    override suspend fun findByFirebaseUid(firebaseUid: String): UserMongo? {
        return collection.find(Filters.eq("firebaseUid", firebaseUid)).first()
    }

    override suspend fun findByUserId(userId: String): UserMongo? {
        if (!ObjectId.isValid(userId)) return null
        return collection.find(Filters.eq("userId", ObjectId(userId))).first()
    }

    private suspend fun createUser(identity: FirebaseUserIdentity, now: Long): UserMongo {
        val userObjectId = ObjectId()
        val newUser = UserMongo(
            userId = userObjectId,
            firebaseUid = identity.firebaseUid,
            phone = identity.phone,
            email = identity.email,
            name = identity.name,
            role = Constant.ROLE.USER,
            isActive = true,
            createdAt = now,
            updatedAt = now,
            lastLoginAt = now
        )

        return try {
            collection.insertOne(newUser)
            newUser
        } catch (e: MongoWriteException) {
            if (e.error?.code == 11000) {
                val existing = findByFirebaseUid(identity.firebaseUid)
                    ?: findByPhone(identity.phone)
                    ?: throw Exception("User already exists but could not be loaded")
                return updateExistingUser(existing, identity, now)
            }
            throw Exception("Failed to create user account")
        }
    }

    private suspend fun updateExistingUser(
        existing: UserMongo,
        identity: FirebaseUserIdentity,
        now: Long
    ): UserMongo {
        collection.updateOne(
            Filters.eq("userId", existing.userId),
            Updates.combine(
                Updates.set("lastLoginAt", now),
                Updates.set("updatedAt", now),
                Updates.set("email", identity.email ?: existing.email),
                Updates.set("name", identity.name ?: existing.name),
                Updates.set("phone", identity.phone)
            )
        )
        return collection.find(Filters.eq("userId", existing.userId)).first() ?: existing
    }

    private suspend fun findByPhone(phone: String): UserMongo? {
        return collection.find(Filters.eq("phone", phone)).first()
    }
}
