package com.aquapetszone.kmp.domain.repository.user

import com.aquapetszone.kmp.config.Constant
import com.aquapetszone.kmp.domain.model.response.UserProfileResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class UserMongo(
    @BsonId
    @Transient
    val id: ObjectId = ObjectId(),

    @Transient
    val userId: ObjectId = ObjectId(),

    val firebaseUid: String,
    val phone: String,
    val email: String? = null,
    val name: String? = null,
    val role: String = Constant.ROLE.USER,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)

fun UserMongo.toProfileResponse(): UserProfileResponse {
    return UserProfileResponse(
        id = userId.toHexString(),
        firebaseUid = firebaseUid,
        phone = phone,
        email = email,
        name = name,
        role = role,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastLoginAt = lastLoginAt
    )
}
