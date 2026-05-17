package com.devscion.auditforge.data.network

import com.devscion.auditforge.PickedFile
import com.devscion.auditforge.domain.model.Upload
import com.devscion.auditforge.domain.model.UploadListResponse
import com.devscion.auditforge.domain.model.UploadType
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import org.koin.core.annotation.Singleton

@Singleton
class UploadApiService(private val httpClient: AuditForgeHttpClient) {

    suspend fun getUploads(sessionId: String): ApiResult<UploadListResponse> = try {
        val response = httpClient.get("sessions/$sessionId/uploads")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error(
                "Failed to load uploads (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun uploadFile(
        sessionId: String,
        file: PickedFile,
        uploadType: UploadType,
    ): ApiResult<Upload> = try {
        val response = httpClient.post("sessions/$sessionId/uploads") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("upload_type", uploadType.serialName)
                        append(
                            "file",
                            file.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=\"${file.name}\"")
                                append(HttpHeaders.ContentType, file.mimeType)
                            },
                        )
                    }
                )
            )
        }
        when (response.status) {
            HttpStatusCode.Created -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.PayloadTooLarge -> ApiResult.Error("File too large (max 100 MB)", 413)
            HttpStatusCode.UnprocessableEntity -> ApiResult.Error(
                "File type not allowed for this upload type",
                422
            )

            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error(
                "Upload failed (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun deleteUpload(sessionId: String, uploadId: String): ApiResult<Unit> = try {
        val response = httpClient.delete("sessions/$sessionId/uploads/$uploadId")
        when (response.status) {
            HttpStatusCode.NoContent -> ApiResult.Success(Unit)
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Upload not found", 404)
            HttpStatusCode.Conflict -> ApiResult.Error(
                "Cannot delete upload while scan is in progress",
                409
            )

            else -> ApiResult.Error(
                "Failed to delete upload (${response.status.value})",
                response.status.value
            )
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}
