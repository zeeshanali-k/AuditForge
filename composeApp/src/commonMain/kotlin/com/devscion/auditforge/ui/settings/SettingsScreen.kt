package com.devscion.auditforge.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.AppFlavor
import com.devscion.auditforge.data.network.AppConfig
import com.devscion.auditforge.getPlatform
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import com.devscion.auditforge.ui.theme.darkAuditForgeColors
import com.devscion.auditforge.ui.theme.lightAuditForgeColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsContent(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    colors: AuditForgeColors,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = Spacing.xxl, vertical = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl),
    ) {
        item {
            Text(
                text = stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
        }

        item {
            SettingsSection(title = stringResource(Res.string.settings_section_api), colors = colors) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    SettingsLabel(
                        label = stringResource(Res.string.settings_api_base_url_label),
                        colors = colors,
                    )
                    Surface(
                        shape = RoundedCornerShape(Shape.input),
                        color = colors.surfaceSunken,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderDefault),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = AppConfig.API_BASE_URL,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                ),
                                color = colors.textSecondary,
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.onIntent(SettingsIntent.TestConnection) },
                            enabled = uiState.connectionTestState != ConnectionTestState.Testing,
                            shape = RoundedCornerShape(Shape.button),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colors.accentDefault,
                            ),
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
                        ) {
                            if (uiState.connectionTestState == ConnectionTestState.Testing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = colors.accentDefault,
                                    strokeWidth = 2.dp,
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(modifier = Modifier.width(Spacing.sm))
                            }
                            Text(
                                text = stringResource(Res.string.settings_api_test_button),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }

                        when (uiState.connectionTestState) {
                            ConnectionTestState.Success -> ConnectionStatus(
                                icon = Icons.Default.CheckCircle,
                                text = stringResource(Res.string.settings_api_test_success),
                                color = colors.statusSuccess,
                            )
                            ConnectionTestState.Failed -> ConnectionStatus(
                                icon = Icons.Default.Error,
                                text = stringResource(Res.string.settings_api_test_failed),
                                color = colors.severityCritical,
                            )
                            else -> Unit
                        }
                    }
                }
            }
        }

        item {
            SettingsSection(
                title = stringResource(Res.string.settings_section_appearance),
                colors = colors,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    SettingsLabel(
                        label = stringResource(Res.string.settings_theme_label),
                        colors = colors,
                    )
                    ThemeSelector(
                        current = currentTheme,
                        onSelect = onThemeChange,
                        colors = colors,
                    )
                }
            }
        }

        item {
            SettingsSection(
                title = stringResource(Res.string.settings_section_about),
                colors = colors,
            ) {
                Column {
                    AboutRow(
                        label = stringResource(Res.string.settings_about_version),
                        value = "1.0.0-alpha",
                        colors = colors,
                    )
                    HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
                    AboutRow(
                        label = stringResource(Res.string.settings_about_platform),
                        value = getPlatform().name,
                        colors = colors,
                    )
                    HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
                    AboutRow(
                        label = stringResource(Res.string.settings_about_data_source),
                        value = if (AppFlavor.USE_MOCK_DATA)
                            stringResource(Res.string.settings_about_mock_data)
                        else stringResource(Res.string.settings_about_live_data),
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    colors: AuditForgeColors,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Shape.card),
            color = colors.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderDefault),
        ) {
            Column(modifier = Modifier.padding(Spacing.lg), content = content)
        }
    }
}

@Composable
private fun SettingsLabel(label: String, colors: AuditForgeColors) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = colors.textSecondary,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun ConnectionStatus(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
private fun ThemeSelector(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    colors: AuditForgeColors,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        ThemeMode.entries.forEach { mode ->
            ThemeOption(
                mode = mode,
                isSelected = current == mode,
                onClick = { onSelect(mode) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: AuditForgeColors,
) {
    val previewColors = when (mode) {
        ThemeMode.Light -> lightAuditForgeColors()
        ThemeMode.Dark -> darkAuditForgeColors()
        ThemeMode.System -> colors
    }

    val borderColor = if (isSelected) colors.accentDefault else colors.borderDefault

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Shape.card))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(Shape.card),
            )
            .clickable(onClick = onClick)
            .padding(Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        ThemePreviewSwatch(previewColors = previewColors)
        Text(
            text = stringResource(themeLabel(mode)),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colors.accentDefault else colors.textSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun ThemePreviewSwatch(previewColors: AuditForgeColors) {
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(previewColors.surfacePrimary)
            .border(1.dp, previewColors.borderDefault, RoundedCornerShape(4.dp)),
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight()
                .background(previewColors.surfaceSecondary)
                .border(
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        previewColors.borderDefault,
                    )
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 6.dp, end = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(previewColors.textPrimary.copy(alpha = 0.7f), RoundedCornerShape(2.dp)),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(4.dp)
                    .background(previewColors.textTertiary.copy(alpha = 0.5f), RoundedCornerShape(2.dp)),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(8.dp)
                    .background(previewColors.accentDefault, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
private fun AboutRow(
    label: String,
    value: String,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun themeLabel(mode: ThemeMode): StringResource = when (mode) {
    ThemeMode.System -> Res.string.settings_theme_system
    ThemeMode.Light -> Res.string.settings_theme_light
    ThemeMode.Dark -> Res.string.settings_theme_dark
}
