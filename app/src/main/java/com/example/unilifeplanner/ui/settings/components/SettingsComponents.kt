package com.example.unilifeplanner.ui.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsHeroHeader(
    selectedThemeMode: ThemeMode,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Personalizza UniLife",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Impostazioni",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestisci aspetto, preferenze e dati del planner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsPill(text = selectedThemeMode.displayLabel())
                SettingsPill(text = "Dati locali")
                SettingsPill(text = "Account e profilo invariati")
            }
        }
    }
}

@Composable
fun ThemeModeSelector(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "Aspetto",
        subtitle = "Scegli come visualizzare l'app.",
        modifier = modifier
    ) {
        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { mode ->
            val option = mode.toThemeOption()
            ThemeModeTile(
                title = option.title,
                description = option.description,
                icon = option.icon,
                selected = selectedThemeMode == mode,
                onClick = { onThemeModeSelected(mode) }
            )
        }
    }
}

@Composable
fun SettingsPreferencesSection(
    modifier: Modifier = Modifier
) {
    SettingsSection(
        title = "Preferenze",
        subtitle = "Stato delle funzioni collegate al planner.",
        modifier = modifier
    ) {
        SettingsInfoRow(
            icon = Icons.Filled.Notifications,
            iconContentDescription = "Notifiche",
            title = "Notifiche",
            supportingText = "I promemoria sono gestiti da lezioni ed esami.",
            status = "Gestione automatica"
        )
        SettingsInfoRow(
            icon = Icons.Filled.Star,
            iconContentDescription = "Preferiti",
            title = "Preferiti",
            supportingText = "Gestisci i preferiti dalla schermata Corsi.",
            status = "Disponibile nei corsi"
        )
        SettingsInfoRow(
            icon = Icons.Filled.CloudSync,
            iconContentDescription = "Import UniBo",
            title = "Import UniBo",
            supportingText = "I dati importati possono essere aggiornati dalle sezioni dedicate.",
            status = "Dati locali"
        )
    }
}

@Composable
fun SettingsDataSection(
    isClearingPlannerData: Boolean,
    onClearPlannerDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f),
                contentColor = MaterialTheme.colorScheme.tertiary
            ) {
                Icon(
                    imageVector = Icons.Filled.Storage,
                    contentDescription = "Dati applicazione",
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Dati applicazione",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Controlla cosa viene rimosso dal planner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Il planner salva localmente corsi, lezioni, appelli, preferiti e dati importati.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SettingsMiniList(
                    title = "Verranno eliminati",
                    items = listOf(
                        "Corsi",
                        "Lezioni",
                        "Appelli d'esame",
                        "Preferiti",
                        "Dati importati da UniBo",
                        "Promemoria collegati"
                    )
                )
                SettingsMiniList(
                    title = "Non verranno eliminati",
                    items = listOf(
                        "Account",
                        "Profilo",
                        "Email",
                        "Tema selezionato"
                    )
                )
            }
        }

        SettingsDangerZone(
            isClearingPlannerData = isClearingPlannerData,
            onClearPlannerDataClick = onClearPlannerDataClick
        )
    }
}

@Composable
fun SettingsDangerZone(
    isClearingPlannerData: Boolean,
    onClearPlannerDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = !isClearingPlannerData,
                role = Role.Button,
                onClick = onClearPlannerDataClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.18f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.error
            ) {
                if (isClearingPlannerData) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(12.dp)
                            .size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Cancella dati planner",
                        modifier = Modifier
                            .padding(12.dp)
                            .size(22.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = "Cancella dati del planner",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Elimina corsi, lezioni, esami, preferiti e dati importati. Account, profilo, email e tema resteranno invariati.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (isClearingPlannerData) {
                        "Cancellazione in corso…"
                    } else {
                        "Cancella dati"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content
            )
        }
    }
}

@Composable
private fun ThemeModeTile(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
    }
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                RadioButton(
                    selected = false,
                    onClick = onClick
                )
            }
        }
    }
}

@Composable
private fun SettingsInfoRow(
    icon: ImageVector,
    iconContentDescription: String,
    title: String,
    supportingText: String,
    status: String
) {
    Row(
        modifier = Modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
            contentColor = MaterialTheme.colorScheme.secondary
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp)
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            SettingsPill(text = status)
        }
    }
}

@Composable
private fun SettingsMiniList(
    title: String,
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            items.forEach { item ->
                Text(
                    text = "- $item",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsPill(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class ThemeOption(
    val title: String,
    val description: String,
    val icon: ImageVector
)

private fun ThemeMode.toThemeOption(): ThemeOption =
    when (this) {
        ThemeMode.LIGHT -> ThemeOption(
            title = "Tema chiaro",
            description = "Usa sempre il tema chiaro",
            icon = Icons.Filled.LightMode
        )
        ThemeMode.DARK -> ThemeOption(
            title = "Tema scuro",
            description = "Usa sempre il tema scuro",
            icon = Icons.Filled.DarkMode
        )
        ThemeMode.SYSTEM -> ThemeOption(
            title = "Automatico",
            description = "Segue le impostazioni del sistema",
            icon = Icons.Filled.PhoneAndroid
        )
    }

private fun ThemeMode.displayLabel(): String =
    when (this) {
        ThemeMode.LIGHT -> "Tema chiaro"
        ThemeMode.DARK -> "Tema scuro"
        ThemeMode.SYSTEM -> "Automatico"
    }

@Preview(showBackground = true)
@Composable
private fun SettingsHeroHeaderPreview() {
    UniLifePlannerTheme {
        SettingsHeroHeader(
            selectedThemeMode = ThemeMode.LIGHT,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorLightPreview() {
    UniLifePlannerTheme {
        ThemeModeSelector(
            selectedThemeMode = ThemeMode.LIGHT,
            onThemeModeSelected = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorDarkPreview() {
    UniLifePlannerTheme {
        ThemeModeSelector(
            selectedThemeMode = ThemeMode.DARK,
            onThemeModeSelected = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorSystemPreview() {
    UniLifePlannerTheme {
        ThemeModeSelector(
            selectedThemeMode = ThemeMode.SYSTEM,
            onThemeModeSelected = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreferencesSectionPreview() {
    UniLifePlannerTheme {
        SettingsPreferencesSection(modifier = Modifier.padding(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDataSectionPreview() {
    UniLifePlannerTheme {
        SettingsDataSection(
            isClearingPlannerData = false,
            onClearPlannerDataClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDangerZonePreview() {
    UniLifePlannerTheme {
        SettingsDangerZone(
            isClearingPlannerData = false,
            onClearPlannerDataClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDangerZoneClearingPreview() {
    UniLifePlannerTheme {
        SettingsDangerZone(
            isClearingPlannerData = true,
            onClearPlannerDataClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SettingsHeroHeader(selectedThemeMode = ThemeMode.DARK)
            ThemeModeSelector(
                selectedThemeMode = ThemeMode.DARK,
                onThemeModeSelected = {}
            )
            SettingsPreferencesSection()
            SettingsDataSection(
                isClearingPlannerData = false,
                onClearPlannerDataClick = {}
            )
        }
    }
}
