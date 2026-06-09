package com.example.unilifeplanner.ui.lessons.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.lessons.LessonListItemUi
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun LessonDaySection(
    dayTitle: String,
    lessons: List<LessonListItemUi>,
    onEditClick: (LessonListItemUi) -> Unit,
    onOpenMapsClick: (LessonListItemUi) -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    onToggleReminderClick: (LessonListItemUi) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = dayTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        lessons.forEach { lesson ->
            LessonTimelineItem(
                lesson = lesson,
                onEditClick = { onEditClick(lesson) },
                onOpenMapsClick = { onOpenMapsClick(lesson) },
                onOpenCourseClick = { onOpenCourseClick(lesson.courseId) },
                onToggleReminderClick = { onToggleReminderClick(lesson) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonDaySectionPreview() {
    val lessons = listOf(
        LessonListItemUi(
            lessonId = 1,
            courseId = 1,
            courseName = "Analisi I",
            courseProfessor = "Prof. Rossi",
            dayOfWeek = 1,
            dayLabel = "Lunedi",
            relativeDayLabel = "Oggi",
            startTime = "09:00",
            endTime = "11:00",
            classroom = "Aula 2",
            building = "Polo",
            locationQuery = null,
            notes = null,
            reminderEnabled = true,
            nextOccurrenceMillis = 0L,
            isPastThisWeek = false
        ),
        LessonListItemUi(
            lessonId = 2,
            courseId = 2,
            courseName = "Fisica",
            courseProfessor = "Prof. Bianchi",
            dayOfWeek = 1,
            dayLabel = "Lunedi",
            relativeDayLabel = "Oggi",
            startTime = "14:00",
            endTime = "16:00",
            classroom = "Lab 3",
            building = "Ingegneria",
            locationQuery = null,
            notes = null,
            reminderEnabled = false,
            nextOccurrenceMillis = 0L,
            isPastThisWeek = false
        )
    )
    UniLifePlannerTheme {
        LessonDaySection(
            dayTitle = "Oggi",
            lessons = lessons,
            onEditClick = {},
            onOpenMapsClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
