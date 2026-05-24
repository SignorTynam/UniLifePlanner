package com.example.unilifeplanner.university.publicimport

import com.example.unilifeplanner.university.unibo.publicdata.UniboPublicConfig

data class PublicTeaching(
    val externalId: String,
    val degreeProgramExternalId: String?,
    val name: String,
    val code: String?,
    val professor: String?,
    val credits: Int?,
    val academicYear: String,
    val officialUrl: String?,
    val sourceProvider: String = UniboPublicConfig.PROVIDER
)
