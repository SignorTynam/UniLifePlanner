package com.example.unilifeplanner.ui.lessons.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import java.time.DayOfWeek
import java.time.LocalDate

private data class WeekDayUi(
    val dayOfWeek: Int,
    val abbreviation: String,
    val isToday: Boolean
)

@Composable
fun LessonsWeekCalendar(
    lessonCountByDay: Map<Int, Int>,
    selectedDayOfWeek: Int?,
    onDaySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now().dayOfWeek.value
    val weekDays = listOf(
        WeekDayUi(DayOfWeek.MONDAY.value, "Lun", today == DayOfWeek.MONDAY.value),
        WeekDayUi(DayOfWeek.TUESDAY.value, "Mar", today == DayOfWeek.TUESDAY.value),
        WeekDayUi(DayOfWeek.WEDNESDAY.value, "Mer", today == DayOfWeek.WEDNESDAY.value),
        WeekDayUi(DayOfWeek.THURSDAY.value, "Gio", today == DayOfWeek.THURSDAY.value),
        WeekDayUi(DayOfWeek.FRIDAY.value, "Ven", today == DayOfWeek.FRIDAY.value),
        WeekDayUi(DayOfWeek.SATURDAY.value, "Sab", today == DayOfWeek.SATURDAY.value),
        WeekDayUi(DayOfWeek.SUNDAY.value, "Dom", today == DayOfWeek.SUNDAY.value)
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedDayOfWeek == null,
                onClick = { onDaySelected(null) },
                label = { Text(text = "Tutte") }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            weekDays.forEach { day ->
                WeekDayCell(
                    day = day,
                    lessonCount = lessonCountByDay[day.dayOfWeek] ?: 0,
                    isSelected = selectedDayOfWeek == day.dayOfWeek,
                    onClick = {
                        onDaySelected(
                            if (selectedDayOfWeek == day.dayOfWeek) null else day.dayOfWeek
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeekDayCell(
    day: WeekDayUi,
    lessonCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        day.isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val dayDescription = buildString {
        append(day.abbreviation)
        if (day.isToday) append(", oggi")
        if (isSelected) append(", selezionato")
        append(", $lessonCount lezioni")
    }

    Surface(
        onClick = onClick,
        modifier = modifier.semantics {
            contentDescription = dayDescription
        },
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = if (day.isToday && !isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = day.abbreviation,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            if (lessonCount > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .then(
                            Modifier.background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                },
                                shape = CircleShape
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = lessonCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LessonsWeekCalendarPreview() {
    UniLifePlannerTheme {
        LessonsWeekCalendar(
            lessonCountByDay = mapOf(1 to 2, 3 to 1, 5 to 3),
            selectedDayOfWeek = 3,
            onDaySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LessonsWeekCalendarDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        LessonsWeekCalendar(
            lessonCountByDay = mapOf(2 to 1, 4 to 2),
            selectedDayOfWeek = null,
            onDaySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
