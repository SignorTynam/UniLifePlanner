package com.example.unilifeplanner.domain.model

data class UniversityPlace(
    val id: Int,
    val name: String,
    val description: String,
    val type: PlaceType,
    val latitude: Double,
    val longitude: Double
)
