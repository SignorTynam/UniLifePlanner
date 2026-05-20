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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CourseRepository(
        AppDatabase.getDatabase(application).courseDao()
    )

    private val _allCourses = MutableStateFlow<List<CourseEntity>>(emptyList())

    private val _courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val courses: StateFlow<List<CourseEntity>> = _courses.asStateFlow()

    private val _selectedCourse = MutableStateFlow<CourseEntity?>(null)
    val selectedCourse: StateFlow<CourseEntity?> = _selectedCourse.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow(CourseStatusFilter.ALL)
    val selectedStatusFilter: StateFlow<CourseStatusFilter> = _selectedStatusFilter.asStateFlow()

    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    private val _selectedSortOption = MutableStateFlow(CourseSortOption.DEFAULT)
    val selectedSortOption: StateFlow<CourseSortOption> = _selectedSortOption.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _addEditUiState = MutableStateFlow(AddEditCourseUiState())
    val addEditUiState: StateFlow<AddEditCourseUiState> = _addEditUiState.asStateFlow()

    private val _courseDetailUiState = MutableStateFlow(CourseDetailUiState())
    val courseDetailUiState: StateFlow<CourseDetailUiState> = _courseDetailUiState.asStateFlow()

    private val filterUiState = combine(
        _allCourses,
        _searchQuery,
        _selectedStatusFilter,
        _showOnlyFavorites,
        _selectedSortOption
    ) { allCourses, searchQuery, statusFilter, favoritesOnly, sortOption ->
        val filteredCourses = applyFilters(
            courses = allCourses,
            query = searchQuery,
            statusFilter = statusFilter,
            favoritesOnly = favoritesOnly,
            sortOption = sortOption
        )

        CourseUiState(
            searchQuery = searchQuery,
            selectedStatusFilter = statusFilter,
            showFavoritesOnly = favoritesOnly,
            selectedSortOption = sortOption,
            courses = allCourses,
            filteredCourses = filteredCourses
        )
    }

    val uiState: StateFlow<CourseUiState> = combine(
        filterUiState,
        _isLoading,
        _errorMessage
    ) { state, isLoading, errorMessage ->
        state.copy(
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
            _courseDetailUiState.value = CourseDetailUiState(isLoading = true)
            repository.getCourseById(courseId)
                .catch { throwable ->
                    val message = throwable.message ?: "Errore nel caricamento del corso"
                    _errorMessage.value = message
                    _courseDetailUiState.value = CourseDetailUiState(errorMessage = message)
                }
                .collect { course ->
                    _selectedCourse.value = course
                    _courseDetailUiState.value = if (course == null) {
                        CourseDetailUiState(errorMessage = "Corso non trovato.")
                    } else {
                        CourseDetailUiState(course = course)
                    }
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
            it.copy(
                professor = value,
                professorError = null,
                errorMessage = null,
                saveSuccess = false
            )
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
        updateVisibleCourses()
    }

    fun onStatusFilterChange(filter: CourseStatusFilter) {
        _selectedStatusFilter.value = filter
        updateVisibleCourses()
    }

    fun onFavoritesOnlyChange(enabled: Boolean) {
        _showOnlyFavorites.value = enabled
        updateVisibleCourses()
    }

    fun onSortOptionChange(option: CourseSortOption) {
        _selectedSortOption.value = option
        updateVisibleCourses()
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedStatusFilter.value = CourseStatusFilter.ALL
        _showOnlyFavorites.value = false
        _selectedSortOption.value = CourseSortOption.DEFAULT
        updateVisibleCourses()
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
            try {
                _courseDetailUiState.update { it.copy(isLoading = true, errorMessage = null) }
                repository.deleteCourse(course)
                _selectedCourse.value = null
                _courseDetailUiState.value = CourseDetailUiState(deleteSuccess = true)
            } catch (exception: Exception) {
                _courseDetailUiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Eliminazione non riuscita"
                    )
                }
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
        toggleFavorite(
            courseId = course.id,
            currentFavorite = course.isFavorite
        )
    }

    fun toggleFavorite(
        courseId: Int,
        currentFavorite: Boolean
    ) {
        viewModelScope.launch {
            try {
                repository.updateFavorite(
                    courseId = courseId,
                    isFavorite = !currentFavorite
                )
            } catch (exception: Exception) {
                val message = exception.message ?: "Aggiornamento preferito non riuscito"
                _errorMessage.value = message
                _courseDetailUiState.update { it.copy(errorMessage = message) }
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
        _addEditUiState.update { it.copy(errorMessage = null) }
        _courseDetailUiState.update { it.copy(errorMessage = null) }
    }

    fun resetDeleteState() {
        _courseDetailUiState.update { it.copy(deleteSuccess = false) }
    }

    private fun observeCourses() {
        coursesJob?.cancel()
        coursesJob = viewModelScope.launch {
            _isLoading.value = true
            repository.allCourses
                .catch { throwable ->
                    _isLoading.value = false
                    _errorMessage.value = throwable.message ?: "Errore nel caricamento dei corsi"
                }
                .collect { courseList ->
                    _allCourses.value = courseList
                    updateVisibleCourses()
                    _isLoading.value = false
                }
        }
    }

    private fun updateVisibleCourses() {
        _courses.value = applyFilters(
            courses = _allCourses.value,
            query = _searchQuery.value,
            statusFilter = _selectedStatusFilter.value,
            favoritesOnly = _showOnlyFavorites.value,
            sortOption = _selectedSortOption.value
        )
    }

    private fun applyFilters(
        courses: List<CourseEntity>,
        query: String,
        statusFilter: CourseStatusFilter,
        favoritesOnly: Boolean,
        sortOption: CourseSortOption
    ): List<CourseEntity> {
        val normalizedQuery = query.trim()

        val filteredCourses = courses
            .asSequence()
            .filter { course ->
                normalizedQuery.isBlank() ||
                    course.name.contains(normalizedQuery, ignoreCase = true) ||
                    course.professor.contains(normalizedQuery, ignoreCase = true) ||
                    course.classroom.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
            .filter { course ->
                statusFilter == CourseStatusFilter.ALL || course.status == statusFilter.name
            }
            .filter { course ->
                !favoritesOnly || course.isFavorite
            }
            .toList()

        return when (sortOption) {
            CourseSortOption.DEFAULT -> filteredCourses
            CourseSortOption.EXAM_DATE_ASC -> filteredCourses.sortedWith(
                compareBy<CourseEntity> { it.examDate == null }
                    .thenBy { it.examDate ?: Long.MAX_VALUE }
                    .thenBy { it.name.lowercase() }
            )

            CourseSortOption.NAME_ASC -> filteredCourses.sortedBy { it.name.lowercase() }
        }
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
