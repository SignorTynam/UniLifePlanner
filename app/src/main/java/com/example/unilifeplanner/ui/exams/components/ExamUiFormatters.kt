package com.example.unilifeplanner.ui.exams.components

import com.example.unilifeplanner.ui.exams.ExamAppealListItemUi
import com.example.unilifeplanner.ui.exams.ExamDateFilter
import com.example.unilifeplanner.ui.exams.ExamSortOption
import com.example.unilifeplanner.ui.exams.ExamsUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

fun hasActiveExamFilters(uiState: ExamsUiState): Boolean {
    return uiState.searchQuery.isNotBlank() ||
        uiState.selectedCourseId != null ||
        uiState.selectedDateFilter != ExamDateFilter.ALL ||
        uiState.selectedSortOption != ExamSortOption.NEXT_UPCOMING ||
        uiState.selectedExamDayMillis != null
}

fun examsAgendaHeaderSubtitle(uiState: ExamsUiState): String {
    return when {
        hasActiveExamFilters(uiState) -> "${uiState.filteredResultCount} risultati filtrati"
        !uiState.hasAnyExams -> "Organizza i tuoi appelli universitari"
        uiState.upcomingExams.isNotEmpty() || uiState.pastExams.isNotEmpty() -> {
            "${uiState.upcomingExamCount} appelli in programma"
        }
        else -> "Organizza i tuoi appelli universitari"
    }
}

fun ExamAppealListItemUi.needsFeedbackRegistration(): Boolean {
    return isPast &&
        (feedbackStatus == "PENDING" || feedbackStatus == "SCHEDULED") &&
        feedbackResult == null
}

fun ExamAppealListItemUi.feedbackPillText(): String? {
    return when {
        needsFeedbackRegistration() -> "Esito da registrare"
        feedbackStatus == "ANSWERED" -> when (feedbackResult) {
            "PASSED" -> listOfNotNull("Superato", feedbackGrade?.let { "voto $it" })
                .joinToString(" · ")
            "FAILED" -> "Non superato"
            "WAITING_RESULT" -> "In attesa"
            "NOT_ATTENDED" -> "Non partecipato"
            else -> null
        }
        else -> null
    }
}

fun formatCalendarStripLabel(dayKeyMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(dayKeyMillis).atZone(zoneId).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

    if (date == today) return "Oggi"

    val daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, date)
    val weekday = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ITALY)
        .replaceFirstChar { it.uppercase(Locale.ITALY) }
        .take(3)

    return if (daysUntil in 1..21) {
        weekday
    } else {
        date.month.getDisplayName(TextStyle.SHORT, Locale.ITALY)
            .replaceFirstChar { it.uppercase(Locale.ITALY) }
            .take(3)
    }
}

fun formatCalendarStripDayNumber(dayKeyMillis: Long): String {
    val zoneId = ZoneId.systemDefault()
    val day = Instant.ofEpochMilli(dayKeyMillis).atZone(zoneId).toLocalDate().dayOfMonth
    return String.format(Locale.ITALY, "%02d", day)
}

fun examDateFilterLabel(filter: ExamDateFilter): String {
    return when (filter) {
        ExamDateFilter.ALL -> "Tutti"
        ExamDateFilter.TODAY -> "Oggi"
        ExamDateFilter.TOMORROW -> "Domani"
        ExamDateFilter.THIS_WEEK -> "Questa settimana"
        ExamDateFilter.THIS_MONTH -> "Questo mese"
        ExamDateFilter.REMINDER_ENABLED -> "Promemoria"
        ExamDateFilter.FEEDBACK_PENDING -> "Esito da registrare"
    }
}

fun examSortLabel(option: ExamSortOption): String {
    return when (option) {
        ExamSortOption.NEXT_UPCOMING -> "Più vicini"
        ExamSortOption.COURSE_NAME_ASC -> "Corso A-Z"
        ExamSortOption.DATE_DESC -> "Più recenti"
        ExamSortOption.FEEDBACK_STATUS -> "Esito"
    }
}
