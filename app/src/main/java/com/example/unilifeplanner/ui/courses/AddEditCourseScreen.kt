package com.example.unilifeplanner.ui.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun AddEditCourseScreen(
    courseId: Int?,
    viewModel: CourseViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.addEditUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(courseId) {
        if (courseId == null) {
            viewModel.resetAddEditCourseState()
        } else {
            viewModel.loadCourse(courseId)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Corso salvato")
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    AddEditCourseContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::updateName,
        onProfessorChange = viewModel::updateProfessor,
        onCreditsChange = viewModel::updateCredits,
        onStatusChange = viewModel::updateStatus,
        onNotesChange = viewModel::updateNotes,
        onSaveClick = viewModel::saveCourse,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditCourseContent(
    uiState: AddEditCourseUiState,
    snackbarHostState: SnackbarHostState,
    onNameChange: (String) -> Unit,
    onProfessorChange: (String) -> Unit,
    onCreditsChange: (String) -> Unit,
    onStatusChange: (CourseStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val title = if (uiState.courseId == null) "Aggiungi corso" else "Modifica corso"

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = title,
                onBackClick = onBackClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Caricamento corso...")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(text = "Nome corso") },
                placeholder = { Text(text = "Programmazione Mobile") },
                isError = uiState.nameError != null,
                supportingText = {
                    uiState.nameError?.let { Text(text = it) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.professor,
                onValueChange = onProfessorChange,
                label = { Text(text = "Docente") },
                placeholder = { Text(text = "Prof. Rossi") },
                isError = uiState.professorError != null,
                supportingText = {
                    uiState.professorError?.let { Text(text = it) }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.credits,
                onValueChange = { value ->
                    if (value.all { it.isDigit() }) {
                        onCreditsChange(value)
                    }
                },
                label = { Text(text = "CFU") },
                isError = uiState.creditsError != null,
                supportingText = {
                    uiState.creditsError?.let { Text(text = it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            CourseStatusSelector(
                selectedStatus = uiState.status,
                onStatusChange = onStatusChange
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text(text = "Note") },
                placeholder = { Text(text = "Ripassare capitoli 4 e 5") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text(text = "Salva")
                }
            }
        }
    }
}

@Composable
private fun CourseStatusSelector(
    selectedStatus: CourseStatus,
    onStatusChange: (CourseStatus) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Stato del corso",
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            CourseStatus.entries.forEach { status ->
                FilterChip(
                    selected = selectedStatus == status,
                    onClick = { onStatusChange(status) },
                    label = { Text(text = statusLabel(status)) }
                )
            }
        }
    }
}

private fun statusLabel(status: CourseStatus): String {
    return when (status) {
        CourseStatus.TO_STUDY -> "Da studiare"
        CourseStatus.IN_PROGRESS -> "In corso"
        CourseStatus.COMPLETED -> "Completato"
    }
}
