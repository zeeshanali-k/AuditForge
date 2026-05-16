package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.model.CreateSessionRequest
import com.devscion.auditforge.data.model.Session
import com.devscion.auditforge.data.model.SessionListResponse
import com.devscion.auditforge.data.model.SessionStatus
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.SessionApiService

class SessionRepositoryImpl(
    private val sessionApiService: SessionApiService,
) : SessionRepository {

    override suspend fun getSessions(page: Int, pageSize: Int, status: SessionStatus?): ApiResult<SessionListResponse> =
        sessionApiService.getSessions(page, pageSize, status)

    override suspend fun getSession(sessionId: String): ApiResult<Session> =
        sessionApiService.getSession(sessionId)

    override suspend fun createSession(request: CreateSessionRequest): ApiResult<Session> =
        sessionApiService.createSession(request)

    override suspend fun deleteSession(sessionId: String): ApiResult<Unit> =
        sessionApiService.deleteSession(sessionId)
}
