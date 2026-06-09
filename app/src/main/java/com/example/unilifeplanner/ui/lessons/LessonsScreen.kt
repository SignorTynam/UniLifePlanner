package com.example.unilifeplanner.ui.lessons

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.notifications.NotificationHelper
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.lessons.components.LessonDaySection
import com.example.unilifeplanner.ui.lessons.components.LessonEmptyState
import com.example.unilifeplanner.ui.lessons.components.LessonFiltersSection
import com.example.unilifeplanner.ui.lessons.components.LessonTimelineItem
import com.example.unilifeplanner.ui.lessons.components.LessonsAgendaHeader
import com.example.unilifeplanner.ui.lessons.components.hasActiveLessonFilters
import com.example.unilifeplanner.ui.utils.ExternalIntentResult
import com.example.unilifeplanner.ui.utils.ExternalIntentUtils
import kotlinx.coroutines.launch

@Composable
fun LessonsScreen(
    initialCourseId: Int?,
    viewModel: LessonsViewModel = viewModel(),
    onMenuClick: () -> Unit,
    onLessonClick: (Int, Int) -> Unit,
    onAddLessonClick: (Int) -> Unit,
    onOpenCourseClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showCoursePicker by remember { mutableStateOf(false) }
    var pendingReminderLessonId by remember { mutableStateOf<Int?>(null) }

    val reminderPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            val lessonId = pendingReminderLessonId
            pendingReminderLessonId = null
            if (granted && lessonId != null) {
                viewModel.onToggleReminder(lessonId, true)
            } else if (!granted) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Permesso notifiche non concesso")
                }
            }
        }
    )

    val handleAddLesson = {
        handleAddLessonClick(
            uiState = uiState,
            onAddLessonClick = onAddLessonClick,
            onShowCoursePicker = { showCoursePicker = true },
            onNoCoursesAvailable = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Nessun corso disponibile")
                }
            }
        )
    }

    LaunchedEffect(initialCourseId) {
        viewModel.setInitialCourseFilter(initialCourseId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
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
                title = "Lezioni",
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
                                contentDescription = "Aggiorna da UniBo"
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = handleAddLesson) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Aggiungi lezione"
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> LessonsLoadingState()
                else -> LessonsContent(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onDateFilterChange = viewModel::onDateFilterChange,
                    onCourseFilterChange = viewModel::onCourseFilterChange,
                    onSortOptionChange = viewModel::onSortOptionChange,
                    onSelectedDayOfWeekChange = viewModel::onSelectedDayOfWeekChange,
                    onClearFilters = viewModel::clearFilters,
                    onTogglePastThisWeek = viewModel::togglePastThisWeekVisibility,
                    onAddLessonClick = handleAddLesson,
                    onLessonClick = onLessonClick,
                    onOpenCourseClick = onOpenCourseClick,
                    onToggleReminder = { lesson, enabled ->
                        if (!enabled) {
                            viewModel.onToggleReminder(lesson.lessonId, false)
                        } else if (!NotificationHelper.hasNotificationPermission(context)) {
                            pendingReminderLessonId = lesson.lessonId
                            reminderPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.onToggleReminder(lesson.lessonId, true)
                        }
                    },
                    onOpenMapsClick = { lesson ->
                        val result = ExternalIntentUtils.openLessonLocationInMaps(
                            context = context,
                            courseName = lesson.courseName,
                            classroom = lesson.classroom,
                            building = lesson.building,
                            locationQuery = lesson.locationQuery
                        )
                        result.messageOrNull()?.let { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message)
                            }
                        }
                    }
                )
            }
        }
    }

    if (showCoursePicker) {
        LessonCoursePickerDialog(
            courses = uiState.availableCourses,
            onCourseSelected = { selectedCourseId ->
                showCoursePicker = false
                onAddLessonClick(selectedCourseId)
            },
            onDismiss = { showCoursePicker = false }
        )
    }
}

