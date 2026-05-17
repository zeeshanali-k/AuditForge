package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingListResponse
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.UpdateFindingStatusRequest

interface FindingsRepository {
    suspend fun getFindings(
        sessionId: String,
        severity: Set<RuleSeverity> = emptySet(),
        status: FindingStatus? = null,
        category: String? = null,
        page: Int = 1,
        pageSize: Int = 100,
    ): ApiResult<FindingListResponse>

    suspend fun getFinding(sessionId: String, findingId: String): ApiResult<Finding>

    suspend fun updateFindingStatus(
        sessionId: String,
        findingId: String,
        request: UpdateFindingStatusRequest,
    ): ApiResult<Finding>

    suspend fun getFindingsSummary(sessionId: String): ApiResult<FindingsSummary>
}
