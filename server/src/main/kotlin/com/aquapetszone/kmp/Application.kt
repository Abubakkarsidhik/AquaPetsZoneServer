package com.aquapetszone.kmp

import com.aquapetszone.kmp.config.FirebaseConfig
import com.aquapetszone.kmp.domain.repository.user.ServerUserRepositoryImpl
import com.aquapetszone.kmp.plugins.configureCors
import kotlinx.coroutines.runBlocking
import com.aquapetszone.kmp.plugins.configureRouting
import com.aquapetszone.kmp.plugins.configureSecurity
import com.aquapetszone.kmp.plugins.configureSerialization
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.cors.routing.CORS


fun main() {
    runCatching { FirebaseConfig.initialize() }
        .onFailure { println("Firebase init skipped: ${it.message}") }
    runBlocking { ServerUserRepositoryImpl().ensureIndexes() }

    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = {
        configureCors()
        configureSerialization()
        configureSecurity()
        configureRouting()
    }).start(wait = true)
}

//fun Application.module() {
//    routing {
//        get("/") {
//            call.respondText("Ktor: Server")
//        }
//    }
//}