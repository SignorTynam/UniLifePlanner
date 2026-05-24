package com.example.unilifeplanner.ui.courses

import com.example.unilifeplanner.data.local.CourseEntity

data class CourseUiState(
    val searchQuery: String = "",
    val selectedStatusFilter: CourseStatusFilter = CourseStatusFilter.ALL,
    val showFavoritesOnly: Boolean = false,
    val selectedSortOption: CourseSortOption = CourseSortOption.DEFAULT,
    val courses: List<CourseEntity> = emptyList(),
    val filteredCourses: List<CourseEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class CourseStatusFilter {
    ALL,
    TO_STUDY,
    IN_PROGRESS,
    COMPLETED
}

enum class CourseSortOption {
    DEFAULT,
    CREDITS_DESC,
    NAME_ASC
}
