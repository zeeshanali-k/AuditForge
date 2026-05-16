package com.devscion.auditforge.ui.sessions.detail.reports

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.Report
import com.devscion.auditforge.domain.model.ReportFormat
import com.devscion.auditforge.domain.model.ReportStatus
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ReportsTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: ReportsViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReportsHeader(
                onGenerate = { viewModel.onIntent(ReportsIntent.ShowGenerateModal) },
                colors = colors,
            )
            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

            when {
                uiState.isLoading && uiState.reports.isEmpty() -> ReportsLoadingSkeleton(colors)
                uiState.reports.isEmpty() -> ReportsEmptyState(
                    onGenerate = { viewModel.onIntent(ReportsIntent.ShowGenerateModal) },
                    colors = colors,
                )
                else -> ReportsList(
                    reports = uiState.reports,
                    downloadingReportId = uiState.downloadingReportId,
                    onDownload = { viewModel.onIntent(ReportsIntent.Download(it)) },
                    colors = colors,
                )
            }
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(ReportsIntent.DismissError) }) {
                        Text(stringResource(Res.string.a11y_close), color = colors.textOnAccent)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(Spacing.lg),
                containerColor = colors.severityCritical,
            ) {
                Text(uiState.error!!, color = colors.textOnAccent)
            }
        }
    }

    if (uiState.showGenerateModal) {
        GenerateReportModal(
            uiState = uiState,
            onIntent = viewModel::onIntent,
            colors = colors,
        )
    }
}

@Composable
private fun ReportsHeader(onGenerate: () -> Unit, colors: AuditForgeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.reports_page_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
        )
        Button(
            onClick = onGenerate,
            shape = RoundedCornerShape(Shape.button),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentDefault,
                contentColor = colors.textOnAccent,
            ),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = stringResource(Res.string.reports_generate_button),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ReportsList(
    reports: List<Report>,
    downloadingReportId: String?,
    onDownload: (Report) -> Unit,
    colors: AuditForgeColors,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl)
            .padding(top = Spacing.sm, bottom = Spacing.xl),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                ReportsTableHeader(colors = colors)
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
            items(reports, key = { it.id }) { report ->
                ReportRow(
                    report = report,
                    isDownloading = downloadingReportId == report.id,
                    onDownload = { onDownload(report) },
                    colors = colors,
                )
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun ReportsTableHeader(colors: AuditForgeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceSunken)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell(stringResource(Res.string.reports_col_format), Modifier.width(96.dp), colors)
        TableHeaderCell(stringResource(Res.string.reports_col_generated), Modifier.weight(1f), colors)
        TableHeaderCell(stringResource(Res.string.reports_col_findings), Modifier.width(80.dp), colors)
        TableHeaderCell(stringResource(Res.string.reports_col_score), Modifier.width(70.dp), colors)
        TableHeaderCell(stringResource(Res.string.reports_col_status), Modifier.width(110.dp), colors)
        Spacer(Modifier.width(120.dp))
    }
}

