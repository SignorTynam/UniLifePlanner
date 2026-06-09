package com.example.unilifeplanner.ui.lessons

data class LessonsUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCourseId: Int? = null,
    val selectedCourseName: String? = null,
    val selectedDateFilter: LessonDateFilter = LessonDateFilter.ALL,
    val selectedSortOption: LessonSortOption = LessonSortOption.NEXT_UPCOMING,
    val selectedDayOfWeek: Int? = null,
    val availableCourses: List<LessonCourseFilterUi> = emptyList(),
    val upcomingLessons: List<LessonListItemUi> = emptyList(),
    val pastThisWeekLessons: List<LessonListItemUi> = emptyList(),
    val showPastThisWeek: Boolean = false,
    val hasAnyLessons: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val todayLessonCount: Int = 0,
    val tomorrowLessonCount: Int = 0,
    val reminderLessonCount: Int = 0,
    val lessonCountByDayOfWeek: Map<Int, Int> = emptyMap(),
    val filteredResultCount: Int = 0
)
