package com.example.unilifeplanner.ui.courses

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.ui.components.UniLifeErrorState
import com.example.unilifeplanner.ui.components.UniLifeLoadingState
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.courses.components.ModernCourseItem

@Composable
fun CoursesScreen(
    viewModel: CourseViewModel = viewModel(),
    onAddCourseClick: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.refreshMessage) {
        uiState.refreshMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearRefreshMessage()
        }
    }

    CoursesScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onStatusFilterChange = viewModel::onStatusFilterChange,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onClearFilters = viewModel::clearFilters,
        onAddCourseClick = onAddCourseClick,
        onCourseClick = onCourseClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onRefreshClick = viewModel::refreshUniboData,
        onMenuClick = onMenuClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoursesScreenContent(
    uiState: CourseUiState,
    snackbarHostState: SnackbarHostState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (CourseStatusFilter) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onSortOptionChange: (CourseSortOption) -> Unit,
    onClearFilters: () -> Unit,
    onAddCourseClick: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onFavoriteClick: (CourseEntity) -> Unit,
    onRefreshClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Corsi ed esami",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator()
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
            FloatingActionButton(onClick = onAddCourseClick) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Aggiungi corso"
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
                uiState.isLoading -> CoursesLoadingState()
                uiState.errorMessage != null -> {
                    CoursesErrorState(message = uiState.errorMessage)
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            CoursesOverviewHeader(uiState = uiState)
                        }

                        item {
                            CoursesFilters(
                                uiState = uiState,
                                onSearchQueryChange = onSearchQueryChange,
                                onStatusFilterChange = onStatusFilterChange,
                                onFavoritesOnlyChange = onFavoritesOnlyChange,
                                onSortOptionChange = onSortOptionChange,
                                onClearFilters = onClearFilters
                            )
                        }

                        if (uiState.filteredCourses.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (hasActiveFilters(uiState)) {
                                        "Risultati della ricerca (${uiState.filteredCourses.size})"
                                    } else {
                                        "Tutti i corsi (${uiState.filteredCourses.size})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                                )
                            }
                        }

                        if (uiState.filteredCourses.isEmpty()) {
                            item {
                                EmptyCoursesState(
                                    hasCourses = uiState.courses.isNotEmpty(),
                                    onAddCourseClick = onAddCourseClick,
                                    onClearFilters = onClearFilters
                                )
                            }
                        } else {
                            items(
                                items = uiState.filteredCourses,
                                key = { course -> course.id }
                            ) { course ->
                                ModernCourseItem(
                                    course = course,
                                    onClick = { onCourseClick(course.id) },
                                    onFavoriteClick = { onFavoriteClick(course) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoursesFilters(
    uiState: CourseUiState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (CourseStatusFilter) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onSortOptionChange: (CourseSortOption) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Modern Pill Search Bar
        androidx.compose.material3.TextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(text = "Cerca corso o docente") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancella ricerca"
                        )
                    }
                }
            },
            singleLine = true,
            shape = CircleShape,
            colors = androidx.compose.material3.TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        // Row 1: Status Chips with counts
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            CourseStatusFilter.entries.forEach { filter ->
                val count = when (filter) {
                    CourseStatusFilter.ALL -> uiState.courses.size
                    CourseStatusFilter.TO_STUDY -> countCoursesByStatus(uiState.courses, CourseStatus.TO_STUDY)
                    CourseStatusFilter.IN_PROGRESS -> countCoursesByStatus(uiState.courses, CourseStatus.IN_PROGRESS)
                    CourseStatusFilter.COMPLETED -> countCoursesByStatus(uiState.courses, CourseStatus.COMPLETED)
                }
                
                FilterChip(
                    selected = uiState.selectedStatusFilter == filter,
                    onClick = { onStatusFilterChange(filter) },
                    label = { Text(text = "${filterLabel(filter)} ($count)") }
                )
            }
        }

        // Row 2: Favorites, Sorting, Clear button
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            FilterChip(
                selected = uiState.showFavoritesOnly,
                onClick = { onFavoritesOnlyChange(!uiState.showFavoritesOnly) },
                label = { Text(text = "Solo preferiti") },
                leadingIcon = {
                    if (uiState.showFavoritesOnly) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )
            
            SortDropdown(
                selectedOption = uiState.selectedSortOption,
                onSortOptionChange = onSortOptionChange
            )
            
            if (hasActiveFilters(uiState)) {
                OutlinedButton(
                    onClick = onClearFilters,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Cancella",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selectedOption: CourseSortOption,
    onSortOptionChange: (CourseSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        FilterChip(
            selected = selectedOption != CourseSortOption.DEFAULT,
            onClick = { expanded = true },
            label = { Text(text = sortLabel(selectedOption)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            CourseSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = sortLabel(option)) },
                    onClick = {
                        onSortOptionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmptyCoursesState(
    hasCourses: Boolean,
    onAddCourseClick: () -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (hasCourses) {
                "Nessun risultato trovato"
            } else {
                "Inizia il tuo percorso"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasCourses) {
                "Modifica i filtri o la ricerca per trovare il corso che cerchi."
            } else {
                "Aggiungi i tuoi corsi per visualizzare il piano di studi ed iniziare a pianificare esami e lezioni."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (hasCourses) {
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Mostra tutti i corsi")
            }
        } else {
            Button(
                onClick = onAddCourseClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Aggiungi corso")
            }
        }
    }
}

@Composable
fun CoursesErrorState(
    message: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        UniLifeErrorState(
            title = "Errore caricamento",
            message = message
        )
    }
}

@Composable
fun CoursesLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        UniLifeLoadingState(message = "Caricamento corsi...")
    }
}

@Composable
private fun CoursesOverviewHeader(
    uiState: CourseUiState,
    modifier: Modifier = Modifier
) {
    val totalC = uiState.courses.size
    val totalCfu = totalCredits(uiState.courses)
    val completedC = countCoursesByStatus(uiState.courses, CourseStatus.COMPLETED)
    val inProgressC = countCoursesByStatus(uiState.courses, CourseStatus.IN_PROGRESS)
    val toStudyC = countCoursesByStatus(uiState.courses, CourseStatus.TO_STUDY)
    val isFiltered = hasActiveFilters(uiState)
    val filteredC = uiState.filteredCourses.size

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Il tuo piano di studi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$totalC corsi • $totalCfu CFU totali",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isFiltered) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = "$filteredC filtrati",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniMetricItem(
                    label = "Da studiare",
                    count = toStudyC,
                    color = statusAccentColor(CourseStatus.TO_STUDY.name),
                    containerColor = statusContainerColor(CourseStatus.TO_STUDY.name),
                    contentColor = statusContentColor(CourseStatus.TO_STUDY.name),
                    modifier = Modifier.weight(1f)
                )
                MiniMetricItem(
                    label = "In corso",
                    count = inProgressC,
                    color = statusAccentColor(CourseStatus.IN_PROGRESS.name),
                    containerColor = statusContainerColor(CourseStatus.IN_PROGRESS.name),
                    contentColor = statusContentColor(CourseStatus.IN_PROGRESS.name),
                    modifier = Modifier.weight(1f)
                )
                MiniMetricItem(
                    label = "Completati",
                    count = completedC,
                    color = statusAccentColor(CourseStatus.COMPLETED.name),
                    containerColor = statusContainerColor(CourseStatus.COMPLETED.name),
                    contentColor = statusContentColor(CourseStatus.COMPLETED.name),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MiniMetricItem(
    label: String,
    count: Int,
    color: Color,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// UI Helper functions
private fun hasActiveFilters(uiState: CourseUiState): Boolean {
    return uiState.searchQuery.isNotEmpty() ||
        uiState.selectedStatusFilter != CourseStatusFilter.ALL ||
        uiState.showFavoritesOnly ||
        uiState.selectedSortOption != CourseSortOption.DEFAULT
}

private fun countCoursesByStatus(courses: List<CourseEntity>, status: CourseStatus): Int {
    return courses.count { it.status == status.name }
}

private fun totalCredits(courses: List<CourseEntity>): Int {
    return courses.sumOf { it.credits }
}

private fun courseInitials(name: String): String {
    val words = name.trim().split(Regex("\\s+"))
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(2).uppercase()
        else -> {
            val first = words[0].take(1)
            val second = words[1].take(1)
            (first + second).uppercase()
        }
    }
}

@Composable
private fun statusAccentColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.primary
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.secondary
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
private fun statusContainerColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.primaryContainer
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.secondaryContainer
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun statusContentColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.onPrimaryContainer
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.onSecondaryContainer
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun filterLabel(filter: CourseStatusFilter): String {
    return when (filter) {
        CourseStatusFilter.ALL -> "Tutti"
        CourseStatusFilter.TO_STUDY -> "Da studiare"
        CourseStatusFilter.IN_PROGRESS -> "In corso"
        CourseStatusFilter.COMPLETED -> "Completati"
    }
}

private fun sortLabel(option: CourseSortOption): String {
    return when (option) {
        CourseSortOption.DEFAULT -> "Ordine standard"
        CourseSortOption.CREDITS_DESC -> "CFU"
        CourseSortOption.NAME_ASC -> "Nome A-Z"
    }
}
