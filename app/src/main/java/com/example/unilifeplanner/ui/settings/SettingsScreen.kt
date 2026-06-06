package com.example.unilifeplanner.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun SettingsScreen(
    onMenuClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearPlannerDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Impostazioni",
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = "Aspetto",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ThemeOptionCard(
                    title = "Tema chiaro",
                    description = "Usa sempre il tema chiaro",
                    selected = uiState.selectedThemeMode == ThemeMode.LIGHT,
                    onClick = { viewModel.onThemeModeSelected(ThemeMode.LIGHT) }
                )
                ThemeOptionCard(
                    title = "Tema scuro",
                    description = "Usa sempre il tema scuro",
                    selected = uiState.selectedThemeMode == ThemeMode.DARK,
                    onClick = { viewModel.onThemeModeSelected(ThemeMode.DARK) }
                )
                ThemeOptionCard(
                    title = "Automatico",
                    description = "Segue le impostazioni del sistema",
                    selected = uiState.selectedThemeMode == ThemeMode.SYSTEM,
                    onClick = { viewModel.onThemeModeSelected(ThemeMode.SYSTEM) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preferenze",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(text = "Notifiche") },
                    supportingContent = { Text(text = "Placeholder non attivo") }
                )
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(text = "Preferiti") },
                    supportingContent = { Text(text = "Gestione disponibile nella sezione corsi") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Dati applicazione",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                ListItem(
                    modifier = Modifier.clickable(
                        enabled = !uiState.isClearingPlannerData,
                        onClick = {
                            viewModel.clearPlannerDataFeedback()
                            showClearPlannerDataDialog = true
                        }
                    ),
                    leadingContent = {
                        if (uiState.isClearingPlannerData) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    headlineContent = {
                        Text(
                            text = if (uiState.isClearingPlannerData) {
                                "Cancellazione in corso..."
                            } else {
                                "Cancella dati del planner"
                            },
                            color = MaterialTheme.colorScheme.error
                        )
                    },
                    supportingContent = {
                        Text(
                            text = "Elimina corsi, lezioni, esami, preferiti e dati importati. Non elimina account, profilo, email o tema."
                        )
                    }
                )
            }

            uiState.clearPlannerDataMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            uiState.clearPlannerDataError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showClearPlannerDataDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isClearingPlannerData) {
                    showClearPlannerDataDialog = false
                }
            },
            title = { Text(text = "Cancellare i dati del planner?") },
            text = {
                Text(
                    text = "Questa azione eliminerà corsi, lezioni, esami, preferiti e dati importati. Account, profilo e impostazioni personali resteranno invariati. L'azione non può essere annullata."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearPlannerDataDialog = false
                        viewModel.clearPlannerData()
                    },
                    enabled = !uiState.isClearingPlannerData
                ) {
                    Text(text = "Cancella dati")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearPlannerDataDialog = false },
                    enabled = !uiState.isClearingPlannerData
                ) {
                    Text(text = "Annulla")
                }
            },
            containerColor = MaterialTheme.colorScheme.errorContainer,
            titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
            textContentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.RadioButton,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            RadioButton(
                selected = selected,
                onClick = onClick
            )
        }
    }
}
