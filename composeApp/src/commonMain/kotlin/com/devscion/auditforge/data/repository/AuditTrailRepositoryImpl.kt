package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.AuditTrailApiService
import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.AuditTrailResponse

class AuditTrailRepositoryImpl(
    private val auditTrailApiService: AuditTrailApiService,
) : AuditTrailRepository {

    override suspend fun getAuditTrail(
        sessionId: String,
        eventType: AuditEventType?,
        fromTimestamp: String?,
        toTimestamp: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<AuditTrailResponse> = auditTrailApiService.getAuditTrail(
        sessionId = sessionId,
        eventType = eventType,
        fromTimestamp = fromTimestamp,
        toTimestamp = toTimestamp,
        page = page,
        pageSize = pageSize,
    )

    override suspend fun verifyChain(sessionId: String): ApiResult<AuditChainVerification> =
        auditTrailApiService.verifyChain(sessionId)
}
