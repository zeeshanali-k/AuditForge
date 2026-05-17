package com.devscion.auditforge.domain.usecase.audittrail

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.AuditTrailRepository
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.AuditTrailResponse
import org.koin.core.annotation.Factory

@Factory
class GetAuditTrailUseCase(private val repository: AuditTrailRepository) {
    suspend operator fun invoke(
        sessionId: String,
        eventType: AuditEventType? = null,
        fromTimestamp: String? = null,
        toTimestamp: String? = null,
        page: Int = 1,
        pageSize: Int = 50,
    ): ApiResult<AuditTrailResponse> = repository.getAuditTrail(
        sessionId = sessionId,
        eventType = eventType,
        fromTimestamp = fromTimestamp,
        toTimestamp = toTimestamp,
        page = page,
        pageSize = pageSize,
    )
}
