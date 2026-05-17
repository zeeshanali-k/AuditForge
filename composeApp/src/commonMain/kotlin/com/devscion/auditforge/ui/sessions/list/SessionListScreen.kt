package com.devscion.auditforge.ui.sessions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import auditforge.composeapp.generated.resources.Res
import auditforge.composeapp.generated.resources.a11y_search
import auditforge.composeapp.generated.resources.a11y_user_menu
import auditforge.composeapp.generated.resources.app_name
import auditforge.composeapp.generated.resources.delete_session_cancel
import auditforge.composeapp.generated.resources.delete_session_confirm
import auditforge.composeapp.generated.resources.delete_session_message
import auditforge.composeapp.generated.resources.delete_session_title
import auditforge.composeapp.generated.resources.nav_policies
import auditforge.composeapp.generated.resources.nav_sessions
import auditforge.composeapp.generated.resources.nav_settings
import auditforge.composeapp.generated.resources.session_created_label
import auditforge.composeapp.generated.resources.session_delete_action
import auditforge.composeapp.generated.resources.session_detail_more_actions
import auditforge.composeapp.generated.resources.session_detail_placeholder
import auditforge.composeapp.generated.resources.session_findings
import auditforge.composeapp.generated.resources.sessions_account
import auditforge.composeapp.generated.resources.sessions_create_button
import auditforge.composeapp.generated.resources.sessions_empty_cta
import auditforge.composeapp.generated.resources.sessions_empty_description
import auditforge.composeapp.generated.resources.sessions_empty_title
import auditforge.composeapp.generated.resources.sessions_filter_all
import auditforge.composeapp.generated.resources.sessions_filter_completed
import auditforge.composeapp.generated.resources.sessions_filter_created
import auditforge.composeapp.generated.resources.sessions_filter_failed
import auditforge.composeapp.generated.resources.sessions_filter_scanning
import auditforge.composeapp.generated.resources.sessions_of
import auditforge.composeapp.generated.resources.sessions_page_title
import auditforge.composeapp.generated.resources.sessions_results
import auditforge.composeapp.generated.resources.sessions_search_placeholder
import auditforge.composeapp.generated.resources.sessions_sign_out
import com.devscion.auditforge.domain.model.SessionStatus
import com.devscion.auditforge.domain.model.SessionSummary
import com.devscion.auditforge.ui.policies.PolicyLibraryContent
import com.devscion.auditforge.ui.sessions.detail.ScoreBadge
import com.devscion.auditforge.ui.sessions.detail.StatusBadge
import com.devscion.auditforge.ui.settings.SettingsContent
import com.devscion.auditforge.ui.settings.ThemeMode
import com.devscion.auditforge.ui.theme.AuditForgeColors
import com.devscion.auditforge.ui.theme.Shape
import com.devscion.auditforge.ui.theme.Spacing
import com.devscion.auditforge.ui.theme.auditForgeColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

enum class NavDestination { Sessions, Policies, Settings }

@Composable
fun SessionListScreen(
    onSessionClick: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    currentTheme: ThemeMode = ThemeMode.System,
    onThemeChange: (ThemeMode) -> Unit = {},
    viewModel: SessionListViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.auditForgeColors
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedNav by remember { mutableStateOf(NavDestination.Sessions) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onIntent(SessionListIntent.DismissError)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surfacePrimary,
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AppSidebar(selected = selectedNav, onSelect = { selectedNav = it }, colors = colors)

            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(
                    searchQuery = uiState.searchQuery,
                    onSearchChange = { viewModel.onIntent(SessionListIntent.UpdateSearch(it)) },
                    onLogout = onLogout,
                    colors = colors,
                )
                HorizontalDivider(color = colors.borderDefault, thickness = 1.dp)
                when (selectedNav) {
                    NavDestination.Sessions -> SessionListContent(
                        uiState = uiState,
                        onIntent = viewModel::onIntent,
                        onSessionClick = onSessionClick,
                        colors = colors
                    )

                    NavDestination.Policies -> PolicyLibraryContent(colors = colors)

                    NavDestination.Settings -> SettingsContent(
                        currentTheme = currentTheme,
                        onThemeChange = onThemeChange,
                        colors = colors,
                    )
                }
            }
        }
    }

    if (uiState.showCreateDialog) {
        CreateSessionDialog(uiState = uiState, onIntent = viewModel::onIntent, colors = colors)
    }

    uiState.sessionToDelete?.let { session ->
        DeleteConfirmationDialog(
            sessionName = session.name,
            isDeleting = uiState.isDeleting,
            onConfirm = { viewModel.onIntent(SessionListIntent.ConfirmDelete) },
            onDismiss = { viewModel.onIntent(SessionListIntent.CancelDelete) },
            colors = colors,
        )
    }
}

