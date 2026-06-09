package com.example.unilifeplanner.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeQuickActionsSection(
    onOpenCoursesClick: () -> Unit,
    onOpenLessonsClick: () -> Unit,
    onOpenExamsClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
    onOpenUniboImportClick: () -> Unit,
    onOpenProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = listOf(
        QuickActionItem("Corsi", Icons.Default.School, onOpenCoursesClick),
        QuickActionItem("Lezioni", Icons.Default.Schedule, onOpenLessonsClick),
        QuickActionItem("Esami", Icons.Default.Event, onOpenExamsClick),
        QuickActionItem("Statistiche", Icons.Default.BarChart, onOpenStatisticsClick),
        QuickActionItem("Importa UniBo", Icons.Default.CloudDownload, onOpenUniboImportClick),
        QuickActionItem("Profilo", Icons.Default.Person, onOpenProfileClick)
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Azioni rapide",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        actions.chunked(2).forEach { rowActions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowActions.forEach { action ->
                    QuickActionTile(action, modifier = Modifier.weight(1f))
                }
                if (rowActions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickActionTile(
    action: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { action.onClick() },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1
            )
        }
    }
}

private data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
