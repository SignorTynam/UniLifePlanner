package com.example.unilifeplanner.ui.courses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.domain.model.CourseStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CourseRepository(
        AppDatabase.getDatabase(application).courseDao()
    )

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?> = _selectedCourse.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow<CourseStatus?>(null)
    val selectedStatusFilter: StateFlow<CourseStatus?> = _selectedStatusFilter.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _addEditUiState = MutableStateFlow(AddEditCourseUiState())
    val addEditUiState: StateFlow<AddEditCourseUiState> = _addEditUiState.asStateFlow()

    val uiState: StateFlow<CourseUiState> = combine(
        _courses,
        _isLoading,
        _errorMessage
    ) { courses, isLoading, errorMessage ->
        CourseUiState(
            courses = courses,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CourseUiState(isLoading = true)
    )

    private var coursesJob: Job? = null
    private var selectedCourseJob: Job? = null

    init {
        loadAllCourses()
    }

    fun loadAllCourses() {
        observeCourses()
    }

    fun loadCourseById(courseId: Int) {
        selectedCourseJob?.cancel()
        selectedCourseJob = viewModelScope.launch {
            repository.getCourseById(courseId)
                .catch { throwable ->
                    _errorMessage.value = throwable.message ?: "Errore nel caricamento del corso"
                }
                .collect { course ->
                    _selectedCourse.value = course
                }
        }
    }

    fun loadCourse(courseId: Int) {
        viewModelScope.launch {
            _addEditUiState.update {
                it.copy(
                    courseId = courseId,
                    isLoading = true,
                    errorMessage = null,
                    saveSuccess = false
                )
            }

            try {
                val course = repository.getCourseById(courseId).first()
                if (course == null) {
                    _addEditUiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Corso non trovato"
                        )
                    }
                    return@launch
                }

                _selectedCourse.value = course
                _addEditUiState.value = AddEditCourseUiState(
                    courseId = course.id,
                    name = course.name,
                    professor = course.professor,
                    examDate = course.examDate,
                    classroom = course.classroom.orEmpty(),
                    credits = course.credits.toString(),
                    status = CourseStatus.entries.firstOrNull { it.name == course.status }
                        ?: CourseStatus.TO_STUDY,
                    notes = course.notes.orEmpty(),
                    isLoading = false
                )
            } catch (exception: Exception) {
                _addEditUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Errore nel caricamento del corso"
                    )
                }
            }
        }
    }

    fun resetAddEditCourseState() {
        _selectedCourse.value = null
        _addEditUiState.value = AddEditCourseUiState()
    }

    fun updateName(value: String) {
        _addEditUiState.update {
            it.copy(name = value, nameError = null, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateProfessor(value: String) {
        _addEditUiState.update {
            it.copy(professor = value, professorError = null, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateExamDate(value: Long?) {
        _addEditUiState.update {
            it.copy(examDate = value, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateClassroom(value: String) {
        _addEditUiState.update {
            it.copy(classroom = value, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateCredits(value: String) {
        _addEditUiState.update {
            it.copy(credits = value, creditsError = null, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateStatus(value: CourseStatus) {
        _addEditUiState.update {
            it.copy(status = value, errorMessage = null, saveSuccess = false)
        }
    }

    fun updateNotes(value: String) {
        _addEditUiState.update {
            it.copy(notes = value, errorMessage = null, saveSuccess = false)
        }
    }

    fun saveCourse() {
        val state = _addEditUiState.value
        val creditsValue = state.credits.toIntOrNull()
        val validatedState = validateAddEditState(state, creditsValue)
        if (validatedState != null) {
            _addEditUiState.value = validatedState
            return
        }

        viewModelScope.launch {
            _addEditUiState.update {
                it.copy(isSaving = true, errorMessage = null, saveSuccess = false)
            }

            try {
                val existingCourse = _selectedCourse.value
                if (state.courseId == null) {
                    repository.insertCourse(
                        name = state.name,
                        professor = state.professor,
                        examDate = state.examDate,
                        classroom = state.classroom,
                        credits = requireNotNull(creditsValue),
                        status = state.status,
                        notes = state.notes
                    )
                } else {
                    if (existingCourse == null) {
                        _addEditUiState.update {
                            it.copy(
                                isSaving = false,
                                errorMessage = "Corso non trovato"
                            )
                        }
                        return@launch
                    }

                    repository.updateCourse(
                        course = existingCourse,
                        name = state.name,
                        professor = state.professor,
                        examDate = state.examDate,
                        classroom = state.classroom,
                        credits = requireNotNull(creditsValue),
                        status = state.status,
                        notes = state.notes
                    )
                }

                _addEditUiState.update {
                    it.copy(isSaving = false, saveSuccess = true)
                }
            } catch (exception: Exception) {
                _addEditUiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = exception.message ?: "Salvataggio non riuscito"
                    )
                }
            }
        }
    }

    fun resetSaveState() {
        _addEditUiState.update { it.copy(saveSuccess = false) }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        observeCourses()
    }

    fun onStatusFilterChange(status: CourseStatus?) {
        _selectedStatusFilter.value = status
        observeCourses()
    }

    fun onFavoriteFilterChange(showOnlyFavorites: Boolean) {
        _showOnlyFavorites.value = showOnlyFavorites
        observeCourses()
    }

    fun insertCourse(
        name: String,
        professor: String,
        examDate: Long?,
        classroom: String?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = false,
        notes: String?
    ) {
        val validationError = validateCourseInput(name, professor, credits)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }

        viewModelScope.launch {
            runDatabaseOperation {
                repository.insertCourse(
                    name = name,
                    professor = professor,
                    examDate = examDate,
                    classroom = classroom,
                    credits = credits,
                    status = status,
                    isFavorite = isFavorite,
                    notes = notes
                )
            }
        }
    }

    fun updateCourse(
        course: CourseEntity,
        name: String,
        professor: String,
        examDate: Long?,
        classroom: String?,
        credits: Int,
        status: CourseStatus,
        isFavorite: Boolean = course.isFavorite,
        notes: String?
    ) {
        val validationError = validateCourseInput(name, professor, credits)
        if (validationError != null) {
            _errorMessage.value = validationError
            return
        }

        viewModelScope.launch {
            runDatabaseOperation {
                repository.updateCourse(
                    course = course,
                    name = name,
                    professor = professor,
                    examDate = examDate,
                    classroom = classroom,
                    credits = credits,
                    status = status,
                    isFavorite = isFavorite,
                    notes = notes
                )
            }
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            runDatabaseOperation {
                repository.deleteCourse(course)
            }
        }
    }

    fun deleteCourseById(courseId: Int) {
        viewModelScope.launch {
            runDatabaseOperation {
                repository.deleteCourseById(courseId)
            }
        }
    }

    fun toggleFavorite(course: CourseEntity) {
        viewModelScope.launch {
            runDatabaseOperation {
                repository.updateFavorite(
                    courseId = course.id,
                    isFavorite = !course.isFavorite
                )
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _addEditUiState.update { it.copy(errorMessage = null) }
    }

    private fun observeCourses() {
        coursesJob?.cancel()
        coursesJob = viewModelScope.launch {
            _isLoading.value = true
            activeCoursesFlow()
                .catch { throwable ->
                    _isLoading.value = false
                    _errorMessage.value = throwable.message ?: "Errore nel caricamento dei corsi"
                }
                .collect { courseList ->
                    _courses.value = applyInMemoryFilters(courseList)
                    _isLoading.value = false
                }
        }
    }

    private fun activeCoursesFlow() = when {
        _searchQuery.value.isNotBlank() -> repository.searchCourses(_searchQuery.value)
        _selectedStatusFilter.value != null -> {
            repository.getCoursesByStatus(requireNotNull(_selectedStatusFilter.value))
        }

        _showOnlyFavorites.value -> repository.getFavoriteCourses()
        else -> repository.allCourses
    }

    private fun applyInMemoryFilters(courses: List<CourseEntity>): List<CourseEntity> {
        val status = _selectedStatusFilter.value
        val query = _searchQuery.value.trim()
        val favoritesOnly = _showOnlyFavorites.value

        return courses
            .asSequence()
            .filter { course ->
                query.isBlank() ||
                    course.name.contains(query, ignoreCase = true) ||
                    course.professor.contains(query, ignoreCase = true)
            }
            .filter { course ->
                status == null || course.status == status.name
            }
            .filter { course ->
                !favoritesOnly || course.isFavorite
            }
            .toList()
    }

    private suspend fun runDatabaseOperation(operation: suspend () -> Unit) {
        try {
            _isLoading.value = true
            _errorMessage.value = null
            operation()
        } catch (exception: Exception) {
            _errorMessage.value = exception.message ?: "Operazione non riuscita"
        } finally {
            _isLoading.value = false
        }
    }

    private fun validateCourseInput(
        name: String,
        professor: String,
        credits: Int
    ): String? {
        return when {
            name.isBlank() -> "Il nome del corso e obbligatorio"
            professor.isBlank() -> "Il docente e obbligatorio"
            credits <= 0 -> "I CFU devono essere maggiori di 0"
            else -> null
        }
    }

    private fun validateAddEditState(
        state: AddEditCourseUiState,
        creditsValue: Int?
    ): AddEditCourseUiState? {
        val nameError = if (state.name.isBlank()) {
            "Il nome del corso e obbligatorio"
        } else {
            null
        }
        val professorError = if (state.professor.isBlank()) {
            "Il docente e obbligatorio"
        } else {
            null
        }
        val creditsError = when {
            state.credits.isBlank() -> "I CFU sono obbligatori"
            creditsValue == null || creditsValue <= 0 -> "I CFU devono essere maggiori di 0"
            else -> null
        }

        return if (nameError == null && professorError == null && creditsError == null) {
            null
        } else {
            state.copy(
                nameError = nameError,
                professorError = professorError,
                creditsError = creditsError,
                errorMessage = "Controlla i campi evidenziati",
                saveSuccess = false
            )
        }
    }
}
