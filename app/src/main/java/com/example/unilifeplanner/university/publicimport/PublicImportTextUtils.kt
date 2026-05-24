package com.example.unilifeplanner.university.publicimport

import java.net.URI
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale

fun normalizeText(value: String): String {
    val withoutMarks = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
    return withoutMarks
        .replace("\\s+".toRegex(), " ")
        .lowercase(Locale.ITALIAN)
}

fun parseCredits(value: String): Int? {
    return value
        .trim()
        .takeIf { it.isNotEmpty() }
        ?.let { "(\\d+)".toRegex().find(it)?.groupValues?.getOrNull(1) }
        ?.toIntOrNull()
}

fun parseDayOfWeekItalian(value: String): Int? {
    return when (normalizeText(value).trim(',', '.', ' ')) {
        "lunedi", "lun" -> 1
        "martedi", "mar" -> 2
        "mercoledi", "mer" -> 3
        "giovedi", "gio" -> 4
        "venerdi", "ven" -> 5
        "sabato", "sab" -> 6
        "domenica", "dom" -> 7
        else -> null
    }
}

fun parseTimeToMinutes(value: String): Int? {
    val match = "^(\\d{1,2}):(\\d{2})$".toRegex().matchEntire(value.trim()) ?: return null
    val hour = match.groupValues[1].toIntOrNull() ?: return null
    val minute = match.groupValues[2].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

fun formatMinutesToTime(minutes: Int): String {
    val hour = minutes / 60
    val minute = minutes % 60
    return "%02d:%02d".format(Locale.ITALIAN, hour, minute)
}

fun buildStableExternalId(vararg parts: String?): String {
    val normalized = parts
        .filterNotNull()
        .joinToString(separator = "|") { normalizeText(it) }
    val digest = MessageDigest.getInstance("SHA-256")
        .digest(normalized.toByteArray(Charsets.UTF_8))
        .joinToString(separator = "") { byte -> "%02x".format(byte) }
    return digest.take(32)
}

fun absoluteUrl(baseUrl: String, href: String): String {
    val trimmed = href.trim()
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
    return URI(normalizedBase).resolve(trimmed).toString()
}

fun academicYearStart(academicYear: String): String =
    academicYear.substringBefore("/").trim()
