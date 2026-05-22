package com.example.unilifeplanner.data.repository

import com.example.unilifeplanner.data.datastore.SettingsDataStore
import com.example.unilifeplanner.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsRepository(
    private val settingsDataStore: SettingsDataStore
) {
    val themeMode: Flow<ThemeMode> = settingsDataStore.themeModeFlow

    suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsDataStore.saveThemeMode(themeMode)
    }
}
