package com.devscion.auditforge.data.repository

import com.devscion.auditforge.PickedFile
import com.devscion.auditforge.domain.model.Upload
import com.devscion.auditforge.domain.model.UploadListResponse
import com.devscion.auditforge.domain.model.UploadType
import com.devscion.auditforge.data.network.ApiResult

interface UploadRepository {
    suspend fun getUploads(sessionId: String): ApiResult<UploadListResponse>
    suspend fun uploadFile(
        sessionId: String,
        file: PickedFile,
        uploadType: UploadType
    ): ApiResult<Upload>

    suspend fun deleteUpload(sessionId: String, uploadId: String): ApiResult<Unit>
}
