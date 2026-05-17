package com.devscion.auditforge.data.network

import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportListResponse
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.koin.core.annotation.Singleton

@Singleton
class ReportsApiService(private val http: AuditForgeHttpClient) {

    suspend fun generateReport(sessionId: String, request: GenerateReportRequest): ApiResult<Report> = try {
        val response = http.post("sessions/$sessionId/reports") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.Accepted -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to generate report (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun getReport(sessionId: String, reportId: String): ApiResult<Report> = try {
        val response = http.get("sessions/$sessionId/reports/$reportId")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Report not found", 404)
            else -> ApiResult.Error("Failed to get report (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun listReports(sessionId: String): ApiResult<ReportListResponse> = try {
        val response = http.get("sessions/$sessionId/reports")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to load reports (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun downloadReport(sessionId: String, reportId: String): ApiResult<ByteArray> = try {
        val response = http.get("sessions/$sessionId/reports/$reportId/download")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Report not found", 404)
            HttpStatusCode.Conflict -> ApiResult.Error("Report is still generating", 409)
            else -> ApiResult.Error("Failed to download report (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}
