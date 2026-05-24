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
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.publicimport.PublicImportStatus
import com.example.unilifeplanner.university.publicimport.PublicTeaching

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
        onQueryChange = viewModel::updateQuery,
        onSearchClick = viewModel::searchDegreePrograms,
        onDegreeProgramClick = viewModel::selectDegreeProgram,
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
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onDegreeProgramClick: (PublicDegreeProgram) -> Unit,
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
                onQueryChange = onQueryChange,
                onSearchClick = onSearchClick
            )

            when (uiState.status) {
                PublicImportStatus.Idle -> Unit
                PublicImportStatus.Loading -> LoadingCard(
                    text = if (uiState.selectedDegreeProgram == null) {
                        "Ricerca corso di laurea in corso..."
                    } else {
                        "Caricamento anteprima import..."
                    }
                )
                PublicImportStatus.Results -> ResultsSection(
                    results = uiState.results,
                    onDegreeProgramClick = onDegreeProgramClick
                )
                PublicImportStatus.Preview -> uiState.preview?.let { preview ->
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
            text = "Importa corsi da Universita di Bologna",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Importa insegnamenti e lezioni pubbliche dell'Universita di Bologna senza collegare l'account studente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    UniLifeCard {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Questa funzione usa solo dati pubblici disponibili sul sito UniBo. Non richiede username, password o accesso all'area riservata.",
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
    onQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit
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
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            label = { Text(text = "Nome corso di laurea") },
            placeholder = { Text(text = "Es. Ingegneria e Scienze Informatiche") },
            singleLine = true,
            isError = uiState.queryError != null,
            supportingText = {
                uiState.queryError?.let { Text(text = it) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onSearchClick,
            enabled = !uiState.isBusy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.School,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerca corso di laurea")
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
        text = "Risultati",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
    if (results.isEmpty()) {
        UniLifeCard {
            Text(
                text = "Nessun corso di laurea trovato. Prova con un nome piu generico.",
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
        Text(text = "${preview.teachings.size} insegnamenti trovati")
        Text(text = "${preview.lessons.size} lezioni trovate")
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
            lessonsCount = preview.lessonsByTeachingExternalId[teaching.externalId].orEmpty().size
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
        Text(text = "Importa nel planner")
    }
}

@Composable
private fun TeachingPreviewCard(
    teaching: PublicTeaching,
    lessonsCount: Int
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
