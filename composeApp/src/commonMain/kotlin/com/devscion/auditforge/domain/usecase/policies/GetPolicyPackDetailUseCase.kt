package com.devscion.auditforge.domain.usecase.policies

import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.PolicyRepository
import org.koin.core.annotation.Factory

@Factory
class GetPolicyPackDetailUseCase(private val policyRepository: PolicyRepository) {
    suspend operator fun invoke(packId: String): ApiResult<PolicyPackDetail> =
        policyRepository.getPolicyPackDetail(packId)
}
