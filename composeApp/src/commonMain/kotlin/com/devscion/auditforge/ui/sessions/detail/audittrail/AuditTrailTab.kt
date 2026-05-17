package com.devscion.auditforge.ui.sessions.detail.audittrail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import auditforge.composeapp.generated.resources.Res
import auditforge.composeapp.generated.resources.a11y_close
import auditforge.composeapp.generated.resources.audit_trail_col_actor
import auditforge.composeapp.generated.resources.audit_trail_col_event
import auditforge.composeapp.generated.resources.audit_trail_col_summary
import auditforge.composeapp.generated.resources.audit_trail_col_timestamp
import auditforge.composeapp.generated.resources.audit_trail_details_actor_id
import auditforge.composeapp.generated.resources.audit_trail_details_copy
import auditforge.composeapp.generated.resources.audit_trail_details_curr_hash
import auditforge.composeapp.generated.resources.audit_trail_details_payload
import auditforge.composeapp.generated.resources.audit_trail_details_prev_hash
import auditforge.composeapp.generated.resources.audit_trail_empty_description
import auditforge.composeapp.generated.resources.audit_trail_empty_title
import auditforge.composeapp.generated.resources.audit_trail_events_label
import auditforge.composeapp.generated.resources.audit_trail_filter_all_events
import auditforge.composeapp.generated.resources.audit_trail_filter_event_type
import auditforge.composeapp.generated.resources.audit_trail_load_more
import auditforge.composeapp.generated.resources.audit_trail_page_title
import auditforge.composeapp.generated.resources.audit_trail_verify_break_point
import auditforge.composeapp.generated.resources.audit_trail_verify_broken
import auditforge.composeapp.generated.resources.audit_trail_verify_button
import auditforge.composeapp.generated.resources.audit_trail_verify_close
import auditforge.composeapp.generated.resources.audit_trail_verify_intact
import auditforge.composeapp.generated.resources.audit_trail_verify_title
import auditforge.composeapp.generated.resources.audit_trail_verify_total_events
import auditforge.composeapp.generated.resources.audit_trail_verify_verified_at
import auditforge.composeapp.generated.resources.audit_trail_verify_verifying
import auditforge.composeapp.generated.resources.findings_clear_filters
import com.devscion.auditforge.domain.model.AuditActorType
import com.devscion.auditforge.domain.model.AuditChainVerification
import com.devscion.auditforge.domain.model.AuditEvent
import com.devscion.auditforge.domain.model.AuditEventType
import com.devscion.auditforge.domain.model.PaginationMeta
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val prettyJson = Json { prettyPrint = true }

@Composable
fun AuditTrailTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: AuditTrailViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AuditTrailHeader(
                isVerifying = uiState.isVerifying,
                onVerify = { viewModel.onIntent(AuditTrailIntent.VerifyChain) },
                colors = colors,
            )
            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

            AuditTrailFilterBar(
                selectedEventType = uiState.selectedEventType,
                onEventTypeSelected = { viewModel.onIntent(AuditTrailIntent.SetEventTypeFilter(it)) },
                colors = colors,
            )
            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

            when {
                uiState.isLoading && uiState.events.isEmpty() -> AuditTrailLoadingSkeleton(colors)
                uiState.events.isEmpty() -> AuditTrailEmptyState(colors)
                else -> AuditTrailEventList(
                    events = uiState.events,
                    expandedEventId = uiState.expandedEventId,
                    pagination = uiState.pagination,
                    isLoadingMore = uiState.isLoadingMore,
                    onToggleExpand = { viewModel.onIntent(AuditTrailIntent.ToggleEventExpanded(it)) },
                    onLoadMore = { viewModel.onIntent(AuditTrailIntent.LoadMore) },
                    colors = colors,
                )
            }
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(AuditTrailIntent.DismissError) }) {
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

    if (uiState.showVerifyModal) {
        ChainVerifyModal(
            isVerifying = uiState.isVerifying,
            result = uiState.verificationResult,
            onDismiss = { viewModel.onIntent(AuditTrailIntent.DismissVerifyModal) },
            colors = colors,
        )
    }
}

