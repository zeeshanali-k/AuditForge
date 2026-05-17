package com.devscion.auditforge.domain.usecase.scans

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ScanRepository
import com.devscion.auditforge.domain.model.CancelScanResponse
import org.koin.core.annotation.Factory

@Factory
class CancelScanUseCase(private val scanRepository: ScanRepository) {
    suspend operator fun invoke(sessionId: String, scanId: String): ApiResult<CancelScanResponse> =
        scanRepository.cancelScan(sessionId, scanId)
}
