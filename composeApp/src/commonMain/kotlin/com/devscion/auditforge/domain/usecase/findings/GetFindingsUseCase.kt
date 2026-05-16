package com.devscion.auditforge.domain.usecase.findings

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.FindingsRepository
import com.devscion.auditforge.domain.model.FindingListResponse
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.RuleSeverity
import org.koin.core.annotation.Factory

@Factory
class GetFindingsUseCase(private val repository: FindingsRepository) {
    suspend operator fun invoke(
        sessionId: String,
        severity: Set<RuleSeverity> = emptySet(),
        status: FindingStatus? = null,
        category: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): ApiResult<FindingListResponse> = repository.getFindings(sessionId, severity, status, category, page, pageSize)
}
