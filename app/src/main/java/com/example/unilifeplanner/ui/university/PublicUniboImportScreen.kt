package com.example.unilifeplanner.ui.university

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.example.unilifeplanner.ui.university.components.CurriculumSelectionSection
import com.example.unilifeplanner.ui.university.components.DegreeProgramsSection
import com.example.unilifeplanner.ui.university.components.ImportPreviewDashboard
import com.example.unilifeplanner.ui.university.components.ImportResultSummary
import com.example.unilifeplanner.ui.university.components.SelectedImportChoiceCard
import com.example.unilifeplanner.ui.university.components.StudyYearSelector
import com.example.unilifeplanner.ui.university.components.UniboImportErrorState
import com.example.unilifeplanner.ui.university.components.UniboImportFilters
import com.example.unilifeplanner.ui.university.components.UniboImportHero
import com.example.unilifeplanner.ui.university.components.UniboImportLoadingState
import com.example.unilifeplanner.ui.university.components.UniboImportStepper
import com.example.unilifeplanner.ui.university.components.sampleCurriculum
import com.example.unilifeplanner.ui.university.components.sampleDegreeProgram
import com.example.unilifeplanner.ui.university.components.samplePreview
import com.example.unilifeplanner.ui.university.components.sampleResult
import com.example.unilifeplanner.ui.university.components.sampleStudyYears
import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportStatus

@Composable
fun PublicUniboImportScreen(
    onMenuClick: () -> Unit,
    onGoToCoursesClick: () -> Unit,
    viewModel: PublicUniboImportViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PublicUniboImportContent(
        uiState = uiState,
        onMenuClick = onMenuClick,
        onAcademicYearChange = viewModel::updateAcademicYear,
        onCampusChange = viewModel::updateCampus,
        onDegreeTypeChange = viewModel::updateDegreeType,
        onLoadDegreeProgramsClick = viewModel::loadDegreePrograms,
        onDegreeProgramClick = viewModel::selectDegreeProgram,
        onCurriculumClick = viewModel::selectCurriculum,
        onStudyYearClick = viewModel::selectStudyYear,
        onImportClick = viewModel::importPreview,
        onGoToCoursesClick = onGoToCoursesClick,
        onImportAnotherClick = viewModel::resetForAnotherImport
    )
}

@Composable
private fun PublicUniboImportContent(
    uiState: PublicUniboImportUiState,
    onMenuClick: () -> Unit,
    onAcademicYearChange: (String) -> Unit,
    onCampusChange: (String) -> Unit,
    onDegreeTypeChange: (String) -> Unit,
    onLoadDegreeProgramsClick: () -> Unit,
    onDegreeProgramClick: (PublicDegreeProgram) -> Unit,
    onCurriculumClick: (PublicCurriculum) -> Unit,
    onStudyYearClick: (Int) -> Unit,
    onImportClick: () -> Unit,
    onGoToCoursesClick: () -> Unit,
    onImportAnotherClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Importa da UniBo",
                onMenuClick = onMenuClick
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
            item { UniboImportHero() }
            item { UniboImportStepper(status = uiState.status) }

            if (uiState.status != PublicImportStatus.Imported) {
                item {
                    UniboImportFilters(
                        uiState = uiState,
                        onAcademicYearChange = onAcademicYearChange,
                        onCampusChange = onCampusChange,
                        onDegreeTypeChange = onDegreeTypeChange,
                        onLoadDegreeProgramsClick = onLoadDegreeProgramsClick
                    )
                }
            }

            item {
                PublicUniboImportBody(
                    uiState = uiState,
                    onDegreeProgramClick = onDegreeProgramClick,
                    onCurriculumClick = onCurriculumClick,
                    onStudyYearClick = onStudyYearClick,
                    onImportClick = onImportClick,
                    onGoToCoursesClick = onGoToCoursesClick,
                    onImportAnotherClick = onImportAnotherClick
                )
            }
        }
    }
}

