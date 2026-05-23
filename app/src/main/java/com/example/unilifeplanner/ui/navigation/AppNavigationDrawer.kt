package com.example.unilifeplanner.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.BuildConfig
import com.example.unilifeplanner.navigation.Screen
import com.example.unilifeplanner.ui.components.UniLifeProfileAvatar

@Composable
fun AppNavigationDrawer(
    drawerState: DrawerState,
    currentRoute: String?,
    isAuthenticated: Boolean,
    gesturesEnabled: Boolean,
    onNavigateHome: () -> Unit,
    onNavigateCourses: () -> Unit,
    onNavigateStatistics: () -> Unit,
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
                onNavigateStatistics = onNavigateStatistics,
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
    onNavigateStatistics: () -> Unit,
    onNavigateMap: () -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateSettings: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        DrawerProfileHeader(
            firstName = uiState.firstName,
            lastName = uiState.lastName,
            email = uiState.email,
            profileImageUri = uiState.profileImageUri,
            appVersion = BuildConfig.VERSION_NAME
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        AppDrawerItem(
            label = "Home",
            icon = Icons.Filled.Home,
            selected = currentRoute == Screen.Home.route,
            onClick = onNavigateHome
        )
        AppDrawerItem(
            label = "Corsi",
            icon = Icons.Filled.School,
            selected = currentRoute == Screen.Courses.route,
            onClick = onNavigateCourses
        )
        AppDrawerItem(
            label = "Statistiche",
            icon = Icons.Filled.BarChart,
            selected = currentRoute == Screen.Statistics.route,
            onClick = onNavigateStatistics
        )
        AppDrawerItem(
            label = "Mappa",
            icon = Icons.Filled.Map,
            selected = currentRoute == Screen.Map.route,
            onClick = onNavigateMap
        )
        AppDrawerItem(
            label = "Profilo",
            icon = Icons.Filled.AccountCircle,
            selected = currentRoute == Screen.Profile.route,
            onClick = onNavigateProfile
        )
        AppDrawerItem(
            label = "Impostazioni",
            icon = Icons.Filled.Settings,
            selected = currentRoute == Screen.Settings.route,
            onClick = onNavigateSettings
        )
        AppDrawerItem(
            label = "Logout",
            icon = Icons.AutoMirrored.Filled.Logout,
            selected = false,
            onClick = onLogout
        )
    }
}

@Composable
fun DrawerProfileHeader(
    firstName: String,
    lastName: String,
    email: String,
    profileImageUri: String?,
    appVersion: String,
    modifier: Modifier = Modifier
) {
    val displayName = listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Studente" }
    val secondaryText = email.trim().ifBlank { "Menu navigazione" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniLifeProfileAvatar(
            profileImageUri = profileImageUri,
            size = 64.dp
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = secondaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Versione $appVersion",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AppDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationDrawerItem(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    )
}
