package com.devscion.auditforge.ui.sessions.detail.findings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.usecase.findings.GetFindingsSummaryUseCase
import com.devscion.auditforge.domain.usecase.findings.GetFindingsUseCase
import com.devscion.auditforge.domain.usecase.findings.UpdateFindingStatusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class FindingsViewModel(
    @InjectedParam private val sessionId: String,
    private val getFindingsUseCase: GetFindingsUseCase,
    private val getFindingsSummaryUseCase: GetFindingsSummaryUseCase,
    private val updateFindingStatusUseCase: UpdateFindingStatusUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FindingsUiState())
    val uiState: StateFlow<FindingsUiState> = _uiState.asStateFlow()

    init {
        onIntent(FindingsIntent.Load)
    }

    fun onIntent(intent: FindingsIntent) {
        when (intent) {
            FindingsIntent.Load -> loadAll()
            is FindingsIntent.ToggleSeverityFilter -> toggleSeverityFilter(intent.severity)
            is FindingsIntent.SetStatusFilter -> _uiState.update { it.copy(filterStatus = intent.status) }
            is FindingsIntent.SetCategoryFilter -> _uiState.update { it.copy(filterCategory = intent.category) }
            is FindingsIntent.SetSearchQuery -> _uiState.update { it.copy(searchQuery = intent.query) }
            FindingsIntent.ClearFilters -> _uiState.update {
                it.copy(filterSeverity = emptySet(), filterStatus = null, filterCategory = null, searchQuery = "")
            }
            is FindingsIntent.SelectFinding -> _uiState.update {
                it.copy(selectedFinding = intent.finding, pendingStatus = intent.finding.status, pendingComment = "")
            }
            FindingsIntent.DismissFindingDetail -> _uiState.update {
                it.copy(selectedFinding = null, pendingStatus = null, pendingComment = "")
            }
            is FindingsIntent.SetPendingStatus -> _uiState.update { it.copy(pendingStatus = intent.status) }
            is FindingsIntent.SetPendingComment -> _uiState.update { it.copy(pendingComment = intent.comment) }
            FindingsIntent.ConfirmStatusUpdate -> confirmStatusUpdate()
            FindingsIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val findingsResult = getFindingsUseCase(sessionId)
            val summaryResult = getFindingsSummaryUseCase(sessionId)

            val findings = when (findingsResult) {
                is ApiResult.Success -> findingsResult.data.data
                else -> null
            }
            val summary = when (summaryResult) {
                is ApiResult.Success -> summaryResult.data
                else -> null
            }
            val error = when {
                findingsResult is ApiResult.Unauthorized || summaryResult is ApiResult.Unauthorized ->
                    "Session expired. Please sign in again."
                findingsResult is ApiResult.NetworkError || summaryResult is ApiResult.NetworkError ->
                    "Network error. Check your connection."
                findingsResult is ApiResult.Error -> findingsResult.message
                summaryResult is ApiResult.Error -> summaryResult.message
                else -> null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    allFindings = findings ?: it.allFindings,
                    summary = summary ?: it.summary,
                    error = error,
                )
            }
        }
    }

    private fun toggleSeverityFilter(severity: RuleSeverity) {
        _uiState.update { state ->
            val updated = if (severity in state.filterSeverity)
                state.filterSeverity - severity
            else
                state.filterSeverity + severity
            state.copy(filterSeverity = updated)
        }
    }

    private fun confirmStatusUpdate() {
        val finding = _uiState.value.selectedFinding ?: return
        val newStatus = _uiState.value.pendingStatus ?: return
        if (newStatus == finding.status) {
            _uiState.update { it.copy(selectedFinding = null, pendingStatus = null, pendingComment = "") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true) }
            when (val result = updateFindingStatusUseCase(
                sessionId, finding.id, newStatus, _uiState.value.pendingComment,
            )) {
                is ApiResult.Success -> {
                    val updated = result.data
                    _uiState.update { state ->
                        state.copy(
                            isUpdatingStatus = false,
                            selectedFinding = null,
                            pendingStatus = null,
                            pendingComment = "",
                            allFindings = state.allFindings.map { if (it.id == updated.id) updated else it },
                        )
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isUpdatingStatus = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isUpdatingStatus = false, error = "Failed to update status. Check your connection.")
                }
            }
        }
    }
}