@Composable
private fun AppSidebar(
    selected: NavDestination,
    onSelect: (NavDestination) -> Unit,
    colors: AuditForgeColors,
) {
    NavigationRail(
        modifier = Modifier.fillMaxHeight().width(200.dp)
            .border(width = 1.dp, color = colors.borderDefault, shape = RoundedCornerShape(0.dp)),
        containerColor = colors.surfaceSecondary,
        contentColor = colors.textSecondary,
        header = { Spacer(modifier = Modifier.height(Spacing.sm)) },
    ) {
        NavDestination.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = {
                    Icon(
                        imageVector = navIcon(dest),
                        contentDescription = navLabel(dest),
                        modifier = Modifier.size(18.dp)
                    )
                },
                label = {
                    Text(
                        text = navLabel(dest),
                        style = MaterialTheme.typography.labelMedium
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
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLogout: () -> Unit,
    colors: AuditForgeColors,
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth()
            .background(colors.surfacePrimary)
            .padding(horizontal = Spacing.xl, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    stringResource(Res.string.sessions_search_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textTertiary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.a11y_search),
                    tint = colors.textTertiary,
                    modifier = Modifier.size(16.dp)
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
            modifier = Modifier.width(320.dp)
                .weight(1f),
        )

        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape)
                        .background(colors.accentDefault), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = colors.textOnAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                TextButton(
                    onClick = { showUserMenu = true },
                    contentPadding = PaddingValues(horizontal = Spacing.xs)
                ) {
                    Text(
                        text = stringResource(Res.string.sessions_account),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.textSecondary
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(Res.string.a11y_user_menu),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            DropdownMenu(expanded = showUserMenu, onDismissRequest = { showUserMenu = false }) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(Res.string.sessions_sign_out),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.auditForgeColors.severityCritical
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.auditForgeColors.severityCritical,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = { showUserMenu = false; onLogout() },
                )
            }
        }
    }
}

@Composable
private fun SessionListContent(
    uiState: SessionListUiState,
    onIntent: (SessionListIntent) -> Unit,
    onSessionClick: (String) -> Unit,
    colors: AuditForgeColors,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.xxl, vertical = Spacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.sessions_page_title),
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary
            )
            Button(
                onClick = { onIntent(SessionListIntent.ShowCreateDialog) },
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentDefault,
                    contentColor = colors.textOnAccent
                ),
                contentPadding = PaddingValues(horizontal = Spacing.lg, vertical = Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.sessions_create_button),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        StatusFilterBar(
            selectedStatus = uiState.statusFilter,
            onSelect = { onIntent(SessionListIntent.UpdateStatusFilter(it)) },
            colors = colors
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        when {
            uiState.isLoading -> SessionListSkeleton(colors)
            uiState.filteredSessions.isEmpty() -> EmptySessionsState(onCreateSession = {
                onIntent(
                    SessionListIntent.ShowCreateDialog
                )
            }, colors = colors)

            else -> SessionList(
                uiState = uiState,
                onIntent = onIntent,
                onSessionClick = onSessionClick,
                colors = colors
            )
        }
    }
}

@Composable
private fun StatusFilterBar(
    selectedStatus: SessionStatus?,
    onSelect: (SessionStatus?) -> Unit,
    colors: AuditForgeColors,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatusFilterChip(
            label = stringResource(Res.string.sessions_filter_all),
            selected = selectedStatus == null,
            onClick = { onSelect(null) },
            colors = colors
        )
        SessionStatus.entries.forEach { status ->
            StatusFilterChip(
                label = stringResource(statusFilterLabel(status)),
                selected = selectedStatus == status,
                onClick = { onSelect(if (selectedStatus == status) null else status) },
                colors = colors,
            )
        }
    }
}

@Composable
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: AuditForgeColors
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label, style = MaterialTheme.typography.labelMedium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.accentDefault.copy(
                alpha = 0.12f
            ), selectedLabelColor = colors.accentDefault, labelColor = colors.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.borderDefault,
            selectedBorderColor = colors.accentDefault
        ),
    )
}

