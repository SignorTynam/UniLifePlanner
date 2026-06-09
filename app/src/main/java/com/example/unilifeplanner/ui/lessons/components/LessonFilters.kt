package com.example.unilifeplanner.ui.lessons.components

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
import com.example.unilifeplanner.ui.lessons.LessonCourseFilterUi
import com.example.unilifeplanner.ui.lessons.LessonDateFilter
import com.example.unilifeplanner.ui.lessons.LessonSortOption
import com.example.unilifeplanner.ui.lessons.LessonsUiState

@Composable
fun LessonFiltersSection(
    uiState: LessonsUiState,
    onSearchQueryChange: (String) -> Unit,
    onDateFilterChange: (LessonDateFilter) -> Unit,
    onCourseFilterChange: (Int?) -> Unit,
    onSortOptionChange: (LessonSortOption) -> Unit,
    onSelectedDayOfWeekChange: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LessonSearchBar(
            query = uiState.searchQuery,
            onQueryChange = onSearchQueryChange
        )

        LessonsWeekCalendar(
            lessonCountByDay = uiState.lessonCountByDayOfWeek,
            selectedDayOfWeek = uiState.selectedDayOfWeek,
            onDaySelected = onSelectedDayOfWeekChange
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            listOf(
                LessonDateFilter.ALL,
                LessonDateFilter.TODAY,
                LessonDateFilter.TOMORROW,
                LessonDateFilter.THIS_WEEK,
                LessonDateFilter.REMINDER_ENABLED
            ).forEach { filter ->
                FilterChip(
                    selected = uiState.selectedDateFilter == filter,
                    onClick = { onDateFilterChange(filter) },
                    label = { Text(text = dateFilterLabel(filter)) }
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            CourseDropdown(
                courses = uiState.availableCourses,
                selectedCourseName = uiState.selectedCourseName,
                onCourseSelected = onCourseFilterChange,
                modifier = Modifier.weight(1f)
            )
            SortDropdown(
                selectedOption = uiState.selectedSortOption,
                onSortOptionChange = onSortOptionChange
            )
        }

        if (hasActiveLessonFilters(uiState)) {
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
private fun CourseDropdown(
    courses: List<LessonCourseFilterUi>,
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
private fun SortDropdown(
    selectedOption: LessonSortOption,
    onSortOptionChange: (LessonSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(text = sortLabel(selectedOption))
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LessonSortOption.entries.forEach { option ->
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

private fun dateFilterLabel(filter: LessonDateFilter): String {
    return when (filter) {
        LessonDateFilter.ALL -> "Tutte"
        LessonDateFilter.TODAY -> "Oggi"
        LessonDateFilter.TOMORROW -> "Domani"
        LessonDateFilter.THIS_WEEK -> "Questa settimana"
        LessonDateFilter.REMINDER_ENABLED -> "Promemoria"
    }
}

private fun sortLabel(option: LessonSortOption): String {
    return when (option) {
        LessonSortOption.NEXT_UPCOMING -> "Più vicine"
        LessonSortOption.DAY_AND_TIME -> "Giorno e orario"
        LessonSortOption.COURSE_NAME_ASC -> "Corso A-Z"
    }
}
