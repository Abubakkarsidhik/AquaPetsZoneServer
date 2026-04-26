package com.aquapetszone.kmp.data.service

import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.ContentType
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest

object S3Service {
    /**
     * Deletes an object from S3 by key. Retries up to 3 times on failure.
     * Throws exception if all attempts fail.
     * @param key S3 object key (e.g., "folder/filename.ext")
     */
    val dotenv by lazy { dotenv() }
    private val BUCKET = dotenv["AWS_S3_BUCKET"]



    fun deleteObject(key: String) {
        val maxRetries = 3
        var attempt = 0
        var lastException: Exception? = null
        while (attempt < maxRetries) {
            try {
                val request = software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(key)
                    .build()
                s3.deleteObject(request)
                return // Success
            } catch (e: Exception) {
                lastException = e
                attempt++
                // Optionally: log the error and retry
                println("[S3Service] Failed to delete S3 object (attempt $attempt/$maxRetries): $key. Error: ${e.message}")
                if (attempt >= maxRetries) {
                    throw Exception("Failed to delete S3 object after $maxRetries attempts: $key", e)
                }
                Thread.sleep(500L * attempt) // Exponential backoff
            }
        }
        // Should not reach here
        throw lastException ?: Exception("Unknown error deleting S3 object: $key")
    }
    private val s3 by lazy {
        // Use dotenv to load env vars if not already loaded
        val accessKey = dotenv["AWS_ACCESS_KEY_ID"] ?: throw IllegalStateException("AWS_ACCESS_KEY_ID not set in environment. Make sure .env is loaded in main() before using S3Service.")
        val secretKey = dotenv["AWS_SECRET_ACCESS_KEY"] ?: throw IllegalStateException("AWS_SECRET_ACCESS_KEY not set in environment. Make sure .env is loaded in main() before using S3Service.")
        S3Client.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build()
    }

    fun upload(bytes: ByteArray, fileName: String, extension: String, contentType: String, folder: String): String {

        val key = "$folder/$fileName.$extension"

        val request = PutObjectRequest.builder()
            .bucket(BUCKET)
            .key(key)
            .contentType(contentType)
            .build()

        s3.putObject(request, RequestBody.fromBytes(bytes))

        return "https://$BUCKET.s3.ap-south-1.amazonaws.com/$key"
    }
}
