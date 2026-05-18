package com.example.unilifeplanner.ui.courses

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.ui.courses.components.formatCourseStatus
import com.example.unilifeplanner.ui.courses.components.formatExamDate
import kotlinx.coroutines.launch

@Composable
fun CourseDetailScreen(
    courseId: Int,
    viewModel: CourseViewModel = viewModel(),
    onEditCourseClick: () -> Unit,
    onBackClick: () -> Unit,
    onCourseDeleted: () -> Unit
) {
    val uiState by viewModel.courseDetailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(courseId) {
        viewModel.loadCourseById(courseId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            viewModel.resetDeleteState()
            onCourseDeleted()
        }
    }

    CourseDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onEditCourseClick = onEditCourseClick,
        onToggleFavorite = { course -> viewModel.toggleFavorite(course) },
        onDeleteCourse = { course -> viewModel.deleteCourse(course) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailContent(
    uiState: CourseDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEditCourseClick: () -> Unit,
    onToggleFavorite: (CourseEntity) -> Unit,
    onDeleteCourse: (CourseEntity) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val course = uiState.course

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Dettaglio corso") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(text = "Back")
                    }
                },
                actions = {
                    if (course != null) {
                        IconButton(onClick = { onToggleFavorite(course) }) {
                            Icon(
                                imageVector = if (course.isFavorite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Filled.StarBorder
                                },
                                contentDescription = if (course.isFavorite) {
                                    "Rimuovi dai preferiti"
                                } else {
                                    "Aggiungi ai preferiti"
                                },
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
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
                uiState.isLoading -> CourseDetailLoadingState()
                course == null -> CourseDetailErrorState(message = "Corso non trovato.")
                else -> CourseDetailBody(
                    course = course,
                    onEditCourseClick = onEditCourseClick,
                    onDeleteClick = { showDeleteDialog = true },
                    onAddToCalendarClick = {
                        if (course.examDate == null) {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Data esame non disponibile")
                            }
                        } else {
                            openCalendarIntent(context, course)
                        }
                    }
                )
            }
        }
    }

    if (showDeleteDialog && course != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Eliminare corso?") },
            text = { Text(text = "Questa azione non puo essere annullata.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteCourse(course)
                    }
                ) {
                    Text(text = "Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Composable
private fun CourseDetailBody(
    course: CourseEntity,
    onEditCourseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onAddToCalendarClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseInfoCard(course = course)
        }
        item {
            ExamInfoCard(
                course = course,
                onAddToCalendarClick = onAddToCalendarClick
            )
        }
        item {
            NotesCard(notes = course.notes)
        }
        item {
            ActionsCard(
                onEditCourseClick = onEditCourseClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@Composable
private fun CourseInfoCard(course: CourseEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = course.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            DetailRow(label = "Docente", value = course.professor)
            DetailRow(label = "CFU", value = course.credits.toString())
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stato",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = {},
                    label = { Text(text = formatCourseStatus(course.status)) }
                )
            }
            DetailRow(
                label = "Preferito",
                value = if (course.isFavorite) "Si" else "No"
            )
        }
    }
}

@Composable
private fun ExamInfoCard(
    course: CourseEntity,
    onAddToCalendarClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Esame",
                style = MaterialTheme.typography.titleLarge
            )
            DetailRow(label = "Data", value = formatExamDate(course.examDate))
            DetailRow(
                label = "Aula",
                value = course.classroom?.takeIf { it.isNotBlank() } ?: "Aula non impostata"
            )
            Button(
                onClick = onAddToCalendarClick,
                enabled = course.examDate != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Aggiungi al calendario")
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Note",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = notes?.takeIf { it.isNotBlank() } ?: "Nessuna nota inserita",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionsCard(
    onEditCourseClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEditCourseClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Edit, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Modifica")
            }
            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Elimina")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun CourseDetailLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CourseDetailErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

fun openCalendarIntent(
    context: Context,
    course: CourseEntity
) {
    val beginTime = course.examDate ?: return
    val endTime = beginTime + 2 * 60 * 60 * 1000
    val description = buildString {
        append("Docente: ${course.professor}")
        append("\nCFU: ${course.credits}")
        course.notes?.takeIf { it.isNotBlank() }?.let { notes ->
            append("\nNote: $notes")
        }
    }

    val intent = Intent(Intent.ACTION_INSERT).apply {
        type = "vnd.android.cursor.item/event"
        putExtra(CalendarContract.Events.TITLE, "Esame: ${course.name}")
        putExtra(CalendarContract.Events.DESCRIPTION, description)
        putExtra(CalendarContract.Events.EVENT_LOCATION, course.classroom.orEmpty())
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime)
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
    }

    context.startActivity(intent)
}
