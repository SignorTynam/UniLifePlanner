package com.example.unilifeplanner.ui.university

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.ui.components.InfoPill
import com.example.unilifeplanner.ui.components.UniLifeCard
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.publicimport.PublicImportStatus
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.publicimport.StudyYearOption
import com.example.unilifeplanner.university.publicimport.formatStudyYearLabel

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
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Importa da UniBo",
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderSection()
            SearchForm(
                uiState = uiState,
                onAcademicYearChange = onAcademicYearChange,
                onCampusChange = onCampusChange,
                onDegreeTypeChange = onDegreeTypeChange,
                onLoadDegreeProgramsClick = onLoadDegreeProgramsClick
            )

            when (uiState.status) {
                PublicImportStatus.Idle -> Unit
                PublicImportStatus.LoadingDegreePrograms -> LoadingCard(
                    text = "Caricamento corsi di laurea..."
                )
                PublicImportStatus.DegreeProgramsLoaded -> ResultsSection(
                    results = uiState.results,
                    onDegreeProgramClick = onDegreeProgramClick
                )
                PublicImportStatus.LoadingCurricula -> {
                    uiState.selectedDegreeProgram?.let { SelectedDegreeProgramCard(it) }
                    LoadingCard(text = "Verifica curriculum disponibili...")
                }
                PublicImportStatus.CurriculumSelection -> {
                    uiState.selectedDegreeProgram?.let { SelectedDegreeProgramCard(it) }
                    CurriculumSection(
                        curricula = uiState.curricula,
                        onCurriculumClick = onCurriculumClick
                    )
                }
                PublicImportStatus.StudyYearSelection -> {
                    uiState.selectedDegreeProgram?.let { SelectedDegreeProgramCard(it) }
                    uiState.selectedCurriculum?.let { SelectedCurriculumCard(it) }
                    StudyYearSection(
                        options = uiState.availableStudyYears,
                        onStudyYearClick = onStudyYearClick
                    )
                }
                PublicImportStatus.LoadingPreview -> {
                    uiState.selectedDegreeProgram?.let { SelectedDegreeProgramCard(it) }
                    uiState.selectedCurriculum?.let { SelectedCurriculumCard(it) }
                    LoadingCard(text = "Caricamento anteprima import...")
                }
                PublicImportStatus.Preview -> uiState.preview?.let { preview ->
                    SelectedDegreeProgramCard(preview.degreeProgram)
                    preview.curriculum?.let { SelectedCurriculumCard(it) }
                    PreviewSection(
                        preview = preview,
                        onImportClick = onImportClick
                    )
                }
                PublicImportStatus.Importing -> LoadingCard(text = "Importazione nel planner...")
                PublicImportStatus.Imported -> uiState.importResult?.let { result ->
                    ImportedSection(
                        result = result,
                        onGoToCoursesClick = onGoToCoursesClick,
                        onImportAnotherClick = onImportAnotherClick
                    )
                }
                PublicImportStatus.Error -> ErrorCard(
                    message = uiState.errorMessage ?: "Operazione non riuscita"
                )
            }
        }
    }
}

