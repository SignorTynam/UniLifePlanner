package com.example.unilifeplanner.university.publicimport

data class PublicImportResult(
    val importedTeachings: Int,
    val updatedTeachings: Int,
    val importedLessons: Int,
    val updatedLessons: Int,
    val importedExamAppeals: Int = 0,
    val updatedExamAppeals: Int = 0,
    val warnings: List<String> = emptyList()
)
