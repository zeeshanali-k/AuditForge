package com.devscion.auditforge.data.repository

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackListResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.PolicyApiService

class PolicyRepositoryImpl(private val policyApiService: PolicyApiService) : PolicyRepository {

    override suspend fun getPolicyPacks(framework: PolicyFramework?): ApiResult<PolicyPackListResponse> =
        policyApiService.getPolicyPacks(framework)

    override suspend fun getPolicyPackDetail(packId: String): ApiResult<PolicyPackDetail> =
        policyApiService.getPolicyPackDetail(packId)
}
