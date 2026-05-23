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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace
import com.example.unilifeplanner.ui.components.UniLifeTopBar
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
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
        onOpenPlaceInMapsClick = { place ->
            val opened = openPlaceInMaps(context, place)
            if (!opened) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Nessuna app disponibile per aprire la mappa")
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
    onOpenPlaceInMapsClick: (UniversityPlace) -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Mappa",
                onMenuClick = onMenuClick,
                actions = {
                    IconButton(
                        onClick = onRefreshLocationClick,
                        enabled = uiState.hasLocationPermission
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CenterFocusStrong,
                            contentDescription = "Centra sulla mia posizione"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (!uiState.hasLocationPermission) {
            LocationPermissionContent(
                modifier = Modifier.padding(innerPadding),
                onRequestPermissionClick = onRequestPermissionClick
            )
        } else {
            UniversityMapContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onPlaceClick = onPlaceClick,
                onRefreshLocationClick = onRefreshLocationClick,
                onOpenPlaceInMapsClick = onOpenPlaceInMapsClick
            )
        }
    }
}

@Composable
private fun LocationPermissionContent(
    modifier: Modifier = Modifier,
    onRequestPermissionClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Per mostrare la tua posizione, consenti l'accesso alla posizione.",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "I luoghi universitari restano salvati localmente. La posizione serve solo per orientarti sulla mappa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onRequestPermissionClick) {
            Text(text = "Concedi permesso")
        }
    }
}

@Composable
private fun UniversityMapContent(
    modifier: Modifier = Modifier,
    uiState: MapUiState,
    onPlaceClick: (UniversityPlace) -> Unit,
    onRefreshLocationClick: () -> Unit,
    onOpenPlaceInMapsClick: (UniversityPlace) -> Unit
) {
    val defaultTarget = remember(uiState.places) {
        uiState.places.firstOrNull()?.toLatLng() ?: DefaultCampusLocation
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            uiState.userLocation ?: defaultTarget,
            16f
        )
    }

    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, 16f)
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            uiState.userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "La tua posizione",
                    snippet = "Posizione rilevata dal dispositivo"
                )
            }

            uiState.places.forEach { place ->
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

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(PaddingValues(16.dp)),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onRefreshLocationClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusStrong,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "Centra sulla mia posizione")
            }

            uiState.selectedPlace?.let { place ->
                SelectedPlaceCard(
                    place = place,
                    onOpenPlaceInMapsClick = { onOpenPlaceInMapsClick(place) }
                )
            }
        }
    }
}

@Composable
private fun SelectedPlaceCard(
    place: UniversityPlace,
    onOpenPlaceInMapsClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = placeTypeLabel(place.type),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = onOpenPlaceInMapsClick) {
                    Text(text = "Apri in Google Maps")
                }
            }
        }
    }
}

fun openPlaceInMaps(
    context: Context,
    place: UniversityPlace
): Boolean {
    val label = Uri.encode(place.name)
    val uri = Uri.parse(
        "geo:${place.latitude},${place.longitude}?q=${place.latitude},${place.longitude}($label)"
    )
    val mapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage("com.google.android.apps.maps")
    }
    val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)

    return try {
        when {
            mapsIntent.resolveActivity(context.packageManager) != null -> {
                context.startActivity(mapsIntent)
                true
            }

            fallbackIntent.resolveActivity(context.packageManager) != null -> {
                context.startActivity(fallbackIntent)
                true
            }

            else -> false
        }
    } catch (_: ActivityNotFoundException) {
        false
    }
}

private fun UniversityPlace.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

private fun placeTypeLabel(type: PlaceType): String {
    return when (type) {
        PlaceType.LIBRARY -> "Biblioteca"
        PlaceType.CANTEEN -> "Mensa"
        PlaceType.STUDY_ROOM -> "Aula studio"
        PlaceType.SECRETARIAT -> "Segreteria"
        PlaceType.LAB -> "Laboratorio"
        PlaceType.BUS_STOP -> "Fermata bus"
        PlaceType.OTHER -> "Altro"
    }
}

private val DefaultCampusLocation = LatLng(45.47812, 9.22786)
