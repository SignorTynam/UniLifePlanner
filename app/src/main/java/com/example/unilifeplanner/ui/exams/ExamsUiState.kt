package com.example.unilifeplanner.ui.exams

data class ExamsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val selectedCourseId: Int? = null,
    val selectedCourseName: String? = null,
    val availableCourses: List<ExamCourseOptionUi> = emptyList(),
    val upcomingExams: List<ExamAppealListItemUi> = emptyList(),
    val pastExams: List<ExamAppealListItemUi> = emptyList(),
    val showPastExams: Boolean = false,
    val hasAnyExams: Boolean = false,
    val importMessage: String? = null
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
    val startMillis: Long,
    val isPast: Boolean
)
