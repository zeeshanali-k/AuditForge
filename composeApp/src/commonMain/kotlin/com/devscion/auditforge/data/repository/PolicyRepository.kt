package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackListResponse
import com.devscion.auditforge.data.network.ApiResult

interface PolicyRepository {
    suspend fun getPolicyPacks(framework: PolicyFramework? = null): ApiResult<PolicyPackListResponse>
    suspend fun getPolicyPackDetail(packId: String): ApiResult<PolicyPackDetail>
}
