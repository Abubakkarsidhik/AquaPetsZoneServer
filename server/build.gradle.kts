import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    application
}

group = "com.aquapetszone.kmp"
version = "1.0.0"
application {
    mainClass.set("com.aquapetszone.kmp.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}
kotlin {
    jvmToolchain(21)
}
dependencies {

    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.cors)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.kotlinx.serialization.json)
    implementation("io.ktor:ktor-client-cio:3.4.0")
    implementation(libs.ktor.json)
    implementation(libs.kmongo)
    implementation(libs.kmongo.coroutine)
    implementation(libs.jbcrypt)
    implementation(libs.s3)

    implementation(libs.ktor.json)
    implementation(libs.ktor.logging)
    implementation(libs.ktor.negotiation)
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")


    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}