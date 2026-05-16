package com.devscion.auditforge.data.network

import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingListResponse
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.UpdateFindingStatusRequest
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import org.koin.core.annotation.Singleton

@Singleton
class FindingsApiService(private val http: AuditForgeHttpClient) {

    suspend fun getFindings(
        sessionId: String,
        severity: Set<RuleSeverity> = emptySet(),
        status: FindingStatus? = null,
        category: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): ApiResult<FindingListResponse> = try {
        val response = http.get("sessions/$sessionId/findings") {
            parameter("page", page)
            parameter("page_size", pageSize)
            parameter("sort", "severity:desc,created_at:asc")
            severity.forEach { parameter("severity", it.name.lowercase()) }
            status?.let { parameter("status", it.toParam()) }
            category?.let { parameter("category", it) }
        }
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to load findings (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun getFinding(sessionId: String, findingId: String): ApiResult<Finding> = try {
        val response = http.get("sessions/$sessionId/findings/$findingId")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Finding not found", 404)
            else -> ApiResult.Error("Failed to load finding (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun updateFindingStatus(
        sessionId: String,
        findingId: String,
        request: UpdateFindingStatusRequest,
    ): ApiResult<Finding> = try {
        val response = http.patch("sessions/$sessionId/findings/$findingId") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Finding not found", 404)
            else -> ApiResult.Error("Failed to update finding (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun getFindingsSummary(sessionId: String): ApiResult<FindingsSummary> = try {
        val response = http.get("sessions/$sessionId/findings/summary")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to load summary (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    private fun FindingStatus.toParam(): String = when (this) {
        FindingStatus.Open -> "open"
        FindingStatus.Acknowledged -> "acknowledged"
        FindingStatus.FalsePositive -> "false_positive"
        FindingStatus.Resolved -> "resolved"
    }
}
