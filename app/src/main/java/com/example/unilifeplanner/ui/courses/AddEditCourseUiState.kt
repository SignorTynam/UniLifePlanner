package com.example.unilifeplanner.ui.courses

import com.example.unilifeplanner.domain.model.CourseStatus

data class AddEditCourseUiState(
    val courseId: Int? = null,
    val name: String = "",
    val professor: String = "",
    val examDate: Long? = null,
    val classroom: String = "",
    val credits: String = "",
    val status: CourseStatus = CourseStatus.TO_STUDY,
    val reminderEnabled: Boolean = false,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val professorError: String? = null,
    val creditsError: String? = null,
    val saveSuccess: Boolean = false
)
