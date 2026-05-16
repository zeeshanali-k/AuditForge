package com.devscion.auditforge.domain.usecase.reports

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ReportsRepository
import com.devscion.auditforge.domain.model.Report
import org.koin.core.annotation.Factory

@Factory
class GetReportUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(sessionId: String, reportId: String): ApiResult<Report> =
        repository.getReport(sessionId, reportId)
}
