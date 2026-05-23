package com.example.unilifeplanner.ui.lessons

data class LessonListItemUi(
    val lessonId: Int,
    val courseId: Int,
    val courseName: String,
    val courseProfessor: String,
    val dayOfWeek: Int,
    val dayLabel: String,
    val relativeDayLabel: String,
    val startTime: String,
    val endTime: String,
    val classroom: String?,
    val building: String?,
    val locationQuery: String?,
    val notes: String?,
    val reminderEnabled: Boolean,
    val nextOccurrenceMillis: Long,
    val isPastThisWeek: Boolean
)
