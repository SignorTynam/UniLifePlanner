package com.example.unilifeplanner.ui.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.data.local.ProfileImageStorage
import com.example.unilifeplanner.data.repository.UserProfileRepository
import com.example.unilifeplanner.domain.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = UserProfileRepository(
        userProfileDataStore = UserProfileDataStore(application.applicationContext)
    )
    private val profileImageStorage = ProfileImageStorage(application.applicationContext)

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun updateFirstName(value: String) {
        _uiState.update { it.copy(firstName = value) }
    }

    fun updateLastName(value: String) {
        _uiState.update { it.copy(lastName = value) }
    }

    fun updateUniversity(value: String) {
        _uiState.update { it.copy(university = value) }
    }

    fun updateDegreeCourse(value: String) {
        _uiState.update { it.copy(degreeCourse = value) }
    }

    fun updateAcademicYear(value: String) {
        _uiState.update { it.copy(academicYear = value) }
    }

    fun onProfileImageSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            val savedImageUri = profileImageStorage.saveProfileImage(
                sourceUri = uri,
                userEmail = repository.getCurrentUserEmail()
            )

            if (savedImageUri == null) {
                _uiState.update {
                    it.copy(errorMessage = "Errore durante il salvataggio della foto profilo")
                }
            } else {
                _uiState.update {
                    it.copy(
                        profileImageUri = savedImageUri,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun saveProfile() {
        val state = _uiState.value

        if (
            state.firstName.isBlank() ||
            state.lastName.isBlank() ||
            state.university.isBlank() ||
            state.degreeCourse.isBlank() ||
            state.academicYear.isBlank()
        ) {
            _uiState.update {
                it.copy(errorMessage = "Compila tutti i campi obbligatori")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                repository.saveProfile(
                    UserProfile(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        email = state.email,
                        university = state.university.trim(),
                        degreeCourse = state.degreeCourse.trim(),
                        academicYear = state.academicYear.trim(),
                        profileImageUri = state.profileImageUri
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successMessage = "Profilo salvato correttamente"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Errore durante il salvataggio del profilo"
                    )
                }
            }
        }
    }

    fun logout() {
        repository.logout()
        _uiState.update {
            it.copy(logoutSuccess = true)
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            repository.getProfile()
                .catch {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = "Errore durante il caricamento del profilo"
                        )
                    }
                }
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email,
                            university = profile.university,
                            degreeCourse = profile.degreeCourse,
                            academicYear = profile.academicYear,
                            profileImageUri = profile.profileImageUri,
                            isLoading = false
                        )
                    }
                }
        }
    }
}
