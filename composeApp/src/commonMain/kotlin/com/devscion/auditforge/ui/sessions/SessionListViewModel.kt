package com.devscion.auditforge.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.usecase.CreateSessionUseCase
import com.devscion.auditforge.domain.usecase.DeleteSessionUseCase
import com.devscion.auditforge.domain.usecase.GetSessionsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SessionListViewModel(
    private val getSessionsUseCase: GetSessionsUseCase,
    private val createSessionUseCase: CreateSessionUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionListUiState())
    val uiState: StateFlow<SessionListUiState> = _uiState.asStateFlow()

    init {
        onIntent(SessionListIntent.Load)
    }

    fun onIntent(intent: SessionListIntent) {
        when (intent) {
            SessionListIntent.Load -> loadSessions(reset = true)
            is SessionListIntent.UpdateSearch -> _uiState.update { it.copy(searchQuery = intent.query) }
            is SessionListIntent.UpdateStatusFilter -> {
                _uiState.update { it.copy(statusFilter = intent.status, sessions = emptyList(), pagination = null) }
                loadSessions(reset = true)
            }
            SessionListIntent.LoadNextPage -> loadNextPage()
            SessionListIntent.ShowCreateDialog -> _uiState.update {
                it.copy(showCreateDialog = true, newSessionName = "", newSessionDescription = "", newSessionEnvironment = null, nameValidationError = null)
            }
            SessionListIntent.HideCreateDialog -> _uiState.update { it.copy(showCreateDialog = false) }
            is SessionListIntent.UpdateNewName -> _uiState.update { it.copy(newSessionName = intent.value, nameValidationError = null) }
            is SessionListIntent.UpdateNewDescription -> _uiState.update { it.copy(newSessionDescription = intent.value) }
            is SessionListIntent.UpdateNewEnvironment -> _uiState.update { it.copy(newSessionEnvironment = intent.value) }
            SessionListIntent.SubmitCreate -> createSession()
            is SessionListIntent.RequestDelete -> _uiState.update { it.copy(sessionToDelete = intent.session) }
            SessionListIntent.ConfirmDelete -> deleteSession()
            SessionListIntent.CancelDelete -> _uiState.update { it.copy(sessionToDelete = null) }
            SessionListIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadSessions(reset: Boolean) {
        val current = _uiState.value
        val page = if (reset) 1 else (current.pagination?.page ?: 0) + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = reset, isLoadingMore = !reset) }
            when (val result = getSessionsUseCase(page = page, status = current.statusFilter)) {
                is ApiResult.Success -> _uiState.update {
                    val sessions = if (reset) result.data.data else it.sessions + result.data.data
                    it.copy(isLoading = false, isLoadingMore = false, sessions = sessions, pagination = result.data.pagination, error = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, isLoadingMore = false, error = "Failed to load sessions. Check your connection.")
                }
            }
        }
    }

    private fun loadNextPage() {
        if (_uiState.value.hasNextPage && !_uiState.value.isLoadingMore) {
            loadSessions(reset = false)
        }
    }

    private fun createSession() {
        val current = _uiState.value
        val name = current.newSessionName.trim()
        val validationError = when {
            name.isBlank() -> "session_name_required"
            name.length < 3 -> "session_name_too_short"
            name.length > 120 -> "session_name_too_long"
            else -> null
        }
        if (validationError != null) {
            _uiState.update { it.copy(nameValidationError = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            when (val result = createSessionUseCase(name, current.newSessionDescription.ifBlank { null }, current.newSessionEnvironment)) {
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isCreating = false, showCreateDialog = false) }
                    loadSessions(reset = true)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isCreating = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isCreating = false, error = "Failed to create session. Check your connection.")
                }
            }
        }
    }

    private fun deleteSession() {
        val sessionId = _uiState.value.sessionToDelete?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, sessionToDelete = null) }
            when (val result = deleteSessionUseCase(sessionId)) {
                is ApiResult.Success -> {
                    _uiState.update { state ->
                        state.copy(isDeleting = false, sessions = state.sessions.filter { it.id != sessionId })
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isDeleting = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isDeleting = false, error = "Failed to delete session. Check your connection.")
                }
            }
        }
    }
}
