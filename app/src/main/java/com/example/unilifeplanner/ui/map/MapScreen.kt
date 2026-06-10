package com.example.unilifeplanner.ui.map

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.data.map.UniversityPlacesDataSource
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.example.unilifeplanner.ui.map.components.CesenaCampusLocation
import com.example.unilifeplanner.ui.map.components.LocationPermissionBanner
import com.example.unilifeplanner.ui.map.components.MapBottomPanel
import com.example.unilifeplanner.ui.map.components.MapDiagnosticsBanner
import com.example.unilifeplanner.ui.map.components.MapFallbackContent
import com.example.unilifeplanner.ui.map.components.SelectedPlacePanel
import com.example.unilifeplanner.ui.map.components.toLatLng
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MapScreen(
    onMenuClick: () -> Unit,
    viewModel: MapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            viewModel.onLocationPermissionResult(granted)
        }
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    MapScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onMenuClick = onMenuClick,
        onRequestPermissionClick = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        },
        onRefreshLocationClick = viewModel::refreshUserLocation,
        onPlaceClick = viewModel::selectPlace,
        onClearSelectedPlaceClick = viewModel::clearSelectedPlace,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onClearSearchClick = { viewModel.onSearchQueryChange("") },
        onPlaceTypeSelected = viewModel::onPlaceTypeSelected,
        onClearFiltersClick = viewModel::clearFilters,
        onMapLoaded = viewModel::onMapLoaded,
        onOpenPlaceInMapsClick = { place ->
            val opened = openPlaceInMaps(context, place)
            if (!opened) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Impossibile aprire Google Maps")
                }
            }
        }
    )
}

