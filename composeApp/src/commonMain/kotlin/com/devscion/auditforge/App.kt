package com.devscion.auditforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.devscion.auditforge.data.storage.TokenStorage
import com.devscion.auditforge.ui.Login
import com.devscion.auditforge.ui.SessionDetail
import com.devscion.auditforge.ui.SessionList
import com.devscion.auditforge.ui.login.LoginScreen
import com.devscion.auditforge.ui.sessions.detail.SessionDetailScreen
import com.devscion.auditforge.ui.sessions.list.SessionListScreen
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
                    onSessionClick = { sessionId ->
                        navController.navigate(SessionDetail(sessionId))
                    },
                    onLogout = {
                        tokenStorage.clearToken()
                        navController.navigate(Login) {
                            popUpTo<SessionList> { inclusive = true }
                        }
                    },
                )
            }

            composable<SessionDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<SessionDetail>()
                SessionDetailScreen(
                    sessionId = route.sessionId,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
