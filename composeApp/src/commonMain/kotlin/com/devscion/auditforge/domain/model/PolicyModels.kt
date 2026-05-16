package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PolicyFramework {
    @SerialName("OWASP")
    OWASP,
    @SerialName("HIPAA")
    HIPAA,
    @SerialName("SOC2")
    SOC2,
    @SerialName("PCI-DSS")
    PCI_DSS,
    @SerialName("GDPR")
    GDPR,
    @SerialName("custom")
    Custom,
}

@Serializable
enum class RuleSeverity {
    @SerialName("critical")
    Critical,
    @SerialName("high")
    High,
    @SerialName("medium")
    Medium,
    @SerialName("low")
    Low,
}

@Serializable
data class PolicyPackSummary(
    val id: String,
    val name: String,
    val framework: PolicyFramework,
    val version: String,
    val description: String,
    @SerialName("rule_count") val ruleCount: Int,
    @SerialName("is_custom") val isCustom: Boolean,
)

@Serializable
data class PolicyRule(
    val id: String,
    val name: String,
    val description: String,
    val severity: RuleSeverity,
    val category: String,
    @SerialName("check_type") val checkType: String,
    @SerialName("compliance_mapping") val complianceMapping: List<String>,
)

@Serializable
data class PolicyPackDetail(
    val id: String,
    val name: String,
    val framework: PolicyFramework,
    val version: String,
    val description: String,
    val rules: List<PolicyRule> = emptyList(),
)

@Serializable
data class PolicyPackListResponse(
    val data: List<PolicyPackSummary>,
)
