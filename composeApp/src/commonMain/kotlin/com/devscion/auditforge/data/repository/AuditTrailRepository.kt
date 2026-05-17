package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.AuditTrailResponse

interface AuditTrailRepository {
    suspend fun getAuditTrail(
        sessionId: String,
        eventType: AuditEventType? = null,
        fromTimestamp: String? = null,
        toTimestamp: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): ApiResult<AuditTrailResponse>

    suspend fun verifyChain(sessionId: String): ApiResult<AuditChainVerification>
}
