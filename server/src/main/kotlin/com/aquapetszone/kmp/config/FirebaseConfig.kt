package com.aquapetszone.kmp.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.github.cdimascio.dotenv.dotenv
import java.io.ByteArrayInputStream
import java.io.FileInputStream

object FirebaseConfig {

    private val dotenv by lazy { dotenv { ignoreIfMissing = true } }

    private fun env(key: String): String? =
        System.getenv(key) ?: dotenv[key]

    @Volatile
    private var initialized = false

    fun initialize() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return

            val projectId = env("FIREBASE_PROJECT_ID")
                ?: error("FIREBASE_PROJECT_ID not set")

            val credentials = loadCredentials()

            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
            }

            initialized = true
            println("------ FIREBASE ADMIN SDK INITIALIZED ------")
            println("PROJECT: $projectId")
            println("--------------------------------------------")
        }
    }

    private fun loadCredentials(): GoogleCredentials {
        val jsonContent = env("FIREBASE_CREDENTIALS_JSON")
        if (!jsonContent.isNullOrBlank()) {
            return GoogleCredentials.fromStream(
                ByteArrayInputStream(jsonContent.trim().toByteArray(Charsets.UTF_8))
            )
        }

        val path = env("FIREBASE_CREDENTIALS_PATH")
        if (!path.isNullOrBlank()) {
            return GoogleCredentials.fromStream(FileInputStream(path))
        }

        error(
            "Firebase credentials not configured. Set FIREBASE_CREDENTIALS_JSON or FIREBASE_CREDENTIALS_PATH"
        )
    }
}
