package com.devscion.auditforge.domain.usecase.reports

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.repository.ReportsRepository
import org.koin.core.annotation.Factory

@Factory
class DownloadReportUseCase(private val repository: ReportsRepository) {
    suspend operator fun invoke(sessionId: String, reportId: String): ApiResult<ByteArray> =
        repository.downloadReport(sessionId, reportId)
}
