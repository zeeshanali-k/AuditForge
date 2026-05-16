package com.devscion.auditforge.ui.sessions.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.usecase.sessions.GetSessionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SessionDetailViewModel(
    @InjectedParam private val sessionId: String,
    private val getSessionUseCase: GetSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionDetailUiState())
    val uiState: StateFlow<SessionDetailUiState> = _uiState.asStateFlow()

    init {
        onIntent(SessionDetailIntent.Load)
    }

    fun onIntent(intent: SessionDetailIntent) {
        when (intent) {
            SessionDetailIntent.Load -> loadSession()
            is SessionDetailIntent.SelectTab -> _uiState.update { it.copy(selectedTab = intent.tab) }
            SessionDetailIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getSessionUseCase(sessionId)) {
                is ApiResult.Success -> _uiState.update { it.copy(isLoading = false, session = result.data) }
                is ApiResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load session. Check your connection.")
                }
            }
        }
    }
}
