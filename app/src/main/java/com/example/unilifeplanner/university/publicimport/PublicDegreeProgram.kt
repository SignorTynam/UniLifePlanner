package com.example.unilifeplanner.university.publicimport

data class PublicDegreeProgram(
    val externalId: String,
    val name: String,
    val campus: String?,
    val degreeType: String?,
    val academicYear: String,
    val officialUrl: String
)
