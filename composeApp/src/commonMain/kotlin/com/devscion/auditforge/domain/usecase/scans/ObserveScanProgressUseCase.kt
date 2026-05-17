package com.devscion.auditforge.domain.usecase.scans

import com.devscion.auditforge.data.repository.ScanRepository
import com.devscion.auditforge.domain.model.ScanProgressEvent
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class ObserveScanProgressUseCase(private val scanRepository: ScanRepository) {
    operator fun invoke(sessionId: String, scanId: String): Flow<ScanProgressEvent> =
        scanRepository.observeScanProgress(sessionId, scanId)
}
