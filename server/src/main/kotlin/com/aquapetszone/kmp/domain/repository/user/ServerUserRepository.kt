package com.aquapetszone.kmp.domain.repository.user

import com.aquapetszone.kmp.data.service.FirebaseUserIdentity
import com.aquapetszone.kmp.domain.model.response.FirebaseLoginData

interface ServerUserRepository {

    suspend fun loginWithFirebase(identity: FirebaseUserIdentity): FirebaseLoginData

    suspend fun findByFirebaseUid(firebaseUid: String): UserMongo?

    suspend fun findByUserId(userId: String): UserMongo?

    suspend fun ensureIndexes()
}
