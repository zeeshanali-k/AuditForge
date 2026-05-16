package com.devscion.auditforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devscion.auditforge.data.storage.TokenStorage
import com.devscion.auditforge.ui.Login
import com.devscion.auditforge.ui.SessionList
import com.devscion.auditforge.ui.login.LoginScreen
import com.devscion.auditforge.ui.sessions.SessionListScreen
import com.devscion.auditforge.ui.theme.AuditForgeTheme

@Suppress("DEPRECATION")
@Composable
fun App() {
    AuditForgeTheme {
        val navController = rememberNavController()
        val tokenStorage = remember { TokenStorage() }
        val startDestination: Any = if (tokenStorage.getToken() != null) SessionList else Login

        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(SessionList) {
                            popUpTo<Login> { inclusive = true }
                        }
                    },
                )
            }

            composable<SessionList> {
                SessionListScreen(
                    onCreateSession = { /* Phase 2 */ },
                    onLogout = {
                        tokenStorage.clearToken()
                        navController.navigate(Login) {
                            popUpTo<SessionList> { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}
