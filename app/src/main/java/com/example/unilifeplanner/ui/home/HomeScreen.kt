package com.example.unilifeplanner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.home.components.HomeFavoriteCoursesSection
import com.example.unilifeplanner.ui.home.components.HomeHeroHeader
import com.example.unilifeplanner.ui.home.components.HomeInsightsSection
import com.example.unilifeplanner.ui.home.components.HomeOnboardingEmptyState
import com.example.unilifeplanner.ui.home.components.HomeProgressOverview
import com.example.unilifeplanner.ui.home.components.HomeQuickActionsSection
import com.example.unilifeplanner.ui.home.components.HomeTodayAgendaSection
import com.example.unilifeplanner.ui.home.components.HomeUpcomingSection
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onOpenCoursesClick: () -> Unit,
    onOpenLessonsClick: () -> Unit,
    onOpenExamsClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
    onOpenUniboImportClick: () -> Unit,
    onOpenProfileClick: () -> Unit,
    onOpenCourseClick: (Int) -> Unit,
    homeViewModel: HomeViewModel = viewModel()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onMenuClick = onMenuClick,
        onOpenCoursesClick = onOpenCoursesClick,
        onOpenLessonsClick = onOpenLessonsClick,
        onOpenExamsClick = onOpenExamsClick,
        onOpenStatisticsClick = onOpenStatisticsClick,
        onOpenUniboImportClick = onOpenUniboImportClick,
        onOpenProfileClick = onOpenProfileClick,
        onOpenCourseClick = onOpenCourseClick
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeSummaryUiState,
    onMenuClick: () -> Unit,
    onOpenCoursesClick: () -> Unit,
    onOpenLessonsClick: () -> Unit,
    onOpenExamsClick: () -> Unit,
    onOpenStatisticsClick: () -> Unit,
    onOpenUniboImportClick: () -> Unit,
    onOpenProfileClick: () -> Unit,
    onOpenCourseClick: (Int) -> Unit
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
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeHeroHeader(
                    firstName = uiState.firstName,
                    lastName = uiState.lastName,
                    profileImageUri = uiState.profileImageUri,
                    todayCommitmentsCount = uiState.todayCommitmentsCount
                )
            }

            if (uiState.totalCourses == 0) {
                item {
                    HomeOnboardingEmptyState(
                        onOpenUniboImportClick = onOpenUniboImportClick,
                        onOpenCoursesClick = onOpenCoursesClick
                    )
                }
                item {
                    HomeQuickActionsSection(
                        onOpenCoursesClick = onOpenCoursesClick,
                        onOpenLessonsClick = onOpenLessonsClick,
                        onOpenExamsClick = onOpenExamsClick,
                        onOpenStatisticsClick = onOpenStatisticsClick,
                        onOpenUniboImportClick = onOpenUniboImportClick,
                        onOpenProfileClick = onOpenProfileClick
                    )
                }
            } else {
                item {
                    HomeTodayAgendaSection(
                        todayLessons = uiState.todayLessons,
                        todayExams = uiState.todayExams,
                        todayCommitmentsCount = uiState.todayCommitmentsCount
                    )
                }
                item {
                    HomeUpcomingSection(
                        nextLesson = uiState.nextLesson,
                        nextExam = uiState.nextExam,
                        onOpenLessonsClick = onOpenLessonsClick,
                        onOpenExamsClick = onOpenExamsClick
                    )
                }
                item {
                    HomeProgressOverview(
                        totalCourses = uiState.totalCourses,
                        completedCourses = uiState.completedCourses,
                        inProgressCourses = uiState.inProgressCourses,
                        toStudyCourses = uiState.toStudyCourses,
                        favoriteCourseCount = uiState.favoriteCourseCount,
                        totalCredits = uiState.totalCredits,
                        completedCredits = uiState.completedCredits,
                        completionPercentage = uiState.completionPercentage
                    )
                }
                item {
                    HomeQuickActionsSection(
                        onOpenCoursesClick = onOpenCoursesClick,
                        onOpenLessonsClick = onOpenLessonsClick,
                        onOpenExamsClick = onOpenExamsClick,
                        onOpenStatisticsClick = onOpenStatisticsClick,
                        onOpenUniboImportClick = onOpenUniboImportClick,
                        onOpenProfileClick = onOpenProfileClick
                    )
                }
                item {
                    HomeFavoriteCoursesSection(
                        favoriteCourses = uiState.favoriteCourses,
                        onOpenCourseClick = onOpenCourseClick,
                        onOpenCoursesClick = onOpenCoursesClick
                    )
                }
                item {
                    HomeInsightsSection(insights = uiState.insights)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenFullPreview() {
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
                favoriteCourseCount = 2,
                totalCredits = 48,
                completedCredits = 18,
                completionPercentage = 37,
                todayLessonCount = 2,
                todayExamCount = 1,
                todayCommitmentsCount = 3,
                todayLessons = listOf(
                    HomeLessonPreviewUi(
                        lessonId = 1,
                        courseId = 1,
                        courseName = "Basi di dati",
                        timeLabel = "09:00",
                        location = "Aula B2"
                    ),
                    HomeLessonPreviewUi(
                        lessonId = 2,
                        courseId = 2,
                        courseName = "Sistemi operativi",
                        timeLabel = "14:00",
                        location = "Lab 3"
                    )
                ),
                todayExams = listOf(
                    HomeExamPreviewUi(
                        examAppealId = 1,
                        courseId = 3,
                        courseName = "Analisi matematica",
                        dateTimeLabel = "11:00",
                        reminderEnabled = true
                    )
                ),
                nextExam = NextExamUi(
                    examAppealId = 1,
                    courseId = 3,
                    courseName = "Algoritmi e strutture dati",
                    examDate = "24 giugno 2026 09:00",
                    relativeDateLabel = "Domani",
                    status = "Promemoria attivo",
                    reminderEnabled = true
                ),
                nextLesson = NextLessonUi(
                    lessonId = 1,
                    courseId = 1,
                    courseName = "Basi di dati",
                    dayAndTime = "Domani, 09:00",
                    relativeDayLabel = "Domani",
                    location = "Aula B2 - Polo Fibonacci"
                ),
                favoriteCourses = listOf(
                    FavoriteCourseUi(
                        id = 1,
                        name = "Analisi matematica",
                        professor = "Prof. Rossi",
                        examDate = "12 luglio 2026 09:00"
                    ),
                    FavoriteCourseUi(
                        id = 2,
                        name = "Basi di dati",
                        professor = "Prof.ssa Verdi",
                        examDate = null
                    )
                ),
                insights = listOf(
                    "Hai 3 impegni oggi.",
                    "Il prossimo esame è Algoritmi e strutture dati (Domani).",
                    "Hai 1 corso ancora da iniziare."
                )
            ),
            onMenuClick = {},
            onOpenCoursesClick = {},
            onOpenLessonsClick = {},
            onOpenExamsClick = {},
            onOpenStatisticsClick = {},
            onOpenUniboImportClick = {},
            onOpenProfileClick = {},
            onOpenCourseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyPreview() {
    UniLifePlannerTheme {
        HomeScreenContent(
            uiState = HomeSummaryUiState(
                firstName = "Studente",
                insights = listOf("Aggiungi o importa corsi per costruire la dashboard.")
            ),
            onMenuClick = {},
            onOpenCoursesClick = {},
            onOpenLessonsClick = {},
            onOpenExamsClick = {},
            onOpenStatisticsClick = {},
            onOpenUniboImportClick = {},
            onOpenProfileClick = {},
            onOpenCourseClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenLongNamePreview() {
    UniLifePlannerTheme {
        HomeScreenContent(
            uiState = HomeSummaryUiState(
                firstName = "Alessandro Massimiliano",
                lastName = "Rossi Bianchi Verdi",
                totalCourses = 2,
                toStudyCourses = 2,
                totalCredits = 18,
                insights = listOf("Hai 2 corsi ancora da iniziare.")
            ),
            onMenuClick = {},
            onOpenCoursesClick = {},
            onOpenLessonsClick = {},
            onOpenExamsClick = {},
            onOpenStatisticsClick = {},
            onOpenUniboImportClick = {},
            onOpenProfileClick = {},
            onOpenCourseClick = {}
        )
    }
}