@Composable
private fun SessionList(
    uiState: SessionListUiState,
    onIntent: (SessionListIntent) -> Unit,
    onSessionClick: (String) -> Unit,
    colors: AuditForgeColors,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState.firstVisibleItemIndex) {
        val lastVisible =
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@LaunchedEffect
        if (lastVisible >= uiState.filteredSessions.size - 3 && uiState.hasNextPage) {
            onIntent(SessionListIntent.LoadNextPage)
        }
    }

    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        modifier = Modifier.fillMaxSize()
    ) {
        items(uiState.filteredSessions, key = { it.id }) { session ->
            SessionRow(
                session = session,
                onClick = { onSessionClick(session.id) },
                onDelete = { onIntent(SessionListIntent.RequestDelete(session)) },
                colors = colors,
            )
        }

        if (uiState.isLoadingMore) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colors.accentDefault,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        uiState.pagination?.let { page ->
            item {
                Text(
                    text = "${uiState.sessions.size} ${stringResource(Res.string.sessions_of)} ${page.totalItems} ${
                        stringResource(
                            Res.string.sessions_results
                        )
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(top = Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: SessionSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    colors: AuditForgeColors,
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth().border(
            width = 1.dp,
            color = colors.borderDefault,
            shape = RoundedCornerShape(Shape.card)
        ).clickable(onClick = onClick),
        shape = RoundedCornerShape(Shape.card),
        color = colors.surfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${stringResource(Res.string.session_created_label)} ${formatDate(session.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                )
            }

            StatusBadge(status = session.status, colors = colors)

            if (session.findingCount > 0) {
                Text(
                    text = "${session.findingCount} ${stringResource(Res.string.session_findings)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }

            session.overallScore?.let { score -> ScoreBadge(score = score, colors = colors) }

            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(Res.string.session_detail_more_actions),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                stringResource(Res.string.session_delete_action),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.auditForgeColors.severityCritical
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.auditForgeColors.severityCritical,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = { showMenu = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionListSkeleton(colors: AuditForgeColors) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        repeat(5) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(68.dp)
                    .border(1.dp, colors.borderDefault, RoundedCornerShape(Shape.card)),
                shape = RoundedCornerShape(Shape.card),
                color = colors.surfaceElevated,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(0.5f).height(14.dp)
                                .background(colors.borderDefault, RoundedCornerShape(4.dp))
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(0.3f).height(10.dp).background(
                                colors.borderDefault.copy(alpha = 0.5f),
                                RoundedCornerShape(4.dp)
                            )
                        )
                    }
                    Box(
                        modifier = Modifier.width(64.dp).height(20.dp)
                            .background(colors.borderDefault, RoundedCornerShape(Shape.badge))
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptySessionsState(onCreateSession: () -> Unit, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg),
            modifier = Modifier.widthIn(max = 480.dp).border(
                width = 1.dp,
                color = colors.borderDefault,
                shape = RoundedCornerShape(Shape.card)
            ).padding(Spacing.xxxl),
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(Shape.card))
                    .background(colors.accentDefault.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Article,
                    contentDescription = null,
                    tint = colors.accentDefault,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = stringResource(Res.string.sessions_empty_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = colors.textPrimary
                )
                Text(
                    text = stringResource(Res.string.sessions_empty_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
            OutlinedButton(
                onClick = onCreateSession,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.accentDefault),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = stringResource(Res.string.sessions_empty_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    sessionName: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    colors: AuditForgeColors,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = {
            Text(
                stringResource(Res.string.delete_session_title),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary
            )
        },
        text = {
            Text(
                stringResource(Res.string.delete_session_message),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                shape = RoundedCornerShape(Shape.button),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.severityCritical,
                    contentColor = colors.textOnAccent
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
                Text(
                    stringResource(Res.string.delete_session_confirm),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(
                    stringResource(Res.string.delete_session_cancel),
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = colors.surfaceElevated,
        shape = RoundedCornerShape(Shape.modal),
    )
}

@Composable
private fun NavPlaceholder(dest: NavDestination, colors: AuditForgeColors) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                imageVector = navIcon(dest),
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = navLabel(dest),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
            )
            Text(
                text = stringResource(Res.string.session_detail_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun navLabel(dest: NavDestination): String = stringResource(
    when (dest) {
        NavDestination.Sessions -> Res.string.nav_sessions
        NavDestination.Policies -> Res.string.nav_policies
        NavDestination.Settings -> Res.string.nav_settings
    }
)

private fun navIcon(dest: NavDestination) = when (dest) {
    NavDestination.Sessions -> Icons.AutoMirrored.Filled.Article
    NavDestination.Policies -> Icons.Default.Security
    NavDestination.Settings -> Icons.Default.Settings
}

@Composable
private fun statusFilterLabel(status: SessionStatus) = when (status) {
    SessionStatus.Created -> Res.string.sessions_filter_created
    SessionStatus.Scanning -> Res.string.sessions_filter_scanning
    SessionStatus.Completed -> Res.string.sessions_filter_completed
    SessionStatus.Failed -> Res.string.sessions_filter_failed
}

private fun formatDate(isoDate: String): String = isoDate.take(10).replace('-', '/')
