package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.AuditActorType
import com.devscion.auditforge.domain.model.AuditActor
import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEvent
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.AuditTrailResponse
import com.devscion.auditforge.domain.model.PaginationMeta
import kotlinx.coroutines.delay
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MockAuditTrailRepository : AuditTrailRepository {

    private val systemActor = AuditActor(
        type = AuditActorType.System,
        id = "system",
        name = "System",
    )
    private val userActor = AuditActor(
        type = AuditActorType.User,
        id = "user-001",
        name = "Jane Doe",
    )

    private val allEvents = listOf(
        AuditEvent(
            id = "evt-001",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:00:00Z",
            eventType = AuditEventType.SessionCreated,
            actor = userActor,
            details = buildJsonObject { put("session_name", "Q2 2026 Compliance Audit") },
            previousHash = "0000000000000000000000000000000000000000000000000000000000000000",
            currentHash = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
        ),
        AuditEvent(
            id = "evt-002",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:05:00Z",
            eventType = AuditEventType.UploadAdded,
            actor = userActor,
            details = buildJsonObject {
                put("filename", "patient-portal.zip")
                put("type", "codebase")
                put("size_bytes", "4718592")
            },
            previousHash = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2",
            currentHash = "b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3",
        ),
        AuditEvent(
            id = "evt-003",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:06:30Z",
            eventType = AuditEventType.UploadAdded,
            actor = userActor,
            details = buildJsonObject {
                put("filename", "api-spec.yaml")
                put("type", "openapi_spec")
                put("size_bytes", "38912")
            },
            previousHash = "b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3d4e5f6a7b2c3",
            currentHash = "c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4",
        ),
        AuditEvent(
            id = "evt-004",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:08:00Z",
            eventType = AuditEventType.PolicyChanged,
            actor = userActor,
            details = buildJsonObject {
                put("added_packs", "owasp-api-top10-v2023, hipaa-technical-safeguards-v1")
                put("removed_packs", "")
            },
            previousHash = "c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4e5f6a7b8c3d4",
            currentHash = "d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5",
        ),
        AuditEvent(
            id = "evt-005",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:10:00Z",
            eventType = AuditEventType.ScanStarted,
            actor = userActor,
            details = buildJsonObject {
                put("scan_id", "scan-001")
                put("policy_packs", "owasp-api-top10-v2023, hipaa-technical-safeguards-v1")
            },
            previousHash = "d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5f6a7b8c9d4e5",
            currentHash = "e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6",
        ),
        AuditEvent(
            id = "evt-006",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:10:12Z",
            eventType = AuditEventType.FindingCreated,
            actor = systemActor,
            details = buildJsonObject {
                put("finding_id", "finding-001")
                put("severity", "critical")
                put("rule", "OWASP-API1:2023")
                put("title", "Broken object-level authorization")
            },
            previousHash = "e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6a7b8c9d0e5f6",
            currentHash = "f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7",
        ),
        AuditEvent(
            id = "evt-007",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:10:28Z",
            eventType = AuditEventType.FindingCreated,
            actor = systemActor,
            details = buildJsonObject {
                put("finding_id", "finding-002")
                put("severity", "high")
                put("rule", "HIPAA-164.312(a)(1)")
                put("title", "Missing access control on PHI endpoint")
            },
            previousHash = "f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7b8c9d0e1f6a7",
            currentHash = "a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8",
        ),
        AuditEvent(
            id = "evt-008",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:11:45Z",
            eventType = AuditEventType.ScanCompleted,
            actor = systemActor,
            details = buildJsonObject {
                put("scan_id", "scan-001")
                put("findings_count", "8")
                put("overall_score", "62")
                put("duration_seconds", "105")
            },
            previousHash = "a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8c9d0e1f2a7b8",
            currentHash = "b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9",
        ),
        AuditEvent(
            id = "evt-009",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:15:00Z",
            eventType = AuditEventType.UserAction,
            actor = userActor,
            details = buildJsonObject {
                put("action", "update_finding_status")
                put("finding_id", "finding-001")
                put("old_status", "open")
                put("new_status", "acknowledged")
                put("comment", "Investigating - may be intentional for internal APIs")
            },
            previousHash = "b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9d0e1f2a3b8c9",
            currentHash = "c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0",
        ),
        AuditEvent(
            id = "evt-010",
            sessionId = "mock-session",
            timestamp = "2026-05-16T09:20:00Z",
            eventType = AuditEventType.ReportGenerated,
            actor = userActor,
            details = buildJsonObject {
                put("report_id", "report-001")
                put("format", "pdf")
                put("signed", "true")
                put("finding_count", "8")
                put("overall_score", "62")
            },
            previousHash = "c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0e1f2a3b4c9d0",
            currentHash = "d0e1f2a3b4c5d0e1f2a3b4c5d0e1f2a3b4c5d0e1f2a3b4c5d0e1f2a3b4c5d0e1",
        ),
    ).sortedByDescending { it.timestamp }

    override suspend fun getAuditTrail(
        sessionId: String,
        eventType: AuditEventType?,
        fromTimestamp: String?,
        toTimestamp: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<AuditTrailResponse> {
        delay(400)
        val filtered = if (eventType != null) {
            allEvents.filter { it.eventType == eventType }
        } else {
            allEvents
        }
        val totalItems = filtered.size
        val totalPages = maxOf(1, (totalItems + pageSize - 1) / pageSize)
        val fromIndex = (page - 1) * pageSize
        val toIndex = minOf(fromIndex + pageSize, totalItems)
        val pageData = if (fromIndex < totalItems) filtered.subList(fromIndex, toIndex) else emptyList()
        return ApiResult.Success(
            AuditTrailResponse(
                data = pageData,
                pagination = PaginationMeta(
                    page = page,
                    pageSize = pageSize,
                    totalItems = totalItems,
                    totalPages = totalPages,
                ),
            )
        )
    }

    override suspend fun verifyChain(sessionId: String): ApiResult<AuditChainVerification> {
        delay(800)
        return ApiResult.Success(
            AuditChainVerification(
                sessionId = sessionId,
                totalEvents = allEvents.size,
                chainIntact = true,
                verifiedAt = "2026-05-16T09:25:00Z",
            )
        )
    }
}
