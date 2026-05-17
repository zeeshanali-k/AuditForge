package com.devscion.auditforge.ui.sessions.detail.findings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.Finding
import com.devscion.auditforge.domain.model.FindingStatus
import com.devscion.auditforge.domain.model.FindingsSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import com.devscion.auditforge.ui.theme.auditForgeColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FindingsTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: FindingsViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading && uiState.allFindings.isEmpty() -> FindingsLoadingSkeleton(colors)
            else -> FindingsContent(
                uiState = uiState,
                onIntent = viewModel::onIntent,
                colors = colors,
            )
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(FindingsIntent.DismissError) }) {
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

    uiState.selectedFinding?.let { finding ->
        FindingDetailDialog(
            finding = finding,
            pendingStatus = uiState.pendingStatus ?: finding.status,
            pendingComment = uiState.pendingComment,
            isUpdating = uiState.isUpdatingStatus,
            onDismiss = { viewModel.onIntent(FindingsIntent.DismissFindingDetail) },
            onSetStatus = { viewModel.onIntent(FindingsIntent.SetPendingStatus(it)) },
            onSetComment = { viewModel.onIntent(FindingsIntent.SetPendingComment(it)) },
            onConfirm = { viewModel.onIntent(FindingsIntent.ConfirmStatusUpdate) },
            colors = colors,
        )
    }
}

