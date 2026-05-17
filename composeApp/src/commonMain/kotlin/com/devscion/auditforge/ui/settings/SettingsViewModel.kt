package com.devscion.auditforge.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devscion.auditforge.data.network.AppConfig
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(private val http: HttpClient) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onIntent(intent: SettingsIntent) {
        when (intent) {
            SettingsIntent.TestConnection -> testConnection()
            SettingsIntent.DismissConnectionResult -> _uiState.update {
                it.copy(connectionTestState = ConnectionTestState.Idle)
            }
        }
    }

    private fun testConnection() {
        viewModelScope.launch {
            _uiState.update { it.copy(connectionTestState = ConnectionTestState.Testing) }
            try {
                http.get(AppConfig.API_BASE_URL)
                _uiState.update { it.copy(connectionTestState = ConnectionTestState.Success) }
            } catch (_: Exception) {
                _uiState.update { it.copy(connectionTestState = ConnectionTestState.Failed) }
            }
        }
    }
}
