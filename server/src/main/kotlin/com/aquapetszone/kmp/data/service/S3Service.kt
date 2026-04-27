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
    private val dotenv by lazy {
        dotenv {
            ignoreIfMissing = true
        }
    }

    private fun env(key: String): String? {
        return System.getenv(key) ?: dotenv[key]
    }

    private val BUCKET = env("AWS_S3_BUCKET")
        ?: throw Exception("AWS_S3_BUCKET not set")

    private val s3 by lazy {
        val accessKey = env("AWS_ACCESS_KEY_ID")
            ?: throw Exception("AWS_ACCESS_KEY_ID not set")

        val secretKey = env("AWS_SECRET_ACCESS_KEY")
            ?: throw Exception("AWS_SECRET_ACCESS_KEY not set")

        S3Client.builder()
            .region(Region.AP_SOUTH_1)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey)
                )
            )
            .build()
    }


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
