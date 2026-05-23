package com.example.unilifeplanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.UserProfileRepository
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.domain.model.UserProfile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeSummaryUiState(
    val firstName: String = "",
    val lastName: String = "",
    val profileImageUri: String? = null,
    val totalCourses: Int = 0,
    val completedCourses: Int = 0,
    val inProgressCourses: Int = 0,
    val toStudyCourses: Int = 0,
    val nextExam: NextExamUi? = null,
    val favoriteCourses: List<FavoriteCourseUi> = emptyList()
)

data class NextExamUi(
    val courseName: String,
    val examDate: String,
    val classroom: String,
    val status: String
)

data class FavoriteCourseUi(
    val id: Int,
    val name: String,
    val professor: String,
    val examDate: String?
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val courseRepository = CourseRepository(
        AppDatabase.getDatabase(application).courseDao()
    )
    private val userProfileRepository = UserProfileRepository(
        userProfileDataStore = UserProfileDataStore(application.applicationContext)
    )

    val uiState: StateFlow<HomeSummaryUiState> = combine(
        courseRepository.allCourses,
        userProfileRepository.getProfile()
    ) { courses, profile ->
        courses.toHomeSummaryUiState(profile)
        }
        .catch {
            emit(HomeSummaryUiState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeSummaryUiState()
        )

    private fun List<CourseEntity>.toHomeSummaryUiState(profile: UserProfile): HomeSummaryUiState {
        val now = System.currentTimeMillis()
        val nextExam = asSequence()
            .filter { it.examDate != null && it.examDate >= now }
            .minByOrNull { it.examDate ?: Long.MAX_VALUE }

        return HomeSummaryUiState(
            firstName = profile.firstName,
            lastName = profile.lastName,
            profileImageUri = profile.profileImageUri,
            totalCourses = size,
            completedCourses = count { it.status == CourseStatus.COMPLETED.name },
            inProgressCourses = count { it.status == CourseStatus.IN_PROGRESS.name },
            toStudyCourses = count { it.status == CourseStatus.TO_STUDY.name },
            nextExam = nextExam?.let { course ->
                NextExamUi(
                    courseName = course.name,
                    examDate = formatDate(course.examDate),
                    classroom = course.classroom?.takeIf { it.isNotBlank() } ?: "Aula non impostata",
                    status = statusLabel(course.status)
                )
            },
            favoriteCourses = filter { it.isFavorite }
                .take(3)
                .map { course ->
                    FavoriteCourseUi(
                        id = course.id,
                        name = course.name,
                        professor = course.professor,
                        examDate = course.examDate?.let { formatDate(it) }
                    )
                }
        )
    }

    private fun formatDate(timestamp: Long?): String {
        if (timestamp == null) return "Data non impostata"

        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun statusLabel(status: String): String {
        return when (status) {
            CourseStatus.TO_STUDY.name -> "Da studiare"
            CourseStatus.IN_PROGRESS.name -> "In corso"
            CourseStatus.COMPLETED.name -> "Completato"
            else -> status
        }
    }
}