@Composable
private fun PublicUniboImportBody(
    uiState: PublicUniboImportUiState,
    onDegreeProgramClick: (PublicDegreeProgram) -> Unit,
    onCurriculumClick: (PublicCurriculum) -> Unit,
    onStudyYearClick: (Int) -> Unit,
    onImportClick: () -> Unit,
    onGoToCoursesClick: () -> Unit,
    onImportAnotherClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        when (uiState.status) {
            PublicImportStatus.Idle -> Unit

            PublicImportStatus.LoadingDegreePrograms,
            PublicImportStatus.LoadingCurricula,
            PublicImportStatus.LoadingPreview -> {
                uiState.selectedDegreeProgram?.let {
                    SelectedImportChoiceCard(
                        label = "Corso selezionato",
                        value = it.name,
                        pill = it.campus
                    )
                }
                uiState.selectedCurriculum?.let {
                    SelectedImportChoiceCard(
                        label = "Curriculum selezionato",
                        value = it.name,
                        pill = it.academicYear
                    )
                }
                UniboImportLoadingState(status = uiState.status)
            }

            PublicImportStatus.DegreeProgramsLoaded -> DegreeProgramsSection(
                results = uiState.results,
                onDegreeProgramClick = onDegreeProgramClick
            )

            PublicImportStatus.CurriculumSelection -> {
                uiState.selectedDegreeProgram?.let {
                    SelectedImportChoiceCard(
                        label = "Corso selezionato",
                        value = it.name,
                        pill = it.campus
                    )
                }
                CurriculumSelectionSection(
                    curricula = uiState.curricula,
                    onCurriculumClick = onCurriculumClick
                )
            }

            PublicImportStatus.StudyYearSelection -> {
                uiState.selectedDegreeProgram?.let {
                    SelectedImportChoiceCard(
                        label = "Corso selezionato",
                        value = it.name,
                        pill = it.campus
                    )
                }
                uiState.selectedCurriculum?.let {
                    SelectedImportChoiceCard(
                        label = "Curriculum selezionato",
                        value = it.name,
                        pill = it.academicYear
                    )
                }
                StudyYearSelector(
                    options = uiState.availableStudyYears,
                    selectedYear = uiState.selectedStudyYear,
                    onStudyYearClick = onStudyYearClick
                )
            }

            PublicImportStatus.Preview -> uiState.preview?.let { preview ->
                ImportPreviewDashboard(
                    preview = preview,
                    isImporting = false,
                    onImportClick = onImportClick
                )
            }

            PublicImportStatus.Importing -> uiState.preview?.let { preview ->
                ImportPreviewDashboard(
                    preview = preview,
                    isImporting = true,
                    onImportClick = onImportClick
                )
            } ?: UniboImportLoadingState(status = uiState.status)

            PublicImportStatus.Imported -> uiState.importResult?.let { result ->
                ImportResultSummary(
                    result = result,
                    onGoToCoursesClick = onGoToCoursesClick,
                    onImportAnotherClick = onImportAnotherClick
                )
            }

            PublicImportStatus.Error -> UniboImportErrorState(
                message = uiState.errorMessage ?: "Operazione non riuscita"
            )
        }
    }
}

@Preview(name = "UniBo import - initial", showBackground = true, heightDp = 780)
@Composable
private fun PreviewPublicUniboImportInitial() {
    UniLifePlannerTheme {
        PublicUniboImportContent(
            uiState = PublicUniboImportUiState(),
            onMenuClick = {},
            onAcademicYearChange = {},
            onCampusChange = {},
            onDegreeTypeChange = {},
            onLoadDegreeProgramsClick = {},
            onDegreeProgramClick = {},
            onCurriculumClick = {},
            onStudyYearClick = {},
            onImportClick = {},
            onGoToCoursesClick = {},
            onImportAnotherClick = {}
        )
    }
}

@Preview(name = "UniBo import - preview dark", showBackground = true, heightDp = 980, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewPublicUniboImportPreviewDark() {
    UniLifePlannerTheme {
        PublicUniboImportContent(
            uiState = PublicUniboImportUiState(
                status = PublicImportStatus.Preview,
                selectedDegreeProgram = sampleDegreeProgram(),
                selectedCurriculum = sampleCurriculum(),
                selectedStudyYear = 2,
                availableStudyYears = sampleStudyYears(),
                preview = samplePreview()
            ),
            onMenuClick = {},
            onAcademicYearChange = {},
            onCampusChange = {},
            onDegreeTypeChange = {},
            onLoadDegreeProgramsClick = {},
            onDegreeProgramClick = {},
            onCurriculumClick = {},
            onStudyYearClick = {},
            onImportClick = {},
            onGoToCoursesClick = {},
            onImportAnotherClick = {}
        )
    }
}

@Preview(name = "UniBo import - imported", showBackground = true, heightDp = 780)
@Composable
private fun PreviewPublicUniboImportImported() {
    UniLifePlannerTheme {
        PublicUniboImportContent(
            uiState = PublicUniboImportUiState(
                status = PublicImportStatus.Imported,
                importResult = sampleResult()
            ),
            onMenuClick = {},
            onAcademicYearChange = {},
            onCampusChange = {},
            onDegreeTypeChange = {},
            onLoadDegreeProgramsClick = {},
            onDegreeProgramClick = {},
            onCurriculumClick = {},
            onStudyYearClick = {},
            onImportClick = {},
            onGoToCoursesClick = {},
            onImportAnotherClick = {}
        )
    }
}
