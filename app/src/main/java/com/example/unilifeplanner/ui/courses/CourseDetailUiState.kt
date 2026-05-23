package com.example.unilifeplanner.ui.courses

import com.example.unilifeplanner.data.local.CourseEntity

data class CourseDetailUiState(
    val course: CourseEntity? = null,
    val lessons: List<LessonUi> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false
)

data class LessonUi(
    val id: Int,
    val courseId: Int,
    val dayOfWeek: Int,
    val dayLabel: String,
    val startTime: String,
    val endTime: String,
    val classroom: String?,
    val building: String?,
    val notes: String?,
    val reminderEnabled: Boolean
)
