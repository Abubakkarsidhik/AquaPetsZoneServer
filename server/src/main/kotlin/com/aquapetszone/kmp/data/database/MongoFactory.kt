package com.aquapetszone.kmp.data.database


import io.github.cdimascio.dotenv.dotenv
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoFactory {

    private val dotenv by lazy {
        dotenv {
            ignoreIfMissing = true
        }
    }

    private fun env(key: String): String? {
        return System.getenv(key) ?: dotenv[key]
    }

    private val isProduction =
        env("PRODUCTION")?.toBoolean() ?: false

    private val connectionString = if (isProduction) {
        env("MONGO_URL_PROD")
            ?: error("MONGO_URL_PROD not set")
    } else {
        env("MONGO_URL_DEV")
            ?: "mongodb://localhost:27017"
    }

    private val dbName = if (isProduction) {
        env("DB_NAME_PROD")
            ?: error("DB_NAME_PROD not set")
    } else {
        env("DB_NAME_DEV")
            ?: "test"
    }

    private val client = KMongo.createClient(connectionString).coroutine

    val database = client.getDatabase(dbName)
}
