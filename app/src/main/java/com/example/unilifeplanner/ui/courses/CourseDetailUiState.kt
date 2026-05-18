package com.example.unilifeplanner.ui.courses

import com.example.unilifeplanner.data.local.CourseEntity

data class CourseDetailUiState(
    val course: CourseEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false
)
