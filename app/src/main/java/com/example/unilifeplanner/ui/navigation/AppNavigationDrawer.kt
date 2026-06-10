package com.example.unilifeplanner.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.BuildConfig
import com.example.unilifeplanner.navigation.Screen
import com.example.unilifeplanner.ui.navigation.components.DrawerFooter
import com.example.unilifeplanner.ui.navigation.components.DrawerSection
import com.example.unilifeplanner.ui.navigation.components.LogoutConfirmDialog
import com.example.unilifeplanner.ui.navigation.components.ModernDrawerHeader
import com.example.unilifeplanner.ui.navigation.components.ModernDrawerItem
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    currentRoute: String?,
    isAuthenticated: Boolean,
    gesturesEnabled: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateCourses: () -> Unit,
    onNavigateExams: () -> Unit,
    onNavigateLessons: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onNavigatePublicUniboImport: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val viewModel: AppDrawerViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            viewModel.loadProfile()
        } else {
            viewModel.clearProfile()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            AppDrawerContent(
                uiState = uiState,
                currentRoute = currentRoute,
                onNavigateHome = onNavigateHome,
                onNavigateCourses = onNavigateCourses,
                onNavigateExams = onNavigateExams,
                onNavigateLessons = onNavigateLessons,
                onNavigateStatistics = onNavigateStatistics,
                onNavigatePublicUniboImport = onNavigatePublicUniboImport,
                onNavigateMap = onNavigateMap,
                onNavigateProfile = onNavigateProfile,
                onNavigateSettings = onNavigateSettings,
                onLogout = onLogout
            )
        },
        content = content
    )
}

@Composable
private fun AppDrawerContent(
    uiState: AppDrawerUiState,
    currentRoute: String?,
    onNavigateHome: () -> Unit,
    onNavigateCourses: () -> Unit,
    onNavigateExams: () -> Unit,
    onNavigateLessons: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onNavigatePublicUniboImport: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                onLogout()
            }
        )
    }

    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 320.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            ModernDrawerHeader(
                firstName = uiState.firstName,
                lastName = uiState.lastName,
                email = uiState.email,
                profileImageUri = uiState.profileImageUri,
                appVersion = BuildConfig.VERSION_NAME,
                onProfileClick = onNavigateProfile
            )

            DrawerSection(title = "Dashboard") {
                ModernDrawerItem(
                    label = "Home",
                    icon = Icons.Filled.Home,
                    selected = currentRoute == Screen.Home.route,
                    onClick = onNavigateHome
                )
            }

            DrawerSection(title = "Studio") {
                ModernDrawerItem(
                    label = "Corsi",
                    icon = Icons.Filled.School,
                    selected = currentRoute == Screen.Courses.route,
                    onClick = onNavigateCourses
                )
                ModernDrawerItem(
                    label = "Lezioni",
                    icon = Icons.Filled.Event,
                    selected = isLessonsRoute(currentRoute),
                    onClick = onNavigateLessons
                )
                ModernDrawerItem(
                    label = "Esami",
                    icon = Icons.Filled.CalendarMonth,
                    selected = isExamsRoute(currentRoute),
                    onClick = onNavigateExams
                )
                ModernDrawerItem(
                    label = "Statistiche",
                    icon = Icons.Filled.BarChart,
                    selected = currentRoute == Screen.Statistics.route,
                    onClick = onNavigateStatistics
                )
            }

            DrawerSection(title = "Strumenti") {
                ModernDrawerItem(
                    label = "Importa UniBo",
                    icon = Icons.Filled.AccountBalance,
                    selected = currentRoute == Screen.PublicUniboImport.route,
                    onClick = onNavigatePublicUniboImport
                )
                ModernDrawerItem(
                    label = "Mappa",
                    icon = Icons.Filled.Map,
                    selected = currentRoute == Screen.Map.route,
                    onClick = onNavigateMap
                )
            }

            DrawerSection(title = "Account") {
                ModernDrawerItem(
                    label = "Profilo",
                    icon = Icons.Filled.AccountCircle,
                    selected = currentRoute == Screen.Profile.route,
                    onClick = onNavigateProfile
                )
                ModernDrawerItem(
                    label = "Impostazioni",
                    icon = Icons.Filled.Settings,
                    selected = currentRoute == Screen.Settings.route,
                    onClick = onNavigateSettings
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            ModernDrawerItem(
                label = "Logout",
                icon = Icons.AutoMirrored.Filled.Logout,
                selected = false,
                onClick = { showLogoutDialog = true },
                modifier = Modifier.padding(top = 4.dp)
            )

            DrawerFooter()
        }
    }
}

private fun isLessonsRoute(currentRoute: String?): Boolean =
    currentRoute?.startsWith("lessons") == true || currentRoute == Screen.Lessons.route

private fun isExamsRoute(currentRoute: String?): Boolean =
    currentRoute?.startsWith("exams") == true || currentRoute == Screen.Exams.route

@Preview(name = "Drawer content - light mode", showBackground = true, widthDp = 320, heightDp = 700)
@Composable
private fun PreviewDrawerContentLight() {
    UniLifePlannerTheme {
        AppDrawerContent(
            uiState = AppDrawerUiState(
                firstName = "Mario",
                lastName = "Rossi",
                email = "mario.rossi@studio.unibo.it",
                profileImageUri = null
            ),
            currentRoute = Screen.Home.route,
            onNavigateHome = {},
            onNavigateCourses = {},
            onNavigateExams = {},
            onNavigateLessons = {},
            onNavigateStatistics = {},
            onNavigatePublicUniboImport = {},
            onNavigateMap = {},
            onNavigateProfile = {},
            onNavigateSettings = {},
            onLogout = {}
        )
    }
}

@Preview(name = "Drawer content - dark mode", showBackground = true, widthDp = 320, heightDp = 700, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerContentDark() {
    UniLifePlannerTheme {
        AppDrawerContent(
            uiState = AppDrawerUiState(
                firstName = "Giulia",
                lastName = "Bianchi",
                email = "giulia.bianchi@studio.unibo.it",
                profileImageUri = null
            ),
            currentRoute = Screen.Courses.route,
            onNavigateHome = {},
            onNavigateCourses = {},
            onNavigateExams = {},
            onNavigateLessons = {},
            onNavigateStatistics = {},
            onNavigatePublicUniboImport = {},
            onNavigateMap = {},
            onNavigateProfile = {},
            onNavigateSettings = {},
            onLogout = {}
        )
    }
}
