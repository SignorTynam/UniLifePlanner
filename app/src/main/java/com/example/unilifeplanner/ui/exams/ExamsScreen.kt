package com.example.unilifeplanner.ui.exams

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.InfoPill
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import kotlinx.coroutines.launch

@Composable
fun ExamsScreen(
    initialCourseId: Int?,
    viewModel: ExamsViewModel = viewModel(),
    onMenuClick: () -> Unit,
    onAddExamClick: (Int?) -> Unit,
    onEditExamClick: (Int) -> Unit,
    onOpenCourseClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var pendingReminderExamId by remember { mutableStateOf<Int?>(null) }

    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val examId = pendingReminderExamId
            pendingReminderExamId = null
            if (granted && examId != null) {
                viewModel.onToggleReminder(examId, true)
            } else if (!granted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permesso notifiche non concesso")
                }
            }
        }
    )

    LaunchedEffect(initialCourseId) {
        viewModel.setInitialCourseFilter(initialCourseId)
    }

    LaunchedEffect(uiState.errorMessage, uiState.importMessage) {
        val message = uiState.errorMessage ?: uiState.importMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Esami",
                onMenuClick = onMenuClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.availableCourses.isEmpty()) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                "Aggiungi prima un corso per poter creare un appello."
                            )
                        }
                    } else {
                        onAddExamClick(uiState.selectedCourseId)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Aggiungi appello"
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> ExamsLoadingState()
                else -> ExamsContent(
                    uiState = uiState,
                    onAddExamClick = { onAddExamClick(uiState.selectedCourseId) },
                    onImportFromUniboClick = viewModel::importExamsFromUnibo,
                    onCourseFilterChange = viewModel::onCourseFilterChange,
                    onClearCourseFilter = viewModel::clearCourseFilter,
                    onTogglePastExams = viewModel::togglePastExamsVisibility,
                    onEditExamClick = onEditExamClick,
                    onDeleteExamClick = viewModel::deleteExamAppeal,
                    onOpenCourseClick = onOpenCourseClick,
                    onToggleReminder = { exam, enabled ->
                        if (!enabled) {
                            viewModel.onToggleReminder(exam.examAppealId, false)
                        } else if (!NotificationHelper.hasNotificationPermission(context)) {
                            pendingReminderExamId = exam.examAppealId
                            reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.onToggleReminder(exam.examAppealId, true)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ExamsContent(
    uiState: ExamsUiState,
    onAddExamClick: () -> Unit,
    onImportFromUniboClick: () -> Unit,
    onCourseFilterChange: (Int?) -> Unit,
    onClearCourseFilter: () -> Unit,
    onTogglePastExams: () -> Unit,
    onEditExamClick: (Int) -> Unit,
    onDeleteExamClick: (Int) -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    onToggleReminder: (ExamAppealListItemUi, Boolean) -> Unit
) {
    var showCourseFilterDialog by remember { mutableStateOf(false) }
    var pendingDeleteExam by remember { mutableStateOf<ExamAppealListItemUi?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ExamsActionsCard(
                selectedCourseName = uiState.selectedCourseName,
                hasCourses = uiState.availableCourses.isNotEmpty(),
                onAddExamClick = onAddExamClick,
                onImportFromUniboClick = onImportFromUniboClick,
                onChooseCourseClick = { showCourseFilterDialog = true },
                onClearCourseFilter = onClearCourseFilter
            )
        }

        if (!uiState.hasAnyExams) {
            item {
                EmptyExamsState(
                    title = "Nessun appello disponibile",
                    message = "Aggiungi manualmente un appello oppure importalo da UniBo quando disponibile."
                )
            }
            return@LazyColumn
        }

        if (uiState.upcomingExams.isEmpty() && uiState.pastExams.isEmpty()) {
            item {
                EmptyExamsState(
                    title = "Nessun appello trovato",
                    message = "Modifica il filtro corso per vedere altri appelli."
                )
            }
            return@LazyColumn
        }

        if (uiState.upcomingExams.isNotEmpty()) {
            item {
                SectionHeader(text = "Prossimi appelli")
            }
            items(
                items = uiState.upcomingExams,
                key = { exam -> exam.examAppealId }
            ) { exam ->
                ExamAppealCard(
                    exam = exam,
                    onEditClick = { onEditExamClick(exam.examAppealId) },
                    onDeleteClick = { pendingDeleteExam = exam },
                    onOpenCourseClick = { onOpenCourseClick(exam.courseId) },
                    onToggleReminderClick = { onToggleReminder(exam, !exam.reminderEnabled) }
                )
            }
        }

        if (uiState.pastExams.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(text = "Appelli passati")
                    TextButton(onClick = onTogglePastExams) {
                        Text(text = if (uiState.showPastExams) "Nascondi" else "Mostra")
                    }
                }
            }
        }

        if (uiState.showPastExams) {
            items(
                items = uiState.pastExams,
                key = { exam -> "past-${exam.examAppealId}" }
            ) { exam ->
                ExamAppealCard(
                    exam = exam,
                    onEditClick = { onEditExamClick(exam.examAppealId) },
                    onDeleteClick = { pendingDeleteExam = exam },
                    onOpenCourseClick = { onOpenCourseClick(exam.courseId) },
                    onToggleReminderClick = { onToggleReminder(exam, !exam.reminderEnabled) }
                )
            }
        }
    }

    if (showCourseFilterDialog) {
        CourseFilterDialog(
            courses = uiState.availableCourses,
            onCourseSelected = { courseId ->
                showCourseFilterDialog = false
                onCourseFilterChange(courseId)
            },
            onDismiss = { showCourseFilterDialog = false }
        )
    }

    pendingDeleteExam?.let { exam ->
        AlertDialog(
            onDismissRequest = { pendingDeleteExam = null },
            title = { Text(text = "Eliminare appello?") },
            text = { Text(text = "Vuoi eliminare questo appello?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteExam = null
                        onDeleteExamClick(exam.examAppealId)
                    }
                ) {
                    Text(text = "Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteExam = null }) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Composable
private fun ExamsActionsCard(
    selectedCourseName: String?,
    hasCourses: Boolean,
    onAddExamClick: () -> Unit,
    onImportFromUniboClick: () -> Unit,
    onChooseCourseClick: () -> Unit,
    onClearCourseFilter: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Appelli d'esame",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Gestisci gli appelli separatamente dai corsi e scegli per quali attivare il promemoria.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddExamClick,
                    enabled = hasCourses,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Event, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "Aggiungi")
                }
                OutlinedButton(
                    onClick = onImportFromUniboClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = "Importa UniBo")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onChooseCourseClick,
                    enabled = hasCourses,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.School, contentDescription = null)
                    Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                    Text(text = selectedCourseName ?: "Tutti i corsi")
                }
                if (selectedCourseName != null) {
                    TextButton(onClick = onClearCourseFilter) {
                        Text(text = "Rimuovi")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExamAppealCard(
    exam: ExamAppealListItemUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onOpenCourseClick: () -> Unit,
    onToggleReminderClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = exam.courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = listOfNotNull(exam.dateLabel, exam.timeLabel).joinToString(" - "),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            exam.location?.takeIf { it.isNotBlank() }?.let { location ->
                Text(
                    text = location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            exam.type?.takeIf { it.isNotBlank() }?.let { type ->
                Text(
                    text = "Tipo: $type",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            exam.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoPill(text = exam.sourceLabel)
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = if (exam.reminderEnabled) {
                                "Partecipo - promemoria attivo"
                            } else {
                                "Promemoria disattivato"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (exam.reminderEnabled) {
                                Icons.Filled.Notifications
                            } else {
                                Icons.Filled.NotificationsOff
                            },
                            contentDescription = null
                        )
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Modifica appello"
                    )
                }
                IconButton(onClick = onOpenCourseClick) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "Apri corso"
                    )
                }
                IconButton(onClick = onToggleReminderClick) {
                    Icon(
                        imageVector = if (exam.reminderEnabled) {
                            Icons.Filled.NotificationsOff
                        } else {
                            Icons.Filled.Notifications
                        },
                        contentDescription = if (exam.reminderEnabled) {
                            "Disattiva promemoria appello"
                        } else {
                            "Attiva promemoria appello"
                        }
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Elimina appello"
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseFilterDialog(
    courses: List<ExamCourseOptionUi>,
    onCourseSelected: (Int?) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Filtra per corso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { onCourseSelected(null) }) {
                    Text(text = "Tutti i corsi")
                }
                courses.forEach { course ->
                    TextButton(onClick = { onCourseSelected(course.courseId) }) {
                        Text(text = course.courseName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Annulla")
            }
        }
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun EmptyExamsState(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(0.16f)
        )
        Spacer(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExamsLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
