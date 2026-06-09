package com.example.unilifeplanner.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.profile.components.ProfileAccountSection
import com.example.unilifeplanner.ui.profile.components.ProfileActionsSection
import com.example.unilifeplanner.ui.profile.components.ProfileDangerZone
import com.example.unilifeplanner.ui.profile.components.ProfileHeroHeader
import com.example.unilifeplanner.ui.profile.components.ProfileLoadingState
import com.example.unilifeplanner.ui.profile.components.ProfileStudentFormSection
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onLogoutSuccess: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            viewModel.onProfileImageSelected(uri)
        }
    )

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }

        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.logoutSuccess) {
        if (uiState.logoutSuccess) {
            onLogoutSuccess()
        }
    }

    ProfileScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onMenuClick = onMenuClick,
        onChooseImageClick = {
            photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onFirstNameChange = viewModel::updateFirstName,
        onLastNameChange = viewModel::updateLastName,
        onUniversityChange = viewModel::updateUniversity,
        onDegreeCourseChange = viewModel::updateDegreeCourse,
        onAcademicYearChange = viewModel::updateAcademicYear,
        onSaveClick = viewModel::saveProfile,
        onLogoutClick = { showLogoutDialog = true }
    )

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "Uscire dall'account?") },
            text = { Text(text = "Verrai riportato alla schermata di accesso.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text(text = "Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Composable
private fun ProfileScreenContent(
    uiState: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onMenuClick: () -> Unit,
    onChooseImageClick: () -> Unit,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUniversityChange: (String) -> Unit,
    onDegreeCourseChange: (String) -> Unit,
    onAcademicYearChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Profilo",
                onMenuClick = onMenuClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                ProfileLoadingState()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ProfileHeroHeader(
                        firstName = uiState.firstName,
                        lastName = uiState.lastName,
                        email = uiState.email,
                        university = uiState.university,
                        degreeCourse = uiState.degreeCourse,
                        profileImageUri = uiState.profileImageUri,
                        onChooseImageClick = onChooseImageClick
                    )
                }
                item {
                    ProfileStudentFormSection(
                        firstName = uiState.firstName,
                        lastName = uiState.lastName,
                        email = uiState.email,
                        university = uiState.university,
                        degreeCourse = uiState.degreeCourse,
                        academicYear = uiState.academicYear,
                        isSaving = uiState.isSaving,
                        onFirstNameChange = onFirstNameChange,
                        onLastNameChange = onLastNameChange,
                        onUniversityChange = onUniversityChange,
                        onDegreeCourseChange = onDegreeCourseChange,
                        onAcademicYearChange = onAcademicYearChange
                    )
                }
                item {
                    ProfileAccountSection(email = uiState.email)
                }
                item {
                    ProfileActionsSection(
                        isSaving = uiState.isSaving,
                        onSaveClick = onSaveClick
                    )
                }
                item {
                    ProfileDangerZone(
                        isSaving = uiState.isSaving,
                        onLogoutClick = onLogoutClick
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    UniLifePlannerTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(
                firstName = "Mario",
                lastName = "Rossi",
                email = "mario.rossi@example.com",
                university = "Università di Bologna",
                degreeCourse = "Ingegneria e scienze informatiche",
                academicYear = "2025/2026"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onMenuClick = {},
            onChooseImageClick = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onUniversityChange = {},
            onDegreeCourseChange = {},
            onAcademicYearChange = {},
            onSaveClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenLoadingPreview() {
    UniLifePlannerTheme {
        ProfileScreenContent(
            uiState = ProfileUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            onMenuClick = {},
            onChooseImageClick = {},
            onFirstNameChange = {},
            onLastNameChange = {},
            onUniversityChange = {},
            onDegreeCourseChange = {},
            onAcademicYearChange = {},
            onSaveClick = {},
            onLogoutClick = {}
        )
    }
}
