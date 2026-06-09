package com.example.unilifeplanner.ui.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.ExamAppealWithCourse
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.domain.exams.examStartMillis
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.weeklyLessonDurationMinutes
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.statistics.components.formatDurationMinutes
import com.example.unilifeplanner.ui.statistics.components.formatPercentageText
import com.example.unilifeplanner.ui.statistics.components.formatStatisticDate
import com.example.unilifeplanner.ui.statistics.components.relativeExamDateLabel
import java.time.DayOfWeek
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
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())

    val uiState: StateFlow<StatisticsUiState> = combine(
        repository.allCourses,
        lessonRepository.getAllLessons(),
        examAppealRepository.getExamAppealsWithCourse()
    ) { courses, lessons, exams ->
        courses.toStatisticsUiState(
            lessons = lessons,
            exams = exams
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatisticsUiState()
        )

    private fun List<CourseEntity>.toStatisticsUiState(
        lessons: List<LessonEntity>,
        exams: List<ExamAppealWithCourse>
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
        val remainingCredits = (totalCredits - completedCredits).coerceAtLeast(0)
        val completionPercentage = if (totalCredits > 0) {
            completedCredits.toFloat() / totalCredits.toFloat()
        } else {
            0f
        }
        val completionPercentageText = formatPercentageText(completionPercentage)

        val courseStatusStats = buildCourseStatusStats(
            toStudyCourses = toStudyCourses,
            inProgressCourses = inProgressCourses,
            completedCourses = completedCourses
        )

        val lessonCountByDay = buildLessonCountByDay(lessons)
        val weeklyLessonMinutes = lessons.sumOf { lesson ->
            weeklyLessonDurationMinutes(
                startTimeMinutes = lesson.startTimeMinutes,
                endTimeMinutes = lesson.endTimeMinutes
            )
        }
        val averageLessonDuration = if (lessons.isEmpty()) {
            "0h"
        } else {
            formatDurationMinutes(weeklyLessonMinutes / lessons.size)
        }
        val busiestLessonDay = lessonCountByDay
            .filter { it.lessonCount > 0 }
            .maxWithOrNull(compareBy<DayStatUi> { it.lessonCount }.thenBy { -it.totalMinutes })
            ?.let { dayStat ->
                dayOfWeekLabel(dayStat.dayOfWeekValue())
            }

        val now = System.currentTimeMillis()
        val examTimings = exams.map { exam ->
            exam to examStartMillis(
                dateMillis = exam.exam.dateMillis,
                timeMinutes = exam.exam.timeMinutes
            )
        }
        val upcomingExams = examTimings.filter { (_, startMillis) -> startMillis > now }
        val pastExams = examTimings.filter { (_, startMillis) -> startMillis <= now }
        val nextExam = upcomingExams.minByOrNull { (_, startMillis) -> startMillis }

        val pendingFeedbackCount = pastExams.count { (examWithCourse, _) ->
            val appeal = examWithCourse.exam
            (appeal.feedbackStatus == "PENDING" || appeal.feedbackStatus == "SCHEDULED") &&
                appeal.feedbackResult == null
        }
        val passedExamCount = exams.count { it.exam.feedbackResult == "PASSED" }
        val failedExamCount = exams.count { it.exam.feedbackResult == "FAILED" }
        val waitingResultExamCount = exams.count { it.exam.feedbackResult == "WAITING_RESULT" }
        val examReminderCount = exams.count { it.exam.reminderEnabled }

        val nextExamDateMillis = nextExam?.first?.exam?.dateMillis
        val insights = buildInsights(
            completionPercentage = completionPercentage,
            toStudyCourses = toStudyCourses,
            inProgressCourses = inProgressCourses,
            completedCourses = completedCourses,
            upcomingExamCount = upcomingExams.size,
            pendingFeedbackCount = pendingFeedbackCount,
            weeklyLessonMinutes = weeklyLessonMinutes,
            weeklyLessonHours = formatDurationMinutes(weeklyLessonMinutes),
            busiestLessonDay = busiestLessonDay
        )

        return StatisticsUiState(
            totalCourses = size,
            completedCourses = completedCourses,
            inProgressCourses = inProgressCourses,
            toStudyCourses = toStudyCourses,
            favoriteCourses = count { it.isFavorite },
            totalWeeklyLessons = lessons.size,
            busiestLessonDay = busiestLessonDay,
            weeklyLessonHours = formatDurationMinutes(weeklyLessonMinutes),
            totalCredits = totalCredits,
            completedCredits = completedCredits,
            completionPercentage = completionPercentage.coerceIn(0f, 1f),
            nextExamName = nextExam?.first?.courseName,
            nextExamDate = nextExamDateMillis,
            isEmpty = false,
            remainingCredits = remainingCredits,
            completionPercentageText = completionPercentageText,
            courseStatusStats = courseStatusStats,
            lessonCountByDay = lessonCountByDay,
            averageLessonDuration = averageLessonDuration,
            weeklyLessonMinutes = weeklyLessonMinutes,
            upcomingExamCount = upcomingExams.size,
            pastExamCount = pastExams.size,
            examReminderCount = examReminderCount,
            pendingFeedbackCount = pendingFeedbackCount,
            passedExamCount = passedExamCount,
            failedExamCount = failedExamCount,
            waitingResultExamCount = waitingResultExamCount,
            nextExamCourseName = nextExam?.first?.courseName,
            nextExamDateLabel = nextExamDateMillis?.let(::formatStatisticDate),
            nextExamRelativeLabel = nextExamDateMillis?.let { relativeExamDateLabel(it, now) },
            insights = insights
        )
    }

    private fun buildCourseStatusStats(
        toStudyCourses: Int,
        inProgressCourses: Int,
        completedCourses: Int
    ): List<CourseStatusStatUi> {
        val total = (toStudyCourses + inProgressCourses + completedCourses).coerceAtLeast(1)
        return listOf(
            CourseStatusStatUi("Da studiare", toStudyCourses, toStudyCourses.toFloat() / total),
            CourseStatusStatUi("In corso", inProgressCourses, inProgressCourses.toFloat() / total),
            CourseStatusStatUi("Completati", completedCourses, completedCourses.toFloat() / total)
        )
    }

    private fun buildLessonCountByDay(lessons: List<LessonEntity>): List<DayStatUi> {
        val grouped = lessons.groupBy { it.dayOfWeek }
        return listOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY,
            DayOfWeek.SATURDAY,
            DayOfWeek.SUNDAY
        ).map { day ->
            val dayLessons = grouped[day.value].orEmpty()
            DayStatUi(
                dayLabel = shortDayLabel(day.value),
                lessonCount = dayLessons.size,
                totalMinutes = dayLessons.sumOf { lesson ->
                    weeklyLessonDurationMinutes(
                        startTimeMinutes = lesson.startTimeMinutes,
                        endTimeMinutes = lesson.endTimeMinutes
                    )
                }
            )
        }
    }

    private fun shortDayLabel(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            DayOfWeek.MONDAY.value -> "Lun"
            DayOfWeek.TUESDAY.value -> "Mar"
            DayOfWeek.WEDNESDAY.value -> "Mer"
            DayOfWeek.THURSDAY.value -> "Gio"
            DayOfWeek.FRIDAY.value -> "Ven"
            DayOfWeek.SATURDAY.value -> "Sab"
            DayOfWeek.SUNDAY.value -> "Dom"
            else -> "?"
        }
    }

    private fun DayStatUi.dayOfWeekValue(): Int {
        return when (dayLabel) {
            "Lun" -> DayOfWeek.MONDAY.value
            "Mar" -> DayOfWeek.TUESDAY.value
            "Mer" -> DayOfWeek.WEDNESDAY.value
            "Gio" -> DayOfWeek.THURSDAY.value
            "Ven" -> DayOfWeek.FRIDAY.value
            "Sab" -> DayOfWeek.SATURDAY.value
            "Dom" -> DayOfWeek.SUNDAY.value
            else -> DayOfWeek.MONDAY.value
        }
    }

    private fun buildInsights(
        completionPercentage: Float,
        toStudyCourses: Int,
        inProgressCourses: Int,
        completedCourses: Int,
        upcomingExamCount: Int,
        pendingFeedbackCount: Int,
        weeklyLessonMinutes: Int,
        weeklyLessonHours: String,
        busiestLessonDay: String?
    ): List<String> {
        val insights = mutableListOf<String>()
        val percent = (completionPercentage * 100).toInt()

        if (percent == 0) {
            insights.add("Inizia completando o aggiornando lo stato dei corsi.")
        }
        if (toStudyCourses > 0) {
            insights.add("Hai $toStudyCourses corsi ancora da iniziare.")
        }
        if (inProgressCourses > 0) {
            insights.add("Hai $inProgressCourses corsi in corso.")
        }
        if (completedCourses > 0) {
            insights.add("Hai completato $completedCourses corsi.")
        }
        if (upcomingExamCount > 0) {
            insights.add("Hai $upcomingExamCount appelli futuri.")
        }
        if (pendingFeedbackCount > 0) {
            insights.add("Ci sono $pendingFeedbackCount esiti da registrare.")
        }
        if (weeklyLessonMinutes > 0) {
            insights.add("Questa settimana hai circa $weeklyLessonHours di lezioni.")
        }
        busiestLessonDay?.let { day ->
            insights.add("Il giorno più carico è $day.")
        }

        return insights.distinct().take(4)
    }

    private fun List<CourseEntity>.countByStatus(status: CourseStatus): Int {
        return count { course -> course.status == status.name }
    }
}
