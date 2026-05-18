package com.devscion.auditforge.ui.sessions.detail.scans

import com.devscion.auditforge.domain.model.LiveFinding
import com.devscion.auditforge.domain.model.PolicyPackSummary
import com.devscion.auditforge.domain.model.Scan
import com.devscion.auditforge.domain.model.ScanStatus

data class ScansUiState(
    val isLoading: Boolean = false,
    val availablePacks: List<PolicyPackSummary> = emptyList(),
    val selectedPackIds: Set<String> = emptySet(),
    val currentScan: Scan? = null,
    val progressPercent: Int = 0,
    val currentStep: String = "",
    val criticalCount: Int = 0,
    val highCount: Int = 0,
    val mediumCount: Int = 0,
    val lowCount: Int = 0,
    val liveFindings: List<LiveFinding> = emptyList(),
    val completedTotalFindings: Int? = null,
    val completedOverallScore: Int? = null,
    val error: String? = null,
    val isCancelling: Boolean = false,
    // True when the session is scanning but the active scan could not be retrieved (real API, no scan ID available)
    val isScanningElsewhere: Boolean = false,
) {
    val isScanning: Boolean
        get() = currentScan?.status == ScanStatus.Running || currentScan?.status == ScanStatus.Queued

    val showScanningView: Boolean
        get() = isScanning || isScanningElsewhere

    val isCompleted: Boolean
        get() = completedTotalFindings != null

    val isFailed: Boolean
        get() = currentScan?.status == ScanStatus.Failed

    val canStartScan: Boolean
        get() = !showScanningView && selectedPackIds.isNotEmpty()
}

sealed class ScansIntent {
    data object Load : ScansIntent()
    data class TogglePackSelection(val packId: String) : ScansIntent()
    data object TriggerScan : ScansIntent()
    data object CancelScan : ScansIntent()
    data object DismissError : ScansIntent()
    data object DismissCompleted : ScansIntent()
}
