package com.example.unilifeplanner.ui.statistics

data class StatisticsUiState(
    val totalCourses: Int = 0,
    val completedCourses: Int = 0,
    val inProgressCourses: Int = 0,
    val toStudyCourses: Int = 0,
    val favoriteCourses: Int = 0,
    val totalWeeklyLessons: Int = 0,
    val busiestLessonDay: String? = null,
    val weeklyLessonHours: String = "0h",
    val totalCredits: Int = 0,
    val completedCredits: Int = 0,
    val completionPercentage: Float = 0f,
    val nextExamName: String? = null,
    val nextExamDate: Long? = null,
    val isEmpty: Boolean = true
)
