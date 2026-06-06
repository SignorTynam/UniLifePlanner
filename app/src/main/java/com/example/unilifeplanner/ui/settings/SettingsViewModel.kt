package com.example.unilifeplanner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.SettingsDataStore
import com.example.unilifeplanner.data.local.AppDatabase
import com.example.unilifeplanner.data.repository.PlannerDataRepository
import com.example.unilifeplanner.data.repository.SettingsRepository
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.notifications.ExamReminderScheduler
import com.example.unilifeplanner.notifications.LessonReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM,
    val isClearingPlannerData: Boolean = false,
    val clearPlannerDataMessage: String? = null,
    val clearPlannerDataError: String? = null
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SettingsRepository(
        settingsDataStore = SettingsDataStore(application.applicationContext)
    )
    private val plannerDataRepository = PlannerDataRepository(
        database = AppDatabase.getDatabase(application),
        lessonReminderScheduler = LessonReminderScheduler(application.applicationContext),
        examReminderScheduler = ExamReminderScheduler(application.applicationContext)
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.themeMode.collect { themeMode ->
                _uiState.update {
                    it.copy(selectedThemeMode = themeMode)
                }
            }
        }
    }

    fun onThemeModeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun clearPlannerData() {
        if (_uiState.value.isClearingPlannerData) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isClearingPlannerData = true,
                    clearPlannerDataMessage = null,
                    clearPlannerDataError = null
                )
            }

            runCatching {
                plannerDataRepository.clearPlannerData()
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isClearingPlannerData = false,
                        clearPlannerDataMessage = "Dati del planner cancellati.",
                        clearPlannerDataError = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isClearingPlannerData = false,
                        clearPlannerDataMessage = null,
                        clearPlannerDataError = throwable.message ?: "Cancellazione non riuscita."
                    )
                }
            }
        }
    }

    fun clearPlannerDataFeedback() {
        _uiState.update {
            it.copy(
                clearPlannerDataMessage = null,
                clearPlannerDataError = null
            )
        }
    }
}
