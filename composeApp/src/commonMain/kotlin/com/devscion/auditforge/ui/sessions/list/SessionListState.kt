package com.devscion.auditforge.ui.sessions.list

import com.devscion.auditforge.domain.model.Pagination
import com.devscion.auditforge.domain.model.SessionStatus
import com.devscion.auditforge.domain.model.SessionSummary
import com.devscion.auditforge.domain.model.TargetEnvironment

data class SessionListUiState(
    val sessions: List<SessionSummary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val pagination: Pagination? = null,
    val statusFilter: SessionStatus? = null,
    val searchQuery: String = "",
    val showCreateDialog: Boolean = false,
    val newSessionName: String = "",
    val newSessionDescription: String = "",
    val newSessionEnvironment: TargetEnvironment? = null,
    val nameValidationError: String? = null,
    val isCreating: Boolean = false,
    val sessionToDelete: SessionSummary? = null,
    val isDeleting: Boolean = false,
) {
    val filteredSessions: List<SessionSummary>
        get() = if (searchQuery.isBlank()) sessions
        else sessions.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val hasNextPage: Boolean
        get() = pagination != null && pagination.page < pagination.totalPages
}

sealed class SessionListIntent {
    data object Load : SessionListIntent()
    data class UpdateSearch(val query: String) : SessionListIntent()
    data class UpdateStatusFilter(val status: SessionStatus?) : SessionListIntent()
    data object LoadNextPage : SessionListIntent()
    data object ShowCreateDialog : SessionListIntent()
    data object HideCreateDialog : SessionListIntent()
    data class UpdateNewName(val value: String) : SessionListIntent()
    data class UpdateNewDescription(val value: String) : SessionListIntent()
    data class UpdateNewEnvironment(val value: TargetEnvironment?) : SessionListIntent()
    data object SubmitCreate : SessionListIntent()
    data class RequestDelete(val session: SessionSummary) : SessionListIntent()
    data object ConfirmDelete : SessionListIntent()
    data object CancelDelete : SessionListIntent()
    data object DismissError : SessionListIntent()
}
