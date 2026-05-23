package com.example.unilifeplanner.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.weeklyLessonDurationMinutes
import com.example.unilifeplanner.domain.model.CourseStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application.applicationContext)
    private val repository = CourseRepository(database.courseDao())
    private val lessonRepository = LessonRepository(database.lessonDao())

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.allCourses,
        lessonRepository.getAllLessons()
    ) { courses, lessons ->
        courses.toStatisticsUiState(lessons)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState()
        )

    private fun List<CourseEntity>.toStatisticsUiState(
        lessons: List<LessonEntity>
    ): StatisticsUiState {
        if (isEmpty()) {
            return StatisticsUiState()
        }

        val completedCourses = countByStatus(CourseStatus.COMPLETED)
        val inProgressCourses = countByStatus(CourseStatus.IN_PROGRESS)
        val toStudyCourses = countByStatus(CourseStatus.TO_STUDY)
        val totalCredits = sumOf { it.credits.coerceAtLeast(0) }
        val completedCredits = filter { it.status == CourseStatus.COMPLETED.name }
            .sumOf { it.credits.coerceAtLeast(0) }
        val completionPercentage = if (totalCredits > 0) {
            completedCredits.toFloat() / totalCredits.toFloat()
        } else {
            0f
        }
        val now = System.currentTimeMillis()
        val nextExam = asSequence()
            .filter { course -> course.examDate != null && course.examDate > now }
            .minByOrNull { course -> requireNotNull(course.examDate) }
        val busiestLessonDay = lessons
            .groupingBy { lesson -> lesson.dayOfWeek }
            .eachCount()
            .maxWithOrNull(compareBy<Map.Entry<Int, Int>> { it.value }.thenBy { -it.key })
            ?.key
            ?.let { dayOfWeekLabel(it) }
        val weeklyLessonMinutes = lessons.sumOf { lesson ->
            weeklyLessonDurationMinutes(
                startTimeMinutes = lesson.startTimeMinutes,
                endTimeMinutes = lesson.endTimeMinutes
            )
        }

        return StatisticsUiState(
            totalCourses = size,
            completedCourses = completedCourses,
            inProgressCourses = inProgressCourses,
            toStudyCourses = toStudyCourses,
            favoriteCourses = count { it.isFavorite },
            totalWeeklyLessons = lessons.size,
            busiestLessonDay = busiestLessonDay,
            weeklyLessonHours = formatLessonHours(weeklyLessonMinutes),
            totalCredits = totalCredits,
            completedCredits = completedCredits,
            completionPercentage = completionPercentage.coerceIn(0f, 1f),
            nextExamName = nextExam?.name,
            nextExamDate = nextExam?.examDate,
            isEmpty = false
        )
    }

    private fun List<CourseEntity>.countByStatus(status: CourseStatus): Int {
        return count { course -> course.status == status.name }
    }

    private fun formatLessonHours(totalMinutes: Int): String {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (minutes == 0) {
            "${hours}h"
        } else {
            "${hours}h ${minutes}min"
        }
    }
}
