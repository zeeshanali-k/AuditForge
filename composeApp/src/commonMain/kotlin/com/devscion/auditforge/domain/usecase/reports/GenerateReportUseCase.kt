package com.devscion.auditforge.domain.usecase.reports

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ReportsRepository
import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import org.koin.core.annotation.Factory

@Factory
class GenerateReportUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(sessionId: String, request: GenerateReportRequest): ApiResult<Report> =
        repository.generateReport(sessionId, request)
}
