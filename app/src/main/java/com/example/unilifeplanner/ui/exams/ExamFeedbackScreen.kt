package com.example.unilifeplanner.ui.exams

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun ExamFeedbackScreen(
    examAppealId: Int,
    onBackClick: () -> Unit,
    onSaved: (Int, String?) -> Unit,
    viewModel: ExamFeedbackViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(examAppealId) {
        viewModel.load(examAppealId)
    }
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onSaved(uiState.courseId, uiState.completionMessage)
        }
    }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Esito esame",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.errorMessage != null -> Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
                else -> FeedbackForm(
                    uiState = uiState,
                    onResultSelected = viewModel::selectResult,
                    onGradeChange = viewModel::updateGrade,
                    onNotesChange = viewModel::updateNotes,
                    onMarkCompletedChange = viewModel::updateMarkCourseCompleted,
                    onSaveClick = viewModel::saveFeedback,
                    onDismissClick = viewModel::dismissFeedback
                )
            }
        }
    }
}

@Composable
private fun FeedbackForm(
    uiState: ExamFeedbackUiState,
    onResultSelected: (ExamFeedbackResult) -> Unit,
    onGradeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onMarkCompletedChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = uiState.courseName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = uiState.dateTimeLabel)
            uiState.location?.let { Text(text = it) }
        }
    }

    Text(
        text = "Com'e andato l'esame?",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Column(modifier = Modifier.selectableGroup()) {
        ExamFeedbackResult.entries.forEach { result ->
            ResultOption(
                result = result,
                selected = uiState.selectedResult == result,
                onSelected = { onResultSelected(result) }
            )
        }
    }

    if (uiState.selectedResult == ExamFeedbackResult.PASSED) {
        OutlinedTextField(
            value = uiState.grade,
            onValueChange = onGradeChange,
            label = { Text(text = "Voto") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.markCourseCompleted,
                onCheckedChange = onMarkCompletedChange
            )
            Text(text = "Segna questo corso come completato")
        }
    }

    OutlinedTextField(
        value = uiState.notes,
        onValueChange = onNotesChange,
        label = { Text(text = "Note personali") },
        modifier = Modifier.fillMaxWidth()
    )
    Button(
        onClick = onSaveClick,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Salva esito")
    }
    OutlinedButton(
        onClick = onDismissClick,
        enabled = !uiState.isSaving,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = "Non chiedermelo piu per questo appello")
    }
}

@Composable
private fun ResultOption(
    result: ExamFeedbackResult,
    selected: Boolean,
    onSelected: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
        Text(text = result.label())
    }
}

private fun ExamFeedbackResult.label(): String {
    return when (this) {
        ExamFeedbackResult.PASSED -> "Superato"
        ExamFeedbackResult.FAILED -> "Non superato"
        ExamFeedbackResult.WAITING_RESULT -> "In attesa del risultato"
        ExamFeedbackResult.NOT_ATTENDED -> "Non ho partecipato"
    }
}
