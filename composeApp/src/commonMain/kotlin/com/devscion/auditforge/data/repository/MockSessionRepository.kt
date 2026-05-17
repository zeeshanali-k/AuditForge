package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.CreateSessionRequest
import com.devscion.auditforge.domain.model.Pagination
import com.devscion.auditforge.domain.model.Session
import com.devscion.auditforge.domain.model.SessionListResponse
import com.devscion.auditforge.domain.model.SessionStatus
import com.devscion.auditforge.domain.model.SessionSummary
import com.devscion.auditforge.domain.model.TargetEnvironment
import com.devscion.auditforge.data.network.ApiResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MockSessionRepository : SessionRepository {

    private val mutex = Mutex()
    private val sessions: MutableList<Session> = mutableListOf(
        Session(
            id = "demo-001",
            name = "Q2 2026 compliance audit",
            description = "Pre-deployment audit for the patient portal release.",
            targetEnvironment = TargetEnvironment.Production,
            status = SessionStatus.Completed,
            findingCount = 23,
            overallScore = 78,
            createdAt = "2026-05-12T10:30:00Z",
            updatedAt = "2026-05-13T15:22:00Z",
            createdBy = "mock-user-001",
        ),
        Session(
            id = "demo-002",
            name = "API security review — payments",
            description = "OWASP API Top 10 review for the payment gateway.",
            targetEnvironment = TargetEnvironment.Staging,
            status = SessionStatus.Scanning,
            findingCount = 7,
            overallScore = null,
            createdAt = "2026-05-14T09:15:00Z",
            updatedAt = "2026-05-14T09:18:00Z",
            createdBy = "mock-user-001",
        ),
        Session(
            id = "demo-003",
            name = "HIPAA readiness check",
            description = null,
            targetEnvironment = TargetEnvironment.Development,
            status = SessionStatus.Created,
            findingCount = 0,
            overallScore = null,
            createdAt = "2026-05-15T16:45:00Z",
            updatedAt = "2026-05-15T16:45:00Z",
            createdBy = "mock-user-001",
        ),
        Session(
            id = "demo-004",
            name = "SOC 2 controls audit",
            description = "Annual SOC 2 Type II controls verification.",
            targetEnvironment = TargetEnvironment.Production,
            status = SessionStatus.Failed,
            findingCount = 12,
            overallScore = 42,
            createdAt = "2026-05-10T14:20:00Z",
            updatedAt = "2026-05-11T08:33:00Z",
            createdBy = "mock-user-001",
        ),
        Session(
            id = "demo-005",
            name = "Mobile app penetration test",
            description = "Pre-release penetration test for the mobile companion app.",
            targetEnvironment = TargetEnvironment.Staging,
            status = SessionStatus.Completed,
            findingCount = 4,
            overallScore = 91,
            createdAt = "2026-05-08T11:00:00Z",
            updatedAt = "2026-05-08T18:42:00Z",
            createdBy = "mock-user-001",
        ),
        Session(
            id = "demo-006",
            name = "PCI DSS quarterly scan",
            description = "Quarterly PCI DSS compliance scan of the cardholder data environment.",
            targetEnvironment = TargetEnvironment.Production,
            status = SessionStatus.Completed,
            findingCount = 9,
            overallScore = 85,
            createdAt = "2026-05-05T08:30:00Z",
            updatedAt = "2026-05-05T13:05:00Z",
            createdBy = "mock-user-001",
        ),
    )

    override suspend fun getSessions(
        page: Int,
        pageSize: Int,
        status: SessionStatus?
    ): ApiResult<SessionListResponse> {
        delay(300)
        return mutex.withLock {
            val filtered = if (status == null) sessions else sessions.filter { it.status == status }
            val from = (page - 1) * pageSize
            val to = minOf(from + pageSize, filtered.size)
            val pageData =
                if (from in 0 until filtered.size) filtered.subList(from, to) else emptyList()
            val totalPages =
                if (filtered.isEmpty()) 1 else (filtered.size + pageSize - 1) / pageSize
            ApiResult.Success(
                SessionListResponse(
                    data = pageData.map { it.toSummary() },
                    pagination = Pagination(page, pageSize, filtered.size, totalPages),
                )
            )
        }
    }

    override suspend fun getSession(sessionId: String): ApiResult<Session> {
        delay(200)
        return mutex.withLock {
            val session = sessions.find { it.id == sessionId }
            if (session != null) ApiResult.Success(session)
            else ApiResult.Error("Session not found", 404)
        }
    }

    override suspend fun createSession(request: CreateSessionRequest): ApiResult<Session> {
        delay(450)
        return mutex.withLock {
            val newSession = Session(
                id = "mock-${Random.nextLong().toString(16).removePrefix("-").take(8)}",
                name = request.name,
                description = request.description,
                targetEnvironment = request.targetEnvironment?.let { value ->
                    TargetEnvironment.entries.find { it.name.lowercase() == value.lowercase() }
                },
                status = SessionStatus.Created,
                findingCount = 0,
                overallScore = null,
                createdAt = MOCK_TIMESTAMP,
                updatedAt = MOCK_TIMESTAMP,
                createdBy = "mock-user-001",
            )
            sessions.add(0, newSession)
            ApiResult.Success(newSession)
        }
    }

    override suspend fun deleteSession(sessionId: String): ApiResult<Unit> {
        delay(250)
        return mutex.withLock {
            val removed = sessions.removeAll { it.id == sessionId }
            if (removed) ApiResult.Success(Unit) else ApiResult.Error("Session not found", 404)
        }
    }

    private fun Session.toSummary() = SessionSummary(
        id = id,
        name = name,
        status = status,
        findingCount = findingCount,
        overallScore = overallScore,
        createdAt = createdAt,
    )

    companion object {
        private const val MOCK_TIMESTAMP = "2026-05-16T10:00:00Z"
    }
}
