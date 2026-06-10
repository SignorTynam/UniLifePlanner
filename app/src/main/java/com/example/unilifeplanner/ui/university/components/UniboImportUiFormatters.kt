package com.example.unilifeplanner.ui.university.components

import com.example.unilifeplanner.university.publicimport.PublicCurriculum
import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicExamAppeal
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.publicimport.PublicImportStatus
import com.example.unilifeplanner.university.publicimport.PublicLesson
import com.example.unilifeplanner.university.publicimport.PublicTeaching
import com.example.unilifeplanner.university.publicimport.StudyYearOption
import com.example.unilifeplanner.university.publicimport.formatStudyYearLabel

enum class UniboImportStep(val index: Int, val label: String) {
    Filters(1, "Filtri"),
    Degree(2, "Corso"),
    Curriculum(3, "Curriculum"),
    Year(4, "Anno"),
    Preview(5, "Anteprima"),
    Import(6, "Import")
}

fun PublicImportStatus.toImportStep(): UniboImportStep {
    return when (this) {
        PublicImportStatus.Idle,
        PublicImportStatus.LoadingDegreePrograms -> UniboImportStep.Filters
        PublicImportStatus.DegreeProgramsLoaded -> UniboImportStep.Degree
        PublicImportStatus.LoadingCurricula,
        PublicImportStatus.CurriculumSelection -> UniboImportStep.Curriculum
        PublicImportStatus.StudyYearSelection -> UniboImportStep.Year
        PublicImportStatus.LoadingPreview,
        PublicImportStatus.Preview -> UniboImportStep.Preview
        PublicImportStatus.Importing,
        PublicImportStatus.Imported -> UniboImportStep.Import
        PublicImportStatus.Error -> UniboImportStep.Filters
    }
}

fun loadingTitle(status: PublicImportStatus): String {
    return when (status) {
        PublicImportStatus.LoadingDegreePrograms -> "Caricamento corsi di laurea"
        PublicImportStatus.LoadingCurricula -> "Verifica curriculum disponibili"
        PublicImportStatus.LoadingPreview -> "Preparazione anteprima import"
        PublicImportStatus.Importing -> "Importazione nel planner"
        else -> "Operazione in corso"
    }
}

fun loadingSubtitle(status: PublicImportStatus): String {
    return when (status) {
        PublicImportStatus.LoadingDegreePrograms -> "Stiamo interrogando i dati pubblici UniBo."
        PublicImportStatus.LoadingCurricula -> "Verifica dei curriculum disponibili."
        PublicImportStatus.LoadingPreview -> "Preparazione dell’anteprima import."
        PublicImportStatus.Importing -> "Importazione nel planner in corso."
        else -> "Attendi qualche istante."
    }
}

fun previewCompletenessLabel(lessonsCount: Int, examsCount: Int): String {
    return if (lessonsCount > 0 && examsCount > 0) "Completo" else "Parziale"
}

fun previewStudyYearLabel(year: Int?): String = year?.let(::formatStudyYearLabel) ?: "Anno non indicato"

internal fun sampleDegreeProgram() = PublicDegreeProgram(
    externalId = "8615",
    name = "Ingegneria e Scienze Informatiche",
    campus = "Cesena",
    degreeType = "Laurea",
    academicYear = "2025/2026",
    officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche",
    durationYears = 3
)

internal fun sampleCurriculum() = PublicCurriculum(
    externalId = "curr-1",
    name = "Sistemi e tecnologie informatiche",
    academicYear = "2025/2026",
    degreeProgramExternalId = "8615",
    officialUrl = "https://corsi.unibo.it/laurea/IngegneriaScienzeInformatiche"
)

internal fun samplePreview() = PublicImportPreview(
    degreeProgram = sampleDegreeProgram(),
    curriculum = sampleCurriculum(),
    selectedStudyYear = 2,
    teachings = listOf(
        PublicTeaching(
            externalId = "t-1",
            degreeProgramExternalId = "8615",
            name = "Sistemi operativi",
            code = "12345",
            professor = "Docente UniBo",
            credits = 9,
            academicYear = "2025/2026",
            officialUrl = null,
            studyYear = 2
        ),
        PublicTeaching(
            externalId = "t-2",
            degreeProgramExternalId = "8615",
            name = "Basi di dati",
            code = "67890",
            professor = null,
            credits = 6,
            academicYear = "2025/2026",
            officialUrl = null,
            studyYear = 2
        )
    ),
    lessons = listOf(
        PublicLesson("l-1", "t-1", null, 1, 540, 660, "Aula 1", "Campus", null, null),
        PublicLesson("l-2", "t-1", null, 2, 540, 660, "Lab 2", "Campus", null, null)
    ),
    examAppeals = listOf(
        PublicExamAppeal("e-1", "t-1", "12345", "Sistemi operativi", "Docente UniBo", 0L, null, "Aula 1", null, null, null)
    ),
    warnings = listOf("Alcuni orari lezione non sono disponibili nei dati pubblici.")
)

internal fun sampleResult() = PublicImportResult(
    importedTeachings = 4,
    updatedTeachings = 1,
    importedLessons = 18,
    updatedLessons = 2,
    importedExamAppeals = 5,
    updatedExamAppeals = 1,
    warnings = listOf("Un insegnamento non aveva docente indicato.")
)

internal fun sampleStudyYears() = listOf(
    StudyYearOption(1, "1° anno"),
    StudyYearOption(2, "2° anno"),
    StudyYearOption(3, "3° anno")
)
