package com.devscion.auditforge.data.repository

import com.devscion.auditforge.PickedFile
import com.devscion.auditforge.domain.model.Upload
import com.devscion.auditforge.domain.model.UploadListResponse
import com.devscion.auditforge.domain.model.UploadType
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.UploadApiService

class UploadRepositoryImpl(private val uploadApiService: UploadApiService) : UploadRepository {

    override suspend fun getUploads(sessionId: String): ApiResult<UploadListResponse> =
        uploadApiService.getUploads(sessionId)

    override suspend fun uploadFile(
        sessionId: String,
        file: PickedFile,
        uploadType: UploadType,
    ): ApiResult<Upload> = uploadApiService.uploadFile(sessionId, file, uploadType)

    override suspend fun deleteUpload(sessionId: String, uploadId: String): ApiResult<Unit> =
        uploadApiService.deleteUpload(sessionId, uploadId)
}
