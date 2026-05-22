package com.example.unilifeplanner.ui.profile

data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val university: String = "",
    val degreeCourse: String = "",
    val academicYear: String = "",
    val profileImageUri: String? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val logoutSuccess: Boolean = false
)
