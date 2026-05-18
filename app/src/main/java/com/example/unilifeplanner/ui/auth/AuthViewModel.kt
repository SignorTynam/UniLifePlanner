package com.example.unilifeplanner.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        AuthUiState(isAuthenticated = authRepository.isUserLoggedIn())
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = null) }
    }

    fun login() {
        val state = _uiState.value
        val validationError = validateLogin(state.email, state.password)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.login(
                email = state.email.trim(),
                password = state.password
            )
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isAuthenticated = true
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Login non riuscito.",
                        isAuthenticated = false
                    )
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val validationError = validateRegister(
            email = state.email,
            password = state.password,
            confirmPassword = state.confirmPassword
        )
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.register(
                email = state.email.trim(),
                password = state.password
            )
            _uiState.update {
                if (result.isSuccess) {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        isAuthenticated = true
                    )
                } else {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message
                            ?: "Registrazione non riuscita.",
                        isAuthenticated = false
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState(isAuthenticated = false)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun validateLogin(
        email: String,
        password: String
    ): String? {
        return when {
            email.isBlank() -> "Inserisci la tua email."
            password.isBlank() -> "Inserisci la password."
            else -> null
        }
    }

    private fun validateRegister(
        email: String,
        password: String,
        confirmPassword: String
    ): String? {
        return when {
            email.isBlank() -> "Inserisci la tua email."
            password.isBlank() -> "Inserisci la password."
            confirmPassword.isBlank() -> "Conferma la password."
            password.length < 6 -> "La password deve contenere almeno 6 caratteri."
            password != confirmPassword -> "Le password non coincidono."
            else -> null
        }
    }
}
