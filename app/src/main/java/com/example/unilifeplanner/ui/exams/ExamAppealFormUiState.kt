package com.example.unilifeplanner.ui.exams

data class ExamAppealFormUiState(
    val examAppealId: Int? = null,
    val courseId: Int? = null,
    val courseName: String = "",
    val courses: List<ExamCourseOptionUi> = emptyList(),
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val notes: String = "",
    val type: String = "",
    val reminderEnabled: Boolean = false,
    val sourceLabel: String = "Manuale",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val courseError: String? = null,
    val dateError: String? = null,
    val timeError: String? = null,
    val saveSuccess: Boolean = false
)
