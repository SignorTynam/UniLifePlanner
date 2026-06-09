package com.example.unilifeplanner.university.publicimport

interface PublicUniversityProvider {
    val provider: String

    suspend fun loadDegreePrograms(
        academicYear: String,
        campus: String?,
        degreeType: String?
    ): List<PublicDegreeProgram>

    suspend fun loadCurricula(degreeProgram: PublicDegreeProgram): List<PublicCurriculum>

    suspend fun loadPreview(
        degreeProgram: PublicDegreeProgram,
        curriculum: PublicCurriculum? = null,
        forceRefresh: Boolean = false
    ): PublicImportPreview

    suspend fun importPreview(preview: PublicImportPreview): PublicImportResult
}
