package com.example.unilifeplanner.ui.courses

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Switch
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
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.courses.components.formatCourseStatus
import com.example.unilifeplanner.ui.courses.components.formatExamDate
import com.example.unilifeplanner.ui.utils.ExternalIntentResult
import com.example.unilifeplanner.ui.utils.ExternalIntentUtils
import kotlinx.coroutines.launch

@Composable
fun CourseDetailScreen(
    courseId: Int,
    viewModel: CourseViewModel = viewModel(),
    onEditCourseClick: () -> Unit,
    onAddLessonClick: (Int) -> Unit,
    onEditLessonClick: (Int, Int) -> Unit,
    onBackClick: () -> Unit,
    onCourseDeleted: () -> Unit
) {
    val uiState by viewModel.courseDetailUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingLessonReminderToggle by remember { mutableStateOf<LessonUi?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val course = uiState.course
            if (granted && course != null) {
                viewModel.toggleExamReminder(course)
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permesso notifiche non concesso")
                }
            }
        }
    )
    val lessonNotificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val lesson = pendingLessonReminderToggle
            pendingLessonReminderToggle = null
            if (granted && lesson != null) {
                viewModel.onToggleLessonReminder(
                    lessonId = lesson.id,
                    enabled = true
                )
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permesso notifiche non concesso")
                }
            }
        }
    )

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
        onAddLessonClick = onAddLessonClick,
        onEditLessonClick = onEditLessonClick,
        onToggleFavorite = { course -> viewModel.toggleFavorite(course) },
        onToggleReminder = { course ->
            if (course.reminderEnabled) {
                viewModel.toggleExamReminder(course)
            } else if (!isValidFutureExamDate(course.examDate)) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(reminderUnavailableMessage(course.examDate))
                }
            } else if (!NotificationHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.toggleExamReminder(course)
            }
        },
        onDeleteCourse = { course -> viewModel.deleteCourse(course) },
        onDeleteLesson = { lessonId -> viewModel.onDeleteLesson(lessonId) },
        onToggleLessonReminder = { lesson, enabled ->
            if (!enabled) {
                viewModel.onToggleLessonReminder(
                    lessonId = lesson.id,
                    enabled = false
                )
            } else if (!NotificationHelper.hasNotificationPermission(context)) {
                pendingLessonReminderToggle = lesson
                lessonNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onToggleLessonReminder(
                    lessonId = lesson.id,
                    enabled = true
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailContent(
    uiState: CourseDetailUiState,
    snackbarHostState: SnackbarHostState,
    onBackClick: () -> Unit,
    onEditCourseClick: () -> Unit,
    onAddLessonClick: (Int) -> Unit,
    onEditLessonClick: (Int, Int) -> Unit,
    onToggleFavorite: (CourseEntity) -> Unit,
    onToggleReminder: (CourseEntity) -> Unit,
    onDeleteCourse: (CourseEntity) -> Unit,
    onDeleteLesson: (Int) -> Unit,
    onToggleLessonReminder: (LessonUi, Boolean) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var lessonToDelete by remember { mutableStateOf<LessonUi?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val course = uiState.course

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Dettaglio corso",
                onBackClick = onBackClick,
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
                    lessons = uiState.lessons,
                    onEditCourseClick = onEditCourseClick,
                    onAddLessonClick = onAddLessonClick,
                    onEditLessonClick = onEditLessonClick,
                    onDeleteClick = { showDeleteDialog = true },
                    onDeleteLessonClick = { lesson -> lessonToDelete = lesson },
                    onToggleReminder = onToggleReminder,
                    onToggleLessonReminder = onToggleLessonReminder,
                    onExternalActionMessage = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
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

    lessonToDelete?.let { lesson ->
        AlertDialog(
            onDismissRequest = { lessonToDelete = null },
            title = { Text(text = "Eliminare lezione?") },
            text = {
                Text(
                    text = "La lezione di ${lesson.dayLabel} ${lesson.startTime} - ${lesson.endTime} verra rimossa."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        lessonToDelete = null
                        onDeleteLesson(lesson.id)
                    }
                ) {
                    Text(text = "Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { lessonToDelete = null }) {
                    Text(text = "Annulla")
                }
            }
        )
    }
}

@Composable
private fun CourseDetailBody(
    course: CourseEntity,
    lessons: List<LessonUi>,
    onEditCourseClick: () -> Unit,
    onAddLessonClick: (Int) -> Unit,
    onEditLessonClick: (Int, Int) -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteLessonClick: (LessonUi) -> Unit,
    onToggleReminder: (CourseEntity) -> Unit,
    onToggleLessonReminder: (LessonUi, Boolean) -> Unit,
    onExternalActionMessage: (String) -> Unit
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
                onToggleReminder = { onToggleReminder(course) }
            )
        }
        item {
            LessonsSection(
                courseId = course.id,
                lessons = lessons,
                onAddLessonClick = onAddLessonClick,
                onEditLessonClick = onEditLessonClick,
                onDeleteLessonClick = onDeleteLessonClick,
                onToggleLessonReminder = onToggleLessonReminder
            )
        }
        item {
            NotesCard(notes = course.notes)
        }
        item {
            ExternalActionsCard(
                course = course,
                onExternalActionMessage = onExternalActionMessage
            )
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
private fun LessonsSection(
    courseId: Int,
    lessons: List<LessonUi>,
    onAddLessonClick: (Int) -> Unit,
    onEditLessonClick: (Int, Int) -> Unit,
    onDeleteLessonClick: (LessonUi) -> Unit,
    onToggleLessonReminder: (LessonUi, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lezioni",
                style = MaterialTheme.typography.titleLarge
            )
            Button(onClick = { onAddLessonClick(courseId) }) {
                Icon(
                    imageVector = Icons.Filled.CalendarMonth,
                    contentDescription = "Aggiungi lezione"
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Aggiungi lezione")
            }
        }

        if (lessons.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Nessuna lezione inserita",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Aggiungi giorni e orari delle lezioni per ricevere promemoria.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            lessons.forEach { lesson ->
                LessonCard(
                    lesson = lesson,
                    onEditClick = {
                        onEditLessonClick(lesson.courseId, lesson.id)
                    },
                    onDeleteClick = {
                        onDeleteLessonClick(lesson)
                    },
                    onToggleReminder = {
                        onToggleLessonReminder(lesson, !lesson.reminderEnabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun LessonCard(
    lesson: LessonUi,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleReminder: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = lesson.dayLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "${lesson.startTime} - ${lesson.endTime}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Modifica lezione"
                        )
                    }
                    IconButton(onClick = onToggleReminder) {
                        Icon(
                            imageVector = if (lesson.reminderEnabled) {
                                Icons.Filled.NotificationsOff
                            } else {
                                Icons.Filled.Notifications
                            },
                            contentDescription = if (lesson.reminderEnabled) {
                                "Disattiva promemoria lezione"
                            } else {
                                "Attiva promemoria lezione"
                            }
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Elimina lezione"
                        )
                    }
                }
            }

            lesson.classroom?.takeIf { it.isNotBlank() }?.let { classroom ->
                DetailRow(label = "Aula", value = classroom)
            }
            lesson.building?.takeIf { it.isNotBlank() }?.let { building ->
                DetailRow(label = "Edificio", value = building)
            }
            lesson.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                DetailRow(label = "Note", value = notes)
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (lesson.reminderEnabled) {
                        Icons.Filled.Notifications
                    } else {
                        Icons.Filled.NotificationsOff
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (lesson.reminderEnabled) {
                        "Promemoria attivo"
                    } else {
                        "Promemoria disattivato"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
    onToggleReminder: () -> Unit
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
            ReminderRow(
                course = course,
                onToggleReminder = onToggleReminder
            )
        }
    }
}

@Composable
private fun ReminderRow(
    course: CourseEntity,
    onToggleReminder: () -> Unit
) {
    val enabled = isValidFutureExamDate(course.examDate)
    val supportingText = when {
        course.examDate == null -> "Aggiungi una data esame per attivare il promemoria"
        !enabled -> "La data dell'esame e passata"
        else -> "Notifiche il giorno prima e il giorno dell'esame"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Promemoria esame",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = course.reminderEnabled && enabled,
            onCheckedChange = { onToggleReminder() },
            enabled = enabled
        )
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
private fun ExternalActionsCard(
    course: CourseEntity,
    onExternalActionMessage: (String) -> Unit
) {
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Azioni esterne",
                style = MaterialTheme.typography.titleLarge
            )

            Button(
                onClick = {
                    ExternalIntentUtils.addCourseToCalendar(context, course)
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.CalendarMonth, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Aggiungi al calendario")
            }

            OutlinedButton(
                onClick = {
                    ExternalIntentUtils.openCourseLocationInMaps(context, course.classroom)
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Map, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Apri su Maps")
            }

            OutlinedButton(
                onClick = {
                    ExternalIntentUtils.shareCourse(context, course)
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Condividi")
            }

            OutlinedButton(
                onClick = {
                    ExternalIntentUtils.sendEmailToProfessor(
                        context = context,
                        email = null,
                        courseName = course.name
                    )
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Email, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Email docente")
            }
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

private fun isValidFutureExamDate(examDate: Long?): Boolean {
    return examDate != null && examDate > System.currentTimeMillis()
}

private fun reminderUnavailableMessage(examDate: Long?): String {
    return if (examDate == null) {
        "Aggiungi una data esame per attivare il promemoria"
    } else {
        "La data dell'esame e passata"
    }
}

private fun ExternalIntentResult.messageOrNull(): String? {
    return when (this) {
        ExternalIntentResult.Success -> null
        is ExternalIntentResult.MissingData -> message
        is ExternalIntentResult.NoCompatibleApp -> message
        is ExternalIntentResult.Error -> message
    }
}
