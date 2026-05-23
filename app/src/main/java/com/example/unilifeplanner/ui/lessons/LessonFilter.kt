package com.example.unilifeplanner.ui.lessons

enum class LessonDateFilter {
    ALL,
    TODAY,
    TOMORROW,
    THIS_WEEK,
    REMINDER_ENABLED
}

data class LessonCourseFilterUi(
    val courseId: Int,
    val courseName: String
)
