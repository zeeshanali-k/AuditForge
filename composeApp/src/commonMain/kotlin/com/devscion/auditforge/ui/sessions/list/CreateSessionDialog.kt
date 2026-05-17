package com.devscion.auditforge.ui.sessions.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.domain.model.TargetEnvironment
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateSessionDialog(
    uiState: SessionListUiState,
    onIntent: (SessionListIntent) -> Unit,
    colors: AuditForgeColors,
) {
    AlertDialog(
        onDismissRequest = { if (!uiState.isCreating) onIntent(SessionListIntent.HideCreateDialog) },
        title = {
            Text(
                text = stringResource(Res.string.create_session_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
            )
        },
        text = {
            CreateSessionForm(uiState = uiState, onIntent = onIntent, colors = colors)
        },
        confirmButton = {
            Button(
                onClick = { onIntent(SessionListIntent.SubmitCreate) },
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentDefault,
                    contentColor = colors.textOnAccent,
                ),
            ) {
                if (uiState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = colors.textOnAccent,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        stringResource(Res.string.create_session_submitting),
                        style = MaterialTheme.typography.labelLarge
                    )
                } else {
                    Text(
                        stringResource(Res.string.create_session_submit),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(SessionListIntent.HideCreateDialog) },
                enabled = !uiState.isCreating
            ) {
                Text(
                    text = stringResource(Res.string.create_session_cancel),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(Shape.modal),
    )
}

@Composable
private fun CreateSessionForm(
    uiState: SessionListUiState,
    onIntent: (SessionListIntent) -> Unit,
    colors: AuditForgeColors,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FieldLabel(stringResource(Res.string.create_session_name_label), colors)
            OutlinedTextField(
                value = uiState.newSessionName,
                onValueChange = { onIntent(SessionListIntent.UpdateNewName(it)) },
                placeholder = {
                    Text(
                        stringResource(Res.string.create_session_name_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary
                    )
                },
                singleLine = true,
                isError = uiState.nameValidationError != null,
                supportingText = uiState.nameValidationError?.let { key ->
                    {
                        Text(
                            stringResource(nameValidationErrorRes(key)),
                            color = colors.severityCritical,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(Shape.input),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.borderFocus,
                    unfocusedBorderColor = colors.borderDefault,
                    errorBorderColor = colors.severityCritical,
                    focusedContainerColor = colors.surfacePrimary,
                    unfocusedContainerColor = colors.surfacePrimary,
                    cursorColor = colors.accentDefault,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FieldLabel(stringResource(Res.string.create_session_description_label), colors)
            OutlinedTextField(
                value = uiState.newSessionDescription,
                onValueChange = { onIntent(SessionListIntent.UpdateNewDescription(it)) },
                placeholder = {
                    Text(
                        stringResource(Res.string.create_session_description_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textTertiary
                    )
                },
                minLines = 3,
                maxLines = 5,
                enabled = !uiState.isCreating,
                shape = RoundedCornerShape(Shape.input),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.borderFocus,
                    unfocusedBorderColor = colors.borderDefault,
                    focusedContainerColor = colors.surfacePrimary,
                    unfocusedContainerColor = colors.surfacePrimary,
                    cursorColor = colors.accentDefault,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            FieldLabel(stringResource(Res.string.create_session_environment_label), colors)
            EnvironmentSelector(
                selected = uiState.newSessionEnvironment,
                onSelect = { onIntent(SessionListIntent.UpdateNewEnvironment(it)) },
                enabled = !uiState.isCreating,
                colors = colors
            )
        }
    }
}

@Composable
private fun EnvironmentSelector(
    selected: TargetEnvironment?,
    onSelect: (TargetEnvironment?) -> Unit,
    enabled: Boolean,
    colors: AuditForgeColors,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        TargetEnvironment.entries.forEach { env ->
            val isSelected = selected == env
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected) null else env) },
                enabled = enabled,
                label = {
                    Text(
                        stringResource(environmentLabel(env)),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = colors.accentDefault.copy(alpha = 0.12f),
                    selectedLabelColor = colors.accentDefault,
                    labelColor = colors.textSecondary,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = isSelected,
                    borderColor = colors.borderDefault,
                    selectedBorderColor = colors.accentDefault,
                ),
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String, colors: AuditForgeColors) {
    Text(text = text, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
}

@Composable
private fun environmentLabel(env: TargetEnvironment) = when (env) {
    TargetEnvironment.Development -> Res.string.create_session_env_development
    TargetEnvironment.Staging -> Res.string.create_session_env_staging
    TargetEnvironment.Production -> Res.string.create_session_env_production
}

private fun nameValidationErrorRes(key: String) = when (key) {
    "session_name_too_short" -> Res.string.error_session_name_too_short
    "session_name_too_long" -> Res.string.error_session_name_too_long
    else -> Res.string.error_session_name_required
}
