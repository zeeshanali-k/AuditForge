package com.devscion.auditforge.domain.usecase.uploads

import com.devscion.auditforge.domain.model.UploadListResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.UploadRepository
import org.koin.core.annotation.Factory

@Factory
class GetUploadsUseCase(private val uploadRepository: UploadRepository) {
    suspend operator fun invoke(sessionId: String): ApiResult<UploadListResponse> =
        uploadRepository.getUploads(sessionId)
}
