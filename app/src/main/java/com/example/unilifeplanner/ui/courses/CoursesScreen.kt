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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.example.unilifeplanner.ui.courses.components.CourseCard

@Composable
fun CoursesScreen(
    viewModel: CourseViewModel = viewModel(),
    onAddCourseClick: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CoursesScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onStatusFilterChange = viewModel::onStatusFilterChange,
        onFavoritesOnlyChange = viewModel::onFavoritesOnlyChange,
        onSortOptionChange = viewModel::onSortOptionChange,
        onClearFilters = viewModel::clearFilters,
        onAddCourseClick = onAddCourseClick,
        onCourseClick = onCourseClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoursesScreenContent(
    uiState: CourseUiState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterChange: (CourseStatusFilter) -> Unit,
    onFavoritesOnlyChange: (Boolean) -> Unit,
    onSortOptionChange: (CourseSortOption) -> Unit,
    onClearFilters: () -> Unit,
    onAddCourseClick: () -> Unit,
    onCourseClick: (Int) -> Unit,
    onFavoriteClick: (CourseEntity) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Corsi ed esami")
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Text(text = "Back")
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
        }
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
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
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
                                CourseCard(
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
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text(text = "Cerca") },
            placeholder = { Text(text = "Nome, docente o aula") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cancella ricerca"
                        )
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            CourseStatusFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedStatusFilter == filter,
                    onClick = { onStatusFilterChange(filter) },
                    label = { Text(text = filterLabel(filter)) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = uiState.showFavoritesOnly,
                onClick = { onFavoritesOnlyChange(!uiState.showFavoritesOnly) },
                label = { Text(text = "Solo preferiti") }
            )
            SortDropdown(
                selectedOption = uiState.selectedSortOption,
                onSortOptionChange = onSortOptionChange
            )
            OutlinedButton(onClick = onClearFilters) {
                Text(text = "Cancella filtri")
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
        OutlinedButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.Sort,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(text = sortLabel(selectedOption))
        }
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
            .padding(vertical = 48.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(0.18f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (hasCourses) {
                "Nessun corso trovato con i filtri selezionati."
            } else {
                "Nessun corso aggiunto"
            },
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (hasCourses) {
                "Modifica ricerca, stato, preferiti o ordinamento."
            } else {
                "Premi + per creare il tuo primo corso."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (hasCourses) {
            OutlinedButton(onClick = onClearFilters) {
                Text(text = "Cancella filtri")
            }
        } else {
            Button(onClick = onAddCourseClick) {
                Text(text = "Aggiungi corso")
            }
        }
    }
}

@Composable
fun CoursesErrorState(
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Si e verificato un errore durante il caricamento dei corsi.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CoursesLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
        CourseSortOption.EXAM_DATE_ASC -> "Data esame"
        CourseSortOption.NAME_ASC -> "Nome A-Z"
    }
}
