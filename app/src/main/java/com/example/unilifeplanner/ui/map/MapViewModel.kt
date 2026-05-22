package com.example.unilifeplanner.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.unilifeplanner.data.location.LocationRepository
import com.example.unilifeplanner.data.map.UniversityPlacesDataSource
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

    private val _uiState = MutableStateFlow(
        MapUiState(
            places = UniversityPlacesDataSource.places,
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
}
