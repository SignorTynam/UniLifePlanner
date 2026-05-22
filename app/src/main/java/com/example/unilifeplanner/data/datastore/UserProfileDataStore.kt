package com.example.unilifeplanner.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.unilifeplanner.domain.model.UserProfile
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_profile_preferences"
)

class UserProfileDataStore(
    private val context: Context
) {
    fun getUserProfile(email: String): Flow<UserProfile> {
        val keys = keysFor(email)

        return context.userProfileDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                UserProfile(
                    firstName = preferences[keys.firstName].orEmpty(),
                    lastName = preferences[keys.lastName].orEmpty(),
                    email = email,
                    university = preferences[keys.university].orEmpty(),
                    degreeCourse = preferences[keys.degreeCourse].orEmpty(),
                    academicYear = preferences[keys.academicYear].orEmpty(),
                    profileImageUri = preferences[keys.profileImageUri]
                )
            }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val keys = keysFor(profile.email)

        context.userProfileDataStore.edit { preferences ->
            preferences[keys.firstName] = profile.firstName
            preferences[keys.lastName] = profile.lastName
            preferences[keys.university] = profile.university
            preferences[keys.degreeCourse] = profile.degreeCourse
            preferences[keys.academicYear] = profile.academicYear

            if (profile.profileImageUri.isNullOrBlank()) {
                preferences.remove(keys.profileImageUri)
            } else {
                preferences[keys.profileImageUri] = profile.profileImageUri
            }
        }
    }

    suspend fun clearUserProfile(email: String) {
        val keys = keysFor(email)

        context.userProfileDataStore.edit { preferences ->
            preferences.remove(keys.firstName)
            preferences.remove(keys.lastName)
            preferences.remove(keys.university)
            preferences.remove(keys.degreeCourse)
            preferences.remove(keys.academicYear)
            preferences.remove(keys.profileImageUri)
        }
    }

    private fun keysFor(email: String): UserProfileKeys {
        val suffix = email.lowercase().trim().ifBlank { "anonymous" }.hashCode().toString()

        return UserProfileKeys(
            firstName = stringPreferencesKey("first_name_$suffix"),
            lastName = stringPreferencesKey("last_name_$suffix"),
            university = stringPreferencesKey("university_$suffix"),
            degreeCourse = stringPreferencesKey("degree_course_$suffix"),
            academicYear = stringPreferencesKey("academic_year_$suffix"),
            profileImageUri = stringPreferencesKey("profile_image_uri_$suffix")
        )
    }
}

private data class UserProfileKeys(
    val firstName: Preferences.Key<String>,
    val lastName: Preferences.Key<String>,
    val university: Preferences.Key<String>,
    val degreeCourse: Preferences.Key<String>,
    val academicYear: Preferences.Key<String>,
    val profileImageUri: Preferences.Key<String>
)
