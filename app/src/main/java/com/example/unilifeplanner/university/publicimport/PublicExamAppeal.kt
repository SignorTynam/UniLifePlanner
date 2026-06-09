package com.example.unilifeplanner.university.publicimport

data class PublicExamAppeal(
    val externalId: String,
    val teachingExternalId: String,
    val teachingCode: String?,
    val teachingName: String,
    val professor: String?,
    val dateMillis: Long,
    val timeMinutes: Int?,
    val location: String?,
    val type: String?,
    val notes: String?,
    val officialUrl: String?
)
