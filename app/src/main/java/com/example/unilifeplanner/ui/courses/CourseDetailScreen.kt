package com.example.unilifeplanner.ui.courses

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.courses.components.courseInitials
import com.example.unilifeplanner.ui.courses.components.formatCourseStatus
import com.example.unilifeplanner.ui.courses.components.statusAccentColor
import com.example.unilifeplanner.ui.courses.components.statusContainerColor
import com.example.unilifeplanner.ui.courses.components.statusContentColor
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.example.unilifeplanner.ui.utils.ExternalIntentResult
import com.example.unilifeplanner.ui.utils.ExternalIntentUtils
import com.example.unilifeplanner.university.publicimport.formatStudyYearLabel
import kotlinx.coroutines.launch

@Composable
fun CourseDetailScreen(
    courseId: Int,
    viewModel: CourseViewModel = viewModel(),
    onEditCourseClick: () -> Unit,
    onOpenCourseLessonsClick: (Int) -> Unit,
    onOpenCourseExamsClick: (Int) -> Unit,
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
        onOpenCourseLessonsClick = onOpenCourseLessonsClick,
        onOpenCourseExamsClick = onOpenCourseExamsClick,
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
    onOpenCourseLessonsClick: (Int) -> Unit,
    onOpenCourseExamsClick: (Int) -> Unit,
    onToggleFavorite: (CourseEntity) -> Unit,
    onDeleteCourse: (CourseEntity) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
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
                    onEditCourseClick = onEditCourseClick,
                    onDeleteClick = { showDeleteDialog = true },
                    onOpenCourseLessonsClick = onOpenCourseLessonsClick,
                    onOpenCourseExamsClick = onOpenCourseExamsClick,
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
            text = { Text(text = "Questa azione non può essere annullata.") },
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
    onOpenCourseLessonsClick: (Int) -> Unit,
    onOpenCourseExamsClick: (Int) -> Unit,
    onExternalActionMessage: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 20.dp,
            end = 20.dp,
            bottom = 32.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseDetailHero(course = course)
        }

        item {
            CourseDetailMetrics(course = course)
        }

        item {
            if (course.status == CourseStatus.COMPLETED.name) {
                CompletedCourseBanner()
            } else {
                CourseDetailNavigationSection(
                    courseId = course.id,
                    onOpenCourseExamsClick = onOpenCourseExamsClick,
                    onOpenCourseLessonsClick = onOpenCourseLessonsClick
                )
            }
        }

        item {
            CourseNotesSection(notes = course.notes)
        }

        item {
            CourseQuickActionsSection(
                course = course,
                onExternalActionMessage = onExternalActionMessage
            )
        }

        item {
            CourseDangerAndEditSection(
                onEditCourseClick = onEditCourseClick,
                onDeleteClick = onDeleteClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CourseDetailHero(
    course: CourseEntity,
    modifier: Modifier = Modifier
) {
    val containerColor = statusContainerColor(course.status).copy(alpha = 0.2f)
    val accentColor = statusAccentColor(course.status)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Initials Circle avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(statusContainerColor(course.status)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = courseInitials(course.name),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusContentColor(course.status)
                    )
                }

                // Course Name
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Docente
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Docente: ${course.professor}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // FlowRow of pills
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                com.example.unilifeplanner.ui.components.InfoPill(
                    text = formatCourseStatus(course.status),
                    containerColor = statusContainerColor(course.status),
                    contentColor = statusContentColor(course.status)
                )
                if (course.sourceProvider == "UNIBO_PUBLIC") {
                    com.example.unilifeplanner.ui.components.InfoPill(text = "UniBo")
                }
                course.studyYear?.let { studyYear ->
                    com.example.unilifeplanner.ui.components.InfoPill(text = formatStudyYearLabel(studyYear))
                }
                com.example.unilifeplanner.ui.components.InfoPill(text = "${course.credits} CFU")
            }
        }
    }
}

