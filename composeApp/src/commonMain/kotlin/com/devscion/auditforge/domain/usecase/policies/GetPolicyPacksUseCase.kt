package com.devscion.auditforge.domain.usecase.policies

import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackListResponse
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.PolicyRepository
import org.koin.core.annotation.Factory

@Factory
class GetPolicyPacksUseCase(private val policyRepository: PolicyRepository) {
    suspend operator fun invoke(framework: PolicyFramework? = null): ApiResult<PolicyPackListResponse> =
        policyRepository.getPolicyPacks(framework)
}
