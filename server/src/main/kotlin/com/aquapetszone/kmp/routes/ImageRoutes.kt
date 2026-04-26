package com.aquapetszone.kmp.routes

import com.aquapetszone.kmp.data.service.S3Service
import com.aquapetszone.kmp.domain.model.response.ApiSuccessResponse
import com.aquapetszone.kmp.domain.model.response.ImageUploadResponse
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.bson.types.ObjectId


fun Route.imageRoutes() {

    post("/upload/image/profile") {
        handleImageUpload(call, "profile")
    }

    post("/upload/image/storeLogo") {
        handleImageUpload(call, "storeLogo")
    }

    post("/upload/image/storeBanner") {
        handleImageUpload(call, "storeBanner")
    }

    post("/upload/image/cheque") {
        handleImageUpload(call, "cheque")
    }

    post("/upload/image/productCover") {
        handleImageUpload(call, "productCover")
    }

    post("/upload/image/productOther") {
        handleImageUpload(call, "productOther")
    }
}

suspend fun handleImageUpload(
    call: ApplicationCall,
    folder: String
) {
    val multipart = call.receiveMultipart()

    val MAX_FILE_SIZE = 10L * 1024 * 1024 // 5 MB
    var imageBytes: ByteArray? = null
    var extension = ""
    var contentType = ""

    var errorOccurred = false

    multipart.forEachPart { part ->
        try {
            if (part is PartData.FileItem && part.name == "image") {

                val (ext, type) = getExtensionAndType(part.contentType)

                extension = ext
                contentType = type

                try {
                    imageBytes = part.provider().readBytesWithLimit(MAX_FILE_SIZE)
                } catch (e: IllegalArgumentException) {

                    if (!errorOccurred) {
                        errorOccurred = true
                        call.respond(
                            HttpStatusCode.PayloadTooLarge,
                            e.message ?: "File too large. Use less than 10MB"
                        )
                    }
                }
            }
        } finally {
            part.dispose()
        }
    }

    if (errorOccurred) return

    if (imageBytes == null) {
        call.respond(HttpStatusCode.BadRequest, "Image file is required")
        return
    }

    val fileName = ObjectId().toHexString()

    val imageUrl = S3Service.upload(
        bytes = imageBytes,
        fileName = fileName,
        extension = extension,
        contentType = contentType,
        folder = folder
    )

    call.respond(
        HttpStatusCode.OK,
        ApiSuccessResponse(
            message = "Image uploaded successfully",
            code = 200,
            data = ImageUploadResponse(imageUrl = imageUrl)
        )
    )
}

fun getExtensionAndType(contentType: ContentType?): Pair<String, String> {
    return when (contentType?.withoutParameters()?.toString()) {
        "image/png" -> "png" to "image/png"
        "image/jpeg", "image/jpg" -> "jpg" to "image/jpeg"
        else -> throw IllegalArgumentException("Unsupported image type")
    }
}

suspend fun ByteReadChannel.readBytesWithLimit(maxSize: Long): ByteArray {
    val packet = readRemaining(maxSize + 1) // read slightly more to detect overflow
    val bytes = packet.readByteArray()

    if (bytes.size > maxSize) {
        throw IllegalArgumentException("File size exceeds limit of ${maxSize / (1024 * 1024)} MB")
    }

    return bytes
}