package com.example.unilifeplanner.ui.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.exams.components.ExamCompletionSection
import com.example.unilifeplanner.ui.exams.components.ExamFeedbackActions
import com.example.unilifeplanner.ui.exams.components.ExamFeedbackDismissDialog
import com.example.unilifeplanner.ui.exams.components.ExamFeedbackErrorState
import com.example.unilifeplanner.ui.exams.components.ExamFeedbackHero
import com.example.unilifeplanner.ui.exams.components.ExamFeedbackLoadingState
import com.example.unilifeplanner.ui.exams.components.ExamGradeSection
import com.example.unilifeplanner.ui.exams.components.ExamNotesSection
import com.example.unilifeplanner.ui.exams.components.ExamResultSelector
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamFeedbackScreen(
    examAppealId: Int,
    onBackClick: () -> Unit,
    onSaved: (Int, String?) -> Unit,
    viewModel: ExamFeedbackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(examAppealId) {
        viewModel.load(examAppealId)
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved(uiState.courseId, uiState.completionMessage)
        }
    }

    val hasBlockingError = !uiState.isLoading && uiState.courseId == 0 &&
        uiState.errorMessage != null

    LaunchedEffect(uiState.errorMessage, hasBlockingError) {
        val message = uiState.errorMessage
        if (message != null && !hasBlockingError) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    ExamFeedbackContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        hasBlockingError = hasBlockingError,
        onBackClick = onBackClick,
        onResultSelected = viewModel::selectResult,
        onGradeChange = viewModel::updateGrade,
        onNotesChange = viewModel::updateNotes,
        onMarkCompletedChange = viewModel::updateMarkCourseCompleted,
        onSaveClick = viewModel::saveFeedback,
        onDismissClick = viewModel::dismissFeedback
    )
}

@Composable
private fun ExamFeedbackContent(
    uiState: ExamFeedbackUiState,
    snackbarHostState: SnackbarHostState,
    hasBlockingError: Boolean,
    onBackClick: () -> Unit,
    onResultSelected: (ExamFeedbackResult) -> Unit,
    onGradeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onMarkCompletedChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    var showDismissDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Esito esame",
                onBackClick = onBackClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        when {
            uiState.isLoading -> ExamFeedbackLoadingState(
                modifier = Modifier.padding(innerPadding)
            )

            hasBlockingError -> ExamFeedbackErrorState(
                title = "Appello non trovato",
                message = "L’appello potrebbe essere stato eliminato.",
                onBackClick = onBackClick,
                modifier = Modifier.padding(innerPadding)
            )

            else -> ExamFeedbackForm(
                uiState = uiState,
                onResultSelected = onResultSelected,
                onGradeChange = onGradeChange,
                onNotesChange = onNotesChange,
                onMarkCompletedChange = onMarkCompletedChange,
                onSaveClick = onSaveClick,
                onDismissClick = { showDismissDialog = true },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    if (showDismissDialog) {
        ExamFeedbackDismissDialog(
            onDismissRequest = { showDismissDialog = false },
            onConfirmClick = {
                showDismissDialog = false
                onDismissClick()
            }
        )
    }
}

@Composable
private fun ExamFeedbackForm(
    uiState: ExamFeedbackUiState,
    onResultSelected: (ExamFeedbackResult) -> Unit,
    onGradeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onMarkCompletedChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsEnabled = !uiState.isSaving

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            ExamFeedbackHero(uiState = uiState)
        }
        item {
            ExamResultSelector(
                selectedResult = uiState.selectedResult,
                onResultSelected = onResultSelected,
                enabled = controlsEnabled
            )
        }
        if (uiState.selectedResult == ExamFeedbackResult.PASSED) {
            item {
                ExamGradeSection(
                    grade = uiState.grade,
                    gradeError = uiState.gradeError,
                    enabled = controlsEnabled,
                    onGradeChange = onGradeChange
                )
            }
            item {
                ExamCompletionSection(
                    checked = uiState.markCourseCompleted,
                    enabled = controlsEnabled,
                    onCheckedChange = onMarkCompletedChange
                )
            }
        }
        item {
            ExamNotesSection(
                notes = uiState.notes,
                enabled = controlsEnabled,
                onNotesChange = onNotesChange
            )
        }
        item {
            ExamFeedbackActions(
                isSaving = uiState.isSaving,
                onSaveClick = onSaveClick,
                onDismissClick = onDismissClick
            )
        }
    }
}

@Preview(name = "Feedback screen - passed", showBackground = true, heightDp = 980)
@Composable
private fun PreviewExamFeedbackScreenPassed() {
    UniLifePlannerTheme {
        ExamFeedbackContent(
            uiState = previewFeedbackState(
                selectedResult = ExamFeedbackResult.PASSED,
                grade = "28"
            ),
            snackbarHostState = remember { SnackbarHostState() },
            hasBlockingError = false,
            onBackClick = {},
            onResultSelected = {},
            onGradeChange = {},
            onNotesChange = {},
            onMarkCompletedChange = {},
            onSaveClick = {},
            onDismissClick = {}
        )
    }
}

@Preview(name = "Feedback screen - waiting dark", showBackground = true, heightDp = 900, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewExamFeedbackScreenWaitingDark() {
    UniLifePlannerTheme {
        ExamFeedbackContent(
            uiState = previewFeedbackState(
                selectedResult = ExamFeedbackResult.WAITING_RESULT,
                location = null
            ),
            snackbarHostState = remember { SnackbarHostState() },
            hasBlockingError = false,
            onBackClick = {},
            onResultSelected = {},
            onGradeChange = {},
            onNotesChange = {},
            onMarkCompletedChange = {},
            onSaveClick = {},
            onDismissClick = {}
        )
    }
}

private fun previewFeedbackState(
    selectedResult: ExamFeedbackResult,
    grade: String = "",
    location: String? = "Aula 3.2",
    isSaving: Boolean = false
): ExamFeedbackUiState {
    return ExamFeedbackUiState(
        isLoading = false,
        isSaving = isSaving,
        examAppealId = 12,
        courseId = 4,
        courseName = "Sistemi operativi e laboratorio",
        dateTimeLabel = "18 giugno 2026, 09:30",
        location = location,
        selectedResult = selectedResult,
        grade = grade,
        notes = "Domande su processi e scheduling.",
        markCourseCompleted = true
    )
}
