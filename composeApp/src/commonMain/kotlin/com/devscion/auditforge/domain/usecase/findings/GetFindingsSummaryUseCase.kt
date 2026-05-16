package com.devscion.auditforge.domain.usecase.findings

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.FindingsRepository
import com.devscion.auditforge.domain.model.FindingsSummary
import org.koin.core.annotation.Factory

@Factory
class GetFindingsSummaryUseCase(private val repository: FindingsRepository) {
    suspend operator fun invoke(sessionId: String): ApiResult<FindingsSummary> =
        repository.getFindingsSummary(sessionId)
}
