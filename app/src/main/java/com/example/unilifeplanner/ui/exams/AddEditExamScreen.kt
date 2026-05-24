package com.example.unilifeplanner.ui.exams

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import kotlinx.coroutines.launch

@Composable
fun AddEditExamScreen(
    courseId: Int?,
    examAppealId: Int?,
    viewModel: AddEditExamViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val savePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.saveExamAppeal()
            } else {
                viewModel.updateReminderEnabled(false)
                viewModel.saveExamAppeal()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Permesso notifiche non concesso. Appello salvato senza promemoria."
                    )
                }
            }
        }
    )
    val switchPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.updateReminderEnabled(true)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permesso notifiche non concesso")
                }
            }
        }
    )

    LaunchedEffect(courseId, examAppealId) {
        viewModel.loadExamAppeal(courseId, examAppealId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Appello salvato")
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    AddEditExamContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCourseSelected = viewModel::updateCourse,
        onDateChange = viewModel::updateDate,
        onTimeChange = viewModel::updateTime,
        onLocationChange = viewModel::updateLocation,
        onTypeChange = viewModel::updateType,
        onNotesChange = viewModel::updateNotes,
        onReminderEnabledChange = { enabled ->
            if (!enabled) {
                viewModel.updateReminderEnabled(false)
            } else if (!NotificationHelper.hasNotificationPermission(context)) {
                switchPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.updateReminderEnabled(true)
            }
        },
        onSaveClick = {
            if (
                uiState.reminderEnabled &&
                !NotificationHelper.hasNotificationPermission(context)
            ) {
                savePermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.saveExamAppeal()
            }
        },
        onBackClick = onNavigateBack
    )
}

@Composable
private fun AddEditExamContent(
    uiState: ExamAppealFormUiState,
    snackbarHostState: SnackbarHostState,
    onCourseSelected: (Int) -> Unit,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onTypeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val title = if (uiState.examAppealId == null) "Aggiungi appello" else "Modifica appello"
    var showCourseDialog by remember { mutableStateOf(false) }

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
                Text(text = "Caricamento appello...")
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
            if (uiState.courses.isEmpty()) {
                Text(
                    text = "Aggiungi prima un corso per poter creare un appello.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Corso associato",
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedButton(
                    onClick = { showCourseDialog = true },
                    enabled = uiState.courses.isNotEmpty() && uiState.examAppealId == null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = uiState.courseName.ifBlank { "Seleziona corso" })
                }
                uiState.courseError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            OutlinedTextField(
                value = uiState.date,
                onValueChange = onDateChange,
                label = { Text(text = "Data appello") },
                placeholder = { Text(text = "24/06/2026") },
                isError = uiState.dateError != null,
                supportingText = {
                    Text(text = uiState.dateError ?: "Formato gg/mm/aaaa")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.time,
                onValueChange = onTimeChange,
                label = { Text(text = "Ora appello") },
                placeholder = { Text(text = "09:00") },
                isError = uiState.timeError != null,
                supportingText = {
                    Text(text = uiState.timeError ?: "Opzionale")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.location,
                onValueChange = onLocationChange,
                label = { Text(text = "Luogo o aula") },
                placeholder = { Text(text = "Aula Magna") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.type,
                onValueChange = onTypeChange,
                label = { Text(text = "Tipo appello") },
                placeholder = { Text(text = "Scritto, orale, progetto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ReminderSwitchSection(
                reminderEnabled = uiState.reminderEnabled,
                onReminderEnabledChange = onReminderEnabledChange
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChange,
                label = { Text(text = "Note") },
                placeholder = { Text(text = "Portare documento e badge") },
                minLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Fonte: ${uiState.sourceLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onSaveClick,
                enabled = !uiState.isSaving && uiState.courses.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = if (uiState.examAppealId == null) {
                            "Salva appello"
                        } else {
                            "Aggiorna appello"
                        }
                    )
                }
            }
        }
    }

    if (showCourseDialog) {
        AlertDialog(
            onDismissRequest = { showCourseDialog = false },
            title = { Text(text = "Scegli corso") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.courses.forEach { course ->
                        TextButton(
                            onClick = {
                                showCourseDialog = false
                                onCourseSelected(course.courseId)
                            }
                        ) {
                            Text(text = course.courseName)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCourseDialog = false }) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Composable
private fun ReminderSwitchSection(
    reminderEnabled: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Partecipo e voglio un promemoria",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Il promemoria appartiene a questo appello, non al corso.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = reminderEnabled,
            onCheckedChange = onReminderEnabledChange
        )
    }
}
