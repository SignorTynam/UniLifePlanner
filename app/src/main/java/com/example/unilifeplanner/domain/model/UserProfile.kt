package com.example.unilifeplanner.domain.model

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val university: String = "",
    val degreeCourse: String = "",
    val academicYear: String = "",
    val profileImageUri: String? = null
)
