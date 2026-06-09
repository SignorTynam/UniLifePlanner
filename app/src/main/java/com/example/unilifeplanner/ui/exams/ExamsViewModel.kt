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
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.university.refresh.UniboRefreshManager
import com.example.unilifeplanner.university.refresh.UniboRefreshSource
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

    private val _selectedCourseId = MutableStateFlow<Int?>(null)
    private val _showPastExams = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _isRefreshing = MutableStateFlow(false)
    private val _refreshMessage = MutableStateFlow<String?>(null)

    private var hasAppliedInitialCourse = false

    private val refreshStateFlow = combine(
        _isRefreshing,
        _refreshMessage
    ) { isRefreshing, refreshMessage ->
        ExamRefreshState(
            isRefreshing = isRefreshing,
            refreshMessage = refreshMessage
        )
    }

    private val controlsFlow = combine(
        _selectedCourseId,
        _showPastExams,
        _isLoading,
        _errorMessage,
        refreshStateFlow
    ) { selectedCourseId, showPast, isLoading, errorMessage, refreshState ->
        ExamControls(
            selectedCourseId = selectedCourseId,
            showPast = showPast,
            isLoading = isLoading,
            errorMessage = errorMessage,
            isRefreshing = refreshState.isRefreshing,
            refreshMessage = refreshState.refreshMessage
        )
    }

    val uiState: StateFlow<ExamsUiState> = combine(
        examAppealRepository.getExamAppealsWithCourse()
            .catch { throwable ->
                _errorMessage.value = throwable.message ?: "Errore nel caricamento degli appelli"
                emit(emptyList())
            },
        courseRepository.allCourses,
        controlsFlow
    ) { exams, courses, controls ->
        val availableCourses = courses
            .filter { it.status != com.example.unilifeplanner.domain.model.CourseStatus.COMPLETED.name }
            .map { course -> ExamCourseOptionUi(course.id, course.name) }
            .sortedBy { it.courseName.lowercase() }
        val effectiveSelectedCourseId = controls.selectedCourseId
            ?.takeIf { id -> availableCourses.any { it.courseId == id } }
        val filteredExams = exams.filter { exam ->
            effectiveSelectedCourseId == null || exam.exam.courseId == effectiveSelectedCourseId
        }
        val now = System.currentTimeMillis()
        val items = filteredExams.map { exam -> exam.toListItem(now) }
        val upcoming = items
            .filter { !it.isPast }
            .sortedBy { it.startMillis }
        val past = items
            .filter { it.isPast }
            .sortedByDescending { it.startMillis }

        ExamsUiState(
            isLoading = controls.isLoading,
            errorMessage = controls.errorMessage,
            selectedCourseId = effectiveSelectedCourseId,
            selectedCourseName = effectiveSelectedCourseId?.let { id ->
                availableCourses.firstOrNull { it.courseId == id }?.courseName
            },
            availableCourses = availableCourses,
            upcomingExams = upcoming,
            pastExams = past,
            showPastExams = controls.showPast,
            hasAnyExams = exams.isNotEmpty(),
            isRefreshing = controls.isRefreshing,
            refreshMessage = controls.refreshMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExamsUiState()
    )

    init {
        viewModelScope.launch {
            examAppealRepository.getExamAppealsWithCourse().collect {
                _isLoading.value = false
            }
        }
    }

    fun setInitialCourseFilter(courseId: Int?) {
        if (hasAppliedInitialCourse || courseId == null) return
        _selectedCourseId.value = courseId
        hasAppliedInitialCourse = true
    }

    fun onCourseFilterChange(courseId: Int?) {
        _selectedCourseId.value = courseId
    }

    fun clearCourseFilter() {
        _selectedCourseId.value = null
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
                _refreshMessage.value = "Aggiornamento UniBo non riuscito. Riprova piu tardi."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun clearRefreshMessage() {
        _refreshMessage.value = null
    }

    private fun ExamAppealWithCourse.toListItem(nowMillis: Long): ExamAppealListItemUi {
        val startMillis = examStartMillis(
            dateMillis = exam.dateMillis,
            timeMinutes = exam.timeMinutes
        )
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
            isPast = startMillis < nowMillis
        )
    }

    private fun sourceLabel(source: String): String {
        return when (source) {
            ExamAppealSource.UNIBO.name -> "UniBo"
            else -> "Manuale"
        }
    }
}

private data class ExamControls(
    val selectedCourseId: Int?,
    val showPast: Boolean,
    val isLoading: Boolean,
    val errorMessage: String?,
    val isRefreshing: Boolean,
    val refreshMessage: String?
)

private data class ExamRefreshState(
    val isRefreshing: Boolean,
    val refreshMessage: String?
)
