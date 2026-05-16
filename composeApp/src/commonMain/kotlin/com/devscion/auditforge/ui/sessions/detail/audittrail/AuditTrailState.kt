package com.devscion.auditforge.ui.sessions.detail.audittrail

import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEvent
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.PaginationMeta

data class AuditTrailUiState(
    val isLoading: Boolean = false,
    val events: List<AuditEvent> = emptyList(),
    val pagination: PaginationMeta? = null,
    val isLoadingMore: Boolean = false,
    val selectedEventType: AuditEventType? = null,
    val expandedEventId: String? = null,
    val isVerifying: Boolean = false,
    val verificationResult: AuditChainVerification? = null,
    val showVerifyModal: Boolean = false,
    val error: String? = null,
)

sealed class AuditTrailIntent {
    data object Load : AuditTrailIntent()
    data object LoadMore : AuditTrailIntent()
    data class SetEventTypeFilter(val eventType: AuditEventType?) : AuditTrailIntent()
    data class ToggleEventExpanded(val eventId: String) : AuditTrailIntent()
    data object VerifyChain : AuditTrailIntent()
    data object DismissVerifyModal : AuditTrailIntent()
    data object DismissError : AuditTrailIntent()
}
