package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
enum class AuditEventType {
    @SerialName("session_created") SessionCreated,
    @SerialName("upload_added") UploadAdded,
    @SerialName("scan_started") ScanStarted,
    @SerialName("scan_completed") ScanCompleted,
    @SerialName("finding_created") FindingCreated,
    @SerialName("report_generated") ReportGenerated,
    @SerialName("policy_changed") PolicyChanged,
    @SerialName("user_action") UserAction,
}

fun AuditEventType.toApiParam(): String = when (this) {
    AuditEventType.SessionCreated -> "session_created"
    AuditEventType.UploadAdded -> "upload_added"
    AuditEventType.ScanStarted -> "scan_started"
    AuditEventType.ScanCompleted -> "scan_completed"
    AuditEventType.FindingCreated -> "finding_created"
    AuditEventType.ReportGenerated -> "report_generated"
    AuditEventType.PolicyChanged -> "policy_changed"
    AuditEventType.UserAction -> "user_action"
}

@Serializable
enum class AuditActorType {
    @SerialName("user") User,
    @SerialName("system") System,
}

@Serializable
data class AuditActor(
    val type: AuditActorType,
    val id: String,
    val name: String,
)

@Serializable
data class AuditEvent(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val timestamp: String,
    @SerialName("event_type") val eventType: AuditEventType,
    val actor: AuditActor,
    val details: JsonObject? = null,
    @SerialName("previous_hash") val previousHash: String,
    @SerialName("current_hash") val currentHash: String,
)

@Serializable
data class AuditTrailResponse(
    val data: List<AuditEvent>,
    val pagination: PaginationMeta? = null,
)

@Serializable
data class AuditChainVerification(
    @SerialName("session_id") val sessionId: String,
    @SerialName("total_events") val totalEvents: Int,
    @SerialName("chain_intact") val chainIntact: Boolean,
    @SerialName("verified_at") val verifiedAt: String,
    @SerialName("first_broken_index") val firstBrokenIndex: Int? = null,
)
