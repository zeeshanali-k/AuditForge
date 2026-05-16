package com.devscion.auditforge.domain.usecase.uploads

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.UploadRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteUploadUseCase(private val uploadRepository: UploadRepository) {
    suspend operator fun invoke(sessionId: String, uploadId: String): ApiResult<Unit> =
        uploadRepository.deleteUpload(sessionId, uploadId)
}
