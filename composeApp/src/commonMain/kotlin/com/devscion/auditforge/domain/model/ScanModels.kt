package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ScanStatus {
    @SerialName("queued") Queued,
    @SerialName("running") Running,
    @SerialName("completed") Completed,
    @SerialName("failed") Failed,
    @SerialName("cancelled") Cancelled,
}

@Serializable
data class Scan(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val status: ScanStatus,
    @SerialName("started_at") val startedAt: String? = null,
    @SerialName("completed_at") val completedAt: String? = null,
    @SerialName("progress_percent") val progressPercent: Int = 0,
    @SerialName("current_step") val currentStep: String? = null,
    @SerialName("findings_so_far") val findingsSoFar: Int = 0,
    @SerialName("policy_pack_ids") val policyPackIds: List<String> = emptyList(),
    @SerialName("connectors_used") val connectorsUsed: List<String> = emptyList(),
    @SerialName("estimated_duration_seconds") val estimatedDurationSeconds: Int? = null,
    @SerialName("cancelled_at") val cancelledAt: String? = null,
)

@Serializable
data class TriggerScanRequest(
    @SerialName("policy_pack_ids") val policyPackIds: List<String>,
    @SerialName("additional_rule_ids") val additionalRuleIds: List<String> = emptyList(),
    val config: ScanConfig = ScanConfig(),
)

@Serializable
data class ScanConfig(
    @SerialName("enable_ai_analysis") val enableAiAnalysis: Boolean = true,
    @SerialName("max_findings_per_rule") val maxFindingsPerRule: Int = 100,
)

// SSE wire payloads (internal to network layer)
@Serializable
data class SseProgressData(
    @SerialName("progress_percent") val progressPercent: Int,
    @SerialName("current_step") val currentStep: String,
    @SerialName("findings_so_far") val findingsSoFar: Int,
)

@Serializable
data class SseFindingData(
    val id: String,
    val severity: String,
    @SerialName("rule_id") val ruleId: String,
    val title: String,
)

@Serializable
data class SseCompletedData(
    @SerialName("scan_id") val scanId: String,
    @SerialName("total_findings") val totalFindings: Int,
    @SerialName("overall_score") val overallScore: Int,
)

@Serializable
data class SseErrorData(
    val code: String,
    val message: String,
)

sealed class ScanProgressEvent {
    data class Progress(
        val progressPercent: Int,
        val currentStep: String,
        val findingsSoFar: Int,
    ) : ScanProgressEvent()

    data class Finding(
        val id: String,
        val severity: RuleSeverity,
        val ruleId: String,
        val title: String,
    ) : ScanProgressEvent()

    data class Completed(
        val scanId: String,
        val totalFindings: Int,
        val overallScore: Int,
    ) : ScanProgressEvent()

    data class ScanError(
        val code: String,
        val message: String,
    ) : ScanProgressEvent()
}

data class LiveFinding(
    val id: String,
    val severity: RuleSeverity,
    val title: String,
    val ruleId: String,
)
