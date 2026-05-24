package com.example.unilifeplanner.university.publicimport

data class PublicTeachingDetails(
    val teaching: PublicTeaching,
    val scheduleUrls: List<String> = emptyList(),
    val warnings: List<String> = emptyList()
)
