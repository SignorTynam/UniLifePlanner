package com.example.unilifeplanner.ui.exams.components

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
import com.example.unilifeplanner.ui.exams.ExamAppealListItemUi
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import java.time.Instant
import java.time.ZoneId

@Composable
fun ExamDaySection(
    dayTitle: String,
    exams: List<ExamAppealListItemUi>,
    onEditClick: (ExamAppealListItemUi) -> Unit,
    onFeedbackClick: (ExamAppealListItemUi) -> Unit,
    onDeleteClick: (ExamAppealListItemUi) -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    onToggleReminderClick: (ExamAppealListItemUi) -> Unit,
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
        exams.forEach { exam ->
            ExamTimelineItem(
                exam = exam,
                onEditClick = { onEditClick(exam) },
                onFeedbackClick = { onFeedbackClick(exam) },
                onDeleteClick = { onDeleteClick(exam) },
                onOpenCourseClick = { onOpenCourseClick(exam.courseId) },
                onToggleReminderClick = { onToggleReminderClick(exam) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExamDaySectionPreview() {
    val dayKey = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val exams = listOf(
        ExamAppealListItemUi(
            examAppealId = 1,
            courseId = 1,
            courseName = "Analisi I",
            dateLabel = "12/06/2026",
            timeLabel = "09:00",
            location = "Aula 2",
            notes = null,
            type = "Scritto",
            reminderEnabled = true,
            sourceLabel = "UniBo",
            feedbackStatus = "NOT_REQUESTED",
            feedbackResult = null,
            feedbackGrade = null,
            startMillis = 0L,
            isPast = false,
            dateMillis = dayKey,
            dayKeyMillis = dayKey,
            relativeDateLabel = "Oggi"
        )
    )
    UniLifePlannerTheme {
        ExamDaySection(
            dayTitle = "Oggi",
            exams = exams,
            onEditClick = {},
            onFeedbackClick = {},
            onDeleteClick = {},
            onOpenCourseClick = {},
            onToggleReminderClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
