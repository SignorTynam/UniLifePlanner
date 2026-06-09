package com.example.unilifeplanner.ui.statistics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.statistics.StatisticsUiState
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamsStatsSection(
    uiState: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Appelli d'esame",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (uiState.nextExamCourseName != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Prossimo esame",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = uiState.nextExamCourseName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        val dateLine = listOfNotNull(
                            uiState.nextExamRelativeLabel,
                            uiState.nextExamDateLabel
                        ).joinToString(" · ")
                        Text(
                            text = dateLine,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Text(
                    text = "Nessun esame futuro registrato",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExamMetricPill("Futuri", uiState.upcomingExamCount)
                ExamMetricPill("Passati", uiState.pastExamCount)
                ExamMetricPill("Promemoria", uiState.examReminderCount)
                if (uiState.pendingFeedbackCount > 0) {
                    ExamMetricPill("Esiti da registrare", uiState.pendingFeedbackCount)
                }
                if (uiState.passedExamCount > 0) {
                    ExamMetricPill("Superati", uiState.passedExamCount)
                }
                if (uiState.failedExamCount > 0) {
                    ExamMetricPill("Non superati", uiState.failedExamCount)
                }
                if (uiState.waitingResultExamCount > 0) {
                    ExamMetricPill("In attesa", uiState.waitingResultExamCount)
                }
            }
        }
    }
}

@Composable
private fun ExamMetricPill(label: String, value: Int) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Preview(showBackground = true, name = "Con esame")
@Composable
private fun ExamsStatsSectionWithExamPreview() {
    UniLifePlannerTheme {
        ExamsStatsSection(
            uiState = StatisticsUiState(
                isEmpty = false,
                nextExamCourseName = "Analisi Matematica I",
                nextExamRelativeLabel = "Domani",
                nextExamDateLabel = "12/06/2026",
                upcomingExamCount = 3,
                pastExamCount = 2,
                examReminderCount = 1,
                pendingFeedbackCount = 1,
                passedExamCount = 4,
                failedExamCount = 1
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Nessun esame")
@Composable
private fun ExamsStatsSectionEmptyPreview() {
    UniLifePlannerTheme {
        ExamsStatsSection(
            uiState = StatisticsUiState(isEmpty = false),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Nome lungo Dark")
@Composable
private fun ExamsStatsSectionLongNameDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        ExamsStatsSection(
            uiState = StatisticsUiState(
                isEmpty = false,
                nextExamCourseName = "Fondamenti di Ingegneria del Software e Progettazione Avanzata",
                nextExamRelativeLabel = "Venerdì",
                nextExamDateLabel = "20/06/2026"
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