@Composable
private fun LessonsContent(
    uiState: LessonsUiState,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (LessonDateFilter) -> Unit,
    onCourseFilterChange: (Int?) -> Unit,
    onSortOptionChange: (LessonSortOption) -> Unit,
    onSelectedDayOfWeekChange: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    onTogglePastThisWeek: () -> Unit,
    onAddLessonClick: () -> Unit,
    onLessonClick: (Int, Int) -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    onToggleReminder: (LessonListItemUi, Boolean) -> Unit,
    onOpenMapsClick: (LessonListItemUi) -> Unit
) {
    val groupedUpcoming = rememberGroupedLessons(uiState.upcomingLessons)

    LazyColumn(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            LessonsAgendaHeader(uiState = uiState)
        }

        item {
            LessonFiltersSection(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onDateFilterChange = onDateFilterChange,
                onCourseFilterChange = onCourseFilterChange,
                onSortOptionChange = onSortOptionChange,
                onSelectedDayOfWeekChange = onSelectedDayOfWeekChange,
                onClearFilters = onClearFilters
            )
        }

        if (!uiState.hasAnyLessons) {
            item {
                LessonEmptyState(
                    title = "Nessuna lezione programmata",
                    message = "Aggiungi una lezione manualmente o importa i dati da UniBo.",
                    primaryActionLabel = "Aggiungi lezione",
                    onPrimaryAction = onAddLessonClick
                )
            }
            return@LazyColumn
        }

        if (uiState.upcomingLessons.isEmpty() && uiState.pastThisWeekLessons.isEmpty()) {
            item {
                LessonEmptyState(
                    title = "Nessuna lezione trovata",
                    message = "Prova a modificare ricerca, giorno, corso o ordinamento.",
                    primaryActionLabel = if (hasActiveLessonFilters(uiState)) {
                        "Cancella filtri"
                    } else {
                        null
                    },
                    onPrimaryAction = if (hasActiveLessonFilters(uiState)) {
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
            LessonDaySection(
                dayTitle = group.dayTitle,
                lessons = group.lessons,
                onEditClick = { lesson -> onLessonClick(lesson.courseId, lesson.lessonId) },
                onOpenMapsClick = onOpenMapsClick,
                onOpenCourseClick = onOpenCourseClick,
                onToggleReminderClick = { lesson ->
                    onToggleReminder(lesson, !lesson.reminderEnabled)
                }
            )
        }

        if (uiState.pastThisWeekLessons.isNotEmpty()) {
            item {
                PastLessonsHeader(
                    isExpanded = uiState.showPastThisWeek,
                    onToggle = onTogglePastThisWeek
                )
            }
        }

        if (uiState.showPastThisWeek) {
            items(
                items = uiState.pastThisWeekLessons,
                key = { lesson -> "past-${lesson.lessonId}" }
            ) { lesson ->
                LessonTimelineItem(
                    lesson = lesson,
                    onEditClick = { onLessonClick(lesson.courseId, lesson.lessonId) },
                    onOpenMapsClick = { onOpenMapsClick(lesson) },
                    onOpenCourseClick = { onOpenCourseClick(lesson.courseId) },
                    onToggleReminderClick = { onToggleReminder(lesson, !lesson.reminderEnabled) }
                )
            }
        }
    }
}

@Composable
private fun PastLessonsHeader(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lezioni già svolte questa settimana",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onToggle) {
                Text(text = if (isExpanded) "Nascondi" else "Mostra")
            }
        }
    }
}

@Composable
private fun LessonCoursePickerDialog(
    courses: List<LessonCourseFilterUi>,
    onCourseSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Scegli corso") },
        text = {
            if (courses.isEmpty()) {
                Text(text = "Nessun corso con lezioni disponibili")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    courses.forEach { course ->
                        TextButton(onClick = { onCourseSelected(course.courseId) }) {
                            Text(text = course.courseName)
                        }
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
private fun LessonsLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

private data class LessonDayGroup(
    val dayTitle: String,
    val lessons: List<LessonListItemUi>
)

@Composable
private fun rememberGroupedLessons(lessons: List<LessonListItemUi>): List<LessonDayGroup> {
    return remember(lessons) {
        lessons
            .groupBy { it.relativeDayLabel }
            .entries
            .sortedBy { (_, groupLessons) ->
                groupLessons.minOf { it.nextOccurrenceMillis }
            }
            .map { (dayTitle, groupLessons) ->
                LessonDayGroup(
                    dayTitle = dayTitle,
                    lessons = groupLessons
                )
            }
    }
}

private fun handleAddLessonClick(
    uiState: LessonsUiState,
    onAddLessonClick: (Int) -> Unit,
    onShowCoursePicker: () -> Unit,
    onNoCoursesAvailable: () -> Unit
) {
    val selectedCourseId = uiState.selectedCourseId
    when {
        selectedCourseId != null -> onAddLessonClick(selectedCourseId)
        uiState.availableCourses.isEmpty() -> onNoCoursesAvailable()
        else -> onShowCoursePicker()
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
