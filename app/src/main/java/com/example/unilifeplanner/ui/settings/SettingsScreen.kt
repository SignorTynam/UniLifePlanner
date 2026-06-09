package com.example.unilifeplanner.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.settings.components.SettingsDataSection
import com.example.unilifeplanner.ui.settings.components.SettingsHeroHeader
import com.example.unilifeplanner.ui.settings.components.SettingsPreferencesSection
import com.example.unilifeplanner.ui.settings.components.ThemeModeSelector
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearPlannerDataDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.clearPlannerDataMessage, uiState.clearPlannerDataError) {
        val feedback = uiState.clearPlannerDataMessage ?: uiState.clearPlannerDataError
        if (feedback != null) {
            snackbarHostState.showSnackbar(feedback)
            viewModel.clearPlannerDataFeedback()
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        showClearPlannerDataDialog = showClearPlannerDataDialog,
        onShowClearPlannerDataDialogChange = { showClearPlannerDataDialog = it },
        onMenuClick = onMenuClick,
        onThemeModeSelected = viewModel::onThemeModeSelected,
        onClearPlannerDataClick = {
            viewModel.clearPlannerDataFeedback()
            showClearPlannerDataDialog = true
        },
        onConfirmClearPlannerData = {
            showClearPlannerDataDialog = false
            viewModel.clearPlannerData()
        }
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    showClearPlannerDataDialog: Boolean,
    onShowClearPlannerDataDialogChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onClearPlannerDataClick: () -> Unit,
    onConfirmClearPlannerData: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Impostazioni",
                onMenuClick = onMenuClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                SettingsHeroHeader(selectedThemeMode = uiState.selectedThemeMode)
            }
            item {
                ThemeModeSelector(
                    selectedThemeMode = uiState.selectedThemeMode,
                    onThemeModeSelected = onThemeModeSelected
                )
            }
            item {
                SettingsPreferencesSection()
            }
            item {
                SettingsDataSection(
                    isClearingPlannerData = uiState.isClearingPlannerData,
                    onClearPlannerDataClick = onClearPlannerDataClick
                )
            }
        }
    }

    if (showClearPlannerDataDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isClearingPlannerData) {
                    onShowClearPlannerDataDialogChange(false)
                }
            },
            title = {
                Text(
                    text = "Cancellare i dati del planner?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Questa azione eliminerà corsi, lezioni, appelli d'esame, preferiti, dati importati e promemoria collegati. Account, profilo, email e tema resteranno invariati. L'azione non può essere annullata."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmClearPlannerData,
                    enabled = !uiState.isClearingPlannerData
                ) {
                    Text(
                        text = "Cancella dati",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { onShowClearPlannerDataDialogChange(false) },
                    enabled = !uiState.isClearingPlannerData
                ) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenLightPreview() {
    UniLifePlannerTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(selectedThemeMode = ThemeMode.LIGHT),
            snackbarHostState = remember { SnackbarHostState() },
            showClearPlannerDataDialog = false,
            onShowClearPlannerDataDialogChange = {},
            onMenuClick = {},
            onThemeModeSelected = {},
            onClearPlannerDataClick = {},
            onConfirmClearPlannerData = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenClearingPreview() {
    UniLifePlannerTheme {
        SettingsScreenContent(
            uiState = SettingsUiState(
                selectedThemeMode = ThemeMode.SYSTEM,
                isClearingPlannerData = true
            ),
            snackbarHostState = remember { SnackbarHostState() },
            showClearPlannerDataDialog = true,
            onShowClearPlannerDataDialogChange = {},
            onMenuClick = {},
            onThemeModeSelected = {},
            onClearPlannerDataClick = {},
            onConfirmClearPlannerData = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        SettingsScreenContent(
            uiState = SettingsUiState(selectedThemeMode = ThemeMode.DARK),
            snackbarHostState = remember { SnackbarHostState() },
            showClearPlannerDataDialog = false,
            onShowClearPlannerDataDialogChange = {},
            onMenuClick = {},
            onThemeModeSelected = {},
            onClearPlannerDataClick = {},
            onConfirmClearPlannerData = {}
        )
    }
}
