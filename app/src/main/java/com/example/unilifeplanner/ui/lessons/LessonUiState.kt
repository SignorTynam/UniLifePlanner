package com.example.unilifeplanner.ui.lessons

data class LessonUiState(
    val courseId: Int = 0,
    val lessonId: Int? = null,
    val dayOfWeek: Int? = null,
    val startTime: String = "",
    val endTime: String = "",
    val classroom: String = "",
    val building: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val dayOfWeekError: String? = null,
    val startTimeError: String? = null,
    val endTimeError: String? = null,
    val saveSuccess: Boolean = false
)
