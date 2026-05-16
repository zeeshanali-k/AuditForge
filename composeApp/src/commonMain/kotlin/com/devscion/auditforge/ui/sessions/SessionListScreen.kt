package com.devscion.auditforge.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.*
import com.devscion.auditforge.ui.theme.*
import org.jetbrains.compose.resources.stringResource

enum class NavDestination {
    Sessions,
    Policies,
    AuditTrail,
    Settings,
}

@Composable
fun SessionListScreen(
    onCreateSession: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val colors = MaterialTheme.auditForgeColors
    var selectedNav by remember { mutableStateOf(NavDestination.Sessions) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfacePrimary),
    ) {
        AppSidebar(
            selected = selectedNav,
            onSelect = { selectedNav = it },
            colors = colors,
        )

        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                onLogout = onLogout,
                colors = colors,
            )

            HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)

            SessionListContent(
                onCreateSession = onCreateSession,
                colors = colors,
            )
        }
    }
}

@Composable
private fun AppSidebar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit,
    colors: AuditForgeColors,
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .width(200.dp)
            .border(
                width = 1.dp,
                color = colors.borderDefault,
                shape = RoundedCornerShape(0.dp),
            ),
        containerColor = colors.surfaceSecondary,
        contentColor = colors.textSecondary,
        header = {
            Spacer(modifier = Modifier.height(Spacing.sm))
        },
    ) {
        NavDestination.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = {
                    Icon(
                        imageVector = navIcon(dest),
                        contentDescription = navLabel(dest),
                        modifier = Modifier.size(18.dp),
                    )
                },
                label = {
                    Text(
                        text = navLabel(dest),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = colors.accentDefault,
                    selectedTextColor = colors.accentDefault,
                    indicatorColor = colors.accentDefault.copy(alpha = 0.12f),
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
                modifier = Modifier.padding(horizontal = Spacing.sm),
            )
        }
    }
}

@Composable
private fun AppTopBar(
    onLogout: () -> Unit,
    colors: AuditForgeColors,
) {
    var searchQuery by remember { mutableStateOf("") }
    var showUserMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.surfacePrimary)
            .padding(horizontal = Spacing.xl),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = stringResource(Res.string.sessions_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.a11y_search),
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp),
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(Shape.input),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.borderFocus,
                unfocusedBorderColor = colors.borderDefault,
                focusedContainerColor = colors.surfacePrimary,
                unfocusedContainerColor = colors.surfacePrimary,
                cursorColor = colors.accentDefault,
            ),
            modifier = Modifier
                .width(320.dp)
                .height(40.dp),
        )

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(colors.accentDefault),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.textOnAccent,
                        modifier = Modifier.size(16.dp),
                    )
                }
                TextButton(
                    onClick = { showUserMenu = true },
                    contentPadding = PaddingValues(horizontal = Spacing.xs),
                ) {
                    Text(
                        text = stringResource(Res.string.sessions_account),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary,
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.a11y_user_menu),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            DropdownMenu(
                expanded = showUserMenu,
                onDismissRequest = { showUserMenu = false },
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.sessions_sign_out),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.auditForgeColors.severityCritical,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.auditForgeColors.severityCritical,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    onClick = {
                        showUserMenu = false
                        onLogout()
                    },
                )
            }
        }
    }
}

@Composable
private fun SessionListContent(
    onCreateSession: () -> Unit,
    colors: AuditForgeColors,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xxl, vertical = Spacing.xl),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(Res.string.sessions_page_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary,
            )
            Button(
                onClick = onCreateSession,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentDefault,
                    contentColor = colors.textOnAccent,
                ),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.sessions_create_button),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))

        EmptySessionsState(
            onCreateSession = onCreateSession,
            colors = colors,
        )
    }
}

@Composable
private fun EmptySessionsState(
    onCreateSession: () -> Unit,
    colors: AuditForgeColors,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier
                .widthIn(max = 480.dp)
                .border(
                    width = 1.dp,
                    color = colors.borderDefault,
                    shape = RoundedCornerShape(Shape.card),
                )
                .padding(Spacing.xxxl),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Shape.card))
                    .background(colors.accentDefault.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Article,
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
                    text = stringResource(Res.string.sessions_empty_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary,
                )
                Text(
                    text = stringResource(Res.string.sessions_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                )
            }

            OutlinedButton(
                onClick = onCreateSession,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.accentDefault,
                ),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.sessions_empty_cta),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun navLabel(dest: NavDestination): String = stringResource(
    when (dest) {
        NavDestination.Sessions -> Res.string.nav_sessions
        NavDestination.Policies -> Res.string.nav_policies
        NavDestination.AuditTrail -> Res.string.nav_audit_trail
        NavDestination.Settings -> Res.string.nav_settings
    }
)

private fun navIcon(dest: NavDestination) = when (dest) {
    NavDestination.Sessions -> Icons.AutoMirrored.Filled.Article
    NavDestination.Policies -> Icons.Default.Security
    NavDestination.AuditTrail -> Icons.Default.History
    NavDestination.Settings -> Icons.Default.Settings
}
