package com.example.unilifeplanner.ui.courses.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.data.local.CourseEntity
import com.example.unilifeplanner.domain.model.CourseStatus
import com.example.unilifeplanner.ui.components.InfoPill
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.example.unilifeplanner.university.publicimport.formatStudyYearLabel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ModernCourseItem(
    course: CourseEntity,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            // Left color stripe indicating status
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusAccentColor(course.status))
            )
            
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circle with initials
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(statusContainerColor(course.status)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = courseInitials(course.name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusContentColor(course.status)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = course.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = course.professor,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Info pills
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusPill(status = course.status)
                        if (course.sourceProvider == "UNIBO_PUBLIC") {
                            InfoPill(text = "UniBo")
                        }
                        course.studyYear?.let { studyYear ->
                            InfoPill(text = formatStudyYearLabel(studyYear))
                        }
                        InfoPill(text = "${course.credits} CFU")
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Favorite Button inside circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (course.isFavorite) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        )
                        .clickable(onClick = onFavoriteClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (course.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = if (course.isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                        tint = if (course.isFavorite) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    InfoPill(
        text = formatCourseStatus(status),
        containerColor = statusContainerColor(status),
        contentColor = statusContentColor(status)
    )
}

// Helper UI functions
internal fun courseInitials(name: String): String {
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
internal fun statusAccentColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.primary
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.secondary
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
}

@Composable
internal fun statusContainerColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.primaryContainer
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.secondaryContainer
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
internal fun statusContentColor(status: String): Color {
    return when (status) {
        CourseStatus.COMPLETED.name -> MaterialTheme.colorScheme.onPrimaryContainer
        CourseStatus.IN_PROGRESS.name -> MaterialTheme.colorScheme.onSecondaryContainer
        CourseStatus.TO_STUDY.name -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

internal fun formatCourseStatus(status: String): String {
    return when (status) {
        CourseStatus.TO_STUDY.name -> "Da studiare"
        CourseStatus.IN_PROGRESS.name -> "In corso"
        CourseStatus.COMPLETED.name -> "Completato"
        else -> status
    }
}

@Preview(showBackground = true)
@Composable
private fun ModernCourseItemPreview() {
    UniLifePlannerTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Standard completed course
            ModernCourseItem(
                course = CourseEntity(
                    id = 1,
                    name = "Analisi Matematica I",
                    professor = "Prof. De Marco",
                    credits = 12,
                    status = CourseStatus.COMPLETED.name,
                    isFavorite = true,
                    createdAt = 0,
                    updatedAt = 0
                ),
                onClick = {},
                onFavoriteClick = {}
            )
            
            // UniBo in progress course
            ModernCourseItem(
                course = CourseEntity(
                    id = 2,
                    name = "Programmazione Mobile",
                    professor = "Prof. Rossi Luca Mario",
                    credits = 6,
                    status = CourseStatus.IN_PROGRESS.name,
                    isFavorite = false,
                    sourceProvider = "UNIBO_PUBLIC",
                    studyYear = 2,
                    createdAt = 0,
                    updatedAt = 0
                ),
                onClick = {},
                onFavoriteClick = {}
            )
            
            // Course to study with long name and multiple professors
            ModernCourseItem(
                course = CourseEntity(
                    id = 3,
                    name = "Progettazione di Sistemi Software e Servizi Cloud ad Alta Affidabilità",
                    professor = "Prof. Bianchi, Prof. Verdi, Prof. Neri",
                    credits = 9,
                    status = CourseStatus.TO_STUDY.name,
                    isFavorite = false,
                    createdAt = 0,
                    updatedAt = 0
                ),
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ModernCourseItemDarkPreview() {
    UniLifePlannerTheme(themeMode = com.example.unilifeplanner.domain.model.ThemeMode.DARK) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            ModernCourseItem(
                course = CourseEntity(
                    id = 2,
                    name = "Programmazione Mobile",
                    professor = "Prof. Rossi",
                    credits = 6,
                    status = CourseStatus.IN_PROGRESS.name,
                    isFavorite = true,
                    sourceProvider = "UNIBO_PUBLIC",
                    studyYear = 2,
                    createdAt = 0,
                    updatedAt = 0
                ),
                onClick = {},
                onFavoriteClick = {}
            )
        }
    }
}
