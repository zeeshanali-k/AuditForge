package com.devscion.auditforge.data.repository

import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.CancelScanResponse
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.model.ScanStatus
import com.devscion.auditforge.domain.model.TriggerScanRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MockScanRepository : ScanRepository {

    private val mutex = Mutex()
    private val scans: MutableMap<String, Scan> = mutableMapOf()

    override suspend fun triggerScan(
        sessionId: String,
        request: TriggerScanRequest
    ): ApiResult<Scan> {
        delay(300)
        return mutex.withLock {
            val existing = scans[sessionId]
            if (existing != null && (existing.status == ScanStatus.Running || existing.status == ScanStatus.Queued)) {
                return@withLock ApiResult.Error("A scan is already running for this session", 409)
            }
            val scan = Scan(
                id = "mock-scan-${Random.nextLong().toString(16).removePrefix("-").take(8)}",
                sessionId = sessionId,
                status = ScanStatus.Queued,
                policyPackIds = request.policyPackIds,
                estimatedDurationSeconds = 12,
            )
            scans[sessionId] = scan
            ApiResult.Success(scan)
        }
    }

    override suspend fun getScan(sessionId: String, scanId: String): ApiResult<Scan> {
        delay(100)
        return mutex.withLock {
            val scan = scans[sessionId]?.takeIf { it.id == scanId }
            if (scan != null) ApiResult.Success(scan) else ApiResult.Error("Scan not found", 404)
        }
    }

    override suspend fun cancelScan(
        sessionId: String,
        scanId: String
    ): ApiResult<CancelScanResponse> {
        delay(200)
        return mutex.withLock {
            val scan = scans[sessionId]?.takeIf { it.id == scanId }
                ?: return@withLock ApiResult.Error("Scan not found", 404)
            val cancelled = scan.copy(status = ScanStatus.Cancelled, cancelledAt = MOCK_TIMESTAMP)
            scans[sessionId] = cancelled
            ApiResult.Success(
                CancelScanResponse(
                    id = cancelled.id,
                    status = cancelled.status,
                    cancelledAt = cancelled.cancelledAt
                )
            )
        }
    }

    override fun observeScanProgress(sessionId: String, scanId: String): Flow<ScanProgressEvent> =
        flow {
            val steps = listOf(
                "Preparing connectors" to 8,
                "Scanning codebase" to 20,
                "Analyzing API spec" to 38,
                "Evaluating OWASP rules" to 52,
                "Running HIPAA checks" to 66,
                "Running AI analysis" to 80,
                "Aggregating findings" to 92,
                "Finalizing report" to 100,
            )

            var findingsSoFar = 0
            val mockFindings = buildMockFindings()

            mutex.withLock {
                scans[sessionId]?.let { scans[sessionId] = it.copy(status = ScanStatus.Running) }
            }

            for ((step, percent) in steps) {
                val scan = mutex.withLock { scans[sessionId] }
                if (scan?.status == ScanStatus.Cancelled) return@flow

                delay(if (percent <= 20) 800L else 1200L)
                emit(ScanProgressEvent.Progress(percent, step, findingsSoFar))

                val findingsForStep = mockFindings.filter { it.atStep == step }
                for (finding in findingsForStep) {
                    delay(300)
                    findingsSoFar++
                    emit(
                        ScanProgressEvent.Finding(
                            finding.id,
                            finding.severity,
                            finding.ruleId,
                            finding.title
                        )
                    )
                }

                mutex.withLock {
                    scans[sessionId]?.let {
                        scans[sessionId] = it.copy(
                            progressPercent = percent,
                            currentStep = step,
                            findingsSoFar = findingsSoFar,
                            status = if (percent == 100) ScanStatus.Completed else ScanStatus.Running,
                        )
                    }
                }
            }

            val scan = mutex.withLock { scans[sessionId] }
            if (scan?.status != ScanStatus.Cancelled) {
                delay(200)
                emit(ScanProgressEvent.Completed(scanId, findingsSoFar, 76))
            }
        }

    private data class MockFindingTemplate(
        val id: String,
        val severity: RuleSeverity,
        val ruleId: String,
        val title: String,
        val atStep: String,
    )

    private fun buildMockFindings(): List<MockFindingTemplate> = listOf(
        MockFindingTemplate(
            "f-001", RuleSeverity.Critical,
            "owasp-api2-broken-auth",
            "API endpoint missing authentication",
            "Scanning codebase",
        ),
        MockFindingTemplate(
            "f-002", RuleSeverity.High,
            "owasp-api8-security-misconfiguration",
            "Debug mode enabled in production config",
            "Scanning codebase",
        ),
        MockFindingTemplate(
            "f-003", RuleSeverity.Critical,
            "owasp-api1-broken-object-auth",
            "Broken object-level authorization in user endpoint",
            "Analyzing API spec",
        ),
        MockFindingTemplate(
            "f-004", RuleSeverity.High,
            "owasp-api3-broken-object-property-auth",
            "Excessive data exposure in GET /users response",
            "Analyzing API spec",
        ),
        MockFindingTemplate(
            "f-005", RuleSeverity.Medium,
            "hipaa-164.312.a.2.iv",
            "Encryption not enforced for data at rest",
            "Running HIPAA checks",
        ),
        MockFindingTemplate(
            "f-006", RuleSeverity.High,
            "hipaa-164.312.b",
            "Audit logging missing for PHI access",
            "Running HIPAA checks",
        ),
        MockFindingTemplate(
            "f-007", RuleSeverity.Medium,
            "owasp-api9-improper-inventory",
            "Undocumented internal API endpoints discovered",
            "Running AI analysis",
        ),
        MockFindingTemplate(
            "f-008", RuleSeverity.Low,
            "owasp-api10-unsafe-consumption",
            "Third-party API response not validated before use",
            "Running AI analysis",
        ),
    )

    companion object {
        private const val MOCK_TIMESTAMP = "2026-05-16T10:00:00Z"
    }
}
