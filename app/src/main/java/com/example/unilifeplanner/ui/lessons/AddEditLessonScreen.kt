package com.example.unilifeplanner.ui.lessons

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.lessons.dayOfWeekLabel
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import kotlinx.coroutines.launch

@Composable
fun AddEditLessonScreen(
    courseId: Int,
    lessonId: Int?,
    viewModel: AddEditLessonViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val saveNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                viewModel.saveLesson()
            } else {
                viewModel.updateReminderEnabled(false)
                viewModel.saveLesson()
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        "Permesso notifiche non concesso. Lezione salvata senza promemoria."
                    )
                }
            }
        }
    )
    val reminderSwitchPermissionLauncher = rememberLauncherForActivityResult(
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

    LaunchedEffect(courseId, lessonId) {
        viewModel.loadLesson(courseId, lessonId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("Lezione salvata")
            viewModel.resetSaveState()
            onNavigateBack()
        }
    }

    AddEditLessonContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onDayOfWeekChange = viewModel::updateDayOfWeek,
        onStartTimeChange = viewModel::updateStartTime,
        onEndTimeChange = viewModel::updateEndTime,
        onClassroomChange = viewModel::updateClassroom,
        onBuildingChange = viewModel::updateBuilding,
        onLocationQueryChange = viewModel::updateLocationQuery,
        onNotesChange = viewModel::updateNotes,
        onReminderEnabledChange = { enabled ->
            if (!enabled) {
                viewModel.updateReminderEnabled(false)
            } else if (!NotificationHelper.hasNotificationPermission(context)) {
                reminderSwitchPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.updateReminderEnabled(true)
            }
        },
        onSaveClick = {
            if (
                uiState.reminderEnabled &&
                !NotificationHelper.hasNotificationPermission(context)
            ) {
                saveNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.saveLesson()
            }
        },
        onBackClick = onNavigateBack
    )
}

@Composable
private fun AddEditLessonContent(
    uiState: LessonUiState,
    snackbarHostState: SnackbarHostState,
    onDayOfWeekChange: (Int) -> Unit,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,
    onClassroomChange: (String) -> Unit,
    onBuildingChange: (String) -> Unit,
    onLocationQueryChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val title = if (uiState.lessonId == null) "Aggiungi lezione" else "Modifica lezione"

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
                Text(text = "Caricamento lezione...")
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
            DayOfWeekSelector(
                selectedDayOfWeek = uiState.dayOfWeek,
                error = uiState.dayOfWeekError,
                onDayOfWeekChange = onDayOfWeekChange
            )

            OutlinedTextField(
                value = uiState.startTime,
                onValueChange = onStartTimeChange,
                label = { Text(text = "Ora inizio") },
                placeholder = { Text(text = "09:00") },
                isError = uiState.startTimeError != null,
                supportingText = {
                    uiState.startTimeError?.let { Text(text = it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.endTime,
                onValueChange = onEndTimeChange,
                label = { Text(text = "Ora fine") },
                placeholder = { Text(text = "11:00") },
                isError = uiState.endTimeError != null,
                supportingText = {
                    uiState.endTimeError?.let { Text(text = it) }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.classroom,
                onValueChange = onClassroomChange,
                label = { Text(text = "Aula") },
                placeholder = { Text(text = "Aula B2") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.building,
                onValueChange = onBuildingChange,
                label = { Text(text = "Edificio") },
                placeholder = { Text(text = "Polo Fibonacci") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.locationQuery,
                onValueChange = onLocationQueryChange,
                label = { Text(text = "Luogo Google Maps") },
                placeholder = { Text(text = "Polo Fibonacci Pisa, Aula B2") },
                supportingText = { Text(text = "Usato per aprire la posizione in Google Maps.") },
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
                placeholder = { Text(text = "Portare il materiale del laboratorio") },
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
                    Text(
                        text = if (uiState.lessonId == null) {
                            "Salva lezione"
                        } else {
                            "Aggiorna lezione"
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DayOfWeekSelector(
    selectedDayOfWeek: Int?,
    error: String?,
    onDayOfWeekChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Giorno della settimana",
            style = MaterialTheme.typography.labelLarge
        )
        listOf(
            listOf(1, 2, 3),
            listOf(4, 5, 6),
            listOf(7)
        ).forEach { rowDays ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowDays.forEach { day ->
                    FilterChip(
                        selected = selectedDayOfWeek == day,
                        onClick = { onDayOfWeekChange(day) },
                        label = { Text(text = dayOfWeekLabel(day)) }
                    )
                }
            }
        }
        error?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
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
                text = "Promemoria la sera prima",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Notifica alle 20:00 del giorno precedente",
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
