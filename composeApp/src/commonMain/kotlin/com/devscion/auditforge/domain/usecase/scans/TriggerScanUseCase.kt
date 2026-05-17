package com.devscion.auditforge.domain.usecase.scans

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ScanRepository
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.TriggerScanRequest
import org.koin.core.annotation.Factory

@Factory
class TriggerScanUseCase(private val scanRepository: ScanRepository) {
    suspend operator fun invoke(sessionId: String, policyPackIds: List<String>): ApiResult<Scan> =
        scanRepository.triggerScan(sessionId, TriggerScanRequest(policyPackIds = policyPackIds))
}
