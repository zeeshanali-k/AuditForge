package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.FindingsApiService
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingListResponse
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.UpdateFindingStatusRequest

class FindingsRepositoryImpl(private val api: FindingsApiService) : FindingsRepository {

    override suspend fun getFindings(
        sessionId: String,
        severity: Set<RuleSeverity>,
        status: FindingStatus?,
        category: String?,
        page: Int,
        pageSize: Int,
    ): ApiResult<FindingListResponse> = api.getFindings(sessionId, severity, status, category, page, pageSize)

    override suspend fun getFinding(sessionId: String, findingId: String): ApiResult<Finding> =
        api.getFinding(sessionId, findingId)

    override suspend fun updateFindingStatus(
        sessionId: String,
        findingId: String,
        request: UpdateFindingStatusRequest,
    ): ApiResult<Finding> = api.updateFindingStatus(sessionId, findingId, request)

    override suspend fun getFindingsSummary(sessionId: String): ApiResult<FindingsSummary> =
        api.getFindingsSummary(sessionId)
}
