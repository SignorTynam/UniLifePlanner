package com.example.unilifeplanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.ExamAppealWithCourse
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.data.repository.UserProfileRepository
import com.example.unilifeplanner.domain.exams.examStartMillis
import com.example.unilifeplanner.domain.exams.formatExamDateTime
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.domain.lessons.lessonStartDateTime
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.domain.model.UserProfile
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
    val favoriteCourseCount: Int = 0,
    val totalCredits: Int = 0,
    val completedCredits: Int = 0,
    val completionPercentage: Int = 0,
    val todayLessonCount: Int = 0,
    val todayExamCount: Int = 0,
    val todayCommitmentsCount: Int = 0,
    val todayLessons: List<HomeLessonPreviewUi> = emptyList(),
    val todayExams: List<HomeExamPreviewUi> = emptyList(),
    val nextExam: NextExamUi? = null,
    val nextLesson: NextLessonUi? = null,
    val favoriteCourses: List<FavoriteCourseUi> = emptyList(),
    val insights: List<String> = emptyList()
)

data class NextExamUi(
    val examAppealId: Int,
    val courseId: Int,
    val courseName: String,
    val examDate: String,
    val relativeDateLabel: String,
    val status: String,
    val reminderEnabled: Boolean
)

data class NextLessonUi(
    val lessonId: Int,
    val courseId: Int,
    val courseName: String,
    val dayAndTime: String,
    val relativeDayLabel: String,
    val location: String?
)

data class HomeLessonPreviewUi(
    val lessonId: Int,
    val courseId: Int,
    val courseName: String,
    val timeLabel: String,
    val location: String?
)

