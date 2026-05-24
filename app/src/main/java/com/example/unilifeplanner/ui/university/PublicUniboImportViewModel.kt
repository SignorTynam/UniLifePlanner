package com.example.unilifeplanner.ui.university

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportStatus
import com.example.unilifeplanner.university.publicimport.unibo.UniboPublicImportException
import com.example.unilifeplanner.university.publicimport.unibo.UniboPublicImportRepository
import com.example.unilifeplanner.university.publicimport.unibo.UniboPublicImporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PublicUniboImportViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = UniboPublicImportRepository(
        importer = UniboPublicImporter(
            courseRepository = CourseRepository(database.courseDao()),
            lessonRepository = LessonRepository(database.lessonDao()),
            lessonReminderScheduler = LessonReminderScheduler(application.applicationContext)
        )
    )

    private val _uiState = MutableStateFlow(PublicUniboImportUiState())
    val uiState: StateFlow<PublicUniboImportUiState> = _uiState.asStateFlow()

    fun updateAcademicYear(value: String) {
        _uiState.update { it.copy(selectedAcademicYear = value, errorMessage = null) }
    }

    fun updateCampus(value: String) {
        _uiState.update { it.copy(selectedCampus = value, errorMessage = null) }
    }

    fun updateDegreeType(value: String) {
        _uiState.update { it.copy(selectedDegreeType = value, errorMessage = null) }
    }

    fun updateQuery(value: String) {
        _uiState.update {
            it.copy(
                query = value,
                queryError = null,
                errorMessage = null
            )
        }
    }

    fun searchDegreePrograms() {
        val state = _uiState.value
        val query = state.query.trim()
        if (query.length < 3) {
            _uiState.update {
                it.copy(
                    queryError = "Inserisci almeno 3 caratteri",
                    errorMessage = null,
                    status = PublicImportStatus.Idle
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = PublicImportStatus.Loading,
                    results = emptyList(),
                    selectedDegreeProgram = null,
                    preview = null,
                    importResult = null,
                    errorMessage = null
                )
            }

            try {
                val results = repository.searchDegreePrograms(
                    query = query,
                    academicYear = state.selectedAcademicYear,
                    campus = state.selectedCampus,
                    degreeType = state.selectedDegreeType
                )
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Results,
                        results = results,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Error,
                        errorMessage = exception.toUserMessage()
                    )
                }
            }
        }
    }

    fun selectDegreeProgram(degreeProgram: PublicDegreeProgram) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = PublicImportStatus.Loading,
                    selectedDegreeProgram = degreeProgram,
                    preview = null,
                    importResult = null,
                    errorMessage = null
                )
            }

            try {
                val preview = repository.loadPreview(degreeProgram)
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Preview,
                        preview = preview,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Error,
                        errorMessage = exception.toUserMessage()
                    )
                }
            }
        }
    }

    fun importPreview() {
        val preview = _uiState.value.preview ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = PublicImportStatus.Importing,
                    errorMessage = null
                )
            }

            try {
                val result = repository.importPreview(preview)
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Imported,
                        importResult = result,
                        errorMessage = null
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.Error,
                        errorMessage = exception.toUserMessage()
                    )
                }
            }
        }
    }

    fun resetForAnotherImport() {
        _uiState.update {
            PublicUniboImportUiState(
                selectedAcademicYear = it.selectedAcademicYear,
                selectedCampus = it.selectedCampus,
                selectedDegreeType = it.selectedDegreeType
            )
        }
    }

    private fun Exception.toUserMessage(): String {
        return when (this) {
            is UniboPublicImportException -> message ?: "Import UniBo non riuscito"
            else -> message ?: "Import UniBo non riuscito"
        }
    }
}
