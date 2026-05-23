package com.example.unilifeplanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.data.repository.UserProfileRepository
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.domain.lessons.nextLessonDateTime
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.domain.model.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
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
    val nextLesson: NextLessonUi? = null,
    val favoriteCourses: List<FavoriteCourseUi> = emptyList()
)

data class NextExamUi(
    val courseName: String,
    val examDate: String,
    val status: String
)

data class NextLessonUi(
    val courseName: String,
    val dayAndTime: String,
    val location: String?
)

data class FavoriteCourseUi(
    val id: Int,
    val name: String,
    val professor: String,
    val examDate: String?
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao())
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val userProfileRepository = UserProfileRepository(
        userProfileDataStore = UserProfileDataStore(application.applicationContext)
    )

    val uiState: StateFlow<HomeSummaryUiState> = combine(
        courseRepository.allCourses,
        lessonRepository.getAllLessons(),
        userProfileRepository.getProfile()
    ) { courses, lessons, profile ->
        courses.toHomeSummaryUiState(
            profile = profile,
            lessons = lessons
        )
        }
        .catch {
            emit(HomeSummaryUiState())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeSummaryUiState()
        )

    private fun List<CourseEntity>.toHomeSummaryUiState(
        profile: UserProfile,
        lessons: List<LessonEntity>
    ): HomeSummaryUiState {
        val now = System.currentTimeMillis()
        val nextExam = asSequence()
            .filter { it.examDate != null && it.examDate >= now }
            .minByOrNull { it.examDate ?: Long.MAX_VALUE }
        val coursesById = associateBy { it.id }
        val nowDateTime = LocalDateTime.now()
        val nextLesson = lessons
            .asSequence()
            .mapNotNull { lesson ->
                val course = coursesById[lesson.courseId] ?: return@mapNotNull null
                val nextDateTime = nextLessonDateTime(
                    dayOfWeek = lesson.dayOfWeek,
                    startTimeMinutes = lesson.startTimeMinutes,
                    now = nowDateTime
                )
                NextLessonCandidate(
                    dateTime = nextDateTime,
                    ui = NextLessonUi(
                        courseName = course.name,
                        dayAndTime = "${relativeDayLabel(nextDateTime.toLocalDate())}, " +
                            formatMinutesToTime(lesson.startTimeMinutes),
                        location = lessonLocation(lesson)
                    )
                )
            }
            .minByOrNull { it.dateTime }
            ?.ui

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
                    status = statusLabel(course.status)
                )
            },
            nextLesson = nextLesson,
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

    private fun relativeDayLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Oggi"
            today.plusDays(1) -> "Domani"
            else -> "${dayOfWeekLabel(date.dayOfWeek.value)}, ${
                date.format(DateTimeFormatter.ofPattern("dd/MM"))
            }"
        }
    }

    private fun lessonLocation(lesson: LessonEntity): String? {
        return listOfNotNull(
            lesson.classroom?.takeIf { it.isNotBlank() },
            lesson.building?.takeIf { it.isNotBlank() }
        )
            .joinToString(" - ")
            .takeIf { it.isNotBlank() }
    }

    private data class NextLessonCandidate(
        val dateTime: LocalDateTime,
        val ui: NextLessonUi
    )
}
