package com.example.unilifeplanner.ui.map.components

import android.location.Location
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace
import com.google.android.gms.maps.model.LatLng

fun placeTypeLabel(type: PlaceType): String {
    return when (type) {
        PlaceType.LIBRARY -> "Biblioteca"
        PlaceType.CANTEEN -> "Mensa"
        PlaceType.STUDY_ROOM -> "Aula studio"
        PlaceType.SECRETARIAT -> "Segreteria"
        PlaceType.LAB -> "Laboratorio"
        PlaceType.BUS_STOP -> "Fermata bus"
        PlaceType.OTHER -> "Campus"
    }
}

fun placeTypePluralLabel(type: PlaceType): String {
    return when (type) {
        PlaceType.LIBRARY -> "Biblioteche"
        PlaceType.CANTEEN -> "Mensa"
        PlaceType.STUDY_ROOM -> "Aule studio"
        PlaceType.SECRETARIAT -> "Segreteria"
        PlaceType.LAB -> "Laboratori"
        PlaceType.BUS_STOP -> "Fermate"
        PlaceType.OTHER -> "Altro"
    }
}

fun placeTypeIcon(type: PlaceType): ImageVector {
    return when (type) {
        PlaceType.LIBRARY -> Icons.AutoMirrored.Filled.MenuBook
        PlaceType.CANTEEN -> Icons.Filled.LocalDining
        PlaceType.STUDY_ROOM -> Icons.Filled.Workspaces
        PlaceType.SECRETARIAT -> Icons.Filled.AccountBalance
        PlaceType.LAB -> Icons.Filled.Computer
        PlaceType.BUS_STOP -> Icons.Filled.DirectionsBus
        PlaceType.OTHER -> Icons.Filled.School
    }
}

fun formatDistanceFromUser(
    userLocation: LatLng?,
    place: UniversityPlace
): String? {
    userLocation ?: return null
    val results = FloatArray(1)
    Location.distanceBetween(
        userLocation.latitude,
        userLocation.longitude,
        place.latitude,
        place.longitude,
        results
    )
    val meters = results.firstOrNull() ?: return null
    return if (meters < 1000f) {
        "${meters.toInt()} m"
    } else {
        String.format("%.1f km", meters / 1000f)
    }
}

fun UniversityPlace.toLatLng(): LatLng = LatLng(latitude, longitude)

val CesenaCampusLocation = LatLng(44.13910, 12.24315)
