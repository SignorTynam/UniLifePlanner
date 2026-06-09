package com.example.unilifeplanner.ui.statistics.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatStatisticDate(timestamp: Long): String {
    val zoneId = ZoneId.systemDefault()
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalDate()
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ITALY))
}

fun relativeExamDateLabel(timestamp: Long, now: Long = System.currentTimeMillis()): String {
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
    val today = Instant.ofEpochMilli(now).atZone(zoneId).toLocalDate()
    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, date)

    return when {
        date == today -> "Oggi"
        date == today.plusDays(1) -> "Domani"
        daysUntil in 2..7 -> date.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            Locale.ITALY
        ).replaceFirstChar { it.uppercase(Locale.ITALY) }
        else -> formatStatisticDate(timestamp)
    }
}

fun progressMotivationMessage(completionPercentage: Float): String {
    val percent = (completionPercentage * 100).toInt()
    return when {
        percent >= 100 -> "Percorso completato"
        percent >= 90 -> "Manca pochissimo"
        percent >= 50 -> "Sei oltre metà percorso"
        percent >= 1 -> "Continua così, stai costruendo il tuo piano"
        else -> "Hai appena iniziato il percorso"
    }
}

fun formatDurationMinutes(totalMinutes: Int): String {
    if (totalMinutes <= 0) return "0h"
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (minutes == 0) {
        "${hours}h"
    } else {
        "${hours}h ${minutes}min"
    }
}

fun formatPercentageText(completionPercentage: Float): String {
    return "${(completionPercentage * 100).toInt()}%"
}
