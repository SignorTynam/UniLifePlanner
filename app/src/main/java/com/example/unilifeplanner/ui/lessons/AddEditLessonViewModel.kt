package com.example.unilifeplanner.ui.lessons

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.local.LessonEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.domain.lessons.formatMinutesToTime
import com.example.unilifeplanner.domain.lessons.formatLessonDate
import com.example.unilifeplanner.domain.lessons.localDateToStartOfDayMillis
import com.example.unilifeplanner.domain.lessons.nextLessonDateMillis
import com.example.unilifeplanner.domain.lessons.parseLessonDate
import com.example.unilifeplanner.domain.lessons.parseTimeToMinutes
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddEditLessonViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val courseRepository = CourseRepository(database.courseDao())
    private val lessonRepository = LessonRepository(database.lessonDao())
    private val lessonReminderScheduler = LessonReminderScheduler(application.applicationContext)

    private val _uiState = MutableStateFlow(LessonUiState(isLoading = true))
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    private var selectedLesson: LessonEntity? = null
    private var selectedCourse: CourseEntity? = null

    fun loadLesson(
        courseId: Int,
        lessonId: Int?
    ) {
        viewModelScope.launch {
            _uiState.value = LessonUiState(
                courseId = courseId,
                lessonId = lessonId,
                isLoading = true
            )

            try {
                val course = courseRepository.getCourseById(courseId).first()
                if (course == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Corso non trovato"
                        )
                    }
                    return@launch
                }

                selectedCourse = course

                if (lessonId == null) {
                    selectedLesson = null
                    _uiState.value = LessonUiState(
                        courseId = courseId,
                        lessonId = null,
                        reminderEnabled = true,
                        isLoading = false
                    )
                    return@launch
                }

                val lesson = lessonRepository.getLessonById(lessonId).first()
                if (lesson == null || lesson.courseId != courseId) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Lezione non trovata"
                        )
                    }
                    return@launch
                }

                selectedLesson = lesson
                val displayedDateMillis = lesson.dateMillis ?: nextLessonDateMillis(
                    dayOfWeek = lesson.dayOfWeek,
                    startTimeMinutes = lesson.startTimeMinutes
                )
                _uiState.value = LessonUiState(
                    courseId = courseId,
                    lessonId = lesson.id,
                    date = formatLessonDate(displayedDateMillis),
                    dayOfWeek = lesson.dayOfWeek,
                    startTime = formatMinutesToTime(lesson.startTimeMinutes),
                    endTime = formatMinutesToTime(lesson.endTimeMinutes),
                    classroom = lesson.classroom.orEmpty(),
                    building = lesson.building.orEmpty(),
                    locationQuery = lesson.locationQuery.orEmpty(),
                    notes = lesson.notes.orEmpty(),
                    reminderEnabled = lesson.reminderEnabled,
                    isLoading = false
                )
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Errore nel caricamento della lezione"
                    )
                }
            }
        }
    }

    fun updateDate(value: String) {
        val parsedDate = parseLessonDate(value)
        _uiState.update {
            it.copy(
                date = value,
                dayOfWeek = parsedDate?.dayOfWeek?.value ?: it.dayOfWeek,
                dateError = null,
                dayOfWeekError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateDayOfWeek(value: Int) {
        _uiState.update {
            it.copy(
                dayOfWeek = value,
                dayOfWeekError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateStartTime(value: String) {
        _uiState.update {
            it.copy(
                startTime = value,
                startTimeError = null,
                endTimeError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateEndTime(value: String) {
        _uiState.update {
            it.copy(
                endTime = value,
                endTimeError = null,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun updateClassroom(value: String) {
        _uiState.update { it.copy(classroom = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateBuilding(value: String) {
        _uiState.update { it.copy(building = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateLocationQuery(value: String) {
        _uiState.update { it.copy(locationQuery = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateNotes(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null, saveSuccess = false) }
    }

    fun updateReminderEnabled(value: Boolean) {
        _uiState.update {
            it.copy(
                reminderEnabled = value,
                errorMessage = null,
                saveSuccess = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetSaveState() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    fun saveLesson() {
        val state = _uiState.value
        val parsedDate = parseLessonDate(state.date)
        val startMinutes = parseTimeToMinutes(state.startTime)
        val endMinutes = parseTimeToMinutes(state.endTime)
        val validatedState = validateState(
            state = state,
            parsedDate = parsedDate,
            startMinutes = startMinutes,
            endMinutes = endMinutes
        )

        if (validatedState != null) {
            _uiState.value = validatedState
            return
        }
        val lessonDate = requireNotNull(parsedDate)

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            }

            try {
                val course = selectedCourse ?: courseRepository.getCourseById(state.courseId).first()
                if (course == null) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Corso non trovato"
                        )
                    }
                    return@launch
                }
                selectedCourse = course

                val lesson = LessonEntity(
                    id = state.lessonId ?: 0,
                    courseId = state.courseId,
                    dateMillis = localDateToStartOfDayMillis(lessonDate),
                    dayOfWeek = lessonDate.dayOfWeek.value,
                    startTimeMinutes = requireNotNull(startMinutes),
                    endTimeMinutes = requireNotNull(endMinutes),
                    classroom = state.classroom,
                    building = state.building,
                    locationQuery = state.locationQuery,
                    notes = state.notes,
                    reminderEnabled = state.reminderEnabled,
                    createdAt = selectedLesson?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val savedLessonId = if (state.lessonId == null) {
                    lessonRepository.insertLesson(lesson).toInt()
                } else {
                    val existingLesson = selectedLesson
                        ?: lessonRepository.getLessonById(state.lessonId).first()

                    if (existingLesson == null) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Lezione non trovata"
                            )
                        }
                        return@launch
                    }

                    lessonRepository.updateLesson(
                        lesson.copy(
                            id = existingLesson.id,
                            createdAt = existingLesson.createdAt
                        )
                    )
                    lessonReminderScheduler.cancelLessonReminder(existingLesson.id)
                    existingLesson.id
                }

                if (state.reminderEnabled) {
                    lessonReminderScheduler.scheduleLessonReminder(
                        lessonId = savedLessonId,
                        courseId = state.courseId,
                        courseName = course.name,
                        dateMillis = localDateToStartOfDayMillis(lessonDate),
                        dayOfWeek = lessonDate.dayOfWeek.value,
                        startTimeMinutes = requireNotNull(startMinutes),
                        classroom = state.classroom.trim().takeIf { it.isNotEmpty() }
                    )
                } else {
                    lessonReminderScheduler.cancelLessonReminder(savedLessonId)
                }

                _uiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Salvataggio lezione non riuscito"
                    )
                }
            }
        }
    }

    private fun validateState(
        state: LessonUiState,
        parsedDate: java.time.LocalDate?,
        startMinutes: Int?,
        endMinutes: Int?
    ): LessonUiState? {
        val dateError = if (parsedDate == null) {
            "Inserisci una data valida nel formato gg/mm/aaaa"
        } else if (parsedDate.dayOfWeek.value !in 1..7) {
            "Data non valida"
        } else {
            null
        }
        val startError = if (startMinutes == null) {
            "Inserisci un orario di inizio valido"
        } else {
            null
        }
        val endError = when {
            endMinutes == null -> "Inserisci un orario di fine valido"
            startMinutes != null && endMinutes <= startMinutes ->
                "L'orario di fine deve essere successivo all'orario di inizio"
            else -> null
        }

        return if (dateError == null && startError == null && endError == null) {
            null
        } else {
            state.copy(
                dateError = dateError,
                dayOfWeekError = null,
                startTimeError = startError,
                endTimeError = endError,
                errorMessage = "Controlla i campi evidenziati",
                saveSuccess = false
            )
        }
    }
}