@Composable
private fun TableHeaderCell(text: String, modifier: Modifier, colors: AuditForgeColors) {
    Box(modifier = modifier) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun ReportRow(
    report: Report,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.width(96.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                imageVector = formatIcon(report.format),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = report.format.name.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            val dateStr = report.generatedAt?.take(10) ?: report.requestedAt?.take(10) ?: "—"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Monospace,
                )
                if (report.signed) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = stringResource(Res.string.reports_signed),
                        tint = colors.severityLow,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.width(80.dp)) {
            Text(
                text = report.findingCount?.toString() ?: "—",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }

        Box(modifier = Modifier.width(70.dp)) {
            if (report.overallScore != null) {
                val scoreColor = when {
                    report.overallScore >= 80 -> colors.severityResolved
                    report.overallScore >= 50 -> colors.severityMedium
                    else -> colors.severityCritical
                }
                Text(
                    text = "${report.overallScore}",
                    style = MaterialTheme.typography.bodySmall,
                    color = scoreColor,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                Text("—", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
            }
        }

        Box(modifier = Modifier.width(110.dp)) {
            ReportStatusBadge(status = report.status, colors = colors)
        }

        Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.CenterEnd) {
            if (report.status == ReportStatus.Ready) {
                OutlinedButton(
                    onClick = onDownload,
                    enabled = !isDownloading,
                    shape = RoundedCornerShape(Shape.button),
                    border = BorderStroke(1.dp, colors.accentDefault),
                    contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                    modifier = Modifier.height(30.dp),
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = colors.accentDefault,
                            strokeWidth = 1.5.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = colors.accentDefault,
                            modifier = Modifier.size(12.dp),
                        )
                        Spacer(Modifier.width(Spacing.xs))
                        Text(
                            text = stringResource(Res.string.reports_download_action),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accentDefault,
                        )
                    }
                }
            } else if (report.status == ReportStatus.Generating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = colors.accentDefault,
                        strokeWidth = 1.5.dp,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportStatusBadge(status: ReportStatus, colors: AuditForgeColors) {
    val (label, containerColor, contentColor) = when (status) {
        ReportStatus.Generating -> Triple(
            stringResource(Res.string.reports_status_generating),
            colors.accentDefault.copy(alpha = 0.12f),
            colors.accentDefault,
        )
        ReportStatus.Ready -> Triple(
            stringResource(Res.string.reports_status_ready),
            colors.severityResolved.copy(alpha = 0.12f),
            colors.severityResolved,
        )
        ReportStatus.Failed -> Triple(
            stringResource(Res.string.reports_status_failed),
            colors.severityCritical.copy(alpha = 0.12f),
            colors.severityCritical,
        )
    }
    Surface(shape = RoundedCornerShape(Shape.badge), color = containerColor) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

@Composable
private fun ReportsEmptyState(onGenerate: () -> Unit, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier.widthIn(max = 480.dp)
                .padding(Spacing.xxxl),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(colors.accentDefault.copy(alpha = 0.1f), RoundedCornerShape(Shape.card)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = colors.accentDefault,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.reports_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(Res.string.reports_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
            OutlinedButton(
                onClick = onGenerate,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentDefault),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.reports_generate_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun ReportsLoadingSkeleton(colors: AuditForgeColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(Shape.card),
            color = colors.surfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
        ) {
            Column {
                Box(
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                        .background(colors.surfaceSunken)
                )
                HorizontalDivider(color = colors.borderDefault)
                repeat(5) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(96.dp).height(12.dp).background(colors.borderDefault, RoundedCornerShape(4.dp)))
                        Box(Modifier.weight(1f).height(12.dp).background(colors.borderDefault.copy(alpha = 0.7f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(80.dp).height(12.dp).background(colors.borderDefault.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(70.dp).height(12.dp).background(colors.borderDefault.copy(alpha = 0.4f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(80.dp).height(16.dp).background(colors.borderDefault.copy(alpha = 0.5f), RoundedCornerShape(Shape.badge)))
                        Box(Modifier.width(80.dp).height(24.dp).background(colors.borderDefault.copy(alpha = 0.3f), RoundedCornerShape(Shape.button)))
                    }
                    HorizontalDivider(color = colors.borderDefault)
                }
            }
        }
    }
}

@Composable
private fun GenerateReportModal(
    uiState: ReportsUiState,
    onIntent: (ReportsIntent) -> Unit,
    colors: AuditForgeColors,
) {
    Dialog(
        onDismissRequest = {
            if (!uiState.isGenerating) onIntent(ReportsIntent.DismissGenerateModal)
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.width(480.dp),
            shape = RoundedCornerShape(Shape.modal),
            color = colors.surfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
        ) {
            Column(modifier = Modifier.padding(Spacing.xl), verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                Text(
                    text = stringResource(Res.string.generate_report_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )

                HorizontalDivider(color = colors.borderDefault)

                FormatSelector(
                    selected = uiState.selectedFormat,
                    onSelect = { onIntent(ReportsIntent.SetFormat(it)) },
                    enabled = !uiState.isGenerating,
                    colors = colors,
                )

                FrameworkDropdown(
                    selected = uiState.selectedFramework,
                    onSelect = { onIntent(ReportsIntent.SetFramework(it)) },
                    enabled = !uiState.isGenerating,
                    colors = colors,
                )

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ToggleRow(
                        label = stringResource(Res.string.generate_report_include_resolved),
                        checked = uiState.includeResolved,
                        onToggle = { onIntent(ReportsIntent.SetIncludeResolved(it)) },
                        enabled = !uiState.isGenerating,
                        colors = colors,
                    )
                    ToggleRow(
                        label = stringResource(Res.string.generate_report_include_remediation),
                        checked = uiState.includeRemediation,
                        onToggle = { onIntent(ReportsIntent.SetIncludeRemediation(it)) },
                        enabled = !uiState.isGenerating,
                        colors = colors,
                    )
                    ToggleRow(
                        label = stringResource(Res.string.generate_report_sign),
                        checked = uiState.signReport,
                        onToggle = { onIntent(ReportsIntent.SetSignReport(it)) },
                        enabled = !uiState.isGenerating,
                        colors = colors,
                    )
                }

                HorizontalDivider(color = colors.borderDefault)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = { onIntent(ReportsIntent.DismissGenerateModal) },
                        enabled = !uiState.isGenerating,
                    ) {
                        Text(
                            text = stringResource(Res.string.generate_report_cancel),
                            color = if (uiState.isGenerating) colors.textTertiary else colors.textSecondary,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }

                    when {
                        uiState.generatedReport != null -> Button(
                            onClick = { onIntent(ReportsIntent.Download(uiState.generatedReport)) },
                            enabled = uiState.downloadingReportId == null,
                            shape = RoundedCornerShape(Shape.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.severityResolved,
                                contentColor = colors.textOnAccent,
                            ),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                        ) {
                            if (uiState.downloadingReportId == uiState.generatedReport.id) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = colors.textOnAccent,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(Modifier.width(Spacing.sm))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(Spacing.sm))
                            }
                            Text(
                                text = stringResource(Res.string.generate_report_download),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        uiState.isGenerating -> Button(
                            onClick = {},
                            enabled = false,
                            shape = RoundedCornerShape(Shape.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentDefault,
                                contentColor = colors.textOnAccent,
                                disabledContainerColor = colors.accentDefault.copy(alpha = 0.6f),
                                disabledContentColor = colors.textOnAccent,
                            ),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = colors.textOnAccent,
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(Spacing.sm))
                            Text(
                                text = stringResource(Res.string.generate_report_generating),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }

                        else -> Button(
                            onClick = { onIntent(ReportsIntent.Generate) },
                            shape = RoundedCornerShape(Shape.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentDefault,
                                contentColor = colors.textOnAccent,
                            ),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                        ) {
                            Text(
                                text = stringResource(Res.string.generate_report_submit),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatSelector(
    selected: ReportFormat,
    onSelect: (ReportFormat) -> Unit,
    enabled: Boolean,
    colors: AuditForgeColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(Res.string.generate_report_format_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ReportFormat.entries.forEachIndexed { index, format ->
                SegmentedButton(
                    selected = selected == format,
                    onClick = { if (enabled) onSelect(format) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ReportFormat.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colors.accentDefault.copy(alpha = 0.12f),
                        activeContentColor = colors.accentDefault,
                        activeBorderColor = colors.accentDefault,
                        inactiveContainerColor = colors.surfacePrimary,
                        inactiveContentColor = colors.textSecondary,
                        inactiveBorderColor = colors.borderDefault,
                    ),
                ) {
                    Text(
                        text = format.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameworkDropdown(
    selected: String?,
    onSelect: (String?) -> Unit,
    enabled: Boolean,
    colors: AuditForgeColors,
) {
    var expanded by remember { mutableStateOf(false) }
    val frameworks = listOf(null, "HIPAA", "SOC2", "OWASP")
    val labels = mapOf(
        null to "All frameworks",
        "HIPAA" to "HIPAA",
        "SOC2" to "SOC 2",
        "OWASP" to "OWASP API Top 10",
    )
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(Res.string.generate_report_framework_label),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        Box {
            OutlinedButton(
                onClick = { if (enabled) expanded = true },
                shape = RoundedCornerShape(Shape.button),
                border = BorderStroke(1.dp, if (selected != null) colors.accentDefault else colors.borderDefault),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
            ) {
                Text(
                    text = labels[selected] ?: "All frameworks",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected != null) colors.accentDefault else colors.textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                frameworks.forEach { fw ->
                    DropdownMenuItem(
                        text = { Text(labels[fw] ?: "All frameworks", style = MaterialTheme.typography.bodySmall) },
                        onClick = { onSelect(fw); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    enabled: Boolean,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) colors.textPrimary else colors.textTertiary,
        )
        Switch(
            checked = checked,
            onCheckedChange = { if (enabled) onToggle(it) },
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.textOnAccent,
                checkedTrackColor = colors.accentDefault,
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.borderDefault,
            ),
        )
    }
}

private fun formatIcon(format: ReportFormat) = when (format) {
    ReportFormat.Pdf -> Icons.Default.PictureAsPdf
    ReportFormat.Json -> Icons.Default.DataObject
    ReportFormat.Csv -> Icons.Default.TableChart
    ReportFormat.Html -> Icons.Default.Code
}
