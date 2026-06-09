package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.exams.ExamAppealListItemUi
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExamTimelineItem(
    exam: ExamAppealListItemUi,
    onEditClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenCourseClick: () -> Unit,
    onToggleReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val feedbackText = exam.feedbackPillText()
    val isFeedbackPending = exam.needsFeedbackRegistration()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        if (isFeedbackPending) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExamDateBlock(
                    dayKeyMillis = exam.dayKeyMillis,
                    timeLabel = exam.timeLabel
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = exam.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = exam.relativeDateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    exam.location?.takeIf { it.isNotBlank() }?.let { location ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    exam.type?.takeIf { it.isNotBlank() }?.let { type ->
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    exam.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusPill(
                            text = exam.sourceLabel,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        StatusPill(
                            text = if (exam.reminderEnabled) {
                                "Promemoria attivo"
                            } else {
                                "Promemoria disattivato"
                            },
                            containerColor = if (exam.reminderEnabled) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            contentColor = if (exam.reminderEnabled) {
                                MaterialTheme.colorScheme.onTertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        feedbackText?.let { text ->
                            StatusPill(
                                text = text,
                                containerColor = if (isFeedbackPending) {
                                    MaterialTheme.colorScheme.errorContainer
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                                contentColor = if (isFeedbackPending) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExamActionPill(
                            label = "Modifica",
                            icon = Icons.Filled.Edit,
                            contentDescription = "Modifica appello",
                            onClick = onEditClick
                        )
                        ExamActionPill(
                            label = "Corso",
                            icon = Icons.Filled.School,
                            contentDescription = "Apri corso",
                            onClick = onOpenCourseClick
                        )
                        ExamActionPill(
                            label = if (isFeedbackPending || exam.isPast) {
                                if (isFeedbackPending) "Registra esito" else "Esito"
                            } else {
                                "Esito"
                            },
                            icon = Icons.Filled.Assignment,
                            contentDescription = "Registra esito appello",
                            onClick = onFeedbackClick
                        )
                        ExamActionPill(
                            label = if (exam.reminderEnabled) "Disattiva" else "Promemoria",
                            icon = if (exam.reminderEnabled) {
                                Icons.Filled.NotificationsOff
                            } else {
                                Icons.Filled.Notifications
                            },
                            contentDescription = if (exam.reminderEnabled) {
                                "Disattiva promemoria appello"
                            } else {
                                "Attiva promemoria appello"
                            },
                            onClick = onToggleReminderClick,
                            enabled = !exam.isPast || !exam.reminderEnabled
                        )
                        ExamActionPill(
                            label = "Elimina",
                            icon = Icons.Filled.Delete,
                            contentDescription = "Elimina appello",
                            onClick = onDeleteClick,
                            isDestructive = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamDateBlock(
    dayKeyMillis: Long,
    timeLabel: String?,
    modifier: Modifier = Modifier
) {
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(dayKeyMillis).atZone(zoneId).toLocalDate()
    val dayNumber = date.dayOfMonth.toString()
    val monthShort = date.month.getDisplayName(TextStyle.SHORT, Locale.ITALY)
        .replaceFirstChar { it.uppercase(Locale.ITALY) }
        .take(3)

    Surface(
        modifier = modifier.width(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = dayNumber,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = monthShort,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            timeLabel?.let { time ->
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExamActionPill(
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = if (isDestructive) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(14.dp),
                tint = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isDestructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}

private fun sampleExam(
    courseName: String = "Analisi Matematica I",
    relativeDateLabel: String = "Oggi",
    sourceLabel: String = "UniBo",
    reminderEnabled: Boolean = true,
    isPast: Boolean = false,
    feedbackStatus: String = "NOT_REQUESTED",
    feedbackResult: String? = null,
    feedbackGrade: String? = null,
    location: String? = "Aula 3.1",
    type: String? = "Scritto",
    notes: String? = null
): ExamAppealListItemUi {
    val now = System.currentTimeMillis()
    val dayKey = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return ExamAppealListItemUi(
        examAppealId = 1,
        courseId = 1,
        courseName = courseName,
        dateLabel = "12/06/2026",
        timeLabel = "09:00",
        location = location,
        notes = notes,
        type = type,
        reminderEnabled = reminderEnabled,
        sourceLabel = sourceLabel,
        feedbackStatus = feedbackStatus,
        feedbackResult = feedbackResult,
        feedbackGrade = feedbackGrade,
        startMillis = now,
        isPast = isPast,
        dateMillis = dayKey,
        dayKeyMillis = dayKey,
        relativeDateLabel = relativeDateLabel
    )
}

@Preview(showBackground = true, name = "UniBo futuro")
@Composable
private fun ExamTimelineItemFuturePreview() {
    UniLifePlannerTheme {
        ExamTimelineItem(
            exam = sampleExam(),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Manuale")
@Composable
private fun ExamTimelineItemManualPreview() {
    UniLifePlannerTheme {
        ExamTimelineItem(
            exam = sampleExam(sourceLabel = "Manuale", type = "Orale"),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Esito da registrare")
@Composable
private fun ExamTimelineItemPendingFeedbackPreview() {
    UniLifePlannerTheme {
        ExamTimelineItem(
            exam = sampleExam(
                isPast = true,
                feedbackStatus = "PENDING",
                relativeDateLabel = "Ieri"
            ),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Superato")
@Composable
private fun ExamTimelineItemPassedPreview() {
    UniLifePlannerTheme {
        ExamTimelineItem(
            exam = sampleExam(
                isPast = true,
                feedbackStatus = "ANSWERED",
                feedbackResult = "PASSED",
                feedbackGrade = "28",
                relativeDateLabel = "Lunedì, 02/06"
            ),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Nome lungo")
@Composable
private fun ExamTimelineItemLongNamePreview() {
    UniLifePlannerTheme {
        ExamTimelineItem(
            exam = sampleExam(
                courseName = "Fondamenti di Ingegneria del Software e Progettazione Avanzata dei Sistemi Informatici"
            ),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ExamTimelineItemDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        ExamTimelineItem(
            exam = sampleExam(reminderEnabled = false, location = null),
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}
