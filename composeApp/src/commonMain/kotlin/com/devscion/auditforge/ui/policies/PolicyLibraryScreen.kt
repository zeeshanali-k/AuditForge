package com.devscion.auditforge.ui.policies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.Res
import auditforge.composeapp.generated.resources.a11y_close
import auditforge.composeapp.generated.resources.a11y_search
import auditforge.composeapp.generated.resources.policies_framework_all
import auditforge.composeapp.generated.resources.policies_framework_custom
import auditforge.composeapp.generated.resources.policies_framework_gdpr
import auditforge.composeapp.generated.resources.policies_framework_hipaa
import auditforge.composeapp.generated.resources.policies_framework_owasp
import auditforge.composeapp.generated.resources.policies_framework_pci_dss
import auditforge.composeapp.generated.resources.policies_framework_soc2
import auditforge.composeapp.generated.resources.policies_rule_count
import auditforge.composeapp.generated.resources.policy_library_empty_description
import auditforge.composeapp.generated.resources.policy_library_empty_title
import auditforge.composeapp.generated.resources.policy_library_rules_heading
import auditforge.composeapp.generated.resources.policy_library_search_placeholder
import auditforge.composeapp.generated.resources.policy_library_select_prompt
import auditforge.composeapp.generated.resources.policy_library_title
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

@Composable
fun PolicyLibraryContent(
    colors: AuditForgeColors,
    viewModel: PolicyLibraryViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            PolicyLibraryHeader(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.onIntent(PolicyLibraryIntent.UpdateSearch(it)) },
                colors = colors,
            )

            FrameworkFilterBar(
                selectedFramework = uiState.frameworkFilter,
                onSelect = { viewModel.onIntent(PolicyLibraryIntent.SetFrameworkFilter(it)) },
                colors = colors,
            )

            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

            when {
                uiState.isLoading -> PolicyLibrarySkeleton(colors)
                uiState.filteredPacks.isEmpty() -> PolicyLibraryEmptyState(colors)
                else -> PolicyLibraryBody(
                    uiState = uiState,
                    onIntent = viewModel::onIntent,
                    colors = colors,
                )
            }
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(PolicyLibraryIntent.DismissError) }) {
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
private fun PolicyLibraryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.policy_library_title),
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary,
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    stringResource(Res.string.policy_library_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.a11y_search),
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
            },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(Res.string.a11y_close),
                            tint = colors.textTertiary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(Shape.input),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.borderFocus,
                unfocusedBorderColor = colors.borderDefault,
                focusedContainerColor = colors.surfacePrimary,
                unfocusedContainerColor = colors.surfacePrimary,
                cursorColor = colors.accentDefault,
            ),
            modifier = Modifier.width(280.dp),
        )
    }
}

@Composable
private fun FrameworkFilterBar(
    selectedFramework: PolicyFramework?,
    onSelect: (PolicyFramework?) -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = Spacing.xxl, end = Spacing.xxl, bottom = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        FrameworkFilterChip(
            label = stringResource(Res.string.policies_framework_all),
            selected = selectedFramework == null,
            onClick = { onSelect(null) },
            colors = colors,
        )
        PolicyFramework.entries.forEach { framework ->
            FrameworkFilterChip(
                label = stringResource(frameworkLabel(framework)),
                selected = selectedFramework == framework,
                onClick = { onSelect(if (selectedFramework == framework) null else framework) },
                colors = colors,
                accentColor = frameworkColor(framework, colors),
            )
        }
    }
}

@Composable
private fun FrameworkFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AuditForgeColors,
    accentColor: Color = colors.accentDefault,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accentColor.copy(alpha = 0.12f),
            selectedLabelColor = accentColor,
            labelColor = colors.textSecondary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.borderDefault,
            selectedBorderColor = accentColor,
        ),
    )
}