@Composable
private fun FindingsContent(
    uiState: FindingsUiState,
    onIntent: (FindingsIntent) -> Unit,
    colors: AuditForgeColors,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        uiState.summary?.let { summary ->
            FindingsSummaryWidget(summary = summary, colors = colors)
        }

        FindingsFilterBar(
            uiState = uiState,
            onIntent = onIntent,
            colors = colors,
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.allFindings.isEmpty() -> FindingsEmptyState(
                    hasCompletedScan = false,
                    colors = colors,
                )
                uiState.displayedFindings.isEmpty() -> FindingsEmptyFiltered(
                    onClearFilters = { onIntent(FindingsIntent.ClearFilters) },
                    colors = colors,
                )
                else -> FindingsTable(
                    findings = uiState.displayedFindings,
                    onSelectFinding = { onIntent(FindingsIntent.SelectFinding(it)) },
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun FindingsSummaryWidget(summary: FindingsSummary, colors: AuditForgeColors) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl, vertical = Spacing.md),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.xl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxl),
        ) {
            ScoreDisplay(score = summary.overallScore, colors = colors)
            VerticalDivider(color = colors.borderDefault, thickness = 1.dp, modifier = Modifier.height(56.dp))
            SeverityBreakdownBar(summary = summary, colors = colors, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ScoreDisplay(score: Int, colors: AuditForgeColors) {
    val color = when {
        score >= 80 -> colors.severityResolved
        score >= 50 -> colors.severityMedium
        else -> colors.severityCritical
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(Res.string.findings_score_label),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun SeverityBreakdownBar(summary: FindingsSummary, colors: AuditForgeColors, modifier: Modifier = Modifier) {
    val total = summary.totalFindings.toFloat().coerceAtLeast(1f)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "${summary.totalFindings} ${stringResource(Res.string.findings_total_label)}",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
        ) {
            val critical = summary.bySeverity.critical / total
            val high = summary.bySeverity.high / total
            val medium = summary.bySeverity.medium / total
            val low = summary.bySeverity.low / total
            if (critical > 0) Box(Modifier.fillMaxHeight().weight(critical).background(colors.severityCritical))
            if (high > 0) Box(Modifier.fillMaxHeight().weight(high).background(colors.severityHigh))
            if (medium > 0) Box(Modifier.fillMaxHeight().weight(medium).background(colors.severityMedium))
            if (low > 0) Box(Modifier.fillMaxHeight().weight(low).background(colors.severityLow))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            SeverityChipCount(count = summary.bySeverity.critical, label = stringResource(Res.string.findings_severity_critical), color = colors.severityCritical, colors = colors)
            SeverityChipCount(count = summary.bySeverity.high, label = stringResource(Res.string.findings_severity_high), color = colors.severityHigh, colors = colors)
            SeverityChipCount(count = summary.bySeverity.medium, label = stringResource(Res.string.findings_severity_medium), color = colors.severityMedium, colors = colors)
            SeverityChipCount(count = summary.bySeverity.low, label = stringResource(Res.string.findings_severity_low), color = colors.severityLow, colors = colors)
        }
    }
}

@Composable
private fun SeverityChipCount(count: Int, label: String, color: androidx.compose.ui.graphics.Color, colors: AuditForgeColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(color))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun FindingsFilterBar(
    uiState: FindingsUiState,
    onIntent: (FindingsIntent) -> Unit,
    colors: AuditForgeColors,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RuleSeverity.entries.forEach { severity ->
                val selected = severity in uiState.filterSeverity
                FilterChip(
                    selected = selected,
                    onClick = { onIntent(FindingsIntent.ToggleSeverityFilter(severity)) },
                    label = {
                        Text(severityLabel(severity), style = MaterialTheme.typography.labelSmall)
                    },
                    leadingIcon = if (selected) null else {
                        { SeverityDot(severity = severity, colors = colors) }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = severityColor(severity, colors).copy(alpha = 0.12f),
                        selectedLabelColor = severityColor(severity, colors),
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = colors.borderDefault,
                        selectedBorderColor = severityColor(severity, colors).copy(alpha = 0.5f),
                    ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            StatusFilterDropdown(
                selected = uiState.filterStatus,
                onSelect = { onIntent(FindingsIntent.SetStatusFilter(it)) },
                colors = colors,
            )

            if (uiState.availableCategories.isNotEmpty()) {
                CategoryFilterDropdown(
                    categories = uiState.availableCategories,
                    selected = uiState.filterCategory,
                    onSelect = { onIntent(FindingsIntent.SetCategoryFilter(it)) },
                    colors = colors,
                )
            }

            SearchField(
                query = uiState.searchQuery,
                onQueryChange = { onIntent(FindingsIntent.SetSearchQuery(it)) },
                colors = colors,
            )

            if (uiState.hasActiveFilters) {
                IconButton(
                    onClick = { onIntent(FindingsIntent.ClearFilters) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.findings_clear_filters),
                        tint = colors.textTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusFilterDropdown(
    selected: FindingStatus?,
    onSelect: (FindingStatus?) -> Unit,
    colors: AuditForgeColors,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(Shape.button),
            border = BorderStroke(1.dp, if (selected != null) colors.accentDefault else colors.borderDefault),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
            modifier = Modifier.height(32.dp),
        ) {
            Text(
                text = selected?.let { findingStatusLabel(it) } ?: stringResource(Res.string.findings_filter_all_status),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected != null) colors.accentDefault else colors.textSecondary,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.findings_filter_all_status), style = MaterialTheme.typography.bodySmall) },
                onClick = { onSelect(null); expanded = false },
            )
            FindingStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(findingStatusLabel(status), style = MaterialTheme.typography.bodySmall) },
                    onClick = { onSelect(status); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterDropdown(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    colors: AuditForgeColors,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(Shape.button),
            border = BorderStroke(1.dp, if (selected != null) colors.accentDefault else colors.borderDefault),
            contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
            modifier = Modifier.height(32.dp),
        ) {
            Text(
                text = selected?.replace("_", " ")?.replaceFirstChar { it.uppercase() }
                    ?: stringResource(Res.string.findings_filter_all_category),
                style = MaterialTheme.typography.labelSmall,
                color = if (selected != null) colors.accentDefault else colors.textSecondary,
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(Res.string.findings_filter_all_category), style = MaterialTheme.typography.bodySmall) },
                onClick = { onSelect(null); expanded = false },
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Text(
                            category.replace("_", " ").replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    onClick = { onSelect(category); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, colors: AuditForgeColors) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                stringResource(Res.string.findings_search_placeholder),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp),
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }, modifier = Modifier.size(20.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        } else null,
        modifier = Modifier.width(200.dp).height(36.dp),
        textStyle = MaterialTheme.typography.bodySmall.copy(color = colors.textPrimary),
        singleLine = true,
        shape = RoundedCornerShape(Shape.input),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.borderFocus,
            unfocusedBorderColor = colors.borderDefault,
            cursorColor = colors.accentDefault,
        ),
    )
}

@Composable
private fun FindingsTable(
    findings: List<Finding>,
    onSelectFinding: (Finding) -> Unit,
    colors: AuditForgeColors,
) {
    Surface(
        modifier = Modifier.fillMaxSize()
            .padding(horizontal = Spacing.xl)
            .padding(top = Spacing.sm, bottom = Spacing.xl),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                FindingsTableHeader(colors = colors)
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
            items(findings, key = { it.id }) { finding ->
                FindingsTableRow(
                    finding = finding,
                    onClick = { onSelectFinding(finding) },
                    colors = colors,
                )
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun FindingsTableHeader(colors: AuditForgeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceSunken)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TableHeaderCell(stringResource(Res.string.findings_col_severity), Modifier.width(100.dp), colors)
        TableHeaderCell(stringResource(Res.string.findings_col_title), Modifier.weight(1f), colors)
        TableHeaderCell(stringResource(Res.string.findings_col_category), Modifier.width(120.dp), colors)
        TableHeaderCell(stringResource(Res.string.findings_col_location), Modifier.width(160.dp), colors)
        TableHeaderCell(stringResource(Res.string.findings_col_status), Modifier.width(110.dp), colors)
        TableHeaderCell(stringResource(Res.string.findings_col_detected), Modifier.width(90.dp), colors)
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
private fun FindingsTableRow(
    finding: Finding,
    onClick: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.width(100.dp),
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
        Text(
            text = finding.title,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = Spacing.sm),
        )
        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = finding.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.width(160.dp)) {
            Text(
                text = finding.evidence.sourcePath +
                    (finding.evidence.lineNumber?.let { ":$it" } ?: ""),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.width(110.dp)) {
            FindingStatusBadge(status = finding.status, colors = colors)
        }
        Box(modifier = Modifier.width(90.dp)) {
            Text(
                text = finding.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

@Composable
private fun FindingsEmptyState(hasCompletedScan: Boolean, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            Text(
                text = stringResource(Res.string.findings_empty_title),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(Res.string.findings_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun FindingsEmptyFiltered(onClearFilters: () -> Unit, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.findings_empty_filtered_title),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            TextButton(onClick = onClearFilters) {
                Text(
                    text = stringResource(Res.string.findings_clear_filters),
                    color = colors.accentDefault,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FindingsLoadingSkeleton(colors: AuditForgeColors) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(96.dp)
                .background(colors.surfaceSecondary, RoundedCornerShape(Shape.card))
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
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
                repeat(8) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.width(100.dp).height(12.dp).background(colors.borderDefault, RoundedCornerShape(4.dp)))
                        Box(Modifier.weight(1f).height(12.dp).background(colors.borderDefault.copy(alpha = 0.7f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(120.dp).height(12.dp).background(colors.borderDefault.copy(alpha = 0.5f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(160.dp).height(12.dp).background(colors.borderDefault.copy(alpha = 0.4f), RoundedCornerShape(4.dp)))
                        Box(Modifier.width(80.dp).height(16.dp).background(colors.borderDefault.copy(alpha = 0.5f), RoundedCornerShape(Shape.badge)))
                        Box(Modifier.width(90.dp).height(12.dp).background(colors.borderDefault.copy(alpha = 0.3f), RoundedCornerShape(4.dp)))
                    }
                    HorizontalDivider(color = colors.borderDefault)
                }
            }
        }
    }
}

