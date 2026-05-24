package com.example.unilifeplanner.university.publicimport

interface PublicUniversityProvider {
    val provider: String

    suspend fun searchDegreePrograms(
        query: String,
        academicYear: String,
        campus: String?,
        degreeType: String?
    ): List<PublicDegreeProgram>

    suspend fun loadPreview(degreeProgram: PublicDegreeProgram): PublicImportPreview

    suspend fun importPreview(preview: PublicImportPreview): PublicImportResult
}
