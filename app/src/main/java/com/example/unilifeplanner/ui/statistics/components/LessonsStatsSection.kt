package com.example.unilifeplanner.ui.statistics.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.statistics.DayStatUi
import com.example.unilifeplanner.ui.statistics.StatisticsUiState
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun LessonsStatsSection(
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
                text = "Lezioni settimanali",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LessonMiniStat("Lezioni", uiState.totalWeeklyLessons.toString())
                LessonMiniStat("Ore totali", uiState.weeklyLessonHours)
                LessonMiniStat("Media", uiState.averageLessonDuration)
            }
            uiState.busiestLessonDay?.let { day ->
                Text(
                    text = "Giorno più pieno: $day",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WeeklyLessonsMiniChart(lessonCountByDay = uiState.lessonCountByDay)
        }
    }
}

@Composable
private fun LessonMiniStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyLessonsMiniChart(
    lessonCountByDay: List<DayStatUi>,
    modifier: Modifier = Modifier
) {
    val maxCount = lessonCountByDay.maxOfOrNull { it.lessonCount } ?: 0
    val highlightThreshold = maxCount.coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        lessonCountByDay.forEach { day ->
            val isBusiest = day.lessonCount > 0 && day.lessonCount == maxCount
            val heightFraction = if (day.lessonCount == 0) {
                0.12f
            } else {
                (day.lessonCount.toFloat() / highlightThreshold.toFloat()).coerceIn(0.2f, 1f)
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = day.lessonCount.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isBusiest && day.lessonCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((48 * heightFraction).dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            if (isBusiest && day.lessonCount > 0) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                            }
                        )
                )
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonsStatsSectionPreview() {
    UniLifePlannerTheme {
        LessonsStatsSection(
            uiState = StatisticsUiState(
                isEmpty = false,
                totalWeeklyLessons = 12,
                weeklyLessonHours = "18h",
                averageLessonDuration = "1h 30min",
                busiestLessonDay = "Mercoledì",
                lessonCountByDay = listOf(
                    DayStatUi("Lun", 2, 180),
                    DayStatUi("Mar", 1, 90),
                    DayStatUi("Mer", 4, 360),
                    DayStatUi("Gio", 2, 180),
                    DayStatUi("Ven", 1, 90),
                    DayStatUi("Sab", 0, 0),
                    DayStatUi("Dom", 0, 0)
                )
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LessonsStatsSectionDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        LessonsStatsSection(
            uiState = StatisticsUiState(
                isEmpty = false,
                totalWeeklyLessons = 0,
                lessonCountByDay = List(7) { i ->
                    DayStatUi(
                        dayLabel = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")[i],
                        lessonCount = 0,
                        totalMinutes = 0
                    )
                }
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
