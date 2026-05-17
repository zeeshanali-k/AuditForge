package com.devscion.auditforge.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ReportStatus {
    @SerialName("generating") Generating,
    @SerialName("ready") Ready,
    @SerialName("failed") Failed,
}

@Serializable
enum class ReportFormat {
    @SerialName("pdf") Pdf,
    @SerialName("json") Json,
    @SerialName("csv") Csv,
    @SerialName("html") Html,
}

@Serializable
data class Report(
    val id: String,
    @SerialName("session_id") val sessionId: String,
    val status: ReportStatus,
    val format: ReportFormat,
    @SerialName("file_size_bytes") val fileSizeBytes: Long? = null,
    @SerialName("generated_at") val generatedAt: String? = null,
    @SerialName("finding_count") val findingCount: Int? = null,
    @SerialName("overall_score") val overallScore: Int? = null,
    val signed: Boolean = false,
    @SerialName("signature_sha256") val signatureSha256: String? = null,
    @SerialName("download_url") val downloadUrl: String? = null,
    @SerialName("compliance_framework") val complianceFramework: String? = null,
    @SerialName("requested_at") val requestedAt: String? = null,
)

@Serializable
data class ReportListResponse(
    val data: List<Report>,
)

@Serializable
data class GenerateReportRequest(
    val format: ReportFormat = ReportFormat.Pdf,
    @SerialName("include_resolved") val includeResolved: Boolean = false,
    @SerialName("include_remediation") val includeRemediation: Boolean = true,
    @SerialName("compliance_framework") val complianceFramework: String? = null,
    val sign: Boolean = true,
)
