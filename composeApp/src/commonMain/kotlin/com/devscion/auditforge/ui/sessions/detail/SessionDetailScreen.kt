package com.devscion.auditforge.ui.sessions.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.Session
import com.devscion.auditforge.domain.model.SessionStatus
import com.devscion.auditforge.ui.sessions.detail.audittrail.AuditTrailTab
import com.devscion.auditforge.ui.sessions.detail.findings.FindingsTab
import com.devscion.auditforge.ui.sessions.detail.policies.PoliciesTab
import com.devscion.auditforge.ui.sessions.detail.reports.ReportsTab
import com.devscion.auditforge.ui.sessions.detail.scans.ScansTab
import com.devscion.auditforge.ui.sessions.detail.uploads.UploadsTab
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import com.devscion.auditforge.ui.theme.auditForgeColors
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.auditForgeColors

    Column(
        modifier = Modifier.fillMaxSize().background(colors.surfacePrimary),
    ) {
        SessionDetailTopBar(session = uiState.session, onBack = onBack, colors = colors)

        HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

        when {
            uiState.isLoading -> LoadingState(colors)
            uiState.error != null -> ErrorState(
                message = uiState.error!!,
                onRetry = { viewModel.onIntent(SessionDetailIntent.Load) },
                colors = colors,
            )

            uiState.session != null -> SessionDetailContent(
                sessionId = sessionId,
                uiState = uiState,
                onIntent = viewModel::onIntent,
                colors = colors,
            )
        }
    }
}

@Composable
private fun SessionDetailTopBar(
    session: Session?,
    onBack: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).background(colors.surfacePrimary)
            .padding(horizontal = Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.session_detail_back),
                tint = colors.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        if (session != null) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            StatusBadge(status = session.status, colors = colors)
            session.overallScore?.let { score -> ScoreBadge(score = score, colors = colors) }
        }
    }
}

@Composable
private fun SessionDetailContent(
    sessionId: String,
    uiState: SessionDetailUiState,
    onIntent: (SessionDetailIntent) -> Unit,
    colors: AuditForgeColors,
) {
    val tabs = SessionDetailTab.entries
    val selectedIndex = tabs.indexOf(uiState.selectedTab)

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = colors.surfacePrimary,
            contentColor = colors.accentDefault,
            edgePadding = Spacing.xl,
            divider = { HorizontalDivider(color = colors.borderDefault, thickness = 1.dp) },
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == uiState.selectedTab,
                    onClick = { onIntent(SessionDetailIntent.SelectTab(tab)) },
                    text = {
                        Text(
                            text = stringResource(tabLabel(tab)),
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    selectedContentColor = colors.accentDefault,
                    unselectedContentColor = colors.textSecondary,
                )
            }
        }

        TabContent(tab = uiState.selectedTab, sessionId = sessionId, colors = colors)
    }
}

@Composable
private fun TabContent(tab: SessionDetailTab, sessionId: String, colors: AuditForgeColors) {
    when (tab) {
        SessionDetailTab.Uploads -> UploadsTab(sessionId = sessionId, colors = colors)
        SessionDetailTab.Policies -> PoliciesTab(sessionId = sessionId, colors = colors)
        SessionDetailTab.Scans -> ScansTab(sessionId = sessionId, colors = colors)
        SessionDetailTab.Findings -> FindingsTab(sessionId = sessionId, colors = colors)
        SessionDetailTab.Reports -> ReportsTab(sessionId = sessionId, colors = colors)
        SessionDetailTab.AuditTrail -> AuditTrailTab(sessionId = sessionId, colors = colors)
    }
}

@Composable
private fun TabPlaceholder(tab: SessionDetailTab, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Text(
                text = stringResource(tabLabel(tab)),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )
            Text(
                text = stringResource(Res.string.session_detail_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun LoadingState(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            CircularProgressIndicator(color = colors.accentDefault)
            Text(
                text = stringResource(Res.string.session_detail_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.severityCritical
            )
            OutlinedButton(
                onClick = onRetry,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentDefault),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            ) {
                Text(stringResource(Res.string.sessions_load_more))
            }
        }
    }
}

@Composable
fun StatusBadge(status: SessionStatus, colors: AuditForgeColors) {
    val (labelRes, containerColor, contentColor) = statusStyle(status, colors)
    Surface(shape = RoundedCornerShape(Shape.badge), color = containerColor) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

@Composable
fun ScoreBadge(score: Int, colors: AuditForgeColors) {
    val color = when {
        score >= 80 -> colors.severityLow
        score >= 50 -> colors.severityMedium
        else -> colors.severityCritical
    }
    Surface(shape = RoundedCornerShape(Shape.badge), color = color.copy(alpha = 0.12f)) {
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = color,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = 2.dp),
        )
    }
}

private data class StatusStyle(
    val labelRes: StringResource,
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color
)

@Composable
private fun statusStyle(status: SessionStatus, colors: AuditForgeColors): StatusStyle =
    when (status) {
        SessionStatus.Created -> StatusStyle(
            Res.string.status_created,
            colors.borderDefault,
            colors.textSecondary
        )

        SessionStatus.Scanning -> StatusStyle(
            Res.string.status_scanning,
            colors.accentDefault.copy(alpha = 0.12f),
            colors.accentDefault
        )

        SessionStatus.Completed -> StatusStyle(
            Res.string.status_completed,
            colors.severityLow.copy(alpha = 0.12f),
            colors.severityLow
        )

        SessionStatus.Failed -> StatusStyle(
            Res.string.status_failed,
            colors.severityCritical.copy(alpha = 0.12f),
            colors.severityCritical
        )
    }

private fun tabLabel(tab: SessionDetailTab): StringResource = when (tab) {
    SessionDetailTab.Uploads -> Res.string.session_detail_tab_uploads
    SessionDetailTab.Policies -> Res.string.session_detail_tab_policies
    SessionDetailTab.Scans -> Res.string.session_detail_tab_scans
    SessionDetailTab.Findings -> Res.string.session_detail_tab_findings
    SessionDetailTab.Reports -> Res.string.session_detail_tab_reports
    SessionDetailTab.AuditTrail -> Res.string.session_detail_tab_audit_trail
}