@Composable
private fun MapScreenContent(
    uiState: MapUiState,
    snackbarHostState: SnackbarHostState,
    onMenuClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    onRefreshLocationClick: () -> Unit,
    onPlaceClick: (UniversityPlace) -> Unit,
    onClearSelectedPlaceClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchClick: () -> Unit,
    onPlaceTypeSelected: (PlaceType?) -> Unit,
    onClearFiltersClick: () -> Unit,
    onMapLoaded: () -> Unit,
    onOpenPlaceInMapsClick: (UniversityPlace) -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Mappa",
                onMenuClick = onMenuClick,
                actions = {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(22.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(
                            onClick = {
                                if (uiState.hasLocationPermission) {
                                    onRefreshLocationClick()
                                } else {
                                    onRequestPermissionClick()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CenterFocusStrong,
                                contentDescription = "Centra sulla mia posizione"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        UniversityMapContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onRequestPermissionClick = onRequestPermissionClick,
            onRefreshLocationClick = onRefreshLocationClick,
            onPlaceClick = onPlaceClick,
            onClearSelectedPlaceClick = onClearSelectedPlaceClick,
            onSearchQueryChange = onSearchQueryChange,
            onClearSearchClick = onClearSearchClick,
            onPlaceTypeSelected = onPlaceTypeSelected,
            onClearFiltersClick = onClearFiltersClick,
            onMapLoaded = onMapLoaded,
            onOpenPlaceInMapsClick = onOpenPlaceInMapsClick
        )
    }
}

@Composable
private fun UniversityMapContent(
    modifier: Modifier = Modifier,
    uiState: MapUiState,
    onRequestPermissionClick: () -> Unit,
    onRefreshLocationClick: () -> Unit,
    onPlaceClick: (UniversityPlace) -> Unit,
    onClearSelectedPlaceClick: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchClick: () -> Unit,
    onPlaceTypeSelected: (PlaceType?) -> Unit,
    onClearFiltersClick: () -> Unit,
    onMapLoaded: () -> Unit,
    onOpenPlaceInMapsClick: (UniversityPlace) -> Unit
) {
    var showMapLoadWarning by remember(uiState.isMapsConfigured) { mutableStateOf(false) }
    var showLocationBanner by remember(uiState.hasLocationPermission) {
        mutableStateOf(!uiState.hasLocationPermission)
    }

    LaunchedEffect(uiState.isMapsConfigured, uiState.isMapLoaded) {
        showMapLoadWarning = false
        if (uiState.isMapsConfigured && !uiState.isMapLoaded) {
            delay(5000)
            if (!uiState.isMapLoaded) {
                showMapLoadWarning = true
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapArea(
            uiState = uiState,
            showMapLoadWarning = showMapLoadWarning,
            onMapLoaded = onMapLoaded,
            onPlaceClick = onPlaceClick
        )

        MapOverlay(
            uiState = uiState,
            showMapLoadWarning = showMapLoadWarning,
            showLocationPermissionBanner = showLocationBanner && !uiState.hasLocationPermission,
            onRequestPermissionClick = onRequestPermissionClick,
            onContinueWithoutLocationClick = { showLocationBanner = false },
            modifier = Modifier.align(Alignment.TopCenter)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            uiState.selectedPlace?.let { place ->
                SelectedPlacePanel(
                    place = place,
                    onOpenPlaceInMapsClick = { onOpenPlaceInMapsClick(place) },
                    onCloseClick = onClearSelectedPlaceClick,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            MapBottomPanel(
                places = uiState.filteredPlaces,
                selectedPlace = uiState.selectedPlace,
                userLocation = uiState.userLocation,
                searchQuery = uiState.searchQuery,
                selectedPlaceType = uiState.selectedPlaceType,
                onSearchQueryChange = onSearchQueryChange,
                onClearSearchClick = onClearSearchClick,
                onPlaceTypeSelected = onPlaceTypeSelected,
                onClearFiltersClick = onClearFiltersClick,
                onPlaceClick = onPlaceClick,
                onOpenPlaceClick = onOpenPlaceInMapsClick
            )
        }
    }
}

@Composable
private fun MapArea(
    uiState: MapUiState,
    showMapLoadWarning: Boolean,
    onMapLoaded: () -> Unit,
    onPlaceClick: (UniversityPlace) -> Unit
) {
    val defaultTarget = remember(uiState.places) {
        uiState.places.firstOrNull()?.toLatLng() ?: CesenaCampusLocation
    }
    val initialTarget = uiState.userLocation ?: defaultTarget
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialTarget, 16f)
    }

    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 16f)
            )
        }
    }

    LaunchedEffect(uiState.selectedPlace?.id) {
        uiState.selectedPlace?.let { place ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(place.toLatLng(), 17f)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Local setup: add MAPS_API_KEY=your_google_maps_key to local.properties.
        if (uiState.isMapsConfigured) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapLoaded = onMapLoaded
            ) {
                uiState.userLocation?.let { location ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "La tua posizione",
                        snippet = "Posizione rilevata dal dispositivo"
                    )
                }

                uiState.filteredPlaces.forEach { place ->
                    Marker(
                        state = MarkerState(position = place.toLatLng()),
                        title = place.name,
                        snippet = place.description,
                        onClick = {
                            onPlaceClick(place)
                            true
                        }
                    )
                }
            }
        }

        if (!uiState.isMapsConfigured || showMapLoadWarning) {
            MapFallbackContent(
                isMapsConfigured = uiState.isMapsConfigured,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

@Composable
private fun MapOverlay(
    uiState: MapUiState,
    showMapLoadWarning: Boolean,
    showLocationPermissionBanner: Boolean,
    onRequestPermissionClick: () -> Unit,
    onContinueWithoutLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(PaddingValues(horizontal = 16.dp, vertical = 12.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (!uiState.isMapsConfigured) {
            MapDiagnosticsBanner(
                title = "Google Maps non configurato",
                message = "Aggiungi MAPS_API_KEY in local.properties per visualizzare la mappa.",
                isError = true
            )
        } else if (showMapLoadWarning) {
            MapDiagnosticsBanner(
                title = "La mappa non e stata caricata",
                message = "Verifica API key, Maps SDK for Android, billing e connessione.",
                isError = true
            )
        }

        if (showLocationPermissionBanner) {
            LocationPermissionBanner(
                onRequestPermissionClick = onRequestPermissionClick,
                onContinueWithoutLocationClick = onContinueWithoutLocationClick
            )
        }
    }
}

fun openPlaceInMaps(
    context: Context,
    place: UniversityPlace
): Boolean {
    val label = Uri.encode(place.name)
    val geoUri = Uri.parse(
        "geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}($label)"
    )
    val webUri = Uri.parse(
        "https://www.google.com/maps/search/?api=1&query=${place.latitude},${place.longitude}"
    )
    val mapsIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackGeoIntent = Intent(Intent.ACTION_VIEW, geoUri)
    val fallbackWebIntent = Intent(Intent.ACTION_VIEW, webUri)

    return try {
        when {
            mapsIntent.resolveActivity(context.packageManager) != null -> {
                context.startActivity(mapsIntent)
                true
            }

            fallbackGeoIntent.resolveActivity(context.packageManager) != null -> {
                context.startActivity(fallbackGeoIntent)
                true
            }

            fallbackWebIntent.resolveActivity(context.packageManager) != null -> {
                context.startActivity(fallbackWebIntent)
                true
            }

            else -> false
        }
    } catch (_: ActivityNotFoundException) {
        false
    }
}

@Preview(name = "Map screen - API key missing", showBackground = true, heightDp = 820)
@Composable
private fun PreviewMapScreenMissingKey() {
    UniLifePlannerTheme {
        MapScreenContent(
            uiState = previewMapState(isMapsConfigured = false),
            snackbarHostState = remember { SnackbarHostState() },
            onMenuClick = {},
            onRequestPermissionClick = {},
            onRefreshLocationClick = {},
            onPlaceClick = {},
            onClearSelectedPlaceClick = {},
            onSearchQueryChange = {},
            onClearSearchClick = {},
            onPlaceTypeSelected = {},
            onClearFiltersClick = {},
            onMapLoaded = {},
            onOpenPlaceInMapsClick = {}
        )
    }
}

@Preview(name = "Map screen - permission missing", showBackground = true, heightDp = 820)
@Composable
private fun PreviewMapScreenPermissionMissing() {
    UniLifePlannerTheme {
        MapScreenContent(
            uiState = previewMapState(hasLocationPermission = false),
            snackbarHostState = remember { SnackbarHostState() },
            onMenuClick = {},
            onRequestPermissionClick = {},
            onRefreshLocationClick = {},
            onPlaceClick = {},
            onClearSelectedPlaceClick = {},
            onSearchQueryChange = {},
            onClearSearchClick = {},
            onPlaceTypeSelected = {},
            onClearFiltersClick = {},
            onMapLoaded = {},
            onOpenPlaceInMapsClick = {}
        )
    }
}

@Preview(name = "Map screen - selected place dark", showBackground = true, heightDp = 820, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMapScreenSelectedDark() {
    UniLifePlannerTheme {
        MapScreenContent(
            uiState = previewMapState(
                selectedPlace = UniversityPlacesDataSource.places.first(),
                isMapsConfigured = false
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onMenuClick = {},
            onRequestPermissionClick = {},
            onRefreshLocationClick = {},
            onPlaceClick = {},
            onClearSelectedPlaceClick = {},
            onSearchQueryChange = {},
            onClearSearchClick = {},
            onPlaceTypeSelected = {},
            onClearFiltersClick = {},
            onMapLoaded = {},
            onOpenPlaceInMapsClick = {}
        )
    }
}

private fun previewMapState(
    isMapsConfigured: Boolean = true,
    hasLocationPermission: Boolean = true,
    selectedPlace: UniversityPlace? = null
): MapUiState {
    val places = UniversityPlacesDataSource.places
    return MapUiState(
        places = places,
        filteredPlaces = places,
        userLocation = LatLng(44.1392, 12.2430),
        selectedPlace = selectedPlace,
        isMapsConfigured = isMapsConfigured,
        hasLocationPermission = hasLocationPermission
    )
}
