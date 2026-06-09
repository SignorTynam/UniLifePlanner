package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import java.time.Instant
import java.time.ZoneId

@Composable
fun ExamsCalendarStrip(
    examCountByDay: Map<Long, Int>,
    selectedExamDayMillis: Long?,
    onDaySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val nowMillis = System.currentTimeMillis()
    val zoneId = ZoneId.systemDefault()
    val todayKey = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val sortedDays = examCountByDay.keys.sorted()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = selectedExamDayMillis == null,
                onClick = { onDaySelected(null) },
                label = { Text(text = "Tutti") }
            )

            sortedDays.forEach { dayKey ->
                val isToday = dayKey == todayKey
                val isSelected = selectedExamDayMillis == dayKey
                val count = examCountByDay[dayKey] ?: 0
                val label = formatCalendarStripLabel(dayKey, nowMillis)
                val dayNumber = formatCalendarStripDayNumber(dayKey)
                val description = buildString {
                    append(label)
                    append(" ")
                    append(dayNumber)
                    if (isToday) append(", oggi")
                    if (isSelected) append(", selezionato")
                    append(", $count appelli")
                }

                ExamDateChip(
                    label = label,
                    dayNumber = dayNumber,
                    count = count,
                    isToday = isToday,
                    isSelected = isSelected,
                    contentDescription = description,
                    onClick = {
                        onDaySelected(if (isSelected) null else dayKey)
                    }
                )
            }
        }
    }
}

@Composable
private fun ExamDateChip(
    label: String,
    dayNumber: String,
    count: Int,
    isToday: Boolean,
    isSelected: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val contentColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = if (isToday && !isSelected) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        } else {
            null
        },
        modifier = Modifier.semantics { this.contentDescription = contentDescription }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                if (count > 0) {
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                } else {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = count.toString(),
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
}

@Preview(showBackground = true)
@Composable
private fun ExamsCalendarStripPreview() {
    val today = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrow = today + 86_400_000L

    UniLifePlannerTheme {
        ExamsCalendarStrip(
            examCountByDay = mapOf(today to 1, tomorrow to 2, today + 432_000_000L to 3),
            selectedExamDayMillis = tomorrow,
            onDaySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun ExamsCalendarStripDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        ExamsCalendarStrip(
            examCountByDay = emptyMap(),
            selectedExamDayMillis = null,
            onDaySelected = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
