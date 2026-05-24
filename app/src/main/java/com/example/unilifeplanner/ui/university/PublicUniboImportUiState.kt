package com.example.unilifeplanner.ui.university

import com.example.unilifeplanner.university.publicimport.PublicDegreeProgram
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicImportResult
import com.example.unilifeplanner.university.publicimport.PublicImportStatus

data class PublicUniboImportUiState(
    val academicYears: List<String> = listOf("2025/2026", "2024/2025", "2023/2024"),
    val campuses: List<String> = listOf("Tutti", "Bologna", "Cesena", "Forli", "Ravenna", "Rimini"),
    val degreeTypes: List<String> = listOf(
        "Tutte",
        "Laurea",
        "Laurea Magistrale",
        "Laurea Magistrale a Ciclo Unico"
    ),
    val selectedAcademicYear: String = "2025/2026",
    val selectedCampus: String = "Tutti",
    val selectedDegreeType: String = "Tutte",
    val query: String = "",
    val queryError: String? = null,
    val status: PublicImportStatus = PublicImportStatus.Idle,
    val results: List<PublicDegreeProgram> = emptyList(),
    val selectedDegreeProgram: PublicDegreeProgram? = null,
    val preview: PublicImportPreview? = null,
    val importResult: PublicImportResult? = null,
    val errorMessage: String? = null
) {
    val isBusy: Boolean =
        status == PublicImportStatus.Loading || status == PublicImportStatus.Importing
}
