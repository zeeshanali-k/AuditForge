package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.model.CreateSessionRequest
import com.devscion.auditforge.data.model.Session
import com.devscion.auditforge.data.model.SessionListResponse
import com.devscion.auditforge.data.model.SessionStatus
import com.devscion.auditforge.data.network.ApiResult

interface SessionRepository {
    suspend fun getSessions(page: Int, pageSize: Int, status: SessionStatus?): ApiResult<SessionListResponse>
    suspend fun getSession(sessionId: String): ApiResult<Session>
    suspend fun createSession(request: CreateSessionRequest): ApiResult<Session>
    suspend fun deleteSession(sessionId: String): ApiResult<Unit>
}
