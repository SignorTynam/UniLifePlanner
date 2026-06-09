package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
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
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.exams.ExamsUiState
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamsAgendaHeader(
    uiState: ExamsUiState,
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
                    text = "Sessione esami",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = examsAgendaHeaderSubtitle(uiState),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ExamMetric(
                    icon = { Icon(Icons.Filled.Event, contentDescription = null) },
                    label = "Prossimi",
                    value = uiState.upcomingExamCount.toString()
                )
                ExamMetric(
                    icon = { Icon(Icons.Filled.CalendarMonth, contentDescription = null) },
                    label = "Questa settimana",
                    value = uiState.thisWeekExamCount.toString()
                )
                ExamMetric(
                    icon = { Icon(Icons.Filled.Notifications, contentDescription = null) },
                    label = "Promemoria",
                    value = uiState.reminderExamCount.toString()
                )
                ExamMetric(
                    icon = { Icon(Icons.Filled.Assignment, contentDescription = null) },
                    label = "Esiti",
                    value = uiState.pendingFeedbackCount.toString()
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
private fun ExamMetric(
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
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamsAgendaHeaderPreview() {
    UniLifePlannerTheme {
        ExamsAgendaHeader(
            uiState = ExamsUiState(
                hasAnyExams = true,
                upcomingExamCount = 4,
                thisWeekExamCount = 2,
                reminderExamCount = 3,
                pendingFeedbackCount = 1,
                selectedCourseName = "Analisi Matematica I"
            )
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ExamsAgendaHeaderDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        ExamsAgendaHeader(
            uiState = ExamsUiState(
                searchQuery = "aula",
                filteredResultCount = 2
            )
        )
    }
}
