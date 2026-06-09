package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.exams.ExamCourseOptionUi
import com.example.unilifeplanner.ui.exams.ExamDateFilter
import com.example.unilifeplanner.ui.exams.ExamSortOption
import com.example.unilifeplanner.ui.exams.ExamsUiState

@Composable
fun ExamFiltersSection(
    uiState: ExamsUiState,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (ExamDateFilter) -> Unit,
    onCourseFilterChange: (Int?) -> Unit,
    onSortOptionChange: (ExamSortOption) -> Unit,
    onSelectedExamDayChange: (Long?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ExamSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange
        )

        ExamsCalendarStrip(
            examCountByDay = uiState.examCountByDay,
            selectedExamDayMillis = uiState.selectedExamDayMillis,
            onDaySelected = onSelectedExamDayChange
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            ExamDateFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedDateFilter == filter,
                    onClick = { onDateFilterChange(filter) },
                    label = { Text(text = examDateFilterLabel(filter)) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            ExamCourseDropdown(
                courses = uiState.availableCourses,
                selectedCourseName = uiState.selectedCourseName,
                onCourseSelected = onCourseFilterChange,
                modifier = Modifier.weight(1f)
            )
            ExamSortDropdown(
                selectedOption = uiState.selectedSortOption,
                onSortOptionChange = onSortOptionChange
            )
        }

        if (hasActiveExamFilters(uiState)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onClearFilters) {
                    Text(text = "Cancella filtri")
                }
            }
        }
    }
}

@Composable
private fun ExamCourseDropdown(
    courses: List<ExamCourseOptionUi>,
    selectedCourseName: String?,
    onCourseSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedCourseName ?: "Tutti i corsi",
                maxLines = 1
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text(text = "Tutti i corsi") },
                onClick = {
                    onCourseSelected(null)
                    expanded = false
                }
            )
            courses.forEach { course ->
                DropdownMenuItem(
                    text = { Text(text = course.courseName) },
                    onClick = {
                        onCourseSelected(course.courseId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ExamSortDropdown(
    selectedOption: ExamSortOption,
    onSortOptionChange: (ExamSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(text = examSortLabel(selectedOption))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ExamSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = examSortLabel(option)) },
                    onClick = {
                        onSortOptionChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
