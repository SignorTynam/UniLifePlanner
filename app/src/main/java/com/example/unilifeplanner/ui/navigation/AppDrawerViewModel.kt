package com.example.unilifeplanner.ui.navigation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.datastore.UserProfileDataStore
import com.example.unilifeplanner.data.repository.UserProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppDrawerUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val profileImageUri: String? = null
)

class AppDrawerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = UserProfileRepository(
        userProfileDataStore = UserProfileDataStore(application.applicationContext)
    )

    private val _uiState = MutableStateFlow(AppDrawerUiState())
    val uiState: StateFlow<AppDrawerUiState> = _uiState.asStateFlow()
    private var profileJob: Job? = null

    fun loadProfile() {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            repository.getProfile()
                .catch {
                    _uiState.update { state -> state.copy(email = repository.getCurrentUserEmail()) }
                }
                .collect { profile ->
                    _uiState.update {
                        it.copy(
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email,
                            profileImageUri = profile.profileImageUri
                        )
                    }
                }
        }
    }

    fun clearProfile() {
        profileJob?.cancel()
        _uiState.value = AppDrawerUiState()
    }
}
