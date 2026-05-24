package com.example.unilifeplanner.university.publicimport

data class PublicLesson(
    val externalId: String,
    val teachingExternalId: String,
    val dateMillis: Long?,
    val dayOfWeek: Int?,
    val startTimeMinutes: Int?,
    val endTimeMinutes: Int?,
    val classroom: String?,
    val building: String?,
    val notes: String?,
    val officialUrl: String?
)
