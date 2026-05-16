package com.devscion.auditforge.ui.sessions.detail.findings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun FindingDetailDialog(
    finding: Finding,
    pendingStatus: FindingStatus,
    pendingComment: String,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onSetStatus: (FindingStatus) -> Unit,
    onSetComment: (String) -> Unit,
    onConfirm: () -> Unit,
    colors: AuditForgeColors,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(Shape.modal),
            color = colors.surfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FindingDetailHeader(finding = finding, onDismiss = onDismiss, colors = colors)
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                ) {
                    FindingDetailMetaColumn(finding = finding, colors = colors)
                    VerticalDivider(color = colors.borderDefault, thickness = 1.dp)
                    FindingDetailContentColumn(finding = finding, colors = colors)
                }

                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
                FindingActionBar(
                    finding = finding,
                    pendingStatus = pendingStatus,
                    pendingComment = pendingComment,
                    isUpdating = isUpdating,
                    onSetStatus = onSetStatus,
                    onSetComment = onSetComment,
                    onConfirm = onConfirm,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun FindingDetailHeader(finding: Finding, onDismiss: () -> Unit, colors: AuditForgeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfacePrimary)
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        SeverityDot(severity = finding.severity, colors = colors)
        Text(
            text = finding.title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        FindingStatusBadge(status = finding.status, colors = colors)
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.a11y_close),
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun FindingDetailMetaColumn(finding: Finding, colors: AuditForgeColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.38f)
            .fillMaxHeight()
            .background(colors.surfaceSecondary)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        MetaSection(label = stringResource(Res.string.finding_detail_rule), colors = colors) {
            Text(
                text = finding.ruleName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = finding.ruleId,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontFamily = FontFamily.Monospace,
            )
        }

        MetaSection(label = stringResource(Res.string.finding_detail_severity), colors = colors) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                SeverityDot(severity = finding.severity, colors = colors)
                Text(
                    text = severityLabel(finding.severity),
                    style = MaterialTheme.typography.bodySmall,
                    color = severityColor(finding.severity, colors),
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        MetaSection(label = stringResource(Res.string.finding_detail_category), colors = colors) {
            Text(
                text = finding.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
        }

        if (finding.complianceRefs.isNotEmpty()) {
            MetaSection(label = stringResource(Res.string.finding_detail_compliance_refs), colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    finding.complianceRefs.forEach { ref ->
                        Surface(
                            shape = RoundedCornerShape(Shape.badge),
                            color = colors.accentDefault.copy(alpha = 0.08f),
                        ) {
                            Text(
                                text = ref,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentDefault,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                            )
                        }
                    }
                }
            }
        }

        MetaSection(label = stringResource(Res.string.finding_detail_source), colors = colors) {
            Text(
                text = finding.evidence.sourcePath +
                    (finding.evidence.lineNumber?.let { ":$it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }

        MetaSection(label = stringResource(Res.string.finding_detail_detected), colors = colors) {
            Text(
                text = finding.createdAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }

        if (!finding.comment.isNullOrBlank()) {
            MetaSection(label = stringResource(Res.string.finding_detail_comment), colors = colors) {
                Text(
                    text = finding.comment,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun FindingDetailContentColumn(finding: Finding, colors: AuditForgeColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        if (!finding.evidence.snippet.isNullOrBlank()) {
            ContentSection(label = stringResource(Res.string.finding_detail_evidence), colors = colors) {
                CodeBlock(
                    code = finding.evidence.snippet,
                    highlightLine = finding.evidence.lineNumber,
                    colors = colors,
                )
                if (!finding.evidence.context.isNullOrBlank()) {
                    Text(
                        text = finding.evidence.context,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
            }
        }

        ContentSection(label = stringResource(Res.string.finding_detail_description), colors = colors) {
            Text(
                text = finding.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }

        ContentSection(label = stringResource(Res.string.finding_detail_remediation), colors = colors) {
            Text(
                text = finding.remediation.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
            if (!finding.remediation.codeExample.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                CodeBlock(code = finding.remediation.codeExample, colors = colors)
            }
        }

        if (finding.remediation.references.isNotEmpty()) {
            ContentSection(label = stringResource(Res.string.finding_detail_references), colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    finding.remediation.references.forEach { ref ->
                        Text(
                            text = ref,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accentDefault,
                            fontFamily = FontFamily.Monospace,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FindingActionBar(
    finding: Finding,
    pendingStatus: FindingStatus,
    pendingComment: String,
    isUpdating: Boolean,
    onSetStatus: (FindingStatus) -> Unit,
    onSetComment: (String) -> Unit,
    onConfirm: () -> Unit,
    colors: AuditForgeColors,
) {
    val statusChanged = pendingStatus != finding.status
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfacePrimary)
            .padding(horizontal = Spacing.xl, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        StatusDropdown(
            selected = pendingStatus,
            onSelect = onSetStatus,
            colors = colors,
        )
        if (statusChanged) {
            OutlinedTextField(
                value = pendingComment,
                onValueChange = onSetComment,
                placeholder = {
                    Text(
                        stringResource(Res.string.finding_detail_comment_placeholder),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                },
                modifier = Modifier.weight(1f).height(48.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
                singleLine = true,
                shape = RoundedCornerShape(Shape.input),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.borderFocus,
                    unfocusedBorderColor = colors.borderDefault,
                    cursorColor = colors.accentDefault,
                ),
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        Button(
            onClick = onConfirm,
            enabled = statusChanged && !isUpdating,
            shape = RoundedCornerShape(Shape.button),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentDefault,
                contentColor = colors.textOnAccent,
                disabledContainerColor = colors.borderDefault,
                disabledContentColor = colors.textTertiary,
            ),
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = colors.textOnAccent,
                    strokeWidth = 2.dp,
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }
            Text(
                text = stringResource(Res.string.finding_detail_update_status),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun StatusDropdown(
    selected: FindingStatus,
    onSelect: (FindingStatus) -> Unit,
    colors: AuditForgeColors,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(Shape.button),
            border = BorderStroke(1.dp, colors.borderDefault),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
        ) {
            Text(
                text = findingStatusLabel(selected),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            FindingStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = findingStatusLabel(status),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    onClick = {
                        onSelect(status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun MetaSection(label: String, colors: AuditForgeColors, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.Medium,
        )
        content()
    }
}

@Composable
private fun ContentSection(label: String, colors: AuditForgeColors, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium,
        )
        content()
    }
}

@Composable
private fun CodeBlock(code: String, highlightLine: Int? = null, colors: AuditForgeColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceSunken,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Box(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(Spacing.md),
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = colors.textPrimary,
            )
        }
    }
}

@Composable
fun SeverityDot(severity: RuleSeverity, colors: AuditForgeColors) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(RoundedCornerShape(50))
            .background(severityColor(severity, colors)),
    )
}

@Composable
fun FindingStatusBadge(status: FindingStatus, colors: AuditForgeColors) {
    val (bg, fg) = findingStatusColors(status, colors)
    Surface(shape = RoundedCornerShape(Shape.badge), color = bg) {
        Text(
            text = findingStatusLabel(status),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

fun findingStatusLabel(status: FindingStatus): String = when (status) {
    FindingStatus.Open -> "Open"
    FindingStatus.Acknowledged -> "Acknowledged"
    FindingStatus.FalsePositive -> "False positive"
    FindingStatus.Resolved -> "Resolved"
}

fun severityLabel(severity: RuleSeverity): String = when (severity) {
    RuleSeverity.Critical -> "Critical"
    RuleSeverity.High -> "High"
    RuleSeverity.Medium -> "Medium"
    RuleSeverity.Low -> "Low"
}

fun severityColor(severity: RuleSeverity, colors: AuditForgeColors) = when (severity) {
    RuleSeverity.Critical -> colors.severityCritical
    RuleSeverity.High -> colors.severityHigh
    RuleSeverity.Medium -> colors.severityMedium
    RuleSeverity.Low -> colors.severityLow
}

private fun findingStatusColors(
    status: FindingStatus,
    colors: AuditForgeColors,
): Pair<androidx.compose.ui.graphics.Color, androidx.compose.ui.graphics.Color> = when (status) {
    FindingStatus.Open -> colors.severityCritical.copy(alpha = 0.12f) to colors.severityCritical
    FindingStatus.Acknowledged -> colors.statusWarning.copy(alpha = 0.12f) to colors.statusWarning
    FindingStatus.FalsePositive -> colors.borderDefault to colors.textTertiary
    FindingStatus.Resolved -> colors.severityResolved.copy(alpha = 0.12f) to colors.severityResolved
}
