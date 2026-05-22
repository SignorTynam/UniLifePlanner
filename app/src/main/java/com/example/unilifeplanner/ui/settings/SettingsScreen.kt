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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Impostazioni",
                onBackClick = onBackClick
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
        }
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
