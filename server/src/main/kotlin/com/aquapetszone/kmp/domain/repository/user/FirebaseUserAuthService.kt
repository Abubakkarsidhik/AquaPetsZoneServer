package com.aquapetszone.kmp.domain.repository.user

import com.aquapetszone.kmp.data.service.FirebaseAuthService as FirebaseTokenVerifier
import com.aquapetszone.kmp.domain.model.response.FirebaseLoginData

/**
 * Application service for buyer Firebase login (USER role only).
 */
class FirebaseUserAuthService(
    private val userRepository: ServerUserRepository = ServerUserRepositoryImpl()
) {

    suspend fun firebaseLogin(idToken: String): FirebaseLoginData {
        val identity = FirebaseTokenVerifier.verifyIdToken(idToken)
        return userRepository.loginWithFirebase(identity)
    }
}
