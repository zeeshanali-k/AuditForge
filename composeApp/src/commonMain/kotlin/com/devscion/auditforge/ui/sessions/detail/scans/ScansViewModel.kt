package com.devscion.auditforge.ui.sessions.detail.scans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.LiveFinding
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.ScanProgressEvent
import com.devscion.auditforge.domain.usecase.policies.GetPolicyPacksUseCase
import com.devscion.auditforge.domain.usecase.scans.CancelScanUseCase
import com.devscion.auditforge.domain.usecase.scans.ObserveScanProgressUseCase
import com.devscion.auditforge.domain.usecase.scans.TriggerScanUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScansViewModel(
    @InjectedParam private val sessionId: String,
    private val getPolicyPacksUseCase: GetPolicyPacksUseCase,
    private val triggerScanUseCase: TriggerScanUseCase,
    private val cancelScanUseCase: CancelScanUseCase,
    private val observeScanProgressUseCase: ObserveScanProgressUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScansUiState())
    val uiState: StateFlow<ScansUiState> = _uiState.asStateFlow()

    private var progressJob: Job? = null

    init {
        onIntent(ScansIntent.Load)
    }

    fun onIntent(intent: ScansIntent) {
        when (intent) {
            ScansIntent.Load -> loadPolicyPacks()
            is ScansIntent.TogglePackSelection -> togglePack(intent.packId)
            ScansIntent.TriggerScan -> triggerScan()
            ScansIntent.CancelScan -> cancelScan()
            ScansIntent.DismissError -> _uiState.update { it.copy(error = null) }
            ScansIntent.DismissCompleted -> _uiState.update {
                it.copy(completedTotalFindings = null, completedOverallScore = null)
            }
        }
    }

    private fun loadPolicyPacks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = getPolicyPacksUseCase()) {
                is ApiResult.Success -> {
                    val packs = result.data.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            availablePacks = packs,
                            selectedPackIds = packs.map { p -> p.id }.toSet(),
                        )
                    }
                }

                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }

                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load policy packs. Check your connection."
                    )
                }
            }
        }
    }

    private fun togglePack(packId: String) {
        _uiState.update { state ->
            val updated = if (packId in state.selectedPackIds)
                state.selectedPackIds - packId
            else
                state.selectedPackIds + packId
            state.copy(selectedPackIds = updated)
        }
    }

    private fun triggerScan() {
        val selectedIds = _uiState.value.selectedPackIds.toList()
        if (selectedIds.isEmpty()) {
            _uiState.update { it.copy(error = "Select at least one policy pack before scanning.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = triggerScanUseCase(sessionId, selectedIds)) {
                is ApiResult.Success -> {
                    val scan = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentScan = scan,
                            progressPercent = 0,
                            currentStep = "",
                            criticalCount = 0,
                            highCount = 0,
                            mediumCount = 0,
                            lowCount = 0,
                            liveFindings = emptyList(),
                            completedTotalFindings = null,
                            completedOverallScore = null,
                        )
                    }
                    startObservingProgress(scan.sessionId, scan.id)
                }

                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }

                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to start scan. Check your connection."
                    )
                }
            }
        }
    }

    private fun startObservingProgress(sessionId: String, scanId: String) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            observeScanProgressUseCase(sessionId, scanId).collect { event ->
                when (event) {
                    is ScanProgressEvent.Progress -> _uiState.update {
                        it.copy(
                            progressPercent = event.progressPercent,
                            currentStep = event.currentStep,
                        )
                    }

                    is ScanProgressEvent.Finding -> {
                        val finding = LiveFinding(
                            id = event.id,
                            severity = event.severity,
                            title = event.title,
                            ruleId = event.ruleId,
                        )
                        _uiState.update { state ->
                            state.copy(
                                liveFindings = listOf(finding) + state.liveFindings,
                                criticalCount = state.criticalCount + (if (event.severity == RuleSeverity.Critical) 1 else 0),
                                highCount = state.highCount + (if (event.severity == RuleSeverity.High) 1 else 0),
                                mediumCount = state.mediumCount + (if (event.severity == RuleSeverity.Medium) 1 else 0),
                                lowCount = state.lowCount + (if (event.severity == RuleSeverity.Low) 1 else 0),
                            )
                        }
                    }

                    is ScanProgressEvent.Completed -> _uiState.update {
                        it.copy(
                            progressPercent = 100,
                            completedTotalFindings = event.totalFindings,
                            completedOverallScore = event.overallScore,
                            currentScan = it.currentScan?.copy(
                                status = com.devscion.auditforge.domain.model.ScanStatus.Completed,
                            ),
                        )
                    }

                    is ScanProgressEvent.ScanError -> _uiState.update {
                        it.copy(error = event.message)
                    }
                }
            }
        }
    }

    private fun cancelScan() {
        val scan = _uiState.value.currentScan ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            progressJob?.cancel()
            when (val result = cancelScanUseCase(sessionId, scan.id)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isCancelling = false, currentScan = it.currentScan?.copy(
                            status = result.data.status,
                            cancelledAt = result.data.cancelledAt,
                        )
                    )
                }

                is ApiResult.Error -> _uiState.update {
                    it.copy(isCancelling = false, error = result.message)
                }

                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(
                        isCancelling = false,
                        error = "Failed to cancel scan. Check your connection."
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
    }
}
