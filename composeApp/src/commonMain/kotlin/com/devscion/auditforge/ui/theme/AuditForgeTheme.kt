package com.devscion.auditforge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class AuditForgeColors(
    val surfacePrimary: Color,
    val surfaceSecondary: Color,
    val surfaceElevated: Color,
    val surfaceSunken: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textOnAccent: Color,
    val borderDefault: Color,
    val borderStrong: Color,
    val borderFocus: Color,
    val accentDefault: Color,
    val accentHover: Color,
    val accentPressed: Color,
    val severityCritical: Color,
    val severityHigh: Color,
    val severityMedium: Color,
    val severityLow: Color,
    val severityResolved: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusInfo: Color,
    val statusNeutral: Color,
    val isDark: Boolean,
)

fun lightAuditForgeColors() = AuditForgeColors(
    surfacePrimary = SurfacePrimaryLight,
    surfaceSecondary = SurfaceSecondaryLight,
    surfaceElevated = SurfaceElevatedLight,
    surfaceSunken = SurfaceSunkenLight,
    textPrimary = TextPrimaryLight,
    textSecondary = TextSecondaryLight,
    textTertiary = TextTertiaryLight,
    textOnAccent = TextOnAccent,
    borderDefault = BorderDefaultLight,
    borderStrong = BorderStrongLight,
    borderFocus = BorderFocusLight,
    accentDefault = AccentDefaultLight,
    accentHover = AccentHoverLight,
    accentPressed = AccentPressedLight,
    severityCritical = SeverityCriticalLight,
    severityHigh = SeverityHighLight,
    severityMedium = SeverityMediumLight,
    severityLow = SeverityLowLight,
    severityResolved = SeverityResolvedLight,
    statusSuccess = StatusSuccessLight,
    statusWarning = StatusWarningLight,
    statusInfo = StatusInfoLight,
    statusNeutral = StatusNeutralLight,
    isDark = false,
)

fun darkAuditForgeColors() = AuditForgeColors(
    surfacePrimary = SurfacePrimaryDark,
    surfaceSecondary = SurfaceSecondaryDark,
    surfaceElevated = SurfaceElevatedDark,
    surfaceSunken = SurfaceSunkenDark,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textTertiary = TextTertiaryDark,
    textOnAccent = TextOnAccent,
    borderDefault = BorderDefaultDark,
    borderStrong = BorderStrongDark,
    borderFocus = BorderFocusDark,
    accentDefault = AccentDefaultDark,
    accentHover = AccentHoverDark,
    accentPressed = AccentPressedDark,
    severityCritical = SeverityCriticalDark,
    severityHigh = SeverityHighDark,
    severityMedium = SeverityMediumDark,
    severityLow = SeverityLowDark,
    severityResolved = SeverityResolvedDark,
    statusSuccess = StatusSuccessDark,
    statusWarning = StatusWarningDark,
    statusInfo = StatusInfoDark,
    statusNeutral = StatusNeutralDark,
    isDark = true,
)

private fun lightColorScheme(): ColorScheme = lightColorScheme(
    primary = M3PrimaryLight,
    onPrimary = M3OnPrimaryLight,
    primaryContainer = M3PrimaryContainerLight,
    onPrimaryContainer = M3OnPrimaryContainerLight,
    secondary = M3SecondaryLight,
    onSecondary = M3OnSecondaryLight,
    secondaryContainer = M3SecondaryContainerLight,
    onSecondaryContainer = M3OnSecondaryContainerLight,
    error = M3ErrorLight,
    onError = M3OnErrorLight,
    errorContainer = M3ErrorContainerLight,
    onErrorContainer = M3OnErrorContainerLight,
    background = SurfacePrimaryLight,
    onBackground = TextPrimaryLight,
    surface = SurfacePrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceSecondaryLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = M3OutlineLight,
    outlineVariant = M3OutlineVariantLight,
)

private fun darkColorScheme(): ColorScheme = darkColorScheme(
    primary = M3PrimaryDark,
    onPrimary = M3OnPrimaryDark,
    primaryContainer = M3PrimaryContainerDark,
    onPrimaryContainer = M3OnPrimaryContainerDark,
    secondary = M3SecondaryDark,
    onSecondary = M3OnSecondaryDark,
    secondaryContainer = M3SecondaryContainerDark,
    onSecondaryContainer = M3OnSecondaryContainerDark,
    error = M3ErrorDark,
    onError = M3OnErrorDark,
    errorContainer = M3ErrorContainerDark,
    onErrorContainer = M3OnErrorContainerDark,
    background = SurfacePrimaryDark,
    onBackground = TextPrimaryDark,
    surface = SurfacePrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceSecondaryDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = M3OutlineDark,
    outlineVariant = M3OutlineVariantDark,
)

val LocalAuditForgeColors = staticCompositionLocalOf { lightAuditForgeColors() }

val MaterialTheme.auditForgeColors: AuditForgeColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAuditForgeColors.current

@Composable
fun AuditForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val customColors = if (darkTheme) darkAuditForgeColors() else lightAuditForgeColors()
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

    CompositionLocalProvider(LocalAuditForgeColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = auditForgeTypography(),
            content = content,
        )
    }
}
