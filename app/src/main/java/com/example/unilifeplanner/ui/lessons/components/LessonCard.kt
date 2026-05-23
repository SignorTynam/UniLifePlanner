package com.example.unilifeplanner.ui.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.lessons.LessonListItemUi

@Composable
fun LessonCard(
    lesson: LessonListItemUi,
    onEditClick: () -> Unit,
    onOpenMapsClick: () -> Unit,
    onOpenCourseClick: () -> Unit,
    onToggleReminderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasLocation = lesson.courseName.isNotBlank() ||
        !lesson.locationQuery.isNullOrBlank() ||
        !lesson.classroom.isNullOrBlank() ||
        !lesson.building.isNullOrBlank()

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = lesson.courseName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${lesson.relativeDayLabel} - ${lesson.startTime} - ${lesson.endTime}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            LessonLocationRow(
                classroom = lesson.classroom,
                building = lesson.building
            )

            lesson.locationQuery?.takeIf { it.isNotBlank() }?.let { locationQuery ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = locationQuery,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (lesson.reminderEnabled) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (lesson.reminderEnabled) {
                        "Promemoria attivo"
                    } else {
                        "Promemoria disattivato"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Modifica lezione"
                    )
                }
                IconButton(
                    onClick = onOpenMapsClick,
                    enabled = hasLocation
                ) {
                    Icon(
                        imageVector = Icons.Filled.Map,
                        contentDescription = "Apri luogo lezione in Google Maps"
                    )
                }
                IconButton(onClick = onOpenCourseClick) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "Apri corso"
                    )
                }
                IconButton(onClick = onToggleReminderClick) {
                    Icon(
                        imageVector = if (lesson.reminderEnabled) {
                            Icons.Filled.NotificationsOff
                        } else {
                            Icons.Filled.Notifications
                        },
                        contentDescription = if (lesson.reminderEnabled) {
                            "Disattiva promemoria lezione"
                        } else {
                            "Attiva promemoria lezione"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonLocationRow(
    classroom: String?,
    building: String?
) {
    val locationText = listOfNotNull(
        classroom?.takeIf { it.isNotBlank() },
        building?.takeIf { it.isNotBlank() }
    )
        .joinToString(" - ")
        .takeIf { it.isNotBlank() }
        ?: return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = locationText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
