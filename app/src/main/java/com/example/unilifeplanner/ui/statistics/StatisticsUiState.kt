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
    val isEmpty: Boolean = true,
    val remainingCredits: Int = 0,
    val completionPercentageText: String = "0%",
    val courseStatusStats: List<CourseStatusStatUi> = emptyList(),
    val lessonCountByDay: List<DayStatUi> = emptyList(),
    val averageLessonDuration: String = "0h",
    val weeklyLessonMinutes: Int = 0,
    val upcomingExamCount: Int = 0,
    val pastExamCount: Int = 0,
    val examReminderCount: Int = 0,
    val pendingFeedbackCount: Int = 0,
    val passedExamCount: Int = 0,
    val failedExamCount: Int = 0,
    val waitingResultExamCount: Int = 0,
    val nextExamCourseName: String? = null,
    val nextExamDateLabel: String? = null,
    val nextExamRelativeLabel: String? = null,
    val insights: List<String> = emptyList()
)

data class CourseStatusStatUi(
    val label: String,
    val count: Int,
    val percentage: Float
)

data class DayStatUi(
    val dayLabel: String,
    val lessonCount: Int,
    val totalMinutes: Int
)