@Composable
private fun CourseDetailMetrics(
    course: CourseEntity,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CourseDetailMetricItem(
                label = "Crediti",
                value = "${course.credits} CFU",
                icon = Icons.Default.School,
                modifier = Modifier.weight(1f)
            )
            CourseDetailMetricItem(
                label = "Anno di corso",
                value = course.studyYear?.let { formatStudyYearLabel(it) } ?: "Nessuno",
                icon = Icons.Default.DateRange,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CourseDetailMetricItem(
                label = "Origine",
                value = if (course.sourceProvider == "UNIBO_PUBLIC") "UniBo" else "Manuale",
                icon = Icons.Default.Info,
                modifier = Modifier.weight(1f)
            )
            CourseDetailMetricItem(
                label = "Preferito",
                value = if (course.isFavorite) "Sì" else "No",
                icon = if (course.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                iconColor = if (course.isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CourseDetailMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CourseDetailNavigationSection(
    courseId: Int,
    onOpenCourseExamsClick: (Int) -> Unit,
    onOpenCourseLessonsClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CourseNavigationTile(
            title = "Esami",
            subtitle = "Appelli collegati a questo corso",
            icon = Icons.Default.EventNote,
            accentColor = MaterialTheme.colorScheme.primary,
            onClick = { onOpenCourseExamsClick(courseId) }
        )
        CourseNavigationTile(
            title = "Lezioni",
            subtitle = "Orario e lezioni settimanali",
            icon = Icons.Default.Schedule,
            accentColor = MaterialTheme.colorScheme.secondary,
            onClick = { onOpenCourseLessonsClick(courseId) }
        )
    }
}

@Composable
private fun CourseNavigationTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Vai a $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CompletedCourseBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Corso completato",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Le lezioni e gli appelli futuri non vengono più mostrati nelle sezioni Lezioni ed Esami.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CourseNotesSection(
    notes: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Note",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = notes?.takeIf { it.isNotBlank() } ?: "Nessuna nota inserita",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (notes.isNullOrBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun CourseQuickActionsSection(
    course: CourseEntity,
    onExternalActionMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Azioni rapide",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            modifier = Modifier.padding(start = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CourseQuickActionButton(
                title = "Condividi",
                icon = Icons.Default.Share,
                onClick = {
                    ExternalIntentUtils.shareCourse(context, course)
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.weight(1f)
            )
            CourseQuickActionButton(
                title = "Email docente",
                icon = Icons.Default.Email,
                onClick = {
                    ExternalIntentUtils.sendEmailToProfessor(
                        context = context,
                        email = null,
                        courseName = course.name
                    )
                        .messageOrNull()
                        ?.let(onExternalActionMessage)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CourseQuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CourseDangerAndEditSection(
    onEditCourseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onEditCourseClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Modifica corso",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Modifica corso")
        }
        
        TextButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Elimina corso",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Elimina corso")
        }
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

private fun ExternalIntentResult.messageOrNull(): String? {
    return when (this) {
        ExternalIntentResult.Success -> null
        is ExternalIntentResult.MissingData -> message
        is ExternalIntentResult.NoCompatibleApp -> message
        is ExternalIntentResult.Error -> message
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreviewToStudy() {
    UniLifePlannerTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            CourseDetailHero(
                course = CourseEntity(
                    id = 1,
                    name = "Programmazione Web",
                    professor = "Prof. Verdi Antonio",
                    credits = 6,
                    status = CourseStatus.TO_STUDY.name,
                    isFavorite = false,
                    sourceProvider = "UNIBO_PUBLIC",
                    studyYear = 1,
                    createdAt = 0,
                    updatedAt = 0
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreviewInProgressFavorite() {
    UniLifePlannerTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val course = CourseEntity(
                    id = 2,
                    name = "Programmazione Mobile",
                    professor = "Prof. Rossi Luca",
                    credits = 8,
                    status = CourseStatus.IN_PROGRESS.name,
                    isFavorite = true,
                    studyYear = 2,
                    createdAt = 0,
                    updatedAt = 0
                )
                CourseDetailHero(course = course)
                CourseDetailMetrics(course = course)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreviewCompleted() {
    UniLifePlannerTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val course = CourseEntity(
                    id = 3,
                    name = "Sistemi Operativi",
                    professor = "Prof. Neri Francesco",
                    credits = 12,
                    status = CourseStatus.COMPLETED.name,
                    isFavorite = false,
                    studyYear = 3,
                    createdAt = 0,
                    updatedAt = 0
                )
                CourseDetailHero(course = course)
                CourseDetailMetrics(course = course)
                CompletedCourseBanner()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreviewLongName() {
    UniLifePlannerTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            CourseDetailHero(
                course = CourseEntity(
                    id = 4,
                    name = "Laboratorio di Progettazione di Sistemi Software ad Alta Affidabilità e Sicurezza Critica",
                    professor = "Prof. Bianchi, Prof. Rossi, Prof. Verdi, Prof. Viola",
                    credits = 9,
                    status = CourseStatus.TO_STUDY.name,
                    isFavorite = true,
                    sourceProvider = "UNIBO_PUBLIC",
                    studyYear = 2,
                    createdAt = 0,
                    updatedAt = 0
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CourseDetailPreviewDarkMode() {
    UniLifePlannerTheme(themeMode = com.example.unilifeplanner.domain.model.ThemeMode.DARK) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                val course = CourseEntity(
                    id = 5,
                    name = "Analisi Matematica I",
                    professor = "Prof. Gialli Beatrice",
                    credits = 12,
                    status = CourseStatus.IN_PROGRESS.name,
                    isFavorite = true,
                    studyYear = 1,
                    createdAt = 0,
                    updatedAt = 0
                )
                CourseDetailHero(course = course)
                CourseDetailMetrics(course = course)
                CourseDetailNavigationSection(
                    courseId = course.id,
                    onOpenCourseExamsClick = {},
                    onOpenCourseLessonsClick = {}
                )
            }
        }
    }
}
