package com.example.unilifeplanner.ui.lessons.components

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
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
import com.example.unilifeplanner.ui.lessons.LessonListItemUi
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonTimelineItem(
    lesson: LessonListItemUi,
    onEditClick: () -> Unit,
    onOpenMapsClick: () -> Unit,
    onOpenCourseClick: () -> Unit,
    onToggleReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val locationLines = formatLessonLocation(lesson)
    val hasLocation = lessonHasMappableLocation(lesson)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
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
                    .background(MaterialTheme.colorScheme.primary)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeBlock(
                    startTime = lesson.startTime,
                    endTime = lesson.endTime
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lesson.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = lesson.relativeDayLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    LocationSection(locationLines = locationLines)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (lesson.reminderEnabled) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (lesson.reminderEnabled) {
                                    Icons.Filled.Notifications
                                } else {
                                    Icons.Filled.NotificationsOff
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (lesson.reminderEnabled) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = if (lesson.reminderEnabled) {
                                    "Promemoria attivo"
                                } else {
                                    "Promemoria disattivato"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (lesson.reminderEnabled) {
                                    MaterialTheme.colorScheme.onTertiaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TimelineActionPill(
                            label = "Modifica",
                            icon = Icons.Filled.Edit,
                            contentDescription = "Modifica lezione",
                            onClick = onEditClick
                        )
                        TimelineActionPill(
                            label = "Mappa",
                            icon = Icons.Filled.Map,
                            contentDescription = "Apri luogo lezione in Google Maps",
                            onClick = onOpenMapsClick,
                            enabled = hasLocation
                        )
                        TimelineActionPill(
                            label = "Corso",
                            icon = Icons.Filled.School,
                            contentDescription = "Apri corso",
                            onClick = onOpenCourseClick
                        )
                        TimelineActionPill(
                            label = if (lesson.reminderEnabled) "Disattiva" else "Promemoria",
                            icon = if (lesson.reminderEnabled) {
                                Icons.Filled.NotificationsOff
                            } else {
                                Icons.Filled.Notifications
                            },
                            contentDescription = if (lesson.reminderEnabled) {
                                "Disattiva promemoria lezione"
                            } else {
                                "Attiva promemoria lezione"
                            },
                            onClick = onToggleReminderClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeBlock(
    startTime: String,
    endTime: String,
    modifier: Modifier = Modifier
) {
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
                text = startTime,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = endTime,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun LocationSection(locationLines: List<String>) {
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
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (locationLines.isEmpty()) {
                Text(
                    text = "Luogo non specificato",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                locationLines.forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineActionPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
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
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

private fun sampleLesson(
    courseName: String = "Analisi Matematica I",
    relativeDayLabel: String = "Oggi",
    classroom: String? = "Aula 1.5",
    building: String? = "Polo Scientifico",
    locationQuery: String? = null,
    reminderEnabled: Boolean = true
) = LessonListItemUi(
    lessonId = 1,
    courseId = 1,
    courseName = courseName,
    courseProfessor = "Prof. Rossi",
    dayOfWeek = 1,
    dayLabel = "Lunedi",
    relativeDayLabel = relativeDayLabel,
    startTime = "09:00",
    endTime = "12:00",
    classroom = classroom,
    building = building,
    locationQuery = locationQuery,
    notes = null,
    reminderEnabled = reminderEnabled,
    nextOccurrenceMillis = System.currentTimeMillis(),
    isPastThisWeek = false
)

@Preview(showBackground = true, name = "In aula")
@Composable
private fun LessonTimelineItemClassroomPreview() {
    UniLifePlannerTheme {
        LessonTimelineItem(
            lesson = sampleLesson(),
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Solo online")
@Composable
private fun LessonTimelineItemOnlinePreview() {
    UniLifePlannerTheme {
        LessonTimelineItem(
            lesson = sampleLesson(
                courseName = "Informatica",
                classroom = null,
                building = null,
                locationQuery = "Microsoft Teams"
            ),
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Senza luogo")
@Composable
private fun LessonTimelineItemNoLocationPreview() {
    UniLifePlannerTheme {
        LessonTimelineItem(
            lesson = sampleLesson(
                classroom = null,
                building = null,
                locationQuery = null,
                reminderEnabled = false
            ),
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Nome lungo")
@Composable
private fun LessonTimelineItemLongNamePreview() {
    UniLifePlannerTheme {
        LessonTimelineItem(
            lesson = sampleLesson(
                courseName = "Fondamenti di Ingegneria del Software e Progettazione Avanzata dei Sistemi"
            ),
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LessonTimelineItemDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        LessonTimelineItem(
            lesson = sampleLesson(
                relativeDayLabel = "Mercoledì, 12/06",
                locationQuery = "Polo Scientifico - Aula 1.5"
            ),
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {}
        )
    }
}
