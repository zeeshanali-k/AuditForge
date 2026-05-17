package com.devscion.auditforge.domain.usecase.findings

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.FindingsRepository
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.UpdateFindingStatusRequest
import org.koin.core.annotation.Factory

@Factory
class UpdateFindingStatusUseCase(private val repository: FindingsRepository) {
    suspend operator fun invoke(
        sessionId: String,
        findingId: String,
        status: FindingStatus,
        comment: String?,
    ): ApiResult<Finding> = repository.updateFindingStatus(
        sessionId,
        findingId,
        UpdateFindingStatusRequest(status = status, comment = comment?.ifBlank { null }),
    )
}
