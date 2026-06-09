package com.example.unilifeplanner.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.uniboImportDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "unibo_import_preferences"
)

data class SavedUniboImportSelection(
    val degreeProgramExternalId: String,
    val degreeProgramName: String,
    val degreeProgramCampus: String?,
    val degreeProgramType: String?,
    val academicYear: String,
    val degreeProgramOfficialUrl: String,
    val degreeProgramDurationYears: Int?,
    val selectedStudyYear: Int?,
    val curriculumExternalId: String?,
    val curriculumName: String?,
    val curriculumOfficialUrl: String?,
    val lastSuccessfulImportAtMillis: Long?,
    val lastManualRefreshAtMillis: Long?,
    val lastAutoRefreshAtMillis: Long?
)

class UniboImportDataStore(
    private val context: Context
) {
    val savedSelectionFlow: Flow<SavedUniboImportSelection?> = context.uniboImportDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val externalId = preferences[DEGREE_PROGRAM_EXTERNAL_ID]?.takeIf { it.isNotBlank() }
                ?: return@map null
            val name = preferences[DEGREE_PROGRAM_NAME]?.takeIf { it.isNotBlank() }
                ?: return@map null
            val academicYear = preferences[ACADEMIC_YEAR]?.takeIf { it.isNotBlank() }
                ?: return@map null
            val officialUrl = preferences[DEGREE_PROGRAM_OFFICIAL_URL]?.takeIf { it.isNotBlank() }
                ?: return@map null

            SavedUniboImportSelection(
                degreeProgramExternalId = externalId,
                degreeProgramName = name,
                degreeProgramCampus = preferences[DEGREE_PROGRAM_CAMPUS]?.takeIf { it.isNotBlank() },
                degreeProgramType = preferences[DEGREE_PROGRAM_TYPE]?.takeIf { it.isNotBlank() },
                academicYear = academicYear,
                degreeProgramOfficialUrl = officialUrl,
                degreeProgramDurationYears = preferences[DEGREE_PROGRAM_DURATION_YEARS],
                selectedStudyYear = preferences[SELECTED_STUDY_YEAR],
                curriculumExternalId = preferences[CURRICULUM_EXTERNAL_ID]?.takeIf { it.isNotBlank() },
                curriculumName = preferences[CURRICULUM_NAME]?.takeIf { it.isNotBlank() },
                curriculumOfficialUrl = preferences[CURRICULUM_OFFICIAL_URL]?.takeIf { it.isNotBlank() },
                lastSuccessfulImportAtMillis = preferences[LAST_SUCCESSFUL_IMPORT_AT],
                lastManualRefreshAtMillis = preferences[LAST_MANUAL_REFRESH_AT],
                lastAutoRefreshAtMillis = preferences[LAST_AUTO_REFRESH_AT]
            )
        }

    suspend fun saveSuccessfulImport(preview: PublicImportPreview) {
        val degreeProgram = preview.degreeProgram
        if (degreeProgram.externalId.isBlank() ||
            degreeProgram.name.isBlank() ||
            degreeProgram.academicYear.isBlank() ||
            degreeProgram.officialUrl.isBlank()
        ) {
            return
        }

        val now = System.currentTimeMillis()
        context.uniboImportDataStore.edit { preferences ->
            preferences[DEGREE_PROGRAM_EXTERNAL_ID] = degreeProgram.externalId
            preferences[DEGREE_PROGRAM_NAME] = degreeProgram.name
            putOptional(preferences, DEGREE_PROGRAM_CAMPUS, degreeProgram.campus)
            putOptional(preferences, DEGREE_PROGRAM_TYPE, degreeProgram.degreeType)
            preferences[ACADEMIC_YEAR] = degreeProgram.academicYear
            preferences[DEGREE_PROGRAM_OFFICIAL_URL] = degreeProgram.officialUrl
            putOptional(preferences, DEGREE_PROGRAM_DURATION_YEARS, degreeProgram.durationYears)
            putOptional(preferences, SELECTED_STUDY_YEAR, preview.selectedStudyYear)
            putOptional(preferences, CURRICULUM_EXTERNAL_ID, preview.curriculum?.externalId)
            putOptional(preferences, CURRICULUM_NAME, preview.curriculum?.name)
            putOptional(preferences, CURRICULUM_OFFICIAL_URL, preview.curriculum?.officialUrl)
            preferences[LAST_SUCCESSFUL_IMPORT_AT] = now
        }
    }

    suspend fun updateManualRefreshTimestamp() {
        context.uniboImportDataStore.edit { preferences ->
            preferences[LAST_MANUAL_REFRESH_AT] = System.currentTimeMillis()
        }
    }

    suspend fun updateAutoRefreshTimestamp() {
        context.uniboImportDataStore.edit { preferences ->
            preferences[LAST_AUTO_REFRESH_AT] = System.currentTimeMillis()
        }
    }

    suspend fun clearSelection() {
        context.uniboImportDataStore.edit { it.clear() }
    }

    private fun putOptional(
        preferences: MutablePreferences,
        key: Preferences.Key<String>,
        value: String?
    ) {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() }
        if (normalized == null) {
            preferences.remove(key)
        } else {
            preferences[key] = normalized
        }
    }

    private fun putOptional(
        preferences: MutablePreferences,
        key: Preferences.Key<Int>,
        value: Int?
    ) {
        if (value == null) {
            preferences.remove(key)
        } else {
            preferences[key] = value
        }
    }

    private companion object {
        val DEGREE_PROGRAM_EXTERNAL_ID = stringPreferencesKey("degree_program_external_id")
        val DEGREE_PROGRAM_NAME = stringPreferencesKey("degree_program_name")
        val DEGREE_PROGRAM_CAMPUS = stringPreferencesKey("degree_program_campus")
        val DEGREE_PROGRAM_TYPE = stringPreferencesKey("degree_program_type")
        val ACADEMIC_YEAR = stringPreferencesKey("academic_year")
        val DEGREE_PROGRAM_OFFICIAL_URL = stringPreferencesKey("degree_program_official_url")
        val DEGREE_PROGRAM_DURATION_YEARS = intPreferencesKey("degree_program_duration_years")
        val SELECTED_STUDY_YEAR = intPreferencesKey("selected_study_year")
        val CURRICULUM_EXTERNAL_ID = stringPreferencesKey("curriculum_external_id")
        val CURRICULUM_NAME = stringPreferencesKey("curriculum_name")
        val CURRICULUM_OFFICIAL_URL = stringPreferencesKey("curriculum_official_url")
        val LAST_SUCCESSFUL_IMPORT_AT = longPreferencesKey("last_successful_import_at")
        val LAST_MANUAL_REFRESH_AT = longPreferencesKey("last_manual_refresh_at")
        val LAST_AUTO_REFRESH_AT = longPreferencesKey("last_auto_refresh_at")
    }
}
