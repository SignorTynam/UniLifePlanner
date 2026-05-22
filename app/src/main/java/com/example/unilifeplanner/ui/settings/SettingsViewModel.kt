package com.example.unilifeplanner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.SettingsDataStore
import com.example.unilifeplanner.data.repository.SettingsRepository
import com.example.unilifeplanner.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedThemeMode: ThemeMode = ThemeMode.SYSTEM
)

class SettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = SettingsRepository(
        settingsDataStore = SettingsDataStore(application.applicationContext)
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
}
