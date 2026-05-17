package com.devscion.auditforge.domain.usecase.scans

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ScanRepository
import com.devscion.auditforge.domain.model.Scan
import org.koin.core.annotation.Factory

@Factory
class GetScanUseCase(private val scanRepository: ScanRepository) {
    suspend operator fun invoke(sessionId: String, scanId: String): ApiResult<Scan> =
        scanRepository.getScan(sessionId, scanId)
}
