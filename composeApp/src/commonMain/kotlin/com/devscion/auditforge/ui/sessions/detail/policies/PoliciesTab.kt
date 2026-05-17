package com.devscion.auditforge.ui.sessions.detail.policies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.Res
import auditforge.composeapp.generated.resources.a11y_close
import auditforge.composeapp.generated.resources.policies_description
import auditforge.composeapp.generated.resources.policies_empty_description
import auditforge.composeapp.generated.resources.policies_empty_title
import auditforge.composeapp.generated.resources.policies_framework_custom
import auditforge.composeapp.generated.resources.policies_framework_gdpr
import auditforge.composeapp.generated.resources.policies_framework_hipaa
import auditforge.composeapp.generated.resources.policies_framework_owasp
import auditforge.composeapp.generated.resources.policies_framework_pci_dss
import auditforge.composeapp.generated.resources.policies_framework_soc2
import auditforge.composeapp.generated.resources.policies_no_packs_selected
import auditforge.composeapp.generated.resources.policies_rule_count
import auditforge.composeapp.generated.resources.policies_save_selection
import auditforge.composeapp.generated.resources.policies_selected_label
import auditforge.composeapp.generated.resources.policies_view_rules
import com.devscion.auditforge.domain.model.PolicyFramework
import com.devscion.auditforge.domain.model.PolicyPackDetail
import com.devscion.auditforge.domain.model.PolicyPackSummary
import com.devscion.auditforge.domain.model.RuleSeverity
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PoliciesTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: PoliciesViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            uiState.isLoading -> PoliciesLoadingSkeleton(colors)
            uiState.packs.isEmpty() -> PoliciesEmptyState(colors)
            else -> PoliciesTwoColumnLayout(
                uiState = uiState,
                onIntent = viewModel::onIntent,
                colors = colors,
            )
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(PoliciesIntent.DismissError) }) {
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
private fun PoliciesTwoColumnLayout(
    uiState: PoliciesUiState,
    onIntent: (PoliciesIntent) -> Unit,
    colors: AuditForgeColors,
) {
    val groupedPacks = uiState.packs.groupBy { it.framework }
    val selectedPacks = uiState.packs.filter { it.id in uiState.selectedPackIds }

    Row(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = Spacing.xl),
        ) {
            item {
                Text(
                    text = stringResource(Res.string.policies_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(bottom = Spacing.xl),
                )
            }

            groupedPacks.forEach { (framework, packs) ->
                item {
                    FrameworkGroupHeader(
                        framework = framework,
                        colors = colors,
                        modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.sm),
                    )
                }
                items(packs, key = { it.id }) { pack ->
                    PackBrowserRow(
                        pack = pack,
                        isSelected = pack.id in uiState.selectedPackIds,
                        onToggle = { onIntent(PoliciesIntent.TogglePackSelection(pack.id)) },
                        colors = colors,
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1.5f),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = PaddingValues(bottom = Spacing.xl),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val label = if (selectedPacks.size == 1)
                        "1 ${stringResource(Res.string.policies_selected_label)}"
                    else
                        "${selectedPacks.size} ${stringResource(Res.string.policies_selected_label)}"
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                    if (uiState.hasChanges) {
                        Button(
                            onClick = { onIntent(PoliciesIntent.SaveSelection) },
                            shape = RoundedCornerShape(Shape.button),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentDefault,
                                contentColor = colors.textOnAccent,
                            ),
                            contentPadding = PaddingValues(
                                horizontal = Spacing.lg,
                                vertical = Spacing.xs
                            ),
                        ) {
                            Text(
                                text = stringResource(Res.string.policies_save_selection),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }

            if (selectedPacks.isEmpty()) {
                item { NoPoliciesSelectedPlaceholder(colors = colors) }
            } else {
                items(selectedPacks, key = { it.id }) { pack ->
                    SelectedPackCard(
                        pack = pack,
                        isExpanded = pack.id == uiState.expandedPackId,
                        isLoadingDetail = uiState.isLoadingDetail && pack.id == uiState.expandedPackId,
                        packDetail = if (pack.id == uiState.expandedPackId) uiState.packDetail else null,
                        onDeselect = { onIntent(PoliciesIntent.TogglePackSelection(pack.id)) },
                        onToggleExpand = {
                            if (pack.id == uiState.expandedPackId) onIntent(PoliciesIntent.CollapsePack)
                            else onIntent(PoliciesIntent.ExpandPack(pack.id))
                        },
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Composable
private fun FrameworkGroupHeader(
    framework: PolicyFramework,
    colors: AuditForgeColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = frameworkColor(framework, colors),
                    shape = RoundedCornerShape(2.dp),
                ),
        )
        Text(
            text = stringResource(frameworkLabel(framework)).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun PackBrowserRow(
    pack: PolicyPackSummary,
    isSelected: Boolean,
    onToggle: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Shape.card))
            .clickable(onClick = onToggle)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
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
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = pack.name,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${pack.ruleCount} ${stringResource(Res.string.policies_rule_count)}",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun NoPoliciesSelectedPlaceholder(colors: AuditForgeColors) {
    val borderColor = colors.borderStrong
    val cornerRadius = Shape.card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    cornerRadius = CornerRadius(cornerRadius.toPx()),
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
                    ),
                )
            }
            .clip(RoundedCornerShape(cornerRadius))
            .padding(Spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.policies_no_packs_selected),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textTertiary,
        )
    }
}

@Composable
private fun SelectedPackCard(
    pack: PolicyPackSummary,
    isExpanded: Boolean,
    isLoadingDetail: Boolean,
    packDetail: PolicyPackDetail?,
    onDeselect: () -> Unit,
    onToggleExpand: () -> Unit,
    colors: AuditForgeColors,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(frameworkLabel(pack.framework)).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = frameworkColor(pack.framework, colors),
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = pack.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${pack.ruleCount} ${stringResource(Res.string.policies_rule_count)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textTertiary,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    TextButton(
                        onClick = onToggleExpand,
                        contentPadding = PaddingValues(
                            horizontal = Spacing.sm,
                            vertical = Spacing.xs
                        ),
                    ) {
                        Text(
                            text = stringResource(Res.string.policies_view_rules),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary,
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    IconButton(onClick = onDeselect, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(Spacing.md))
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
                Spacer(modifier = Modifier.height(Spacing.md))
                when {
                    isLoadingDetail -> Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = colors.accentDefault,
                            strokeWidth = 2.dp,
                        )
                    }

                    packDetail != null -> Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        packDetail.rules.forEach { rule ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                            ) {
                                SeverityDot(severity = rule.severity, colors = colors)
                                Text(
                                    text = rule.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textPrimary,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = rule.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeverityDot(severity: RuleSeverity, colors: AuditForgeColors) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(severityDotColor(severity, colors), CircleShape),
    )
}

@Composable
private fun PoliciesEmptyState(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(40.dp),
            )
            Text(
                text = stringResource(Res.string.policies_empty_title),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(Res.string.policies_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun PoliciesLoadingSkeleton(colors: AuditForgeColors) {
    Row(
        modifier = Modifier.fillMaxSize().padding(Spacing.xl),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(0.8f).height(12.dp)
                    .background(colors.borderDefault, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.height(Spacing.sm))
            repeat(5) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(36.dp).background(
                        colors.borderDefault.copy(alpha = 0.4f),
                        RoundedCornerShape(Shape.card)
                    )
                )
            }
        }
        Column(
            modifier = Modifier.weight(1.5f),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(80.dp).background(
                        colors.borderDefault.copy(alpha = 0.4f),
                        RoundedCornerShape(Shape.card)
                    )
                )
            }
        }
    }
}

@Composable
private fun frameworkColor(framework: PolicyFramework, colors: AuditForgeColors): Color =
    when (framework) {
        PolicyFramework.HIPAA -> colors.accentDefault
        PolicyFramework.OWASP -> colors.severityCritical
        PolicyFramework.SOC2 -> colors.statusSuccess
        PolicyFramework.PCI_DSS -> colors.severityHigh
        PolicyFramework.GDPR -> colors.severityMedium
        PolicyFramework.NIS2 -> colors.severityMedium//TODO
        PolicyFramework.Custom -> colors.borderStrong
    }

private fun severityDotColor(severity: RuleSeverity, colors: AuditForgeColors): Color =
    when (severity) {
        RuleSeverity.Critical -> colors.severityCritical
        RuleSeverity.High -> colors.severityHigh
        RuleSeverity.Medium -> colors.severityMedium
        RuleSeverity.Low -> colors.severityLow
    }

private fun frameworkLabel(framework: PolicyFramework): StringResource = when (framework) {
    PolicyFramework.OWASP -> Res.string.policies_framework_owasp
    PolicyFramework.HIPAA -> Res.string.policies_framework_hipaa
    PolicyFramework.SOC2 -> Res.string.policies_framework_soc2
    PolicyFramework.PCI_DSS -> Res.string.policies_framework_pci_dss
    PolicyFramework.GDPR -> Res.string.policies_framework_gdpr
    PolicyFramework.NIS2 -> Res.string.policies_framework_gdpr
    PolicyFramework.Custom -> Res.string.policies_framework_custom
}
