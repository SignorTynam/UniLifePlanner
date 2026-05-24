package com.example.unilifeplanner.ui.exams

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.ExamAppealEntity
import com.example.unilifeplanner.data.local.ExamAppealSource
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.domain.exams.examDateToStartOfDayMillis
import com.example.unilifeplanner.domain.exams.examStartMillis
import com.example.unilifeplanner.domain.exams.formatExamDate
import com.example.unilifeplanner.domain.exams.parseExamDate
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.domain.lessons.parseTimeToMinutes
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditExamViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao())
    private val examAppealRepository = ExamAppealRepository(database.examAppealDao())
    private val examReminderScheduler = ExamReminderScheduler(application.applicationContext)

    private val _uiState = MutableStateFlow(ExamAppealFormUiState())
    val uiState: StateFlow<ExamAppealFormUiState> = _uiState.asStateFlow()

    private var selectedExamAppeal: ExamAppealEntity? = null

    fun loadExamAppeal(
        courseId: Int?,
        examAppealId: Int?
    ) {
        viewModelScope.launch {
            _uiState.value = ExamAppealFormUiState(
                examAppealId = examAppealId,
                courseId = courseId,
                isLoading = true
            )

            try {
                val courses = courseRepository.allCourses.first()
                    .map { course -> ExamCourseOptionUi(course.id, course.name) }
                    .sortedBy { it.courseName.lowercase() }

                if (courses.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            courses = emptyList(),
                            errorMessage = "Aggiungi prima un corso per poter creare un appello."
                        )
                    }
                    return@launch
                }

                if (examAppealId == null) {
                    val selectedCourseId = courseId ?: courses.singleOrNull()?.courseId
                    _uiState.value = ExamAppealFormUiState(
                        courseId = selectedCourseId,
                        courseName = courses.firstOrNull { it.courseId == selectedCourseId }
                            ?.courseName
                            .orEmpty(),
                        courses = courses,
                        reminderEnabled = false,
                        isLoading = false
                    )
                    return@launch
                }

                val examAppeal = examAppealRepository.getExamAppealById(examAppealId).first()
                if (examAppeal == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            courses = courses,
                            errorMessage = "Appello non trovato"
                        )
                    }
                    return@launch
                }

                selectedExamAppeal = examAppeal
                _uiState.value = ExamAppealFormUiState(
                    examAppealId = examAppeal.id,
                    courseId = examAppeal.courseId,
                    courseName = courses.firstOrNull { it.courseId == examAppeal.courseId }
                        ?.courseName
                        .orEmpty(),
                    courses = courses,
                    date = formatExamDate(examAppeal.dateMillis),
                    time = examAppeal.timeMinutes?.let { formatMinutesToTime(it) }.orEmpty(),
                    location = examAppeal.location.orEmpty(),
                    notes = examAppeal.notes.orEmpty(),
                    type = examAppeal.type.orEmpty(),
                    reminderEnabled = examAppeal.reminderEnabled,
                    sourceLabel = sourceLabel(examAppeal.source),
                    isLoading = false
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Errore nel caricamento dell'appello"
                    )
                }
            }
        }
    }

    fun updateCourse(courseId: Int) {
        _uiState.update { state ->
            state.copy(
                courseId = courseId,
                courseName = state.courses.firstOrNull { it.courseId == courseId }
                    ?.courseName
                    .orEmpty(),
                courseError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateDate(value: String) {
        _uiState.update {
            it.copy(
                date = value,
                dateError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateTime(value: String) {
        _uiState.update {
            it.copy(
                time = value,
                timeError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateLocation(value: String) {
        _uiState.update { it.copy(location = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateType(value: String) {
        _uiState.update { it.copy(type = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateReminderEnabled(value: Boolean) {
        _uiState.update { it.copy(reminderEnabled = value, errorMessage = null, saveSuccess = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun saveExamAppeal() {
        val state = _uiState.value
        val parsedDate = parseExamDate(state.date)
        val parsedTime = state.time.trim()
            .takeIf { it.isNotEmpty() }
            ?.let { parseTimeToMinutes(it) }
        val timeInvalid = state.time.isNotBlank() && parsedTime == null
        val validatedState = validateState(
            state = state,
            parsedDate = parsedDate,
            parsedTime = parsedTime,
            timeInvalid = timeInvalid
        )

        if (validatedState != null) {
            _uiState.value = validatedState
            return
        }

        val date = requireNotNull(parsedDate)
        val courseId = requireNotNull(state.courseId)
        val dateMillis = examDateToStartOfDayMillis(date)
        val startMillis = examStartMillis(dateMillis, parsedTime)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
            try {
                val existing = selectedExamAppeal
                    ?: state.examAppealId?.let { examAppealRepository.getExamAppealById(it).first() }
                val examAppeal = ExamAppealEntity(
                    id = state.examAppealId ?: 0,
                    courseId = courseId,
                    dateMillis = dateMillis,
                    timeMinutes = parsedTime,
                    location = state.location,
                    notes = state.notes,
                    type = state.type,
                    reminderEnabled = state.reminderEnabled,
                    reminderDateTimeMillis = existing?.reminderDateTimeMillis,
                    source = existing?.source ?: ExamAppealSource.MANUAL.name,
                    externalId = existing?.externalId,
                    officialUrl = existing?.officialUrl,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val savedExamAppealId = if (state.examAppealId == null) {
                    examAppealRepository.insertExamAppeal(examAppeal).toInt()
                } else {
                    if (existing == null) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Appello non trovato"
                            )
                        }
                        return@launch
                    }
                    examAppealRepository.updateExamAppeal(
                        examAppeal.copy(
                            id = existing.id,
                            createdAt = existing.createdAt
                        )
                    )
                    examReminderScheduler.cancelExamAppealReminders(existing.id)
                    existing.id
                }

                if (state.reminderEnabled && startMillis > System.currentTimeMillis()) {
                    val courseName = state.courses.firstOrNull { it.courseId == courseId }
                        ?.courseName
                        .orEmpty()
                    examReminderScheduler.scheduleExamAppealReminders(
                        examAppealId = savedExamAppealId,
                        courseId = courseId,
                        courseName = courseName,
                        examDateMillis = dateMillis,
                        timeMinutes = parsedTime,
                        reminderDateTimeMillis = existing?.reminderDateTimeMillis
                    )
                } else {
                    examReminderScheduler.cancelExamAppealReminders(savedExamAppealId)
                }

                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Salvataggio appello non riuscito"
                    )
                }
            }
        }
    }

    private fun validateState(
        state: ExamAppealFormUiState,
        parsedDate: java.time.LocalDate?,
        parsedTime: Int?,
        timeInvalid: Boolean
    ): ExamAppealFormUiState? {
        val courseError = if (state.courseId == null) {
            "Seleziona un corso"
        } else {
            null
        }
        val dateError = if (parsedDate == null) {
            "Inserisci una data valida nel formato gg/mm/aaaa"
        } else {
            null
        }
        val timeError = if (timeInvalid) {
            "Inserisci un orario valido, ad esempio 09:00"
        } else {
            null
        }
        val reminderDateError = if (
            state.reminderEnabled &&
            parsedDate != null &&
            !timeInvalid &&
            examStartMillis(examDateToStartOfDayMillis(parsedDate), parsedTime) <= System.currentTimeMillis()
        ) {
            "Non puoi attivare un promemoria per un appello passato"
        } else {
            null
        }

        return if (
            courseError == null &&
            dateError == null &&
            timeError == null &&
            reminderDateError == null
        ) {
            null
        } else {
            state.copy(
                courseError = courseError,
                dateError = dateError ?: reminderDateError,
                timeError = timeError,
                errorMessage = "Controlla i campi evidenziati",
                saveSuccess = false
            )
        }
    }

    private fun sourceLabel(source: String): String {
        return when (source) {
            ExamAppealSource.UNIBO.name -> "UniBo"
            else -> "Manuale"
        }
    }
}
