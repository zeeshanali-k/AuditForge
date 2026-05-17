package com.devscion.auditforge.ui.sessions.detail.reports

import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportFormat

data class ReportsUiState(
    val isLoading: Boolean = false,
    val reports: List<Report> = emptyList(),
    val showGenerateModal: Boolean = false,
    val selectedFormat: ReportFormat = ReportFormat.Pdf,
    val selectedFramework: String? = null,
    val includeResolved: Boolean = false,
    val includeRemediation: Boolean = true,
    val signReport: Boolean = true,
    val isGenerating: Boolean = false,
    val generatedReport: Report? = null,
    val downloadingReportId: String? = null,
    val error: String? = null,
)

sealed class ReportsIntent {
    data object Load : ReportsIntent()
    data object ShowGenerateModal : ReportsIntent()
    data object DismissGenerateModal : ReportsIntent()
    data class SetFormat(val format: ReportFormat) : ReportsIntent()
    data class SetFramework(val framework: String?) : ReportsIntent()
    data class SetIncludeResolved(val value: Boolean) : ReportsIntent()
    data class SetIncludeRemediation(val value: Boolean) : ReportsIntent()
    data class SetSignReport(val value: Boolean) : ReportsIntent()
    data object Generate : ReportsIntent()
    data class Download(val report: Report) : ReportsIntent()
    data object DismissError : ReportsIntent()
}
