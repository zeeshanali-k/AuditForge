package com.devscion.auditforge.data.network

import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.AuditTrailResponse
import com.devscion.auditforge.domain.model.toApiParam
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import org.koin.core.annotation.Singleton

@Singleton
class AuditTrailApiService(private val http: AuditForgeHttpClient) {

    suspend fun getAuditTrail(
        sessionId: String,
        eventType: AuditEventType? = null,
        fromTimestamp: String? = null,
        toTimestamp: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): ApiResult<AuditTrailResponse> = try {
        val response = http.get("sessions/$sessionId/audit-trail") {
            parameter("page", page)
            parameter("page_size", pageSize)
            eventType?.let { parameter("event_type", it.toApiParam()) }
            fromTimestamp?.let { parameter("from_timestamp", it) }
            toTimestamp?.let { parameter("to_timestamp", it) }
        }
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to load audit trail (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun verifyChain(sessionId: String): ApiResult<AuditChainVerification> = try {
        val response = http.get("sessions/$sessionId/audit-trail/verify")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to verify chain (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}
