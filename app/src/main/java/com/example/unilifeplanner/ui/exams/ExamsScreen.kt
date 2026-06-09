package com.example.unilifeplanner.ui.exams

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.exams.components.ExamDaySection
import com.example.unilifeplanner.ui.exams.components.ExamEmptyState
import com.example.unilifeplanner.ui.exams.components.ExamFiltersSection
import com.example.unilifeplanner.ui.exams.components.ExamTimelineItem
import com.example.unilifeplanner.ui.exams.components.ExamsAgendaHeader
import com.example.unilifeplanner.ui.exams.components.PastExamsHeader
import com.example.unilifeplanner.ui.exams.components.hasActiveExamFilters
import kotlinx.coroutines.launch

@Composable
fun ExamsScreen(
    initialCourseId: Int?,
    viewModel: ExamsViewModel = viewModel(),
    onMenuClick: () -> Unit,
    onAddExamClick: (Int?) -> Unit,
    onEditExamClick: (Int) -> Unit,
    onOpenFeedbackClick: (Int) -> Unit,
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

    val handleAddExam: () -> Unit = {
        if (uiState.availableCourses.isEmpty()) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    "Aggiungi prima un corso per poter creare un appello."
                )
            }
        } else {
            onAddExamClick(uiState.selectedCourseId)
        }
        Unit
    }

    LaunchedEffect(initialCourseId) {
        viewModel.setInitialCourseFilter(initialCourseId)
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.refreshMessage) {
        uiState.refreshMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRefreshMessage()
        }
    }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Esami",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(
                        onClick = viewModel::refreshUniboData,
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Aggiorna appelli da UniBo"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = handleAddExam) {
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
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onDateFilterChange = viewModel::onDateFilterChange,
                    onCourseFilterChange = viewModel::onCourseFilterChange,
                    onSortOptionChange = viewModel::onSortOptionChange,
                    onSelectedExamDayChange = viewModel::onSelectedExamDayChange,
                    onClearFilters = viewModel::clearFilters,
                    onTogglePastExams = viewModel::togglePastExamsVisibility,
                    onAddExamClick = handleAddExam,
                    onEditExamClick = onEditExamClick,
                    onOpenFeedbackClick = onOpenFeedbackClick,
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
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (ExamDateFilter) -> Unit,
    onCourseFilterChange: (Int?) -> Unit,
    onSortOptionChange: (ExamSortOption) -> Unit,
    onSelectedExamDayChange: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    onTogglePastExams: () -> Unit,
    onAddExamClick: () -> Unit,
    onEditExamClick: (Int) -> Unit,
    onOpenFeedbackClick: (Int) -> Unit,
    onDeleteExamClick: (Int) -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    onToggleReminder: (ExamAppealListItemUi, Boolean) -> Unit
) {
    var pendingDeleteExam by remember { mutableStateOf<ExamAppealListItemUi?>(null) }
    val groupedUpcoming = rememberGroupedExams(uiState.upcomingExams)

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            ExamsAgendaHeader(uiState = uiState)
        }

        item {
            ExamFiltersSection(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onDateFilterChange = onDateFilterChange,
                onCourseFilterChange = onCourseFilterChange,
                onSortOptionChange = onSortOptionChange,
                onSelectedExamDayChange = onSelectedExamDayChange,
                onClearFilters = onClearFilters
            )
        }

        if (uiState.availableCourses.isEmpty()) {
            item {
                ExamEmptyState(
                    title = "Nessun corso disponibile",
                    message = "Aggiungi prima un corso per poter creare un appello.",
                    primaryActionLabel = null,
                    onPrimaryAction = null
                )
            }
            return@LazyColumn
        }

        if (!uiState.hasAnyExams) {
            item {
                ExamEmptyState(
                    title = "Nessun appello disponibile",
                    message = "Aggiungi un appello manualmente o importa i dati da UniBo.",
                    primaryActionLabel = "Aggiungi appello",
                    onPrimaryAction = onAddExamClick
                )
            }
            return@LazyColumn
        }

        if (uiState.upcomingExams.isEmpty() && uiState.pastExams.isEmpty()) {
            item {
                ExamEmptyState(
                    title = "Nessun appello trovato",
                    message = "Prova a modificare ricerca, data, corso o ordinamento.",
                    primaryActionLabel = if (hasActiveExamFilters(uiState)) {
                        "Cancella filtri"
                    } else {
                        null
                    },
                    onPrimaryAction = if (hasActiveExamFilters(uiState)) {
                        onClearFilters
                    } else {
                        null
                    }
                )
            }
            return@LazyColumn
        }

        items(
            items = groupedUpcoming,
            key = { group -> group.dayTitle }
        ) { group ->
            ExamDaySection(
                dayTitle = group.dayTitle,
                exams = group.exams,
                onEditClick = { exam -> onEditExamClick(exam.examAppealId) },
                onFeedbackClick = { exam -> onOpenFeedbackClick(exam.examAppealId) },
                onDeleteClick = { exam -> pendingDeleteExam = exam },
                onOpenCourseClick = onOpenCourseClick,
                onToggleReminderClick = { exam ->
                    onToggleReminder(exam, !exam.reminderEnabled)
                }
            )
        }

        if (uiState.pastExams.isNotEmpty()) {
            item {
                PastExamsHeader(
                    isExpanded = uiState.showPastExams,
                    onToggle = onTogglePastExams
                )
            }
        }

        if (uiState.showPastExams) {
            items(
                items = uiState.pastExams,
                key = { exam -> "past-${exam.examAppealId}" }
            ) { exam ->
                ExamTimelineItem(
                    exam = exam,
                    onEditClick = { onEditExamClick(exam.examAppealId) },
                    onFeedbackClick = { onOpenFeedbackClick(exam.examAppealId) },
                    onDeleteClick = { pendingDeleteExam = exam },
                    onOpenCourseClick = { onOpenCourseClick(exam.courseId) },
                    onToggleReminderClick = { onToggleReminder(exam, !exam.reminderEnabled) }
                )
            }
        }
    }

    pendingDeleteExam?.let { exam ->
        AlertDialog(
            onDismissRequest = { pendingDeleteExam = null },
            title = { Text(text = "Eliminare appello?") },
            text = {
                Text(text = "Vuoi eliminare questo appello? Questa azione non può essere annullata.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDeleteExam = null
                        onDeleteExamClick(exam.examAppealId)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
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
private fun ExamsLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private data class ExamDayGroup(
    val dayTitle: String,
    val exams: List<ExamAppealListItemUi>
)

@Composable
private fun rememberGroupedExams(exams: List<ExamAppealListItemUi>): List<ExamDayGroup> {
    return remember(exams) {
        exams
            .groupBy { it.relativeDateLabel }
            .entries
            .sortedBy { (_, groupExams) ->
                groupExams.minOf { it.startMillis }
            }
            .map { (dayTitle, groupExams) ->
                ExamDayGroup(
                    dayTitle = dayTitle,
                    exams = groupExams.sortedBy { it.startMillis }
                )
            }
    }
}
