package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportListResponse

interface ReportsRepository {
    suspend fun generateReport(sessionId: String, request: GenerateReportRequest): ApiResult<Report>
    suspend fun getReport(sessionId: String, reportId: String): ApiResult<Report>
    suspend fun listReports(sessionId: String): ApiResult<ReportListResponse>
    suspend fun downloadReport(sessionId: String, reportId: String): ApiResult<ByteArray>
}