data class HomeExamPreviewUi(
    val examAppealId: Int,
    val courseId: Int,
    val courseName: String,
    val dateTimeLabel: String,
    val reminderEnabled: Boolean
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
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val userProfileRepository = UserProfileRepository(
        userProfileDataStore = UserProfileDataStore(application.applicationContext)
    )

    val uiState: StateFlow<HomeSummaryUiState> = combine(
        courseRepository.allCourses,
        lessonRepository.getAllLessons(),
        examAppealRepository.getExamAppealsWithCourse(),
        userProfileRepository.getProfile()
    ) { courses, lessons, exams, profile ->
        courses.toHomeSummaryUiState(
            profile = profile,
            lessons = lessons,
            exams = exams
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
        lessons: List<LessonEntity>,
        exams: List<ExamAppealWithCourse>
    ): HomeSummaryUiState {
        val now = System.currentTimeMillis()
        val today = LocalDate.now()
        val nowDateTime = LocalDateTime.now()
        val zoneId = ZoneId.systemDefault()
        val coursesById = associateBy { it.id }

        // Prossimo esame
        val nextExamCandidate = exams
            .asSequence()
            .map { exam ->
                exam to examStartMillis(
                    dateMillis = exam.exam.dateMillis,
                    timeMinutes = exam.exam.timeMinutes
                )
            }
            .filter { (_, startMillis) -> startMillis >= now }
            .minByOrNull { (_, startMillis) -> startMillis }

        // Prossima lezione
        val nextLessonCandidate = lessons
            .asSequence()
            .mapNotNull { lesson ->
                val course = coursesById[lesson.courseId] ?: return@mapNotNull null
                val nextDateTime = lessonStartDateTime(
                    dateMillis = lesson.dateMillis,
                    dayOfWeek = lesson.dayOfWeek,
                    startTimeMinutes = lesson.startTimeMinutes,
                    nowMillis = now
                )
                if (!nextDateTime.isAfter(nowDateTime)) return@mapNotNull null
                NextLessonCandidate(
                    dateTime = nextDateTime,
                    lesson = lesson,
                    courseName = course.name
                )
            }
            .minByOrNull { it.dateTime }

        // Impegni di oggi
        val todayLessonsList = lessons
            .filter { lesson ->
                val lessonDate = lesson.dateMillis?.let {
                    java.time.Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
                }
                // Se dateMillis è null, consideriamo la lezione come ricorrente settimanale
                lessonDate == today || (lesson.dateMillis == null && lesson.dayOfWeek == today.dayOfWeek.value)
            }
            .mapNotNull { lesson ->
                val course = coursesById[lesson.courseId] ?: return@mapNotNull null
                HomeLessonPreviewUi(
                    lessonId = lesson.id,
                    courseId = lesson.courseId,
                    courseName = course.name,
                    timeLabel = formatMinutesToTime(lesson.startTimeMinutes),
                    location = lessonLocation(lesson)
                )
            }
            .sortedBy { it.timeLabel }
            .take(2)

        val todayExamsList = exams
            .filter { exam ->
                val examDate = java.time.Instant.ofEpochMilli(exam.exam.dateMillis)
                    .atZone(zoneId)
                    .toLocalDate()
                examDate == today
            }
            .map { exam ->
                HomeExamPreviewUi(
                    examAppealId = exam.exam.id,
                    courseId = exam.exam.courseId,
                    courseName = exam.courseName,
                    dateTimeLabel = exam.exam.timeMinutes?.let { formatMinutesToTime(it) } ?: "Orario N.D.",
                    reminderEnabled = exam.exam.reminderEnabled
                )
            }
            .sortedBy { it.dateTimeLabel }
            .take(1)

        val todayCommitmentsCount = lessons.count { lesson ->
            val lessonDate = lesson.dateMillis?.let {
                java.time.Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
            }
            lessonDate == today || (lesson.dateMillis == null && lesson.dayOfWeek == today.dayOfWeek.value)
        } + exams.count { exam ->
            val examDate = java.time.Instant.ofEpochMilli(exam.exam.dateMillis)
                .atZone(zoneId)
                .toLocalDate()
            examDate == today
        }

        // Crediti e percentuali
        val totalCredits = sumOf { it.credits }
        val completedCredits = filter { it.status == CourseStatus.COMPLETED.name }.sumOf { it.credits }
        val completionPercentage = if (totalCredits == 0) 0 else (completedCredits * 100 / totalCredits)

        // Preferiti con prossimo esame
        val favoriteCoursesList = filter { it.isFavorite }
            .take(3)
            .map { course ->
                val courseExams = exams.filter { it.exam.courseId == course.id }
                val nextCourseExam = courseExams
                    .asSequence()
                    .map { exam ->
                        exam to examStartMillis(
                            dateMillis = exam.exam.dateMillis,
                            timeMinutes = exam.exam.timeMinutes
                        )
                    }
                    .filter { (_, startMillis) -> startMillis >= now }
                    .minByOrNull { (_, startMillis) -> startMillis }

                FavoriteCourseUi(
                    id = course.id,
                    name = course.name,
                    professor = course.professor,
                    examDate = nextCourseExam?.let { (exam, _) ->
                        formatExamDateTime(exam.exam.dateMillis, exam.exam.timeMinutes)
                    }
                )
            }

        val state = HomeSummaryUiState(
            firstName = profile.firstName,
            lastName = profile.lastName,
            profileImageUri = profile.profileImageUri,
            totalCourses = size,
            completedCourses = count { it.status == CourseStatus.COMPLETED.name },
            inProgressCourses = count { it.status == CourseStatus.IN_PROGRESS.name },
            toStudyCourses = count { it.status == CourseStatus.TO_STUDY.name },
            favoriteCourseCount = count { it.isFavorite },
            totalCredits = totalCredits,
            completedCredits = completedCredits,
            completionPercentage = completionPercentage,
            todayLessonCount = todayLessonsList.size,
            todayExamCount = todayExamsList.size,
            todayCommitmentsCount = todayCommitmentsCount,
            todayLessons = todayLessonsList,
            todayExams = todayExamsList,
            nextExam = nextExamCandidate?.let { (exam, _) ->
                NextExamUi(
                    examAppealId = exam.exam.id,
                    courseId = exam.exam.courseId,
                    courseName = exam.courseName,
                    examDate = formatExamDateTime(
                        dateMillis = exam.exam.dateMillis,
                        timeMinutes = exam.exam.timeMinutes
                    ),
                    relativeDateLabel = relativeDateLabel(
                        java.time.Instant.ofEpochMilli(exam.exam.dateMillis)
                            .atZone(zoneId)
                            .toLocalDate()
                    ),
                    status = if (exam.exam.reminderEnabled) "Promemoria attivo" else "Promemoria disattivato",
                    reminderEnabled = exam.exam.reminderEnabled
                )
            },
            nextLesson = nextLessonCandidate?.let { cand ->
                NextLessonUi(
                    lessonId = cand.lesson.id,
                    courseId = cand.lesson.courseId,
                    courseName = cand.courseName,
                    dayAndTime = "${relativeDayLabel(cand.dateTime.toLocalDate())}, " +
                        formatMinutesToTime(cand.lesson.startTimeMinutes),
                    relativeDayLabel = relativeDayLabel(cand.dateTime.toLocalDate()),
                    location = lessonLocation(cand.lesson)
                )
            },
            favoriteCourses = favoriteCoursesList
        )

        return state.copy(
            insights = buildHomeInsights(state)
        )
    }

    private fun buildHomeInsights(state: HomeSummaryUiState): List<String> {
        val insights = mutableListOf<String>()
        if (state.totalCourses == 0) {
            insights.add("Aggiungi o importa corsi per costruire la dashboard.")
            return insights
        }

        if (state.todayCommitmentsCount > 0) {
            insights.add("Hai ${state.todayCommitmentsCount} impegni oggi.")
        } else {
            insights.add("Nessun impegno oggi. Giornata libera per studiare!")
        }

        state.nextExam?.let {
            insights.add("Il prossimo esame è ${it.courseName} (${it.relativeDateLabel}).")
        }

        if (state.toStudyCourses > 0) {
            insights.add("Hai ${state.toStudyCourses} corsi ancora da iniziare.")
        }

        if (state.completionPercentage == 0 && state.totalCourses > 0) {
            insights.add("Inizia aggiornando lo stato dei corsi.")
        } else if (state.completionPercentage >= 50) {
            insights.add("Sei oltre metà percorso! Continua così.")
        }

        return insights
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

    private fun relativeDateLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when {
            date == today -> "Oggi"
            date == today.plusDays(1) -> "Domani"
            date.isBefore(today.plusWeeks(1)) -> dayOfWeekLabel(date.dayOfWeek.value)
            else -> date.format(DateTimeFormatter.ofPattern("dd MMM"))
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
        val lesson: LessonEntity,
        val courseName: String
    )
}
