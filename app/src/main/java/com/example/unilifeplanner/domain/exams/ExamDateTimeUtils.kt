package com.example.unilifeplanner.domain.exams

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val EXAM_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY)
private val EXAM_DATE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ITALY)
private val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun parseExamDate(value: String): LocalDate? {
    val normalized = value.trim()
    if (normalized.isBlank()) return null

    return listOf(EXAM_DATE_FORMATTER, ISO_DATE_FORMATTER)
        .firstNotNullOfOrNull { formatter ->
            try {
                LocalDate.parse(normalized, formatter)
            } catch (_: DateTimeParseException) {
                null
            }
        }
}

fun examDateToStartOfDayMillis(
    date: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    return date.atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun formatExamDate(
    dateMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    return Instant.ofEpochMilli(dateMillis)
        .atZone(zoneId)
        .toLocalDate()
        .format(EXAM_DATE_FORMATTER)
}

fun examStartMillis(
    dateMillis: Long,
    timeMinutes: Int?,
    zoneId: ZoneId = ZoneId.systemDefault()
): Long {
    val date = Instant.ofEpochMilli(dateMillis)
        .atZone(zoneId)
        .toLocalDate()
    val time = timeMinutes?.let { minutes ->
        java.time.LocalTime.of(minutes / 60, minutes % 60)
    } ?: java.time.LocalTime.MIDNIGHT

    return date.atTime(time)
        .atZone(zoneId)
        .toInstant()
        .toEpochMilli()
}

fun formatExamDateTime(
    dateMillis: Long,
    timeMinutes: Int?,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    val dateTime = Instant.ofEpochMilli(
        examStartMillis(
            dateMillis = dateMillis,
            timeMinutes = timeMinutes,
            zoneId = zoneId
        )
    )
        .atZone(zoneId)
        .toLocalDateTime()

    return if (timeMinutes == null) {
        dateTime.toLocalDate().format(EXAM_DATE_FORMATTER)
    } else {
        dateTime.format(EXAM_DATE_TIME_FORMATTER)
    }
}