@Composable
private fun HeaderSection() {
    Text(
        text = "Importa da UniBo",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold
    )
    UniLifeCard {
        Icon(
            imageVector = Icons.Filled.CloudDownload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Importa corsi, lezioni e appelli da UniBo",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Importa insegnamenti, lezioni/laboratori e appelli d'esame pubblici senza collegare l'account studente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "L'import viene limitato all'anno di corso che selezioni.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchForm(
    uiState: PublicUniboImportUiState,
    onAcademicYearChange: (String) -> Unit,
    onCampusChange: (String) -> Unit,
    onDegreeTypeChange: (String) -> Unit,
    onLoadDegreeProgramsClick: () -> Unit
) {
    UniLifeCard {
        SelectionField(
            label = "Anno accademico",
            value = uiState.selectedAcademicYear,
            options = uiState.academicYears,
            onSelected = onAcademicYearChange
        )
        SelectionField(
            label = "Campus",
            value = uiState.selectedCampus,
            options = uiState.campuses,
            onSelected = onCampusChange
        )
        SelectionField(
            label = "Tipologia",
            value = uiState.selectedDegreeType,
            options = uiState.degreeTypes,
            onSelected = onDegreeTypeChange
        )
        Button(
            onClick = onLoadDegreeProgramsClick,
            enabled = !uiState.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Carica corsi di laurea")
        }
    }
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option) },
                        onClick = {
                            expanded = false
                            onSelected(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultsSection(
    results: List<PublicDegreeProgram>,
    onDegreeProgramClick: (PublicDegreeProgram) -> Unit
) {
    Text(
        text = "Scegli corso di laurea",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    if (results.isEmpty()) {
        UniLifeCard {
            Text(
                text = "Nessun corso di laurea trovato per i filtri selezionati.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    results.forEach { degreeProgram ->
        DegreeProgramResultCard(
            degreeProgram = degreeProgram,
            onClick = { onDegreeProgramClick(degreeProgram) }
        )
    }
}

@Composable
private fun SelectedDegreeProgramCard(degreeProgram: PublicDegreeProgram) {
    UniLifeCard {
        Text(
            text = "Corso selezionato",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = degreeProgram.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CurriculumSection(
    curricula: List<PublicCurriculum>,
    onCurriculumClick: (PublicCurriculum) -> Unit
) {
    Text(
        text = "Scegli curriculum",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    curricula.forEach { curriculum ->
        UniLifeCard {
            Text(
                text = curriculum.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            InfoPill(text = curriculum.academicYear)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Link,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = curriculum.officialUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Button(
                onClick = { onCurriculumClick(curriculum) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Seleziona curriculum")
            }
        }
    }
}

@Composable
private fun SelectedCurriculumCard(curriculum: PublicCurriculum) {
    UniLifeCard {
        Text(
            text = "Curriculum selezionato",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = curriculum.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StudyYearSection(
    options: List<StudyYearOption>,
    onStudyYearClick: (Int) -> Unit
) {
    Text(
        text = "In quale anno sei?",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    UniLifeCard {
        Text(
            text = "Scegli l'anno di corso per importare solo insegnamenti, lezioni e appelli collegati al tuo anno.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        options.forEach { option ->
            Button(
                onClick = { onStudyYearClick(option.year) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = option.label)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DegreeProgramResultCard(
    degreeProgram: PublicDegreeProgram,
    onClick: () -> Unit
) {
    UniLifeCard {
        Text(
            text = degreeProgram.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InfoPill(text = degreeProgram.academicYear)
            degreeProgram.campus?.let { InfoPill(text = it) }
            degreeProgram.degreeType?.let { InfoPill(text = it) }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = degreeProgram.officialUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Seleziona")
        }
    }
}

@Composable
private fun PreviewSection(
    preview: PublicImportPreview,
    onImportClick: () -> Unit
) {
    Text(
        text = "Anteprima import",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    UniLifeCard {
        Text(
            text = preview.degreeProgram.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        preview.curriculum?.let { curriculum ->
            Text(text = "Curriculum: ${curriculum.name}")
        }
        preview.selectedStudyYear?.let { studyYear ->
            Text(text = "Anno scelto: ${formatStudyYearLabel(studyYear)}")
        }
        Text(text = "${preview.teachings.size} insegnamenti trovati")
        Text(text = "${preview.lessons.size} lezioni trovate")
        Text(text = "${preview.examAppeals.size} appelli d'esame trovati")
        Text(text = "${preview.warnings.size} avvisi")
    }

    if (preview.warnings.isNotEmpty()) {
        WarningCard(warnings = preview.warnings)
    }

    Text(
        text = "Insegnamenti trovati",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    preview.teachings.forEach { teaching ->
        TeachingPreviewCard(
            teaching = teaching,
            lessonsCount = preview.lessonsByTeachingExternalId[teaching.externalId].orEmpty().size,
            examAppealsCount = preview.examAppealsByTeachingExternalId[teaching.externalId]
                .orEmpty()
                .size
        )
    }
    Button(
        onClick = onImportClick,
        enabled = preview.teachings.isNotEmpty(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Filled.CloudDownload,
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Importa corsi, lezioni e appelli")
    }
}

@Composable
private fun TeachingPreviewCard(
    teaching: PublicTeaching,
    lessonsCount: Int,
    examAppealsCount: Int
) {
    UniLifeCard {
        Text(
            text = teaching.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "CFU: ${teaching.credits ?: 0}",
            style = MaterialTheme.typography.bodyMedium
        )
        teaching.studyYear?.let { studyYear ->
            InfoPill(text = formatStudyYearLabel(studyYear))
        }
        Text(
            text = "Docente: ${teaching.professor ?: "Docente non indicato"}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        InfoPill(
            text = if (lessonsCount > 0) {
                "Lezioni trovate: $lessonsCount"
            } else {
                "Lezioni non disponibili"
            }
        )
        InfoPill(
            text = if (examAppealsCount > 0) {
                "Appelli trovati: $examAppealsCount"
            } else {
                "Appelli non disponibili"
            }
        )
    }
}

@Composable
private fun WarningCard(warnings: List<String>) {
    UniLifeCard {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Attenzione",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        warnings.take(8).forEach { warning ->
            Text(
                text = "- $warning",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (warnings.size > 8) {
            Text(
                text = "Altri ${warnings.size - 8} avvisi non mostrati.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImportedSection(
    result: PublicImportResult,
    onGoToCoursesClick: () -> Unit,
    onImportAnotherClick: () -> Unit
) {
    UniLifeCard {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Import completato",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = "Insegnamenti importati: ${result.importedTeachings}")
        Text(text = "Insegnamenti aggiornati: ${result.updatedTeachings}")
        Text(text = "Lezioni importate: ${result.importedLessons}")
        Text(text = "Lezioni aggiornate: ${result.updatedLessons}")
        Text(text = "Appelli importati: ${result.importedExamAppeals}")
        Text(text = "Appelli aggiornati: ${result.updatedExamAppeals}")
        Button(
            onClick = onGoToCoursesClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Vai ai corsi")
        }
        OutlinedButton(
            onClick = onImportAnotherClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Importa un altro corso di laurea")
        }
    }
    if (result.warnings.isNotEmpty()) {
        WarningCard(warnings = result.warnings)
    }
}

@Composable
private fun LoadingCard(text: String) {
    UniLifeCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String) {
    UniLifeCard {
        Icon(
            imageVector = Icons.Filled.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            text = "Errore",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
