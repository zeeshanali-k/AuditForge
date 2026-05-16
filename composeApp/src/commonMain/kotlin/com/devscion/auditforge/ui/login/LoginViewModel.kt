package com.devscion.auditforge.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.ApiResult
import com.devscion.auditforge.data.storage.TokenStorage
import com.devscion.auditforge.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val tokenStorage: TokenStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun hasExistingToken(): Boolean = tokenStorage.getToken() != null

    fun onIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> _uiState.update { it.copy(email = intent.value, error = null) }
            is LoginIntent.UpdatePassword -> _uiState.update { it.copy(password = intent.value, error = null) }
            LoginIntent.TogglePasswordVisibility -> _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
            LoginIntent.ToggleRememberMe -> _uiState.update { it.copy(rememberMe = !it.rememberMe) }
            LoginIntent.SignIn -> signIn()
            LoginIntent.ClearError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun signIn() {
        val current = _uiState.value
        val validationError = validate(current.email, current.password)
        if (validationError != null) {
            _uiState.update { it.copy(error = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = loginUseCase(current.email.trim(), current.password)) {
                is ApiResult.Success -> {
                    tokenStorage.saveToken(result.data.token)
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = if (result.code == 401) LoginError.InvalidCredentials
                                else LoginError.Unknown(result.message),
                    )
                }
                ApiResult.Unauthorized -> _uiState.update {
                    it.copy(isLoading = false, error = LoginError.InvalidCredentials)
                }
                ApiResult.NetworkError -> _uiState.update {
                    it.copy(isLoading = false, error = LoginError.NetworkError)
                }
            }
        }
    }

    private fun validate(email: String, password: String): LoginError? = when {
        email.isBlank() -> LoginError.EmailRequired
        !email.contains("@") -> LoginError.EmailInvalid
        password.isBlank() -> LoginError.PasswordRequired
        password.length < 6 -> LoginError.PasswordTooShort
        else -> null
    }
}
