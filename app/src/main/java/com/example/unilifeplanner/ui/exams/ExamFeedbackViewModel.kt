package com.example.unilifeplanner.ui.exams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.domain.courses.CourseCompletionManager
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult
import com.example.unilifeplanner.domain.exams.formatExamDateTime
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExamFeedbackViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao())
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())
    private val completionManager = CourseCompletionManager(
        lessonRepository = lessonRepository,
        examAppealRepository = examAppealRepository,
        lessonReminderScheduler = LessonReminderScheduler(application.applicationContext),
        examReminderScheduler = ExamReminderScheduler(application.applicationContext)
    )

    private val _uiState = MutableStateFlow(ExamFeedbackUiState())
    val uiState: StateFlow<ExamFeedbackUiState> = _uiState.asStateFlow()

    fun load(examAppealId: Int) {
        viewModelScope.launch {
            _uiState.value = ExamFeedbackUiState(isLoading = true, examAppealId = examAppealId)
            val examWithCourse = examAppealRepository.getExamAppealWithCourseById(examAppealId)
                .first()
            if (examWithCourse == null) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Appello non trovato")
                }
                return@launch
            }
            val exam = examWithCourse.exam
            val savedResult = exam.feedbackResult?.let { value ->
                runCatching { ExamFeedbackResult.valueOf(value) }.getOrNull()
            }
            _uiState.value = ExamFeedbackUiState(
                isLoading = false,
                examAppealId = exam.id,
                courseId = exam.courseId,
                courseName = examWithCourse.courseName,
                dateTimeLabel = formatExamDateTime(exam.dateMillis, exam.timeMinutes),
                location = exam.location,
                selectedResult = savedResult ?: ExamFeedbackResult.PASSED,
                grade = exam.feedbackGrade.orEmpty(),
                notes = exam.feedbackNotes.orEmpty(),
                markCourseCompleted = savedResult == null || savedResult == ExamFeedbackResult.PASSED
            )
        }
    }

    fun selectResult(result: ExamFeedbackResult) {
        _uiState.update {
            it.copy(
                selectedResult = result,
                grade = if (result == ExamFeedbackResult.PASSED) it.grade else "",
                gradeError = null,
                markCourseCompleted = result == ExamFeedbackResult.PASSED && it.markCourseCompleted,
                errorMessage = null
            )
        }
    }

    fun updateGrade(value: String) {
        val limitedValue = value.take(12)
        _uiState.update {
            it.copy(
                grade = limitedValue,
                gradeError = validateExamGrade(limitedValue),
                errorMessage = null
            )
        }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun updateMarkCourseCompleted(value: Boolean) {
        _uiState.update { it.copy(markCourseCompleted = value, errorMessage = null) }
    }

    fun saveFeedback() {
        val state = _uiState.value
        if (state.isSaving) return
        val normalizedGrade = if (state.selectedResult == ExamFeedbackResult.PASSED) {
            normalizeExamGrade(state.grade)
        } else {
            ""
        }
        val gradeError = if (state.selectedResult == ExamFeedbackResult.PASSED) {
            validateExamGrade(normalizedGrade)
        } else {
            null
        }
        if (gradeError != null) {
            _uiState.update {
                it.copy(
                    grade = normalizedGrade,
                    gradeError = gradeError,
                    errorMessage = "Controlla il voto inserito"
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    grade = normalizedGrade,
                    gradeError = null,
                    errorMessage = null
                )
            }
            try {
                examAppealRepository.saveExamFeedback(
                    examAppealId = state.examAppealId,
                    result = state.selectedResult,
                    grade = normalizedGrade,
                    notes = state.notes
                )
                var completionMessage: String? = null
                if (state.selectedResult == ExamFeedbackResult.PASSED &&
                    state.markCourseCompleted
                ) {
                    val course = courseRepository.getCourseById(state.courseId).first()
                    if (course != null && course.status != CourseStatus.COMPLETED.name) {
                        courseRepository.updateCourse(
                            course.copy(
                                status = CourseStatus.COMPLETED.name,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                        completionManager.disableFutureRemindersForCompletedCourse(course.id)
                        completionMessage = "Corso segnato come completato."
                    }
                }
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveSuccess = true,
                        completionMessage = completionMessage
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Non sono riuscito a salvare l’esito. Riprova."
                    )
                }
            }
        }
    }

    fun dismissFeedback() {
        if (_uiState.value.isSaving) return
        val examAppealId = _uiState.value.examAppealId
        viewModelScope.launch {
            examAppealRepository.dismissExamFeedback(examAppealId)
            _uiState.update { it.copy(saveSuccess = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun normalizeExamGrade(value: String): String {
        return value.trim().uppercase()
    }

    private fun validateExamGrade(value: String): String? {
        val normalized = normalizeExamGrade(value)
        if (normalized.isBlank()) return null
        if (normalized == "30L" || normalized == "30 E LODE") return null
        val numericGrade = normalized.toIntOrNull()
        return when {
            numericGrade == null -> "Inserisci un voto tra 18 e 30, oppure 30L."
            numericGrade !in 18..30 -> "Il voto deve essere compreso tra 18 e 30."
            else -> null
        }
    }
}
