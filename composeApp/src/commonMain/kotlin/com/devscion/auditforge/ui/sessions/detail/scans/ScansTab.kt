package com.devscion.auditforge.ui.sessions.detail.scans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.LiveFinding
import com.devscion.auditforge.domain.model.PolicyPackSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.domain.model.ScanStatus
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import com.devscion.auditforge.ui.theme.auditForgeColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScansTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: ScansViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.currentScan == null -> ScansLoadingSkeleton(colors)
            uiState.isScanning -> LiveScanView(
                uiState = uiState,
                onCancel = { viewModel.onIntent(ScansIntent.CancelScan) },
                colors = colors,
            )
            uiState.isCompleted -> ScanCompletedView(
                uiState = uiState,
                onDismiss = { viewModel.onIntent(ScansIntent.DismissCompleted) },
                onRunAgain = {
                    viewModel.onIntent(ScansIntent.DismissCompleted)
                },
                colors = colors,
            )
            else -> ReadyToScanView(
                uiState = uiState,
                onTogglePack = { viewModel.onIntent(ScansIntent.TogglePackSelection(it)) },
                onRunScan = { viewModel.onIntent(ScansIntent.TriggerScan) },
                colors = colors,
            )
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(ScansIntent.DismissError) }) {
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
}

@Composable
private fun ReadyToScanView(
    uiState: ScansUiState,
    onTogglePack: (String) -> Unit,
    onRunScan: () -> Unit,
    colors: AuditForgeColors,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        contentPadding = PaddingValues(bottom = Spacing.xl),
    ) {
        item {
            Text(
                text = stringResource(Res.string.scan_select_packs_title),
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = stringResource(Res.string.scan_select_packs_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }

        if (uiState.availablePacks.isEmpty() && !uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Policy,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(40.dp),
                        )
                        Text(
                            text = stringResource(Res.string.scan_no_packs_available),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                        )
                    }
                }
            }
        } else {
            items(uiState.availablePacks, key = { it.id }) { pack ->
                PackSelectionRow(
                    pack = pack,
                    isSelected = pack.id in uiState.selectedPackIds,
                    onToggle = { onTogglePack(pack.id) },
                    colors = colors,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.md))
            Button(
                onClick = onRunScan,
                enabled = uiState.canStartScan,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentDefault,
                    contentColor = colors.textOnAccent,
                    disabledContainerColor = colors.borderDefault,
                    disabledContentColor = colors.textTertiary,
                ),
                contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.scan_run_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            if (!uiState.canStartScan && uiState.availablePacks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = stringResource(Res.string.scan_run_hint_select_packs),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun PackSelectionRow(
    pack: PolicyPackSummary,
    isSelected: Boolean,
    onToggle: () -> Unit,
    colors: AuditForgeColors,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Shape.card),
        color = if (isSelected) colors.accentDefault.copy(alpha = 0.06f) else colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) colors.accentDefault.copy(alpha = 0.4f) else colors.borderDefault,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(18.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.accentDefault,
                    uncheckedColor = colors.borderStrong,
                    checkmarkColor = colors.textOnAccent,
                ),
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${pack.framework.name} · ${pack.ruleCount} ${stringResource(Res.string.policies_rule_count)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun LiveScanView(
    uiState: ScansUiState,
    onCancel: () -> Unit,
    colors: AuditForgeColors,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val statusText = if (uiState.currentScan?.status == ScanStatus.Queued)
                    stringResource(Res.string.scan_status_queued)
                else
                    stringResource(Res.string.scan_status_running)
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accentDefault,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(Res.string.scan_in_progress_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
            Button(
                onClick = onCancel,
                enabled = !uiState.isCancelling,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.severityCritical.copy(alpha = 0.12f),
                    contentColor = colors.severityCritical,
                ),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            ) {
                if (uiState.isCancelling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.severityCritical,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.scan_cancel_button),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.currentStep.ifEmpty { stringResource(Res.string.scan_preparing) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${uiState.progressPercent}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accentDefault,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            LinearProgressIndicator(
                progress = { uiState.progressPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = colors.accentDefault,
                trackColor = colors.borderDefault,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            SeverityCounter(
                label = stringResource(Res.string.scan_severity_critical),
                count = uiState.criticalCount,
                color = colors.severityCritical,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            SeverityCounter(
                label = stringResource(Res.string.scan_severity_high),
                count = uiState.highCount,
                color = colors.severityHigh,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            SeverityCounter(
                label = stringResource(Res.string.scan_severity_medium),
                count = uiState.mediumCount,
                color = colors.severityMedium,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            SeverityCounter(
                label = stringResource(Res.string.scan_severity_low),
                count = uiState.lowCount,
                color = colors.severityLow,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
        }

        if (uiState.liveFindings.isNotEmpty()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.scan_live_findings_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = Spacing.sm),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    items(uiState.liveFindings, key = { it.id }) { finding ->
                        LiveFindingRow(finding = finding, colors = colors)
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityCounter(
    label: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    colors: AuditForgeColors,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(Shape.card),
        color = color.copy(alpha = 0.08f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.2f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.md, horizontal = Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun LiveFindingRow(finding: LiveFinding, colors: AuditForgeColors) {
    val severityColor = severityColor(finding.severity, colors)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceElevated, RoundedCornerShape(Shape.card))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(severityColor, CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = finding.title,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = finding.ruleId,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(
            shape = RoundedCornerShape(Shape.badge),
            color = severityColor.copy(alpha = 0.12f),
        ) {
            Text(
                text = finding.severity.name,
                style = MaterialTheme.typography.labelSmall,
                color = severityColor,
                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun ScanCompletedView(
    uiState: ScansUiState,
    onDismiss: () -> Unit,
    onRunAgain: () -> Unit,
    colors: AuditForgeColors,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier.widthIn(max = 440.dp).padding(Spacing.xl),
        ) {
            Surface(
                shape = CircleShape,
                color = colors.statusSuccess.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.statusSuccess,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
            Text(
                text = stringResource(Res.string.scan_complete_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                uiState.completedTotalFindings?.let { count ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(Res.string.scan_complete_findings_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                }
                uiState.completedOverallScore?.let { score ->
                    val scoreColor = when {
                        score >= 80 -> colors.severityLow
                        score >= 50 -> colors.severityMedium
                        else -> colors.severityCritical
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score/100",
                            style = MaterialTheme.typography.headlineMedium,
                            color = scoreColor,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = stringResource(Res.string.scan_complete_score_label),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedButton(
                    onClick = onRunAgain,
                    shape = RoundedCornerShape(Shape.button),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                    contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md),
                ) {
                    Text(
                        text = stringResource(Res.string.scan_run_again_button),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(Shape.button),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentDefault,
                        contentColor = colors.textOnAccent,
                    ),
                    contentPadding = PaddingValues(horizontal = Spacing.xl, vertical = Spacing.md),
                ) {
                    Text(
                        text = stringResource(Res.string.scan_view_findings_button),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun ScansLoadingSkeleton(colors: AuditForgeColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(0.4f).height(16.dp)
                .background(colors.borderDefault, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        repeat(4) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp).background(
                    colors.borderDefault.copy(alpha = 0.4f),
                    RoundedCornerShape(Shape.card),
                )
            )
        }
    }
}

private fun severityColor(
    severity: RuleSeverity,
    colors: AuditForgeColors,
): androidx.compose.ui.graphics.Color = when (severity) {
    RuleSeverity.Critical -> colors.severityCritical
    RuleSeverity.High -> colors.severityHigh
    RuleSeverity.Medium -> colors.severityMedium
    RuleSeverity.Low -> colors.severityLow
}
