package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.ReportsApiService
import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportListResponse

class ReportsRepositoryImpl(private val api: ReportsApiService) : ReportsRepository {
    override suspend fun generateReport(sessionId: String, request: GenerateReportRequest) =
        api.generateReport(sessionId, request)

    override suspend fun getReport(sessionId: String, reportId: String) =
        api.getReport(sessionId, reportId)

    override suspend fun listReports(sessionId: String) =
        api.listReports(sessionId)

    override suspend fun downloadReport(sessionId: String, reportId: String) =
        api.downloadReport(sessionId, reportId)
}
