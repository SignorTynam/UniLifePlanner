package com.example.unilifeplanner.ui.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.data.map.UniversityPlacesDataSource
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.domain.model.UniversityPlace
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.google.android.gms.maps.model.LatLng

@Composable
fun MapBottomPanel(
    places: List<UniversityPlace>,
    selectedPlace: UniversityPlace?,
    userLocation: LatLng?,
    searchQuery: String,
    selectedPlaceType: PlaceType?,
    onSearchQueryChange: (String) -> Unit,
    onClearSearchClick: () -> Unit,
    onPlaceTypeSelected: (PlaceType?) -> Unit,
    onClearFiltersClick: () -> Unit,
    onPlaceClick: (UniversityPlace) -> Unit,
    onOpenPlaceClick: (UniversityPlace) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Luoghi universitari",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${places.size} risultati disponibili",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (searchQuery.isNotBlank() || selectedPlaceType != null) {
                    TextButton(onClick = onClearFiltersClick) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = null
                        )
                        Text(text = "Reset")
                    }
                }
            }

            MapSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClearClick = onClearSearchClick
            )

            MapFilterChips(
                selectedType = selectedPlaceType,
                onTypeSelected = onPlaceTypeSelected
            )

            if (places.isEmpty()) {
                EmptyPlacesContent()
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 310.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        MapPlaceItem(
                            place = place,
                            distanceText = formatDistanceFromUser(userLocation, place),
                            selected = selectedPlace?.id == place.id,
                            onClick = { onPlaceClick(place) },
                            onOpenClick = { onOpenPlaceClick(place) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyPlacesContent() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Nessun risultato",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Prova a cambiare ricerca o filtro luogo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Map bottom panel - configured", showBackground = true, heightDp = 560)
@Composable
private fun PreviewMapBottomPanel() {
    UniLifePlannerTheme {
        MapBottomPanel(
            places = UniversityPlacesDataSource.places,
            selectedPlace = UniversityPlacesDataSource.places.first(),
            userLocation = CesenaCampusLocation,
            searchQuery = "",
            selectedPlaceType = null,
            onSearchQueryChange = {},
            onClearSearchClick = {},
            onPlaceTypeSelected = {},
            onClearFiltersClick = {},
            onPlaceClick = {},
            onOpenPlaceClick = {}
        )
    }
}

@Preview(name = "Map bottom panel - no results dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMapBottomPanelNoResultsDark() {
    UniLifePlannerTheme {
        MapBottomPanel(
            places = emptyList(),
            selectedPlace = null,
            userLocation = null,
            searchQuery = "xyz",
            selectedPlaceType = PlaceType.LIBRARY,
            onSearchQueryChange = {},
            onClearSearchClick = {},
            onPlaceTypeSelected = {},
            onClearFiltersClick = {},
            onPlaceClick = {},
            onOpenPlaceClick = {}
        )
    }
}
