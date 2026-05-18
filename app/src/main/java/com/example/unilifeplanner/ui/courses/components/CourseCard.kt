package com.example.unilifeplanner.ui.courses.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Room
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ExamDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
fun CourseCard(
    course: CourseEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    CourseInfoRow(
                        icon = Icons.Filled.Person,
                        text = "Docente: ${course.professor}"
                    )
                }
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (course.isFavorite) {
                            Icons.Filled.Star
                        } else {
                            Icons.Filled.StarBorder
                        },
                        contentDescription = if (course.isFavorite) {
                            "Rimuovi dai preferiti"
                        } else {
                            "Aggiungi ai preferiti"
                        },
                        tint = if (course.isFavorite) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            CourseInfoRow(
                icon = Icons.Filled.CalendarMonth,
                text = "Esame: ${formatExamDate(course.examDate)}"
            )
            course.classroom?.takeIf { it.isNotBlank() }?.let { classroom ->
                Spacer(modifier = Modifier.height(6.dp))
                CourseInfoRow(
                    icon = Icons.Filled.Room,
                    text = "Aula: $classroom"
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            CourseInfoRow(
                icon = Icons.Filled.School,
                text = "CFU: ${course.credits}"
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Stato: ${formatCourseStatus(course.status)}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CourseInfoRow(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatCourseStatus(status: String): String {
    return when (status) {
        CourseStatus.TO_STUDY.name -> "Da studiare"
        CourseStatus.IN_PROGRESS.name -> "In corso"
        CourseStatus.COMPLETED.name -> "Completato"
        else -> status
    }
}

fun formatExamDate(timestamp: Long?): String {
    if (timestamp == null) return "Data non impostata"

    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(ExamDateFormatter)
}

@Preview(showBackground = true)
@Composable
private fun CourseCardPreview() {
    UniLifePlannerTheme {
        CourseCard(
            course = CourseEntity(
                id = 1,
                name = "Programmazione Mobile",
                professor = "Prof. Rossi",
                examDate = 1782345600000,
                classroom = "A2",
                credits = 6,
                status = CourseStatus.IN_PROGRESS.name,
                isFavorite = true,
                notes = null,
                createdAt = 0L,
                updatedAt = 0L
            ),
            onClick = {},
            onFavoriteClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
