package com.example.unilifeplanner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onOpenCourses: () -> Unit,
    onAddCourse: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onOpenCourses = onOpenCourses,
        onAddCourse = onAddCourse,
        onOpenStatistics = onOpenStatistics,
        onOpenMap = onOpenMap,
        onOpenProfile = onOpenProfile,
        onOpenSettings = onOpenSettings,
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    uiState: HomeSummaryUiState,
    onOpenCourses: () -> Unit,
    onAddCourse: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val closeDrawerAndRun: (() -> Unit) -> Unit = { action ->
        coroutineScope.launch {
            drawerState.close()
            action()
        }
    }

    BackHandler(enabled = drawerState.isOpen) {
        coroutineScope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeNavigationDrawer(
                onHomeClick = {
                    coroutineScope.launch {
                        drawerState.close()
                    }
                },
                onOpenCourses = { closeDrawerAndRun(onOpenCourses) },
                onAddCourse = { closeDrawerAndRun(onAddCourse) },
                onOpenStatistics = { closeDrawerAndRun(onOpenStatistics) },
                onOpenMap = { closeDrawerAndRun(onOpenMap) },
                onOpenProfile = { closeDrawerAndRun(onOpenProfile) },
                onOpenSettings = { closeDrawerAndRun(onOpenSettings) },
                onLogout = { closeDrawerAndRun(onLogout) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "UniLife Planner")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Apri menu"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    WelcomeSection(studentName = uiState.studentName)
                }
                if (uiState.totalCourses == 0) {
                    item {
                        EmptyStateCard(
                            title = "Nessun corso registrato",
                            message = "Apri il menu laterale e scegli Aggiungi corso per iniziare a organizzare il tuo piano di studi."
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
}

@Composable
private fun HomeNavigationDrawer(
    onHomeClick: () -> Unit,
    onOpenCourses: () -> Unit,
    onAddCourse: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
        ) {
            Text(
                text = "UniLife Planner",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Menu navigazione",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Versione 1.0.1",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        HomeDrawerItem(
            label = "Home",
            icon = Icons.Filled.Home,
            selected = true,
            onClick = onHomeClick
        )
        HomeDrawerItem(
            label = "Corsi",
            icon = Icons.Filled.MenuBook,
            onClick = onOpenCourses
        )
        HomeDrawerItem(
            label = "Aggiungi corso",
            icon = Icons.Filled.Add,
            onClick = onAddCourse
        )
        HomeDrawerItem(
            label = "Statistiche",
            icon = Icons.Filled.BarChart,
            onClick = onOpenStatistics
        )
        HomeDrawerItem(
            label = "Mappa",
            icon = Icons.Filled.Map,
            onClick = onOpenMap
        )
        HomeDrawerItem(
            label = "Profilo",
            icon = Icons.Filled.AccountCircle,
            onClick = onOpenProfile
        )
        HomeDrawerItem(
            label = "Impostazioni",
            icon = Icons.Filled.Settings,
            onClick = onOpenSettings
        )
        HomeDrawerItem(
            label = "Logout",
            icon = Icons.Filled.Logout,
            onClick = onLogout
        )
    }
}

@Composable
private fun HomeDrawerItem(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
        }
    )
}

@Composable
fun WelcomeSection(
    studentName: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Ciao, $studentName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    icon = Icons.Filled.MenuBook,
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
                studentName = "studente",
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
            onOpenCourses = {},
            onAddCourse = {},
            onOpenStatistics = {},
            onOpenMap = {},
            onOpenProfile = {},
            onOpenSettings = {},
            onLogout = {}
        )
    }
}
