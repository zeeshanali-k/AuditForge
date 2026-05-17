package com.devscion.auditforge.ui.settings

enum class ThemeMode { System, Light, Dark }

enum class ConnectionTestState { Idle, Testing, Success, Failed }

data class SettingsUiState(
    val connectionTestState: ConnectionTestState = ConnectionTestState.Idle,
)

sealed class SettingsIntent {
    data object TestConnection : SettingsIntent()
    data object DismissConnectionResult : SettingsIntent()
}
