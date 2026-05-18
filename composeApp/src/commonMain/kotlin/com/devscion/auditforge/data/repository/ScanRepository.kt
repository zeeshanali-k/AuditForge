package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.CancelScanResponse
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.model.TriggerScanRequest
import kotlinx.coroutines.flow.Flow

interface ScanRepository {
    suspend fun triggerScan(sessionId: String, request: TriggerScanRequest): ApiResult<Scan>
    suspend fun getScan(sessionId: String, scanId: String): ApiResult<Scan>
    suspend fun getActiveScan(sessionId: String): ApiResult<Scan?>
    suspend fun cancelScan(sessionId: String, scanId: String): ApiResult<CancelScanResponse>
    fun observeScanProgress(sessionId: String, scanId: String): Flow<ScanProgressEvent>
}
