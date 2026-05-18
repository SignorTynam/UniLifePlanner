package com.example.unilifeplanner.ui.courses

import com.example.unilifeplanner.data.local.CourseEntity

data class CourseUiState(
    val courses: List<CourseEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