@Composable
private fun PolicyLibraryBody(
    uiState: PolicyLibraryUiState,
    onIntent: (PolicyLibraryIntent) -> Unit,
    colors: AuditForgeColors,
) {
    val groupedPacks = uiState.filteredPacks.groupBy { it.framework }

    Row(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(0.42f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(
                horizontal = Spacing.xxl,
                vertical = Spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            groupedPacks.forEach { (framework, packs) ->
                item(key = "header-${framework.name}") {
                    LibraryFrameworkHeader(
                        framework = framework,
                        colors = colors,
                        modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.sm),
                    )
                }
                items(packs, key = { it.id }) { pack ->
                    PackCard(
                        pack = pack,
                        isSelected = pack.id == uiState.selectedPackId,
                        onClick = { onIntent(PolicyLibraryIntent.SelectPack(pack.id)) },
                        colors = colors,
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                }
            }
        }

        VerticalDivider(color = colors.borderDefault, thickness = 1.dp)

        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .background(colors.surfaceSecondary),
        ) {
            when {
                uiState.selectedPackId == null -> PackSelectPrompt(colors)
                uiState.isLoadingDetail -> PackDetailLoading(colors)
                uiState.packDetail != null -> PackDetailPanel(
                    detail = uiState.packDetail,
                    onClose = { onIntent(PolicyLibraryIntent.CloseDetail) },
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun LibraryFrameworkHeader(
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
                .background(frameworkColor(framework, colors), RoundedCornerShape(2.dp)),
        )
        Text(
            text = stringResource(frameworkLabel(framework)).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun PackCard(
    pack: PolicyPackSummary,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AuditForgeColors,
) {
    val frameworkColor = frameworkColor(pack.framework, colors)
    val borderColor = if (isSelected) colors.accentDefault else colors.borderDefault
    val bgColor =
        if (isSelected) colors.accentDefault.copy(alpha = 0.04f) else colors.surfaceElevated

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(Shape.card))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Shape.card),
        color = bgColor,
        tonalElevation = 0.dp,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(
                        color = frameworkColor,
                        shape = RoundedCornerShape(topStart = Shape.card, bottomStart = Shape.card),
                    ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = pack.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = pack.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${pack.ruleCount} ${stringResource(Res.string.policies_rule_count)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                    Text(
                        text = "v${pack.version}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.ChevronRight else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isSelected) colors.accentDefault else colors.borderStrong,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = Spacing.md)
                    .size(16.dp),
            )
        }
    }
}

@Composable
private fun PackSelectPrompt(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = colors.borderStrong,
                modifier = Modifier.size(36.dp),
            )
            Text(
                text = stringResource(Res.string.policy_library_select_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun PackDetailLoading(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(28.dp),
            color = colors.accentDefault,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun PackDetailPanel(
    detail: PolicyPackDetail,
    onClose: () -> Unit,
    colors: AuditForgeColors,
) {
    val frameworkColor = frameworkColor(detail.framework, colors)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Surface(
                            shape = RoundedCornerShape(Shape.badge),
                            color = frameworkColor.copy(alpha = 0.12f),
                        ) {
                            Text(
                                text = stringResource(frameworkLabel(detail.framework)).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = frameworkColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(
                                    horizontal = Spacing.sm,
                                    vertical = Spacing.xs,
                                ),
                            )
                        }
                        Text(
                            text = "v${detail.version}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                        )
                    }
                    Text(
                        text = detail.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.a11y_close),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = detail.description,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xl))
            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            Spacer(modifier = Modifier.height(Spacing.lg))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Text(
                    text = stringResource(Res.string.policy_library_rules_heading),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Surface(
                    shape = RoundedCornerShape(Shape.badge),
                    color = colors.borderDefault,
                ) {
                    Text(
                        text = "${detail.rules.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        items(detail.rules, key = { it.id }) { rule ->
            Surface(
                modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.xs),
                shape = RoundedCornerShape(Shape.card),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderDefault),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 3.dp)
                            .size(8.dp)
                            .background(severityColor(rule.severity, colors), CircleShape),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = rule.name,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = rule.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary,
                            )
                            if (rule.complianceMapping.isNotEmpty()) {
                                Text(
                                    text = "·",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                )
                                Text(
                                    text = checkTypeLabel(rule.checkType),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                )
                            }
                        }
                        if (rule.complianceMapping.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                rule.complianceMapping.take(3).forEach { ref ->
                                    Surface(
                                        shape = RoundedCornerShape(Shape.badge),
                                        color = colors.accentDefault.copy(alpha = 0.08f),
                                    ) {
                                        Text(
                                            text = ref,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = colors.accentDefault,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(
                                                horizontal = Spacing.xs,
                                                vertical = 2.dp,
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(Spacing.xl)) }
    }
}

@Composable
private fun PolicyLibrarySkeleton(colors: AuditForgeColors) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(0.42f)
                .padding(horizontal = Spacing.xxl, vertical = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(10.dp)
                    .background(colors.borderDefault, RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            repeat(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            colors.borderDefault.copy(alpha = 0.5f),
                            RoundedCornerShape(Shape.card),
                        ),
                )
            }
        }
        VerticalDivider(color = colors.borderDefault, thickness = 1.dp)
        Box(
            modifier = Modifier
                .weight(0.58f)
                .fillMaxHeight()
                .background(colors.surfaceSecondary),
        )
    }
}

@Composable
private fun PolicyLibraryEmptyState(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Shape.card))
                    .background(colors.borderDefault),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = stringResource(Res.string.policy_library_empty_title),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(Res.string.policy_library_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

private fun checkTypeLabel(checkType: String): String = when (checkType) {
    "ai_evaluation" -> "AI evaluation"
    "schema_validation" -> "Schema validation"
    "pattern" -> "Pattern match"
    else -> checkType
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

private fun severityColor(severity: RuleSeverity, colors: AuditForgeColors): Color =
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
