package com.devscion.auditforge.ui.sessions.detail.uploads

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.UploadSummary
import com.devscion.auditforge.domain.model.UploadType
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun UploadsTab(
    sessionId: String,
    colors: AuditForgeColors,
    viewModel: UploadsViewModel = koinViewModel(parameters = { parametersOf(sessionId) }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
            if (uiState.isUploading) {
                UploadProgressBar(colors = colors)
            } else {
                DropZone(
                    onClick = { viewModel.onIntent(UploadsIntent.ShowTypeSelector) },
                    colors = colors,
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> UploadsLoadingSkeleton(colors)
                uiState.uploads.isEmpty() -> UploadsEmptyState(colors)
                else -> UploadsFileTable(
                    uploads = uiState.uploads,
                    onDelete = { viewModel.onIntent(UploadsIntent.RequestDelete(it)) },
                    colors = colors,
                )
            }
        }

        if (uiState.error != null) {
            Snackbar(
                action = {
                    TextButton(onClick = { viewModel.onIntent(UploadsIntent.DismissError) }) {
                        Text(stringResource(Res.string.a11y_close), color = colors.textOnAccent)
                    }
                },
                modifier = Modifier.padding(Spacing.lg),
                containerColor = colors.severityCritical,
            ) {
                Text(uiState.error!!, color = colors.textOnAccent)
            }
        }
    }

    if (uiState.showTypeSelector) {
        UploadTypeSelectorDialog(
            onSelect = { viewModel.onIntent(UploadsIntent.SelectTypeAndPickFile(it)) },
            onDismiss = { viewModel.onIntent(UploadsIntent.HideTypeSelector) },
            colors = colors,
        )
    }

    uiState.uploadToDelete?.let { upload ->
        DeleteUploadDialog(
            filename = upload.filename,
            isDeleting = uiState.isDeleting,
            onConfirm = { viewModel.onIntent(UploadsIntent.ConfirmDelete) },
            onDismiss = { viewModel.onIntent(UploadsIntent.CancelDelete) },
            colors = colors,
        )
    }
}

@Composable
private fun DropZone(onClick: () -> Unit, colors: AuditForgeColors) {
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
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 4f)),
                    ),
                )
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(colors.surfaceSecondary)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(24.dp),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text = stringResource(Res.string.uploads_drop_zone_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = stringResource(Res.string.uploads_drop_zone_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }
        }
    }
}

@Composable
private fun UploadProgressBar(colors: AuditForgeColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceSecondary,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = stringResource(Res.string.upload_uploading),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textPrimary,
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = colors.accentDefault,
                trackColor = colors.borderDefault,
            )
        }
    }
}

@Composable
private fun UploadsFileTable(
    uploads: List<UploadSummary>,
    onDelete: (UploadSummary) -> Unit,
    colors: AuditForgeColors,
) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl)
            .padding(bottom = Spacing.xl),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.surfaceSunken)
                        .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TableHeaderCell(
                        text = stringResource(Res.string.uploads_table_col_filename),
                        modifier = Modifier.weight(1f),
                        colors = colors,
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.uploads_table_col_type),
                        modifier = Modifier.width(80.dp),
                        colors = colors,
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.uploads_table_col_size),
                        modifier = Modifier.width(120.dp),
                        colors = colors,
                    )
                    TableHeaderCell(
                        text = stringResource(Res.string.uploads_table_col_uploaded),
                        modifier = Modifier.width(130.dp),
                        colors = colors,
                    )
                    Box(modifier = Modifier.width(40.dp))
                }
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
            items(uploads, key = { it.id }) { upload ->
                UploadTableRow(
                    upload = upload,
                    onDelete = { onDelete(upload) },
                    colors = colors,
                )
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
        }
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
private fun UploadTableRow(
    upload: UploadSummary,
    onDelete: () -> Unit,
    colors: AuditForgeColors,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).padding(end = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = uploadTypeIcon(upload.uploadType),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = upload.filename,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.width(80.dp)) {
            Text(
                text = stringResource(uploadTypeLabel(upload.uploadType)),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
            )
        }
        Box(modifier = Modifier.width(120.dp)) {
            Text(
                text = formatFileSize(upload.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }
        Box(modifier = Modifier.width(130.dp)) {
            Text(
                text = upload.uploadedAt.take(10),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.upload_delete_action),
                    tint = colors.textTertiary,
                    modifier = Modifier.size(13.dp),
                )
            }
        }
    }
}

