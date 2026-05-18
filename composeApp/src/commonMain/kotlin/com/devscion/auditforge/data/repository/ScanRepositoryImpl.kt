package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.ScanApiService
import com.devscion.auditforge.domain.model.CancelScanResponse
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.model.TriggerScanRequest
import kotlinx.coroutines.flow.Flow

class ScanRepositoryImpl(private val scanApiService: ScanApiService) : ScanRepository {

    override suspend fun triggerScan(
        sessionId: String,
        request: TriggerScanRequest
    ): ApiResult<Scan> =
        scanApiService.triggerScan(sessionId, request)

    override suspend fun getScan(sessionId: String, scanId: String): ApiResult<Scan> =
        scanApiService.getScan(sessionId, scanId)

    override suspend fun getActiveScan(sessionId: String): ApiResult<Scan?> =
        ApiResult.Success(null)

    override suspend fun cancelScan(
        sessionId: String,
        scanId: String
    ): ApiResult<CancelScanResponse> =
        scanApiService.cancelScan(sessionId, scanId)

    override fun observeScanProgress(sessionId: String, scanId: String): Flow<ScanProgressEvent> =
        scanApiService.observeScanProgress(sessionId, scanId)
}
