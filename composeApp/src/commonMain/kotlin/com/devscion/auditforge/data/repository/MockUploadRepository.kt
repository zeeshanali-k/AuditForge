package com.devscion.auditforge.data.repository

import com.devscion.auditforge.PickedFile
import com.devscion.auditforge.domain.model.Upload
import com.devscion.auditforge.domain.model.UploadListResponse
import com.devscion.auditforge.domain.model.UploadSummary
import com.devscion.auditforge.domain.model.UploadType
import com.devscion.auditforge.data.network.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MockUploadRepository : UploadRepository {

    private val mutex = Mutex()
    private val uploads: MutableMap<String, MutableList<Upload>> = mutableMapOf(
        "demo-001" to mutableListOf(
            Upload(
                id = "upload-001",
                sessionId = "demo-001",
                filename = "patient-portal.zip",
                sizeBytes = 4_523_112,
                uploadType = UploadType.Codebase,
                uploadedAt = "2026-05-12T10:35:00Z",
                checksumSha256 = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
            ),
            Upload(
                id = "upload-002",
                sessionId = "demo-001",
                filename = "api-spec.yaml",
                sizeBytes = 18_432,
                uploadType = UploadType.OpenapiSpec,
                uploadedAt = "2026-05-12T10:36:00Z",
                checksumSha256 = "d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5",
            ),
        ),
        "demo-002" to mutableListOf(
            Upload(
                id = "upload-003",
                sessionId = "demo-002",
                filename = "payment-gateway-api.yaml",
                sizeBytes = 22_016,
                uploadType = UploadType.OpenapiSpec,
                uploadedAt = "2026-05-14T09:16:00Z",
                checksumSha256 = "g7h8i9j0k1l2g7h8i9j0k1l2g7h8i9j0k1l2g7h8i9j0k1l2g7h8i9j0k1l2g7h8",
            ),
        ),
    )

    override suspend fun getUploads(sessionId: String): ApiResult<UploadListResponse> {
        delay(200)
        return mutex.withLock {
            val list = uploads[sessionId] ?: emptyList()
            ApiResult.Success(UploadListResponse(data = list.map { it.toSummary() }))
        }
    }

    override suspend fun uploadFile(
        sessionId: String,
        file: PickedFile,
        uploadType: UploadType,
    ): ApiResult<Upload> {
        delay(800)
        return mutex.withLock {
            val newUpload = Upload(
                id = "upload-${Random.nextLong().toString(16).removePrefix("-").take(8)}",
                sessionId = sessionId,
                filename = file.name,
                sizeBytes = file.bytes.size.toLong(),
                uploadType = uploadType,
                uploadedAt = MOCK_TIMESTAMP,
                checksumSha256 = "mock-checksum-${file.name.hashCode().toString(16)}",
            )
            uploads.getOrPut(sessionId) { mutableListOf() }.add(newUpload)
            ApiResult.Success(newUpload)
        }
    }

    override suspend fun deleteUpload(sessionId: String, uploadId: String): ApiResult<Unit> {
        delay(250)
        return mutex.withLock {
            val list = uploads[sessionId]
            val removed = list?.removeAll { it.id == uploadId } ?: false
            if (removed) ApiResult.Success(Unit) else ApiResult.Error("Upload not found", 404)
        }
    }

    private fun Upload.toSummary() = UploadSummary(
        id = id,
        filename = filename,
        uploadType = uploadType,
        sizeBytes = sizeBytes,
        uploadedAt = uploadedAt,
    )

    companion object {
        private const val MOCK_TIMESTAMP = "2026-05-16T10:00:00Z"
    }
}
