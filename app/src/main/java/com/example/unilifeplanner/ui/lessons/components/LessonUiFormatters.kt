package com.example.unilifeplanner.ui.lessons.components

import com.example.unilifeplanner.ui.lessons.LessonDateFilter
import com.example.unilifeplanner.ui.lessons.LessonListItemUi
import com.example.unilifeplanner.ui.lessons.LessonSortOption
import com.example.unilifeplanner.ui.lessons.LessonsUiState

fun formatLessonLocation(lesson: LessonListItemUi): List<String> {
    val primary = listOfNotNull(
        lesson.classroom?.takeIf { it.isNotBlank() },
        lesson.building?.takeIf { it.isNotBlank() }
    ).joinToString(" · ")

    val query = lesson.locationQuery?.takeIf { it.isNotBlank() }

    return when {
        primary.isNotBlank() && query != null && !isSimilarLocation(primary, query) -> {
            listOf(primary, query)
        }
        primary.isNotBlank() -> listOf(primary)
        query != null -> listOf(query)
        else -> emptyList()
    }
}

fun lessonHasMappableLocation(lesson: LessonListItemUi): Boolean {
    return lesson.courseName.isNotBlank() ||
        !lesson.locationQuery.isNullOrBlank() ||
        !lesson.classroom.isNullOrBlank() ||
        !lesson.building.isNullOrBlank()
}

fun hasActiveLessonFilters(uiState: LessonsUiState): Boolean {
    return uiState.searchQuery.isNotBlank() ||
        uiState.selectedCourseId != null ||
        uiState.selectedDateFilter != LessonDateFilter.ALL ||
        uiState.selectedSortOption != LessonSortOption.NEXT_UPCOMING ||
        uiState.selectedDayOfWeek != null
}

fun agendaHeaderSubtitle(uiState: LessonsUiState): String {
    return when {
        hasActiveLessonFilters(uiState) -> {
            "${uiState.filteredResultCount} risultati filtrati"
        }
        !uiState.hasAnyLessons -> "Organizza il tuo calendario universitario"
        uiState.upcomingLessons.isNotEmpty() -> {
            "${uiState.upcomingLessons.size} lezioni in programma"
        }
        else -> "Organizza il tuo calendario universitario"
    }
}

private fun isSimilarLocation(primary: String, query: String): Boolean {
    val normalizedPrimary = primary.lowercase().replace(Regex("\\s+"), " ").trim()
    val normalizedQuery = query.lowercase().replace(Regex("\\s+"), " ").trim()
    return normalizedPrimary == normalizedQuery ||
        normalizedPrimary.contains(normalizedQuery) ||
        normalizedQuery.contains(normalizedPrimary)
}
