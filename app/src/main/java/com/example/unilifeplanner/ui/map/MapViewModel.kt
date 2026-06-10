package com.example.unilifeplanner.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.BuildConfig
import com.example.unilifeplanner.data.location.LocationRepository
import com.example.unilifeplanner.data.map.UniversityPlacesDataSource
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application.applicationContext)
    private val allPlaces = UniversityPlacesDataSource.places

    private val _uiState = MutableStateFlow(
        MapUiState(
            places = allPlaces,
            filteredPlaces = allPlaces,
            isMapsConfigured = BuildConfig.MAPS_API_KEY.isNotBlank(),
            hasLocationPermission = locationRepository.hasLocationPermission()
        )
    )
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        refreshUserLocation()
    }

    fun onLocationPermissionResult(granted: Boolean) {
        _uiState.update {
            it.copy(
                hasLocationPermission = granted,
                errorMessage = if (granted) null else "Permesso posizione non concesso"
            )
        }

        if (granted) {
            refreshUserLocation()
        }
    }

    fun refreshUserLocation() {
        viewModelScope.launch {
            if (!locationRepository.hasLocationPermission()) {
                _uiState.update {
                    it.copy(
                        hasLocationPermission = false,
                        isLoading = false
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasLocationPermission = true,
                    errorMessage = null
                )
            }

            try {
                val location = locationRepository.getCurrentLocation()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userLocation = location,
                        errorMessage = if (location == null) {
                            "Posizione non disponibile. Verifica che GPS e servizi Google Play siano attivi."
                        } else {
                            null
                        }
                    )
                }
            } catch (_: SecurityException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        hasLocationPermission = false,
                        errorMessage = "Permesso posizione non disponibile"
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Impossibile recuperare la posizione"
                    )
                }
            }
        }
    }

    fun selectPlace(place: UniversityPlace) {
        _uiState.update {
            it.copy(selectedPlace = place)
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(searchQuery = query).withFilteredPlaces()
        }
    }

    fun onPlaceTypeSelected(type: PlaceType?) {
        _uiState.update { current ->
            current.copy(selectedPlaceType = type).withFilteredPlaces()
        }
    }

    fun onMapLoaded() {
        _uiState.update {
            it.copy(isMapLoaded = true)
        }
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedPlaceType = null,
                filteredPlaces = it.places
            )
        }
    }

    fun clearSelectedPlace() {
        _uiState.update {
            it.copy(selectedPlace = null)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }

    private fun MapUiState.withFilteredPlaces(): MapUiState {
        val normalizedQuery = searchQuery.trim().lowercase()
        val filtered = places.filter { place ->
            val matchesType = selectedPlaceType == null || place.type == selectedPlaceType
            val matchesQuery = normalizedQuery.isBlank() ||
                place.name.lowercase().contains(normalizedQuery) ||
                place.description.lowercase().contains(normalizedQuery) ||
                place.type.searchLabel().lowercase().contains(normalizedQuery)

            matchesType && matchesQuery
        }
        return copy(
            filteredPlaces = filtered,
            selectedPlace = selectedPlace?.takeIf { selected ->
                filtered.any { it.id == selected.id }
            }
        )
    }

    private fun PlaceType.searchLabel(): String {
        return when (this) {
            PlaceType.LIBRARY -> "biblioteca biblioteche"
            PlaceType.CANTEEN -> "mensa ristoro"
            PlaceType.STUDY_ROOM -> "aula studio"
            PlaceType.SECRETARIAT -> "segreteria studenti"
            PlaceType.LAB -> "laboratorio informatico"
            PlaceType.BUS_STOP -> "fermata bus trasporti"
            PlaceType.OTHER -> "campus sede altro"
        }
    }
}
