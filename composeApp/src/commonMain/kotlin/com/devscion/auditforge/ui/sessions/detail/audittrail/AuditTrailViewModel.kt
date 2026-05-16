package com.devscion.auditforge.ui.sessions.detail.audittrail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.usecase.audittrail.GetAuditTrailUseCase
import com.devscion.auditforge.domain.usecase.audittrail.VerifyAuditChainUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class AuditTrailViewModel(
    @InjectedParam private val sessionId: String,
    private val getAuditTrailUseCase: GetAuditTrailUseCase,
    private val verifyAuditChainUseCase: VerifyAuditChainUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuditTrailUiState())
    val uiState: StateFlow<AuditTrailUiState> = _uiState.asStateFlow()

    init {
        onIntent(AuditTrailIntent.Load)
    }

    fun onIntent(intent: AuditTrailIntent) {
        when (intent) {
            AuditTrailIntent.Load -> load()
            AuditTrailIntent.LoadMore -> loadMore()
            is AuditTrailIntent.SetEventTypeFilter -> applyFilter(intent.eventType)
            is AuditTrailIntent.ToggleEventExpanded -> toggleExpanded(intent.eventId)
            AuditTrailIntent.VerifyChain -> verifyChain()
            AuditTrailIntent.DismissVerifyModal -> _uiState.update {
                it.copy(showVerifyModal = false, verificationResult = null)
            }
            AuditTrailIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun load() {
        val eventType = _uiState.value.selectedEventType
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getAuditTrailUseCase(sessionId, eventType = eventType, page = 1)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        events = result.data.data,
                        pagination = result.data.pagination,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load audit trail. Check your connection.")
                }
            }
        }
    }

    private fun loadMore() {
        val state = _uiState.value
        val pagination = state.pagination ?: return
        if (state.isLoadingMore || pagination.page >= pagination.totalPages) return

        val nextPage = pagination.page + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            when (val result = getAuditTrailUseCase(
                sessionId,
                eventType = state.selectedEventType,
                page = nextPage,
            )) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        events = it.events + result.data.data,
                        pagination = result.data.pagination,
                    )
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoadingMore = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoadingMore = false, error = "Failed to load more events. Check your connection.")
                }
            }
        }
    }

    private fun applyFilter(eventType: AuditEventType?) {
        _uiState.update { it.copy(selectedEventType = eventType, events = emptyList(), pagination = null) }
        load()
    }

    private fun toggleExpanded(eventId: String) {
        _uiState.update { state ->
            val newId = if (state.expandedEventId == eventId) null else eventId
            state.copy(expandedEventId = newId)
        }
    }

    private fun verifyChain() {
        viewModelScope.launch {
            _uiState.update { it.copy(isVerifying = true, showVerifyModal = true, verificationResult = null) }
            when (val result = verifyAuditChainUseCase(sessionId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isVerifying = false, verificationResult = result.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isVerifying = false, showVerifyModal = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isVerifying = false, showVerifyModal = false, error = "Failed to verify chain. Check your connection.")
                }
            }
        }
    }
}
