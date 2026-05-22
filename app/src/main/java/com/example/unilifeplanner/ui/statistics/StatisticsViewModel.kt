package com.example.unilifeplanner.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.domain.model.CourseStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StatisticsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = CourseRepository(
        AppDatabase.getDatabase(application.applicationContext).courseDao()
    )

    val uiState: StateFlow<StatisticsUiState> = repository.allCourses
        .map { courses -> courses.toStatisticsUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState()
        )

    private fun List<CourseEntity>.toStatisticsUiState(): StatisticsUiState {
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

        return StatisticsUiState(
            totalCourses = size,
            completedCourses = completedCourses,
            inProgressCourses = inProgressCourses,
            toStudyCourses = toStudyCourses,
            favoriteCourses = count { it.isFavorite },
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
}