@Composable
private fun AuditTrailHeader(
    isVerifying: Boolean,
    onVerify: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.audit_trail_page_title),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.Medium,
        )
        OutlinedButton(
            onClick = onVerify,
            enabled = !isVerifying,
            shape = RoundedCornerShape(Shape.button),
            border = BorderStroke(
                1.dp,
                if (isVerifying) colors.borderDefault else colors.accentDefault
            ),
            contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = colors.accentDefault,
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = colors.accentDefault,
                    modifier = Modifier.size(14.dp),
                )
            }
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = if (isVerifying) {
                    stringResource(Res.string.audit_trail_verify_verifying)
                } else {
                    stringResource(Res.string.audit_trail_verify_button)
                },
                style = MaterialTheme.typography.labelLarge,
                color = if (isVerifying) colors.textTertiary else colors.accentDefault,
            )
        }
    }
}

@Composable
private fun AuditTrailFilterBar(
    selectedEventType: AuditEventType?,
    onEventTypeSelected: (AuditEventType?) -> Unit,
    colors: AuditForgeColors,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.audit_trail_filter_event_type),
            style = MaterialTheme.typography.labelMedium,
            color = colors.textSecondary,
        )
        Box {
            OutlinedButton(
                onClick = { dropdownExpanded = true },
                shape = RoundedCornerShape(Shape.button),
                border = BorderStroke(
                    1.dp,
                    if (selectedEventType != null) colors.accentDefault else colors.borderDefault,
                ),
                contentPadding = PaddingValues(horizontal = Spacing.md, vertical = Spacing.xs),
                modifier = Modifier.height(34.dp),
            ) {
                Text(
                    text = selectedEventType?.let { eventTypeLabel(it) }
                        ?: stringResource(Res.string.audit_trail_filter_all_events),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selectedEventType != null) colors.accentDefault else colors.textSecondary,
                )
                Spacer(Modifier.width(Spacing.xs))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
            }
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.audit_trail_filter_all_events),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    onClick = { onEventTypeSelected(null); dropdownExpanded = false },
                )
                AuditEventType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = {
                            Text(eventTypeLabel(type), style = MaterialTheme.typography.bodySmall)
                        },
                        onClick = { onEventTypeSelected(type); dropdownExpanded = false },
                        leadingIcon = {
                            Icon(
                                imageVector = eventTypeIcon(type),
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(14.dp),
                            )
                        },
                    )
                }
            }
        }

        if (selectedEventType != null) {
            TextButton(
                onClick = { onEventTypeSelected(null) },
                contentPadding = PaddingValues(horizontal = Spacing.sm, vertical = Spacing.xs),
            ) {
                Text(
                    text = stringResource(Res.string.findings_clear_filters),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun AuditTrailEventList(
    events: List<AuditEvent>,
    expandedEventId: String?,
    pagination: PaginationMeta?,
    isLoadingMore: Boolean,
    onToggleExpand: (String) -> Unit,
    onLoadMore: () -> Unit,
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
                AuditTableHeader(colors)
                HorizontalDivider(color = colors.borderDefault)
            }

            items(events, key = { it.id }) { event ->
                val isExpanded = expandedEventId == event.id
                Column {
                    AuditEventRow(
                        event = event,
                        isExpanded = isExpanded,
                        onToggle = { onToggleExpand(event.id) },
                        colors = colors,
                    )
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        AuditEventDetail(event = event, colors = colors)
                    }
                    HorizontalDivider(color = colors.borderDefault)
                }
            }

            item {
                if (pagination != null && pagination.page < pagination.totalPages) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = colors.accentDefault,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            TextButton(onClick = onLoadMore) {
                                Text(
                                    text = stringResource(Res.string.audit_trail_load_more),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.accentDefault,
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceSunken)
                            .padding(Spacing.sm),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "${events.size} ${stringResource(Res.string.audit_trail_events_label)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditTableHeader(colors: AuditForgeColors) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceSunken)
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(160.dp)) {
            Text(
                text = stringResource(Res.string.audit_trail_col_timestamp).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(modifier = Modifier.width(160.dp)) {
            Text(
                text = stringResource(Res.string.audit_trail_col_event).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(modifier = Modifier.width(140.dp)) {
            Text(
                text = stringResource(Res.string.audit_trail_col_actor).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.Medium,
            )
        }
        Box(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.audit_trail_col_summary).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.width(32.dp))
    }
}

@Composable
private fun AuditEventRow(
    event: AuditEvent,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(160.dp)) {
            Text(
                text = formatTimestamp(event.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }

        Row(
            modifier = Modifier.width(160.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Icon(
                imageVector = eventTypeIcon(event.eventType),
                contentDescription = null,
                tint = eventTypeColor(event.eventType, colors),
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = eventTypeLabel(event.eventType),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
            )
        }

        Row(
            modifier = Modifier.width(140.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            val actorIcon: ImageVector = if (event.actor.type == AuditActorType.System) {
                Icons.Default.Memory
            } else {
                Icons.Default.Person
            }
            Icon(
                imageVector = actorIcon,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(13.dp),
            )
            Text(
                text = event.actor.name,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }

        Text(
            text = eventSummary(event),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )

        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = colors.textTertiary,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun AuditEventDetail(event: AuditEvent, colors: AuditForgeColors) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceSunken)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        HorizontalDivider(color = colors.borderDefault)

        DetailRow(
            label = stringResource(Res.string.audit_trail_details_actor_id),
            value = event.actor.id ?: "",
            colors = colors,
        )

        if (event.details != null) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    text = stringResource(Res.string.audit_trail_details_payload),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    fontWeight = FontWeight.Medium,
                )
                Surface(
                    shape = RoundedCornerShape(Shape.badge),
                    color = colors.surfacePrimary,
                    border = BorderStroke(1.dp, colors.borderDefault),
                ) {
                    val jsonText = try {
                        prettyJson.encodeToString(JsonObject.serializer(), event.details)
                    } catch (_: Exception) {
                        event.details.toString()
                    }
                    Text(
                        text = jsonText,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(Spacing.sm),
                    )
                }
            }
        }

        HashRow(
            label = stringResource(Res.string.audit_trail_details_prev_hash),
            hash = event.previousHash,
            colors = colors,
        )
        HashRow(
            label = stringResource(Res.string.audit_trail_details_curr_hash),
            hash = event.currentHash,
            colors = colors,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, colors: AuditForgeColors) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun HashRow(label: String, hash: String, colors: AuditForgeColors) {
    var copied by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = hash.take(16) + "…",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(hash))
                copied = true
            },
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = stringResource(Res.string.audit_trail_details_copy),
                tint = if (copied) colors.severityResolved else colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
        }
    }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2_000)
            copied = false
        }
    }
}

