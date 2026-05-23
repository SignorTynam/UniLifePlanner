package com.example.unilifeplanner.domain.lessons

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

private const val REMINDER_HOUR = 20
private const val REMINDER_MINUTE = 0

fun parseTimeToMinutes(value: String): Int? {
    val match = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(value.trim()) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null

    return if (hour in 0..23 && minute in 0..59) {
        hour * 60 + minute
    } else {
        null
    }
}

fun formatMinutesToTime(minutes: Int): String {
    val normalizedMinutes = minutes.coerceIn(0, 23 * 60 + 59)
    return String.format(
        Locale.ITALY,
        "%02d:%02d",
        normalizedMinutes / 60,
        normalizedMinutes % 60
    )
}

fun dayOfWeekLabel(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        DayOfWeek.MONDAY.value -> "Lunedi"
        DayOfWeek.TUESDAY.value -> "Martedi"
        DayOfWeek.WEDNESDAY.value -> "Mercoledi"
        DayOfWeek.THURSDAY.value -> "Giovedi"
        DayOfWeek.FRIDAY.value -> "Venerdi"
        DayOfWeek.SATURDAY.value -> "Sabato"
        DayOfWeek.SUNDAY.value -> "Domenica"
        else -> "Giorno non valido"
    }
}

fun nextLessonDateTime(
    dayOfWeek: Int,
    startTimeMinutes: Int,
    zoneId: ZoneId = ZoneId.systemDefault(),
    now: LocalDateTime = LocalDateTime.now(zoneId)
): LocalDateTime {
    val targetDay = DayOfWeek.of(dayOfWeek)
    val daysUntil = (targetDay.value - now.dayOfWeek.value + 7) % 7
    val startTime = LocalTime.of(startTimeMinutes / 60, startTimeMinutes % 60)
    var nextLesson = now.toLocalDate()
        .plusDays(daysUntil.toLong())
        .atTime(startTime)

    if (!nextLesson.isAfter(now)) {
        nextLesson = nextLesson.plusWeeks(1)
    }

    return nextLesson
}

fun nextOccurrenceMillis(
    dayOfWeek: Int,
    startTimeMinutes: Int,
    nowMillis: Long = System.currentTimeMillis()
): Long {
    val zoneId = ZoneId.systemDefault()
    val now = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDateTime()
    val targetDay = DayOfWeek.of(dayOfWeek)
    val startTime = LocalTime.of(startTimeMinutes / 60, startTimeMinutes % 60)
    val daysUntil = (targetDay.value - now.dayOfWeek.value + 7) % 7
    var occurrence = now.toLocalDate()
        .plusDays(daysUntil.toLong())
        .atTime(startTime)

    if (occurrence.isBefore(now)) {
        occurrence = occurrence.plusWeeks(1)
    }

    return occurrence.atZone(zoneId).toInstant().toEpochMilli()
}

fun isAlreadyPassedThisWeek(
    dayOfWeek: Int,
    startTimeMinutes: Int,
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    val zoneId = ZoneId.systemDefault()
    val now = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDateTime()
    val nowDay = now.dayOfWeek.value

    return when {
        dayOfWeek < nowDay -> true
        dayOfWeek > nowDay -> false
        else -> {
            val nowMinutes = now.hour * 60 + now.minute
            startTimeMinutes < nowMinutes
        }
    }
}

fun relativeLessonLabel(
    occurrenceMillis: Long,
    nowMillis: Long = System.currentTimeMillis()
): String {
    val zoneId = ZoneId.systemDefault()
    val occurrenceDate = Instant.ofEpochMilli(occurrenceMillis)
        .atZone(zoneId)
        .toLocalDate()
    val nowDate = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDate()
    val daysDifference = ChronoUnit.DAYS.between(nowDate, occurrenceDate)

    return when (daysDifference) {
        0L -> "Oggi"
        1L -> "Domani"
        7L -> "${dayOfWeekLabel(occurrenceDate.dayOfWeek.value)} prossimo"
        else -> dayOfWeekLabel(occurrenceDate.dayOfWeek.value)
    }
}

fun nextLessonReminderAtMillis(
    dayOfWeek: Int,
    startTimeMinutes: Int,
    zoneId: ZoneId = ZoneId.systemDefault(),
    now: LocalDateTime = LocalDateTime.now(zoneId)
): Long {
    val nextLesson = nextLessonDateTime(
        dayOfWeek = dayOfWeek,
        startTimeMinutes = startTimeMinutes,
        zoneId = zoneId,
        now = now
    )
    var reminderDateTime = nextLesson.toLocalDate()
        .minusDays(1)
        .atTime(REMINDER_HOUR, REMINDER_MINUTE)

    if (!reminderDateTime.isAfter(now)) {
        reminderDateTime = nextLesson.plusWeeks(1)
            .toLocalDate()
            .minusDays(1)
            .atTime(REMINDER_HOUR, REMINDER_MINUTE)
    }

    return reminderDateTime
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun weeklyLessonDurationMinutes(
    startTimeMinutes: Int,
    endTimeMinutes: Int
): Int = (endTimeMinutes - startTimeMinutes).coerceAtLeast(0)
