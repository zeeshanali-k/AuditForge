package com.devscion.auditforge.ui.login

sealed class LoginError {
    data object EmailRequired : LoginError()
    data object EmailInvalid : LoginError()
    data object PasswordRequired : LoginError()
    data object PasswordTooShort : LoginError()
    data object InvalidCredentials : LoginError()
    data object NetworkError : LoginError()
    data class Unknown(val rawMessage: String) : LoginError()
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val isSuccess: Boolean = false,
)

sealed class LoginIntent {
    data class UpdateEmail(val value: String) : LoginIntent()
    data class UpdatePassword(val value: String) : LoginIntent()
    data object TogglePasswordVisibility : LoginIntent()
    data object ToggleRememberMe : LoginIntent()
    data object SignIn : LoginIntent()
    data object ClearError : LoginIntent()
}
