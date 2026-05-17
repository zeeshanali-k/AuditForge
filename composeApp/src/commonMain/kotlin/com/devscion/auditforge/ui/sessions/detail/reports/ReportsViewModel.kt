package com.devscion.auditforge.ui.sessions.detail.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.ReportDownloader
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.domain.model.GenerateReportRequest
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportFormat
import com.devscion.auditforge.domain.model.ReportStatus
import com.devscion.auditforge.domain.usecase.reports.DownloadReportUseCase
import com.devscion.auditforge.domain.usecase.reports.GenerateReportUseCase
import com.devscion.auditforge.domain.usecase.reports.GetReportUseCase
import com.devscion.auditforge.domain.usecase.reports.ListReportsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ReportsViewModel(
    @InjectedParam private val sessionId: String,
    private val listReportsUseCase: ListReportsUseCase,
    private val generateReportUseCase: GenerateReportUseCase,
    private val getReportUseCase: GetReportUseCase,
    private val downloadReportUseCase: DownloadReportUseCase,
    private val reportDownloader: ReportDownloader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null

    init {
        onIntent(ReportsIntent.Load)
    }

    fun onIntent(intent: ReportsIntent) {
        when (intent) {
            ReportsIntent.Load -> load()
            ReportsIntent.ShowGenerateModal -> _uiState.update {
                it.copy(showGenerateModal = true, generatedReport = null)
            }
            ReportsIntent.DismissGenerateModal -> dismissModal()
            is ReportsIntent.SetFormat -> _uiState.update { it.copy(selectedFormat = intent.format) }
            is ReportsIntent.SetFramework -> _uiState.update { it.copy(selectedFramework = intent.framework) }
            is ReportsIntent.SetIncludeResolved -> _uiState.update { it.copy(includeResolved = intent.value) }
            is ReportsIntent.SetIncludeRemediation -> _uiState.update { it.copy(includeRemediation = intent.value) }
            is ReportsIntent.SetSignReport -> _uiState.update { it.copy(signReport = intent.value) }
            ReportsIntent.Generate -> generate()
            is ReportsIntent.Download -> downloadReport(intent.report)
            ReportsIntent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = listReportsUseCase(sessionId)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isLoading = false, reports = result.data.data)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to load reports. Check your connection.")
                }
            }
        }
    }

    private fun generate() {
        val state = _uiState.value
        val request = GenerateReportRequest(
            format = state.selectedFormat,
            includeResolved = state.includeResolved,
            includeRemediation = state.includeRemediation,
            complianceFramework = state.selectedFramework,
            sign = state.signReport,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            when (val result = generateReportUseCase(sessionId, request)) {
                is ApiResult.Success -> {
                    val pending = result.data
                    _uiState.update { s -> s.copy(reports = listOf(pending) + s.reports) }
                    startPolling(pending)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(isGenerating = false, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(isGenerating = false, error = "Failed to generate report. Check your connection.")
                }
            }
        }
    }

    private fun startPolling(report: Report) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(2_000)
                when (val result = getReportUseCase(sessionId, report.id)) {
                    is ApiResult.Success -> {
                        val updated = result.data
                        _uiState.update { state ->
                            val updatedList = state.reports.map { if (it.id == updated.id) updated else it }
                            when (updated.status) {
                                ReportStatus.Ready -> state.copy(
                                    reports = updatedList,
                                    isGenerating = false,
                                    generatedReport = updated,
                                )
                                ReportStatus.Failed -> state.copy(
                                    reports = updatedList,
                                    isGenerating = false,
                                    error = "Report generation failed",
                                )
                                ReportStatus.Generating -> state.copy(reports = updatedList)
                            }
                        }
                        if (updated.status != ReportStatus.Generating) break
                    }
                    is ApiResult.Error -> {
                        _uiState.update { it.copy(isGenerating = false, error = result.message) }
                        break
                    }
                    ApiResult.Unauthorized, ApiResult.NetworkError -> {
                        _uiState.update { it.copy(isGenerating = false, error = "Network error during report generation.") }
                        break
                    }
                }
            }
        }
    }

    private fun dismissModal() {
        pollingJob?.cancel()
        pollingJob = null
        _uiState.update {
            it.copy(
                showGenerateModal = false,
                generatedReport = null,
                isGenerating = false,
                selectedFormat = ReportFormat.Pdf,
                selectedFramework = null,
                includeResolved = false,
                includeRemediation = true,
                signReport = true,
            )
        }
    }

    private fun downloadReport(report: Report) {
        viewModelScope.launch {
            _uiState.update { it.copy(downloadingReportId = report.id) }
            when (val result = downloadReportUseCase(sessionId, report.id)) {
                is ApiResult.Success -> {
                    val fileName = buildFileName(report)
                    val mimeType = mimeTypeFor(report.format)
                    try {
                        reportDownloader.downloadReport(fileName, result.data, mimeType)
                    } catch (_: Exception) {
                    }
                    _uiState.update { state ->
                        val isModalReport = state.generatedReport?.id == report.id
                        state.copy(
                            downloadingReportId = null,
                            showGenerateModal = if (isModalReport) false else state.showGenerateModal,
                            generatedReport = if (isModalReport) null else state.generatedReport,
                        )
                    }
                    if (!_uiState.value.showGenerateModal) {
                        pollingJob?.cancel()
                        pollingJob = null
                    }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(downloadingReportId = null, error = result.message)
                }
                ApiResult.Unauthorized, ApiResult.NetworkError -> _uiState.update {
                    it.copy(downloadingReportId = null, error = "Failed to download report. Check your connection.")
                }
            }
        }
    }

    private fun buildFileName(report: Report): String {
        val date = report.generatedAt?.take(10) ?: report.requestedAt?.take(10) ?: "report"
        return "audit-report-$date.${report.format.name.lowercase()}"
    }

    private fun mimeTypeFor(format: ReportFormat): String = when (format) {
        ReportFormat.Pdf -> "application/pdf"
        ReportFormat.Json -> "application/json"
        ReportFormat.Csv -> "text/csv"
        ReportFormat.Html -> "text/html"
    }
}
