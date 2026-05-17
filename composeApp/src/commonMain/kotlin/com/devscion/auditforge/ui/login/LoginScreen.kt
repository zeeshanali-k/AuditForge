package com.devscion.auditforge.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.ui.theme.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.auditForgeColors
    val errorMessage = uiState.error?.let { loginErrorString(it) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceSecondary),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 400.dp),
        ) {
            ProductNameHeader(colors = colors)
            Spacer(modifier = Modifier.height(Spacing.xl))
            LoginCard(
                uiState = uiState,
                onIntent = viewModel::onIntent,
                errorMessage = errorMessage,
                colors = colors,
            )
        }
    }
}

@Composable
private fun loginErrorString(error: LoginError): String = stringResource(
    when (error) {
        LoginError.EmailRequired -> Res.string.error_email_required
        LoginError.EmailInvalid -> Res.string.error_email_invalid
        LoginError.PasswordRequired -> Res.string.error_password_required
        LoginError.PasswordTooShort -> Res.string.error_password_too_short
        LoginError.InvalidCredentials -> Res.string.error_invalid_credentials
        LoginError.NetworkError -> Res.string.error_network
        is LoginError.Unknown -> Res.string.error_unknown
    }
)

@Composable
private fun ProductNameHeader(colors: AuditForgeColors) {
    Text(
        text = stringResource(Res.string.app_name),
        style = MaterialTheme.typography.displayLarge,
        color = colors.textPrimary,
    )
    Spacer(modifier = Modifier.height(Spacing.sm))
    Text(
        text = stringResource(Res.string.app_tagline),
        style = MaterialTheme.typography.bodyMedium,
        color = colors.textSecondary,
    )
}

@Composable
private fun LoginCard(
    uiState: LoginUiState,
    onIntent: (LoginIntent) -> Unit,
    errorMessage: String?,
    colors: AuditForgeColors,
) {
    val passwordFocusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = colors.borderDefault,
                shape = RoundedCornerShape(Shape.card),
            ),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FieldLabel(stringResource(Res.string.login_email_label))
                AuditForgeTextField(
                    value = uiState.email,
                    onValueChange = { onIntent(LoginIntent.UpdateEmail(it)) },
                    placeholder = stringResource(Res.string.login_email_placeholder),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() },
                    ),
                    enabled = !uiState.isLoading,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                FieldLabel(stringResource(Res.string.login_password_label))
                AuditForgeTextField(
                    value = uiState.password,
                    onValueChange = { onIntent(LoginIntent.UpdatePassword(it)) },
                    placeholder = stringResource(Res.string.login_password_label),
                    visualTransformation = if (uiState.passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (!uiState.isLoading) onIntent(LoginIntent.SignIn) },
                    ),
                    enabled = !uiState.isLoading,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    trailingIcon = {
                        IconButton(onClick = { onIntent(LoginIntent.TogglePasswordVisibility) }) {
                            Icon(
                                imageVector = if (uiState.passwordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = stringResource(
                                    if (uiState.passwordVisible) {
                                        Res.string.login_password_hide
                                    } else {
                                        Res.string.login_password_show
                                    }
                                ),
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    },
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Checkbox(
                    checked = uiState.rememberMe,
                    onCheckedChange = { onIntent(LoginIntent.ToggleRememberMe) },
                    enabled = !uiState.isLoading,
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accentDefault,
                        uncheckedColor = colors.borderStrong,
                        checkmarkColor = colors.textOnAccent,
                    ),
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(Res.string.login_remember_me),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                errorMessage?.let {
                    ErrorBanner(message = it, colors = colors)
                }
            }

            SignInButton(
                onClick = { onIntent(LoginIntent.SignIn) },
                isLoading = uiState.isLoading,
                colors = colors,
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.auditForgeColors.textSecondary,
    )
}

@Composable
private fun AuditForgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    enabled: Boolean = true,
    colors: AuditForgeColors,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textTertiary,
            )
        },
        singleLine = true,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        enabled = enabled,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(Shape.input),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.borderFocus,
            unfocusedBorderColor = colors.borderDefault,
            disabledBorderColor = colors.borderDefault,
            focusedContainerColor = colors.surfacePrimary,
            unfocusedContainerColor = colors.surfacePrimary,
            disabledContainerColor = colors.surfaceSunken,
            cursorColor = colors.accentDefault,
        ),
        modifier = modifier,
    )
}

@Composable
private fun ErrorBanner(message: String, colors: AuditForgeColors) {
    Surface(
        shape = RoundedCornerShape(Shape.badge),
        color = colors.severityCritical.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.severityCritical,
            modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
        )
    }
}

@Composable
private fun SignInButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    colors: AuditForgeColors,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(Shape.button),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.accentDefault,
            contentColor = colors.textOnAccent,
            disabledContainerColor = colors.accentDefault.copy(alpha = 0.5f),
            disabledContentColor = colors.textOnAccent.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = Spacing.lg),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = colors.textOnAccent,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = stringResource(Res.string.login_signing_in),
                style = MaterialTheme.typography.labelLarge,
            )
        } else {
            Text(
                text = stringResource(Res.string.login_sign_in),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
