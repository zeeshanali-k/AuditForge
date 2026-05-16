package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportListResponse
import com.devscion.auditforge.domain.model.ReportStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class MockReportsRepository : ReportsRepository {

    private val mutex = Mutex()
    private val reports: MutableMap<String, MutableList<Report>> = mutableMapOf()
    private var idCounter = 1

    override suspend fun generateReport(sessionId: String, request: GenerateReportRequest): ApiResult<Report> {
        delay(400)
        return mutex.withLock {
            val report = Report(
                id = "report-${idCounter++}",
                sessionId = sessionId,
                status = ReportStatus.Generating,
                format = request.format,
                requestedAt = MOCK_TIMESTAMP,
                signed = request.sign,
                complianceFramework = request.complianceFramework,
            )
            reports.getOrPut(sessionId) { mutableListOf() }.add(0, report)
            ApiResult.Success(report)
        }
    }

    override suspend fun getReport(sessionId: String, reportId: String): ApiResult<Report> {
        delay(300)
        return mutex.withLock {
            val list = reports[sessionId] ?: return@withLock ApiResult.Error("Report not found", 404)
            val idx = list.indexOfFirst { it.id == reportId }
            if (idx < 0) return@withLock ApiResult.Error("Report not found", 404)
            val existing = list[idx]
            val ready = existing.copy(
                status = ReportStatus.Ready,
                generatedAt = MOCK_TIMESTAMP,
                findingCount = 8,
                overallScore = 76,
                fileSizeBytes = 245_678L,
                signatureSha256 = if (existing.signed) "abc123def456789abcdef0123456789abc123def456789abcdef012345678" else null,
            )
            list[idx] = ready
            ApiResult.Success(ready)
        }
    }

    override suspend fun listReports(sessionId: String): ApiResult<ReportListResponse> {
        delay(350)
        return mutex.withLock {
            ApiResult.Success(ReportListResponse(data = reports.getOrDefault(sessionId, mutableListOf()).toList()))
        }
    }

    override suspend fun downloadReport(sessionId: String, reportId: String): ApiResult<ByteArray> {
        delay(500)
        val mockBytes = "%PDF-1.4\n%Mock AuditForge report $reportId\n1 0 obj<</Type /Catalog>>endobj".encodeToByteArray()
        return ApiResult.Success(mockBytes)
    }

    companion object {
        private const val MOCK_TIMESTAMP = "2026-05-16T10:00:00Z"
    }
}