@Composable
private fun AuditTrailEmptyState(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier.widthIn(max = 480.dp).padding(Spacing.xxxl),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        colors.accentDefault.copy(alpha = 0.1f),
                        RoundedCornerShape(Shape.card)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.History,
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
                    text = stringResource(Res.string.audit_trail_empty_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(Res.string.audit_trail_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }
        }
    }
}

@Composable
private fun AuditTrailLoadingSkeleton(colors: AuditForgeColors) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl)
            .padding(top = Spacing.sm, bottom = Spacing.xl),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(36.dp).background(colors.surfaceSunken)
            )
            HorizontalDivider(color = colors.borderDefault)
            repeat(7) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.width(140.dp).height(12.dp)
                            .background(colors.borderDefault, RoundedCornerShape(4.dp))
                    )
                    Box(
                        Modifier.width(120.dp).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.7f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                    Box(
                        Modifier.width(100.dp).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.6f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                    Box(
                        Modifier.weight(1f).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.4f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                }
                HorizontalDivider(color = colors.borderDefault)
            }
        }
    }
}

@Composable
private fun ChainVerifyModal(
    isVerifying: Boolean,
    result: AuditChainVerification?,
    onDismiss: () -> Unit,
    colors: AuditForgeColors,
) {
    Dialog(
        onDismissRequest = { if (!isVerifying) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.width(440.dp),
            shape = RoundedCornerShape(Shape.modal),
            color = colors.surfaceElevated,
            border = BorderStroke(1.dp, colors.borderDefault),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            ) {
                Text(
                    text = stringResource(Res.string.audit_trail_verify_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                )

                HorizontalDivider(color = colors.borderDefault)

                when {
                    isVerifying -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            ) {
                                CircularProgressIndicator(
                                    color = colors.accentDefault,
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                )
                                Text(
                                    text = stringResource(Res.string.audit_trail_verify_verifying),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textSecondary,
                                )
                            }
                        }
                    }

                    result != null -> {
                        val icon =
                            if (result.chainIntact) Icons.Default.CheckCircle else Icons.Default.Cancel
                        val tint =
                            if (result.chainIntact) colors.severityResolved else colors.severityCritical
                        val headlineRes = if (result.chainIntact) {
                            Res.string.audit_trail_verify_intact
                        } else {
                            Res.string.audit_trail_verify_broken
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(32.dp),
                            )
                            Text(
                                text = stringResource(headlineRes),
                                style = MaterialTheme.typography.titleSmall,
                                color = tint,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            VerifyMetaRow(
                                label = stringResource(Res.string.audit_trail_verify_total_events),
                                value = result.totalEvents.toString(),
                                colors = colors,
                            )
                            VerifyMetaRow(
                                label = stringResource(Res.string.audit_trail_verify_verified_at),
                                value = formatTimestamp(result.verifiedAt),
                                colors = colors,
                            )
                            if (!result.chainIntact && result.firstBrokenIndex != null) {
                                VerifyMetaRow(
                                    label = stringResource(Res.string.audit_trail_verify_break_point),
                                    value = "#${result.firstBrokenIndex + 1}",
                                    colors = colors,
                                )
                            }
                        }

                        HorizontalDivider(color = colors.borderDefault)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(Shape.button),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentDefault,
                                    contentColor = colors.textOnAccent,
                                ),
                                contentPadding = PaddingValues(
                                    horizontal = Spacing.lg,
                                    vertical = Spacing.sm
                                ),
                            ) {
                                Text(
                                    text = stringResource(Res.string.audit_trail_verify_close),
                                    style = MaterialTheme.typography.labelLarge,
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
private fun VerifyMetaRow(label: String, value: String, colors: AuditForgeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun formatTimestamp(iso: String): String = try {
    "${iso.take(10)} ${iso.substring(11, 19)}"
} catch (_: Exception) {
    iso
}

private fun eventTypeLabel(type: AuditEventType): String = when (type) {
    AuditEventType.SessionCreated -> "Session created"
    AuditEventType.UploadAdded -> "File uploaded"
    AuditEventType.ScanStarted -> "Scan started"
    AuditEventType.ScanCompleted -> "Scan completed"
    AuditEventType.FindingCreated -> "Finding created"
    AuditEventType.ReportGenerated -> "Report generated"
    AuditEventType.ScanCancelled -> "Scan cancelled"
    AuditEventType.ReportRequested -> "Report requested"
    AuditEventType.PolicyChanged -> "Policy changed"
    AuditEventType.UserAction -> "User action"
}

private fun eventSummary(event: AuditEvent): String {
    val d = event.details ?: return eventTypeLabel(event.eventType)
    fun str(key: String) = d[key]?.toString()?.trim('"') ?: ""
    return when (event.eventType) {
        AuditEventType.SessionCreated -> str("session_name").ifBlank { "New session" }
        AuditEventType.UploadAdded -> str("filename").ifBlank { "File added" }
        AuditEventType.ScanStarted -> "Scan ${str("scan_id")} started"
        AuditEventType.ScanCancelled -> "Scan ${str("scan_id")} cancelled"
        AuditEventType.ScanCompleted -> "${str("findings_count")} findings · score ${str("overall_score")}"
        AuditEventType.ReportRequested -> "${str("report_requested")} report requested"
        AuditEventType.FindingCreated -> "[${str("severity")}] ${str("title")}".ifBlank { "Finding created" }
        AuditEventType.ReportGenerated -> "${str("format").uppercase()} report generated"
        AuditEventType.PolicyChanged -> str("added_packs").takeIf { it.isNotBlank() }
            ?.let { "Added: $it" } ?: "Policy updated"

        AuditEventType.UserAction -> str("action").ifBlank { "User action" }
    }
}

private fun eventTypeIcon(type: AuditEventType): ImageVector = when (type) {
    AuditEventType.SessionCreated -> Icons.Default.FolderOpen
    AuditEventType.UploadAdded -> Icons.Default.Upload
    AuditEventType.ScanStarted -> Icons.Default.PlayArrow
    AuditEventType.ScanCompleted -> Icons.Default.CheckCircle
    AuditEventType.ScanCancelled -> Icons.Default.Cancel
    AuditEventType.FindingCreated -> Icons.Default.BugReport
    AuditEventType.ReportRequested -> Icons.Default.Report
    AuditEventType.ReportGenerated -> Icons.Default.Description
    AuditEventType.PolicyChanged -> Icons.Default.Shield
    AuditEventType.UserAction -> Icons.Default.Person
}

@Composable
private fun eventTypeColor(type: AuditEventType, colors: AuditForgeColors) = when (type) {
    AuditEventType.SessionCreated -> colors.accentDefault
    AuditEventType.UploadAdded -> colors.accentDefault
    AuditEventType.ScanStarted -> colors.accentDefault
    AuditEventType.ScanCancelled -> colors.severityHigh
    AuditEventType.ScanCompleted -> colors.severityResolved
    AuditEventType.FindingCreated -> colors.severityHigh
    AuditEventType.ReportGenerated -> colors.severityLow
    AuditEventType.ReportRequested -> colors.severityLow
    AuditEventType.PolicyChanged -> colors.textSecondary
    AuditEventType.UserAction -> colors.textSecondary
}
