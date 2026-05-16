package com.devscion.auditforge.domain.usecase.uploads

import com.devscion.auditforge.PickedFile
import com.devscion.auditforge.domain.model.Upload
import com.devscion.auditforge.domain.model.UploadType
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.UploadRepository
import org.koin.core.annotation.Factory

@Factory
class UploadFileUseCase(private val uploadRepository: UploadRepository) {
    suspend operator fun invoke(
        sessionId: String,
        file: PickedFile,
        uploadType: UploadType,
    ): ApiResult<Upload> = uploadRepository.uploadFile(sessionId, file, uploadType)
}
