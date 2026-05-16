package com.devscion.auditforge.domain.usecase.reports

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ReportsRepository
import com.devscion.auditforge.domain.model.ReportListResponse
import org.koin.core.annotation.Factory

@Factory
class ListReportsUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(sessionId: String): ApiResult<ReportListResponse> =
        repository.listReports(sessionId)
}
