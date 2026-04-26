package com.aquapetszone.kmp.data.database


import io.github.cdimascio.dotenv.dotenv
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object MongoFactory {

    val dotenv by lazy { dotenv() }
    private val isProduction = dotenv["PRODUCTION"]?.toBoolean() ?: false

    private val connectionString = if (isProduction) {
        dotenv["MONGO_URL_PROD"]
            ?: error("MONGO_URL_PROD not set")
    } else {
        dotenv["MONGO_URL_DEV"]
            ?: "mongodb://localhost:27017"
    }

    private val dbName = if (isProduction) {
        dotenv["DB_NAME_PROD"] ?: error("DB_NAME_PROD not set")
    } else {
        dotenv["DB_NAME_DEV"] ?: error("DB_NAME_DEV not set")
    }

    private val client = KMongo.createClient(connectionString).coroutine

    val database = client.getDatabase(dbName)
}
