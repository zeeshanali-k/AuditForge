package com.devscion.auditforge.ui.sessions.detail

import com.devscion.auditforge.domain.model.Session

enum class SessionDetailTab { Uploads, Policies, Scans, Findings, Reports, AuditTrail }

data class SessionDetailUiState(
    val session: Session? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: SessionDetailTab = SessionDetailTab.Uploads,
)

sealed class SessionDetailIntent {
    data object Load : SessionDetailIntent()
    data class SelectTab(val tab: SessionDetailTab) : SessionDetailIntent()
    data object DismissError : SessionDetailIntent()
}
