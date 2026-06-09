package com.example.unilifeplanner.ui.lessons.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.lessons.LessonsUiState
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun LessonsAgendaHeader(
    uiState: LessonsUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "La tua settimana",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = agendaHeaderSubtitle(uiState),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AgendaMetric(
                    icon = { Icon(Icons.Filled.Today, contentDescription = null) },
                    label = "Oggi",
                    value = uiState.todayLessonCount.toString()
                )
                AgendaMetric(
                    icon = { Icon(Icons.Filled.WbSunny, contentDescription = null) },
                    label = "Domani",
                    value = uiState.tomorrowLessonCount.toString()
                )
                AgendaMetric(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                    label = "Promemoria",
                    value = uiState.reminderLessonCount.toString()
                )
            }

            uiState.selectedCourseName?.let { courseName ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = courseName,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaMetric(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonsAgendaHeaderPreview() {
    UniLifePlannerTheme {
        LessonsAgendaHeader(
            uiState = LessonsUiState(
                hasAnyLessons = true,
                todayLessonCount = 2,
                tomorrowLessonCount = 1,
                reminderLessonCount = 3,
                selectedCourseName = "Analisi Matematica I",
                upcomingLessons = emptyList()
            )
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LessonsAgendaHeaderDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        LessonsAgendaHeader(
            uiState = LessonsUiState(
                searchQuery = "aula",
                filteredResultCount = 4,
                todayLessonCount = 0,
                tomorrowLessonCount = 0,
                reminderLessonCount = 1
            )
        )
    }
}
