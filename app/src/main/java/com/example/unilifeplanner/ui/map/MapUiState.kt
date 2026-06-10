package com.example.unilifeplanner.ui.map

import com.example.unilifeplanner.domain.model.UniversityPlace
import com.example.unilifeplanner.domain.model.PlaceType
import com.google.android.gms.maps.model.LatLng

data class MapUiState(
    val isLoading: Boolean = false,
    val userLocation: LatLng? = null,
    val places: List<UniversityPlace> = emptyList(),
    val filteredPlaces: List<UniversityPlace> = emptyList(),
    val selectedPlace: UniversityPlace? = null,
    val searchQuery: String = "",
    val selectedPlaceType: PlaceType? = null,
    val isMapsConfigured: Boolean = true,
    val isMapLoaded: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val errorMessage: String? = null
)
