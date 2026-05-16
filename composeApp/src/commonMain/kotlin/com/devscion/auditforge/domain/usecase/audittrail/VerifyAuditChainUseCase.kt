package com.devscion.auditforge.domain.usecase.audittrail

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.AuditTrailRepository
import com.devscion.auditforge.domain.model.AuditChainVerification
import org.koin.core.annotation.Factory

@Factory
class VerifyAuditChainUseCase(private val repository: AuditTrailRepository) {
    suspend operator fun invoke(sessionId: String): ApiResult<AuditChainVerification> =
        repository.verifyChain(sessionId)
}
