package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class FindingStatus {
    @SerialName("open") Open,
    @SerialName("acknowledged") Acknowledged,
    @SerialName("false_positive") FalsePositive,
    @SerialName("resolved") Resolved,
}

@Serializable
data class FindingEvidence(
    @SerialName("source_type") val sourceType: String,
    @SerialName("source_path") val sourcePath: String,
    @SerialName("line_number") val lineNumber: Int? = null,
    val snippet: String? = null,
    val context: String? = null,
)

@Serializable
data class FindingRemediation(
    val summary: String,
    @SerialName("code_example") val codeExample: String? = null,
    val references: List<String> = emptyList(),
)

@Serializable
data class Finding(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    @SerialName("scan_id") val scanId: String,
    @SerialName("rule_id") val ruleId: String,
    @SerialName("rule_name") val ruleName: String,
    val severity: RuleSeverity,
    val category: String,
    val title: String,
    val description: String,
    val evidence: FindingEvidence,
    val remediation: FindingRemediation,
    @SerialName("compliance_refs") val complianceRefs: List<String> = emptyList(),
    val status: FindingStatus,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String? = null,
    val comment: String? = null,
)

@Serializable
data class FindingListResponse(
    val data: List<Finding>,
    val pagination: PaginationMeta? = null,
)

@Serializable
data class PaginationMeta(
    val page: Int,
    @SerialName("page_size") val pageSize: Int,
    @SerialName("total_items") val totalItems: Int,
    @SerialName("total_pages") val totalPages: Int,
)

@Serializable
data class SeverityBreakdown(
    val critical: Int = 0,
    val high: Int = 0,
    val medium: Int = 0,
    val low: Int = 0,
)

@Serializable
data class StatusBreakdown(
    val open: Int = 0,
    val acknowledged: Int = 0,
    @SerialName("false_positive") val falsePositive: Int = 0,
    val resolved: Int = 0,
)

@Serializable
data class ScoreBreakdown(
    @SerialName("rules_passed") val rulesPassed: Int,
    @SerialName("rules_failed") val rulesFailed: Int,
    @SerialName("rules_total") val rulesTotal: Int,
)

@Serializable
data class FindingsSummary(
    @SerialName("session_id") val sessionId: String,
    @SerialName("scan_id") val scanId: String? = null,
    @SerialName("total_findings") val totalFindings: Int,
    @SerialName("by_severity") val bySeverity: SeverityBreakdown,
    @SerialName("by_status") val byStatus: StatusBreakdown,
    @SerialName("by_category") val byCategory: Map<String, Int> = emptyMap(),
    @SerialName("overall_score") val overallScore: Int,
    @SerialName("score_breakdown") val scoreBreakdown: ScoreBreakdown? = null,
    @SerialName("policy_coverage") val policyCoverage: List<PolicyCoverage> = emptyList(),
)

@Serializable
data class PolicyCoverage(
    @SerialName("pack_id") val packId: String,
    @SerialName("rules_evaluated") val rulesEvaluated: Int,
    @SerialName("rules_passed") val rulesPassed: Int,
    @SerialName("rules_failed") val rulesFailed: Int,
)

@Serializable
data class UpdateFindingStatusRequest(
    val status: FindingStatus,
    val comment: String? = null,
)
