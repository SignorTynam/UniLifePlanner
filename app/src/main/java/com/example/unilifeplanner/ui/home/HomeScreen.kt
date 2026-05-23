package com.example.unilifeplanner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.UniLifeProfileAvatar
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onMenuClick = onMenuClick
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeSummaryUiState,
    onMenuClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "UniLife Planner",
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeSection(
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    profileImageUri = uiState.profileImageUri
                )
            }
            if (uiState.totalCourses == 0) {
                item {
                    EmptyStateCard(
                        title = "Nessun corso registrato",
                        message = "Apri il menu laterale e scegli Corsi per aggiungere il tuo primo corso."
                    )
                }
            }
            item {
                NextExamCard(nextExam = uiState.nextExam)
            }
            item {
                StudySummaryCard(uiState = uiState)
            }
            item {
                FavoriteCoursesSection(favoriteCourses = uiState.favoriteCourses)
            }
        }
    }
}

@Composable
fun WelcomeSection(
    firstName: String,
    lastName: String,
    profileImageUri: String?,
    modifier: Modifier = Modifier
) {
    val displayName = listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "studente" }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UniLifeProfileAvatar(
                profileImageUri = profileImageUri,
                size = 64.dp,
                modifier = Modifier.size(64.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Ciao, $displayName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Organizza corsi, esami e scadenze in un unico posto.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun StudySummaryCard(
    uiState: HomeSummaryUiState,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Riepilogo studio",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryItem(
                    label = "Totali",
                    value = uiState.totalCourses.toString(),
                    icon = Icons.Filled.School,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Completati",
                    value = uiState.completedCourses.toString(),
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryItem(
                    label = "In corso",
                    value = uiState.inProgressCourses.toString(),
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Da studiare",
                    value = uiState.toStudyCourses.toString(),
                    icon = Icons.Filled.PendingActions,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NextExamCard(
    nextExam: NextExamUi?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Prossimo esame",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (nextExam == null) {
                EmptyStateCard(
                    title = "Nessun esame programmato",
                    message = "Aggiungi un corso con una data d'esame per visualizzarlo qui."
                )
            } else {
                Text(
                    text = nextExam.courseName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Data: ${nextExam.examDate}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Aula: ${nextExam.classroom}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(onClick = {}) {
                    Text(text = nextExam.status)
                }
            }
        }
    }
}

@Composable
fun FavoriteCoursesSection(
    favoriteCourses: List<FavoriteCourseUi>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Preferiti",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (favoriteCourses.isEmpty()) {
            EmptyStateCard(
                title = "Nessun corso preferito",
                message = "Tocca la stellina su un corso per salvarlo tra i preferiti."
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                favoriteCourses.forEach { course ->
                    FavoriteCourseItem(course = course)
                }
            }
        }
    }
}

@Composable
fun FavoriteCourseItem(
    course: FavoriteCourseUi,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = course.professor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                course.examDate?.let { examDate ->
                    Text(
                        text = "Esame: $examDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    UniLifePlannerTheme {
        HomeScreenContent(
            uiState = HomeSummaryUiState(
                firstName = "Mario",
                lastName = "Rossi",
                profileImageUri = null,
                totalCourses = 6,
                completedCourses = 2,
                inProgressCourses = 3,
                toStudyCourses = 1,
                nextExam = NextExamUi(
                    courseName = "Algoritmi e strutture dati",
                    examDate = "24 giugno 2026",
                    classroom = "Aula B2",
                    status = "Da preparare"
                ),
                favoriteCourses = listOf(
                    FavoriteCourseUi(
                        id = 1,
                        name = "Analisi matematica",
                        professor = "Prof. Rossi",
                        examDate = "12 luglio 2026"
                    ),
                    FavoriteCourseUi(
                        id = 2,
                        name = "Basi di dati",
                        professor = "Prof.ssa Verdi",
                        examDate = "Da definire"
                    )
                )
            ),
            onMenuClick = {}
        )
    }
}
