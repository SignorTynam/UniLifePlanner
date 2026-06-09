package com.example.unilifeplanner.ui.exams

data class ExamsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedCourseId: Int? = null,
    val selectedCourseName: String? = null,
    val selectedDateFilter: ExamDateFilter = ExamDateFilter.ALL,
    val selectedSortOption: ExamSortOption = ExamSortOption.NEXT_UPCOMING,
    val selectedExamDayMillis: Long? = null,
    val availableCourses: List<ExamCourseOptionUi> = emptyList(),
    val upcomingExams: List<ExamAppealListItemUi> = emptyList(),
    val pastExams: List<ExamAppealListItemUi> = emptyList(),
    val showPastExams: Boolean = false,
    val hasAnyExams: Boolean = false,
    val isRefreshing: Boolean = false,
    val refreshMessage: String? = null,
    val filteredResultCount: Int = 0,
    val upcomingExamCount: Int = 0,
    val thisWeekExamCount: Int = 0,
    val reminderExamCount: Int = 0,
    val pendingFeedbackCount: Int = 0,
    val examCountByDay: Map<Long, Int> = emptyMap()
)

data class ExamCourseOptionUi(
    val courseId: Int,
    val courseName: String
)

data class ExamAppealListItemUi(
    val examAppealId: Int,
    val courseId: Int,
    val courseName: String,
    val dateLabel: String,
    val timeLabel: String?,
    val location: String?,
    val notes: String?,
    val type: String?,
    val reminderEnabled: Boolean,
    val sourceLabel: String,
    val feedbackStatus: String,
    val feedbackResult: String?,
    val feedbackGrade: String?,
    val startMillis: Long,
    val isPast: Boolean,
    val dateMillis: Long,
    val dayKeyMillis: Long,
    val relativeDateLabel: String
)