@Composable
private fun UploadsEmptyState(colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            Text(
                text = stringResource(Res.string.uploads_empty_title),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = stringResource(Res.string.uploads_empty_description),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textTertiary,
            )
        }
    }
}

@Composable
private fun UploadsLoadingSkeleton(colors: AuditForgeColors) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xl)
            .padding(bottom = Spacing.xl),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.borderDefault),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceSunken)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
            ) {
                Box(
                    modifier = Modifier.weight(1f).height(11.dp)
                        .background(colors.borderDefault, RoundedCornerShape(4.dp))
                )
            }
            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            repeat(5) {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    Box(
                        modifier = Modifier.size(14.dp)
                            .background(colors.borderDefault, RoundedCornerShape(2.dp))
                    )
                    Box(
                        modifier = Modifier.weight(1f).height(12.dp)
                            .background(colors.borderDefault, RoundedCornerShape(4.dp))
                    )
                    Box(
                        modifier = Modifier.width(80.dp).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                    Box(
                        modifier = Modifier.width(120.dp).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                    Box(
                        modifier = Modifier.width(130.dp).height(12.dp).background(
                            colors.borderDefault.copy(alpha = 0.5f),
                            RoundedCornerShape(4.dp)
                        )
                    )
                    Box(modifier = Modifier.width(40.dp))
                }
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun UploadTypeSelectorDialog(
    onSelect: (UploadType) -> Unit,
    onDismiss: () -> Unit,
    colors: AuditForgeColors,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(Res.string.upload_type_select_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                UploadType.entries.forEach { type ->
                    TextButton(
                        onClick = { onSelect(type) },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(
                            horizontal = Spacing.sm,
                            vertical = Spacing.xs
                        ),
                    ) {
                        Text(
                            text = stringResource(uploadTypeLabel(type)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textPrimary,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.create_session_cancel), color = colors.textSecondary)
            }
        },
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(Shape.modal),
    )
}

@Composable
private fun DeleteUploadDialog(
    filename: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    colors: AuditForgeColors,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = {
            Text(
                stringResource(Res.string.upload_confirm_delete_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
        },
        text = {
            Text(
                stringResource(Res.string.upload_confirm_delete_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.severityCritical,
                    contentColor = colors.textOnAccent,
                ),
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.textOnAccent,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                }
                Text(stringResource(Res.string.upload_confirm_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(
                    stringResource(Res.string.upload_confirm_delete_cancel),
                    color = colors.textSecondary
                )
            }
        },
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(Shape.modal),
    )
}

private fun uploadTypeIcon(type: UploadType) = when (type) {
    UploadType.Codebase -> Icons.Default.Code
    UploadType.OpenapiSpec -> Icons.AutoMirrored.Filled.LibraryBooks
    UploadType.Config -> Icons.Default.Tune
    UploadType.DbSchema -> Icons.Default.TableChart
    UploadType.EnvFile -> Icons.Default.Description
    UploadType.Terraform -> Icons.Default.Cloud
    UploadType.Kubernetes -> Icons.Default.Hub
}

private fun uploadTypeLabel(type: UploadType): StringResource = when (type) {
    UploadType.Codebase -> Res.string.upload_type_codebase
    UploadType.OpenapiSpec -> Res.string.upload_type_openapi_spec
    UploadType.Config -> Res.string.upload_type_config
    UploadType.DbSchema -> Res.string.upload_type_db_schema
    UploadType.EnvFile -> Res.string.upload_type_env_file
    UploadType.Terraform -> Res.string.upload_type_terraform
    UploadType.Kubernetes -> Res.string.upload_type_kubernetes
}

private fun formatFileSize(bytes: Long): String = when {
    bytes < 1_024 -> "$bytes B"
    bytes < 1_048_576 -> "${bytes / 1_024} KB"
    else -> "${"%.1f".format(bytes / 1_048_576.0)} MB"
}
