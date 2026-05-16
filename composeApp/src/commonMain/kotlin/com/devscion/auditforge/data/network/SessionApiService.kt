package com.devscion.auditforge.data.network

import com.devscion.auditforge.data.model.CreateSessionRequest
import com.devscion.auditforge.data.model.Session
import com.devscion.auditforge.data.model.SessionListResponse
import com.devscion.auditforge.data.model.SessionStatus
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.koin.core.annotation.Singleton

@Singleton
class SessionApiService(private val httpClient: AuditForgeHttpClient) {

    suspend fun getSessions(
        page: Int = 1,
        pageSize: Int = 20,
        status: SessionStatus? = null,
    ): ApiResult<SessionListResponse> = try {
        val response = httpClient.get("sessions") {
            parameter("page", page)
            parameter("page_size", pageSize)
            status?.let { parameter("status", it.name.lowercase()) }
        }
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            else -> ApiResult.Error("Failed to load sessions (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun getSession(sessionId: String): ApiResult<Session> = try {
        val response = httpClient.get("sessions/$sessionId")
        when (response.status) {
            HttpStatusCode.OK -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            else -> ApiResult.Error("Failed to load session (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun createSession(request: CreateSessionRequest): ApiResult<Session> = try {
        val response = httpClient.post("sessions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        when (response.status) {
            HttpStatusCode.Created -> ApiResult.Success(response.body())
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.UnprocessableEntity -> ApiResult.Error("Invalid session data", 422)
            else -> ApiResult.Error("Failed to create session (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }

    suspend fun deleteSession(sessionId: String): ApiResult<Unit> = try {
        val response = httpClient.delete("sessions/$sessionId")
        when (response.status) {
            HttpStatusCode.NoContent -> ApiResult.Success(Unit)
            HttpStatusCode.Unauthorized -> ApiResult.Unauthorized
            HttpStatusCode.NotFound -> ApiResult.Error("Session not found", 404)
            HttpStatusCode.Conflict -> ApiResult.Error("Cannot delete session while scan is in progress", 409)
            else -> ApiResult.Error("Failed to delete session (${response.status.value})", response.status.value)
        }
    } catch (e: Exception) {
        ApiResult.NetworkError
    }
}
