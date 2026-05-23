package com.example.unilifeplanner.ui.lessons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.LessonWithCourse
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.domain.lessons.isAlreadyPassedThisWeek
import com.example.unilifeplanner.domain.lessons.nextOccurrenceMillis
import com.example.unilifeplanner.domain.lessons.relativeLessonLabel
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LessonsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val courseRepository = CourseRepository(database.courseDao())
    private val lessonReminderScheduler = LessonReminderScheduler(application.applicationContext)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCourseId = MutableStateFlow<Int?>(null)
    private val _selectedDateFilter = MutableStateFlow(LessonDateFilter.ALL)
    private val _selectedSortOption = MutableStateFlow(LessonSortOption.NEXT_UPCOMING)
    private val _showPastThisWeek = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isLoading = MutableStateFlow(true)

    private var hasAppliedInitialCourse = false

    private val lessonsFlow = lessonRepository.getLessonsWithCourse()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val filterStateFlow = combine(
        _searchQuery,
        _selectedCourseId,
        _selectedDateFilter,
        _selectedSortOption,
        _showPastThisWeek
    ) { searchQuery, selectedCourseId, dateFilter, sortOption, showPast ->
        LessonFilterState(
            searchQuery = searchQuery,
            selectedCourseId = selectedCourseId,
            dateFilter = dateFilter,
            sortOption = sortOption,
            showPast = showPast
        )
    }

    val uiState: StateFlow<LessonsUiState> = combine(
        lessonsFlow,
        filterStateFlow,
        _isLoading,
        _errorMessage
    ) { lessons, filters, isLoading, errorMessage ->
        val nowMillis = System.currentTimeMillis()
        val availableCourses = lessons
            .map { LessonCourseFilterUi(it.lesson.courseId, it.courseName) }
            .distinctBy { it.courseId }
            .sortedBy { it.courseName.lowercase() }
        val selectedCourseName = filters.selectedCourseId?.let { courseId ->
            availableCourses.firstOrNull { it.courseId == courseId }?.courseName
        }

        val filteredLessons = lessons
            .filter { matchesSearch(it, filters.searchQuery) }
            .filter { filters.selectedCourseId == null || it.lesson.courseId == filters.selectedCourseId }
            .filter { matchesDateFilter(it, filters.dateFilter, nowMillis) }

        val lessonItems = filteredLessons.map { it.toLessonListItemUi(nowMillis) }
        val (pastThisWeek, upcoming) = lessonItems.partition { it.isPastThisWeek }

        LessonsUiState(
            isLoading = isLoading,
            errorMessage = errorMessage,
            searchQuery = filters.searchQuery,
            selectedCourseId = filters.selectedCourseId,
            selectedCourseName = selectedCourseName,
            selectedDateFilter = filters.dateFilter,
            selectedSortOption = filters.sortOption,
            availableCourses = availableCourses,
            upcomingLessons = sortLessons(upcoming, filters.sortOption),
            pastThisWeekLessons = sortPastLessons(pastThisWeek),
            showPastThisWeek = filters.showPast,
            hasAnyLessons = lessons.isNotEmpty()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LessonsUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            lessonsFlow.collect { _isLoading.value = false }
        }
    }

    fun setInitialCourseFilter(courseId: Int?) {
        if (hasAppliedInitialCourse || courseId == null) return
        _selectedCourseId.value = courseId
        hasAppliedInitialCourse = true
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onDateFilterChange(filter: LessonDateFilter) {
        _selectedDateFilter.value = filter
    }

    fun onCourseFilterChange(courseId: Int?) {
        _selectedCourseId.value = courseId
    }

    fun onSortOptionChange(option: LessonSortOption) {
        _selectedSortOption.value = option
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCourseId.value = null
        _selectedDateFilter.value = LessonDateFilter.ALL
        _selectedSortOption.value = LessonSortOption.NEXT_UPCOMING
    }

    fun togglePastThisWeekVisibility() {
        _showPastThisWeek.update { !it }
    }

    fun onToggleReminder(lessonId: Int, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val lesson = lessonRepository.getLessonById(lessonId).first()
                if (lesson == null) {
                    _errorMessage.value = "Lezione non trovata"
                    return@launch
                }

                lessonRepository.updateLessonReminderEnabled(
                    lessonId = lessonId,
                    enabled = enabled
                )

                if (enabled) {
                    val course = courseRepository.getCourseById(lesson.courseId).first()
                    if (course == null) {
                        _errorMessage.value = "Corso non trovato"
                        return@launch
                    }

                    lessonReminderScheduler.scheduleLessonReminder(
                        lessonId = lesson.id,
                        courseId = lesson.courseId,
                        courseName = course.name,
                        dayOfWeek = lesson.dayOfWeek,
                        startTimeMinutes = lesson.startTimeMinutes,
                        classroom = lesson.classroom
                    )
                } else {
                    lessonReminderScheduler.cancelLessonReminder(lessonId)
                }
            } catch (exception: Exception) {
                _errorMessage.value = exception.message
                    ?: "Aggiornamento promemoria lezione non riuscito"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    private fun matchesSearch(lesson: LessonWithCourse, query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return true

        return listOf(
            lesson.courseName,
            lesson.courseProfessor,
            lesson.lesson.classroom,
            lesson.lesson.building,
            lesson.lesson.locationQuery,
            lesson.lesson.notes
        ).any { value -> value?.contains(normalizedQuery, ignoreCase = true) == true }
    }

    private fun matchesDateFilter(
        lesson: LessonWithCourse,
        filter: LessonDateFilter,
        nowMillis: Long
    ): Boolean {
        val zoneId = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()
        val lessonDay = lesson.lesson.dayOfWeek

        return when (filter) {
            LessonDateFilter.ALL -> true
            LessonDateFilter.TODAY -> lessonDay == today.dayOfWeek.value
            LessonDateFilter.TOMORROW -> lessonDay == today.plusDays(1).dayOfWeek.value
            LessonDateFilter.THIS_WEEK -> true
            LessonDateFilter.REMINDER_ENABLED -> lesson.lesson.reminderEnabled
        }
    }

    private fun sortLessons(
        lessons: List<LessonListItemUi>,
        option: LessonSortOption
    ): List<LessonListItemUi> {
        return when (option) {
            LessonSortOption.NEXT_UPCOMING -> lessons.sortedBy { it.nextOccurrenceMillis }
            LessonSortOption.DAY_AND_TIME -> lessons.sortedWith(
                compareBy<LessonListItemUi> { it.dayOfWeek }.thenBy { it.startTime }
            )
            LessonSortOption.COURSE_NAME_ASC -> lessons.sortedBy { it.courseName.lowercase() }
        }
    }

    private fun sortPastLessons(lessons: List<LessonListItemUi>): List<LessonListItemUi> {
        return lessons.sortedWith(
            compareByDescending<LessonListItemUi> { it.dayOfWeek }
                .thenByDescending { it.startTime }
        )
    }

    private fun LessonWithCourse.toLessonListItemUi(nowMillis: Long): LessonListItemUi {
        val occurrenceMillis = nextOccurrenceMillis(
            dayOfWeek = lesson.dayOfWeek,
            startTimeMinutes = lesson.startTimeMinutes,
            nowMillis = nowMillis
        )
        val isPastThisWeek = isAlreadyPassedThisWeek(
            dayOfWeek = lesson.dayOfWeek,
            startTimeMinutes = lesson.startTimeMinutes,
            nowMillis = nowMillis
        )
        val relativeLabel = if (isPastThisWeek) {
            dayOfWeekLabel(lesson.dayOfWeek)
        } else {
            relativeLessonLabel(occurrenceMillis, nowMillis)
        }

        return LessonListItemUi(
            lessonId = lesson.id,
            courseId = lesson.courseId,
            courseName = courseName,
            courseProfessor = courseProfessor,
            dayOfWeek = lesson.dayOfWeek,
            dayLabel = dayOfWeekLabel(lesson.dayOfWeek),
            relativeDayLabel = relativeLabel,
            startTime = formatMinutesToTime(lesson.startTimeMinutes),
            endTime = formatMinutesToTime(lesson.endTimeMinutes),
            classroom = lesson.classroom,
            building = lesson.building,
            locationQuery = lesson.locationQuery,
            notes = lesson.notes,
            reminderEnabled = lesson.reminderEnabled,
            nextOccurrenceMillis = occurrenceMillis,
            isPastThisWeek = isPastThisWeek
        )
    }
}

private data class LessonFilterState(
    val searchQuery: String,
    val selectedCourseId: Int?,
    val dateFilter: LessonDateFilter,
    val sortOption: LessonSortOption,
    val showPast: Boolean
)
