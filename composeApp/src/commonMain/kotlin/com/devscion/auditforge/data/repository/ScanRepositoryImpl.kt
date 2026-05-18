package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.network.ScanApiService
import com.devscion.auditforge.domain.model.CancelScanResponse
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.model.ScanStatus
import com.devscion.auditforge.domain.model.TriggerScanRequest
import kotlinx.coroutines.flow.Flow

class ScanRepositoryImpl(private val scanApiService: ScanApiService) : ScanRepository {

    // Persists scan IDs across ViewModel recreations within the same app session.
    // The repository is a Koin singleton, so this survives tab switches.
    private val activeScanIdCache = mutableMapOf<String, String>() // sessionId -> scanId

    override suspend fun triggerScan(
        sessionId: String,
        request: TriggerScanRequest,
    ): ApiResult<Scan> {
        val result = scanApiService.triggerScan(sessionId, request)
        if (result is ApiResult.Success) {
            activeScanIdCache[sessionId] = result.data.id
        }
        return result
    }

    override suspend fun getScan(sessionId: String, scanId: String): ApiResult<Scan> =
        scanApiService.getScan(sessionId, scanId)

    override suspend fun getActiveScan(sessionId: String): ApiResult<Scan?> {
        val scanId = activeScanIdCache[sessionId] ?: return ApiResult.Success(null)
        return when (val result = scanApiService.getScan(sessionId, scanId)) {
            is ApiResult.Success -> {
                val scan = result.data
                if (scan.status == ScanStatus.Queued || scan.status == ScanStatus.Running) {
                    ApiResult.Success(scan)
                } else {
                    activeScanIdCache.remove(sessionId)
                    ApiResult.Success(null)
                }
            }
            else -> {
                activeScanIdCache.remove(sessionId)
                ApiResult.Success(null)
            }
        }
    }

    override suspend fun cancelScan(
        sessionId: String,
        scanId: String,
    ): ApiResult<CancelScanResponse> {
        val result = scanApiService.cancelScan(sessionId, scanId)
        if (result is ApiResult.Success) {
            activeScanIdCache.remove(sessionId)
        }
        return result
    }

    override fun observeScanProgress(sessionId: String, scanId: String): Flow<ScanProgressEvent> =
        scanApiService.observeScanProgress(sessionId, scanId)
}
