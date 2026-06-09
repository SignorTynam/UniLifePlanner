package com.example.unilifeplanner.ui.university

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.datastore.UniboImportDataStore
import com.example.unilifeplanner.data.repository.CourseRepository
import com.example.unilifeplanner.data.repository.ExamAppealRepository
import com.example.unilifeplanner.data.repository.LessonRepository
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import com.example.unilifeplanner.university.publicimport.PublicCurriculum
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
    private val uniboImportDataStore = UniboImportDataStore(application.applicationContext)
    private val repository = UniboPublicImportRepository(
        importer = UniboPublicImporter(
            courseRepository = CourseRepository(database.courseDao()),
            lessonRepository = LessonRepository(database.lessonDao()),
            lessonReminderScheduler = LessonReminderScheduler(application.applicationContext),
            examAppealRepository = ExamAppealRepository(database.examAppealDao()),
            examReminderScheduler = ExamReminderScheduler(application.applicationContext)
        )
    )

    private val _uiState = MutableStateFlow(PublicUniboImportUiState())
    val uiState: StateFlow<PublicUniboImportUiState> = _uiState.asStateFlow()

    fun updateAcademicYear(value: String) {
        _uiState.update { it.resetSelection().copy(selectedAcademicYear = value) }
    }

    fun updateCampus(value: String) {
        _uiState.update { it.resetSelection().copy(selectedCampus = value) }
    }

    fun updateDegreeType(value: String) {
        _uiState.update { it.resetSelection().copy(selectedDegreeType = value) }
    }

    fun loadDegreePrograms() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = PublicImportStatus.LoadingDegreePrograms,
                    results = emptyList(),
                    selectedDegreeProgram = null,
                    curricula = emptyList(),
                    selectedCurriculum = null,
                    preview = null,
                    importResult = null,
                    errorMessage = null
                )
            }

            try {
                val results = repository.loadDegreePrograms(
                    academicYear = state.selectedAcademicYear,
                    campus = state.selectedCampus,
                    degreeType = state.selectedDegreeType
                )
                _uiState.update {
                    it.copy(
                        status = PublicImportStatus.DegreeProgramsLoaded,
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
                    status = PublicImportStatus.LoadingCurricula,
                    selectedDegreeProgram = degreeProgram,
                    curricula = emptyList(),
                    selectedCurriculum = null,
                    preview = null,
                    importResult = null,
                    errorMessage = null
                )
            }

            try {
                val curricula = repository.loadCurricula(degreeProgram)
                if (curricula.size > 1) {
                    _uiState.update {
                        it.copy(
                            status = PublicImportStatus.CurriculumSelection,
                            curricula = curricula,
                            errorMessage = null
                        )
                    }
                } else {
                    val curriculum = curricula.singleOrNull()
                    loadPreviewFor(
                        degreeProgram = degreeProgram,
                        curriculum = curriculum,
                        knownCurricula = curricula
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

    fun selectCurriculum(curriculum: PublicCurriculum) {
        val degreeProgram = _uiState.value.selectedDegreeProgram ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    status = PublicImportStatus.LoadingPreview,
                    selectedCurriculum = curriculum,
                    preview = null,
                    importResult = null,
                    errorMessage = null
                )
            }

            try {
                loadPreviewFor(
                    degreeProgram = degreeProgram,
                    curriculum = curriculum,
                    knownCurricula = _uiState.value.curricula
                )
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
                uniboImportDataStore.saveSuccessfulImport(preview)
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

    private suspend fun loadPreviewFor(
        degreeProgram: PublicDegreeProgram,
        curriculum: PublicCurriculum?,
        knownCurricula: List<PublicCurriculum>
    ) {
        _uiState.update {
            it.copy(
                status = PublicImportStatus.LoadingPreview,
                selectedDegreeProgram = degreeProgram,
                curricula = knownCurricula,
                selectedCurriculum = curriculum,
                preview = null,
                importResult = null,
                errorMessage = null
            )
        }
        val preview = repository.loadPreview(
            degreeProgram = degreeProgram,
            curriculum = curriculum
        )
        _uiState.update {
            it.copy(
                status = PublicImportStatus.Preview,
                preview = preview,
                errorMessage = null
            )
        }
    }

    private fun PublicUniboImportUiState.resetSelection(): PublicUniboImportUiState {
        return copy(
            status = PublicImportStatus.Idle,
            results = emptyList(),
            selectedDegreeProgram = null,
            curricula = emptyList(),
            selectedCurriculum = null,
            preview = null,
            importResult = null,
            errorMessage = null
        )
    }
}
