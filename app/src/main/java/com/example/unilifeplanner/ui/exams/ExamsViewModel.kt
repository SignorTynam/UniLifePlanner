package com.example.unilifeplanner.ui.exams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.ExamAppealSource
import com.example.unilifeplanner.data.local.ExamAppealWithCourse
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.domain.exams.examStartMillis
import com.example.unilifeplanner.domain.exams.formatExamDate
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.ui.exams.components.needsFeedbackRegistration
import com.example.unilifeplanner.university.refresh.UniboRefreshManager
import com.example.unilifeplanner.university.refresh.UniboRefreshSource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())
    private val courseRepository = CourseRepository(database.courseDao())
    private val examReminderScheduler = ExamReminderScheduler(application.applicationContext)
    private val uniboRefreshManager = UniboRefreshManager(application.applicationContext)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCourseId = MutableStateFlow<Int?>(null)
    private val _selectedDateFilter = MutableStateFlow(ExamDateFilter.ALL)
    private val _selectedSortOption = MutableStateFlow(ExamSortOption.NEXT_UPCOMING)
    private val _selectedExamDayMillis = MutableStateFlow<Long?>(null)
    private val _showPastExams = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshMessage = MutableStateFlow<String?>(null)

    private var hasAppliedInitialCourse = false

    private val filterStateFlow = combine(
        combine(
            _searchQuery,
            _selectedCourseId,
            _selectedDateFilter
        ) { searchQuery, selectedCourseId, dateFilter ->
            Triple(searchQuery, selectedCourseId, dateFilter)
        },
        combine(
            _selectedSortOption,
            _selectedExamDayMillis,
            _showPastExams
        ) { sortOption, selectedExamDayMillis, showPast ->
            Triple(sortOption, selectedExamDayMillis, showPast)
        }
    ) { searchCourseDate, sortDayPast ->
        val (searchQuery, selectedCourseId, dateFilter) = searchCourseDate
        val (sortOption, selectedExamDayMillis, showPast) = sortDayPast
        ExamFilterState(
            searchQuery = searchQuery,
            selectedCourseId = selectedCourseId,
            dateFilter = dateFilter,
            sortOption = sortOption,
            selectedExamDayMillis = selectedExamDayMillis,
            showPast = showPast
        )
    }

    private val refreshStateFlow = combine(
        _isRefreshing,
        _refreshMessage
    ) { isRefreshing, refreshMessage ->
        ExamRefreshState(
            isRefreshing = isRefreshing,
            refreshMessage = refreshMessage
        )
    }

    private val examsFlow = examAppealRepository.getExamAppealsWithCourse()
        .catch { throwable ->
            _errorMessage.value = throwable.message ?: "Errore nel caricamento degli appelli"
            emit(emptyList())
        }

    private val dataStateFlow = combine(
        examsFlow,
        courseRepository.allCourses,
        filterStateFlow
    ) { exams, courses, filters ->
        Triple(exams, courses, filters)
    }

    private val statusStateFlow = combine(
        _isLoading,
        _errorMessage,
        refreshStateFlow
    ) { isLoading, errorMessage, refreshState ->
        Triple(isLoading, errorMessage, refreshState)
    }

    val uiState: StateFlow<ExamsUiState> = combine(
        dataStateFlow,
        statusStateFlow
    ) { dataState, statusState ->
        val (exams, courses, filters) = dataState
        val (isLoading, errorMessage, refreshState) = statusState
        val nowMillis = System.currentTimeMillis()
        val availableCourses = courses
            .filter { it.status != com.example.unilifeplanner.domain.model.CourseStatus.COMPLETED.name }
            .map { course -> ExamCourseOptionUi(course.id, course.name) }
            .sortedBy { it.courseName.lowercase() }
        val effectiveSelectedCourseId = filters.selectedCourseId
            ?.takeIf { id -> availableCourses.any { it.courseId == id } }
        val selectedCourseName = effectiveSelectedCourseId?.let { id ->
            availableCourses.firstOrNull { it.courseId == id }?.courseName
        }

        val baseFiltered = exams
            .filter { effectiveSelectedCourseId == null || it.exam.courseId == effectiveSelectedCourseId }
            .filter { matchesSearch(it, filters.searchQuery) }
            .filter { matchesDateFilter(it, filters.dateFilter, nowMillis) }

        val itemsBeforeDayFilter = baseFiltered.map { it.toListItem(nowMillis) }
        val dayFilteredItems = filters.selectedExamDayMillis?.let { dayMillis ->
            itemsBeforeDayFilter.filter { it.dayKeyMillis == dayMillis }
        } ?: itemsBeforeDayFilter

        val (pastBeforeSort, upcomingBeforeSort) = dayFilteredItems.partition { it.isPast }
        val upcoming = sortExams(upcomingBeforeSort, filters.sortOption, isPast = false)
        val past = sortExams(pastBeforeSort, filters.sortOption, isPast = true)

        val agendaBase = exams
            .filter { effectiveSelectedCourseId == null || it.exam.courseId == effectiveSelectedCourseId }
            .filter { matchesSearch(it, filters.searchQuery) }
            .map { it.toListItem(nowMillis) }

        val upcomingAgenda = agendaBase.filter { !it.isPast }
        val pastAgenda = agendaBase.filter { it.isPast }

        ExamsUiState(
            isLoading = isLoading,
            errorMessage = errorMessage,
            searchQuery = filters.searchQuery,
            selectedCourseId = effectiveSelectedCourseId,
            selectedCourseName = selectedCourseName,
            selectedDateFilter = filters.dateFilter,
            selectedSortOption = filters.sortOption,
            selectedExamDayMillis = filters.selectedExamDayMillis,
            availableCourses = availableCourses,
            upcomingExams = upcoming,
            pastExams = past,
            showPastExams = filters.showPast,
            hasAnyExams = exams.isNotEmpty(),
            isRefreshing = refreshState.isRefreshing,
            refreshMessage = refreshState.refreshMessage,
            filteredResultCount = upcoming.size + past.size,
            upcomingExamCount = upcomingAgenda.size,
            thisWeekExamCount = countExamsThisWeek(upcomingAgenda, nowMillis),
            reminderExamCount = agendaBase.count { it.reminderEnabled },
            pendingFeedbackCount = pastAgenda.count { it.needsFeedbackRegistration() },
            examCountByDay = upcomingAgenda
                .groupingBy { it.dayKeyMillis }
                .eachCount()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExamsUiState()
    )

    init {
        viewModelScope.launch {
            examsFlow.collect {
                _isLoading.value = false
            }
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

    fun onDateFilterChange(filter: ExamDateFilter) {
        _selectedDateFilter.value = filter
    }

    fun onSortOptionChange(option: ExamSortOption) {
        _selectedSortOption.value = option
    }

    fun onSelectedExamDayChange(dayMillis: Long?) {
        _selectedExamDayMillis.value = dayMillis
    }

    fun onCourseFilterChange(courseId: Int?) {
        _selectedCourseId.value = courseId
    }

    fun clearCourseFilter() {
        _selectedCourseId.value = null
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedCourseId.value = null
        _selectedDateFilter.value = ExamDateFilter.ALL
        _selectedSortOption.value = ExamSortOption.NEXT_UPCOMING
        _selectedExamDayMillis.value = null
    }

    fun togglePastExamsVisibility() {
        _showPastExams.update { !it }
    }

    fun onToggleReminder(examAppealId: Int, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val examWithCourse = examAppealRepository
                    .getExamAppealWithCourseById(examAppealId)
                    .first()
                if (examWithCourse == null) {
                    _errorMessage.value = "Appello non trovato"
                    return@launch
                }

                val exam = examWithCourse.exam
                val startMillis = examStartMillis(
                    dateMillis = exam.dateMillis,
                    timeMinutes = exam.timeMinutes
                )
                if (enabled && startMillis <= System.currentTimeMillis()) {
                    _errorMessage.value = "Non puoi attivare un promemoria per un appello passato"
                    return@launch
                }

                examAppealRepository.updateExamAppealReminderEnabled(
                    examAppealId = examAppealId,
                    enabled = enabled
                )

                if (enabled) {
                    examReminderScheduler.scheduleExamAppealReminders(
                        examAppealId = exam.id,
                        courseId = exam.courseId,
                        courseName = examWithCourse.courseName,
                        examDateMillis = exam.dateMillis,
                        timeMinutes = exam.timeMinutes,
                        reminderDateTimeMillis = exam.reminderDateTimeMillis
                    )
                    examAppealRepository.markFeedbackScheduled(exam.id)
                } else {
                    examReminderScheduler.cancelExamAppealReminders(exam.id)
                }
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Aggiornamento promemoria non riuscito"
            }
        }
    }

    fun deleteExamAppeal(examAppealId: Int) {
        viewModelScope.launch {
            try {
                examReminderScheduler.cancelExamAppealReminders(examAppealId)
                examAppealRepository.deleteExamAppealById(examAppealId)
            } catch (exception: Exception) {
                _errorMessage.value = exception.message ?: "Eliminazione appello non riuscita"
            }
        }
    }

    fun clearMessages() {
        _errorMessage.value = null
    }

    fun refreshUniboData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _refreshMessage.value = uniboRefreshManager.refreshImportedUniboData(
                    source = UniboRefreshSource.MANUAL,
                    force = true
                ).message
            } catch (exception: Exception) {
                _refreshMessage.value = "Aggiornamento UniBo non riuscito. Riprova più tardi."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearRefreshMessage() {
        _refreshMessage.value = null
    }

    private fun matchesSearch(exam: ExamAppealWithCourse, query: String): Boolean {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank()) return true

        return listOf(
            exam.courseName,
            exam.exam.location,
            exam.exam.type,
            exam.exam.notes
        ).any { value -> value?.contains(normalizedQuery, ignoreCase = true) == true }
    }

    private fun matchesDateFilter(
        exam: ExamAppealWithCourse,
        filter: ExamDateFilter,
        nowMillis: Long
    ): Boolean {
        val zoneId = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()
        val examDate = Instant.ofEpochMilli(exam.exam.dateMillis)
            .atZone(zoneId)
            .toLocalDate()
        val startMillis = examStartMillis(
            dateMillis = exam.exam.dateMillis,
            timeMinutes = exam.exam.timeMinutes
        )

        return when (filter) {
            ExamDateFilter.ALL -> true
            ExamDateFilter.TODAY -> examDate == today
            ExamDateFilter.TOMORROW -> examDate == today.plusDays(1)
            ExamDateFilter.THIS_WEEK -> !examDate.isBefore(today) &&
                !examDate.isAfter(today.plusDays(6))
            ExamDateFilter.THIS_MONTH -> examDate.year == today.year &&
                examDate.month == today.month
            ExamDateFilter.REMINDER_ENABLED -> exam.exam.reminderEnabled
            ExamDateFilter.FEEDBACK_PENDING -> startMillis < nowMillis &&
                (exam.exam.feedbackStatus == "PENDING" ||
                    exam.exam.feedbackStatus == "SCHEDULED") &&
                exam.exam.feedbackResult == null
        }
    }

    private fun sortExams(
        exams: List<ExamAppealListItemUi>,
        option: ExamSortOption,
        isPast: Boolean
    ): List<ExamAppealListItemUi> {
        return when (option) {
            ExamSortOption.NEXT_UPCOMING -> if (isPast) {
                exams.sortedByDescending { it.startMillis }
            } else {
                exams.sortedBy { it.startMillis }
            }
            ExamSortOption.COURSE_NAME_ASC -> exams.sortedBy { it.courseName.lowercase() }
            ExamSortOption.DATE_DESC -> exams.sortedByDescending { it.startMillis }
            ExamSortOption.FEEDBACK_STATUS -> exams.sortedWith(
                compareByDescending<ExamAppealListItemUi> { it.needsFeedbackRegistration() }
                    .thenByDescending { it.startMillis }
            )
        }
    }

    private fun countExamsThisWeek(
        exams: List<ExamAppealListItemUi>,
        nowMillis: Long
    ): Int {
        val zoneId = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()
        val weekEnd = today.plusDays(6)

        return exams.count { exam ->
            val examDate = Instant.ofEpochMilli(exam.dayKeyMillis)
                .atZone(zoneId)
                .toLocalDate()
            !examDate.isBefore(today) && !examDate.isAfter(weekEnd)
        }
    }

    private fun ExamAppealWithCourse.toListItem(nowMillis: Long): ExamAppealListItemUi {
        val zoneId = ZoneId.systemDefault()
        val startMillis = examStartMillis(
            dateMillis = exam.dateMillis,
            timeMinutes = exam.timeMinutes
        )
        val dayKeyMillis = Instant.ofEpochMilli(exam.dateMillis)
            .atZone(zoneId)
            .toLocalDate()
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()

        return ExamAppealListItemUi(
            examAppealId = exam.id,
            courseId = exam.courseId,
            courseName = courseName,
            dateLabel = formatExamDate(exam.dateMillis),
            timeLabel = exam.timeMinutes?.let { formatMinutesToTime(it) },
            location = exam.location,
            notes = exam.notes,
            type = exam.type,
            reminderEnabled = exam.reminderEnabled,
            sourceLabel = sourceLabel(exam.source),
            feedbackStatus = exam.feedbackStatus,
            feedbackResult = exam.feedbackResult,
            feedbackGrade = exam.feedbackGrade,
            startMillis = startMillis,
            isPast = startMillis < nowMillis,
            dateMillis = exam.dateMillis,
            dayKeyMillis = dayKeyMillis,
            relativeDateLabel = relativeExamDateLabel(dayKeyMillis, nowMillis, zoneId)
        )
    }

    private fun relativeExamDateLabel(
        dayKeyMillis: Long,
        nowMillis: Long,
        zoneId: ZoneId
    ): String {
        val examDate = Instant.ofEpochMilli(dayKeyMillis)
            .atZone(zoneId)
            .toLocalDate()
        val today = Instant.ofEpochMilli(nowMillis)
            .atZone(zoneId)
            .toLocalDate()

        return when (examDate) {
            today -> "Oggi"
            today.plusDays(1) -> "Domani"
            else -> "${dayOfWeekLabel(examDate.dayOfWeek.value)}, ${
                examDate.format(DateTimeFormatter.ofPattern("dd/MM"))
            }"
        }
    }

    private fun sourceLabel(source: String): String {
        return when (source) {
            ExamAppealSource.UNIBO.name -> "UniBo"
            else -> "Manuale"
        }
    }
}

private data class ExamFilterState(
    val searchQuery: String,
    val selectedCourseId: Int?,
    val dateFilter: ExamDateFilter,
    val sortOption: ExamSortOption,
    val selectedExamDayMillis: Long?,
    val showPast: Boolean
)

private data class ExamRefreshState(
    val isRefreshing: Boolean,
    val refreshMessage: String?
)
