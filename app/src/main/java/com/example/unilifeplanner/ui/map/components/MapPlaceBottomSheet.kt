package com.example.unilifeplanner.ui.map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.data.map.UniversityPlacesDataSource
import com.example.unilifeplanner.domain.model.UniversityPlace
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun SelectedPlacePanel(
    place: UniversityPlace,
    onOpenPlaceInMapsClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            PlaceTypePill(label = placeTypeLabel(place.type))
            Text(
                text = place.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenPlaceInMapsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Apri in Google Maps")
                }
                OutlinedButton(
                    onClick = onCloseClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Chiudi")
                }
            }
        }
    }
}

@Composable
private fun PlaceTypePill(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
            shape = RoundedCornerShape(50)
        ),
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Preview(name = "Selected place panel", showBackground = true)
@Composable
private fun PreviewSelectedPlacePanel() {
    UniLifePlannerTheme {
        SelectedPlacePanel(
            place = UniversityPlacesDataSource.places.first(),
            onOpenPlaceInMapsClick = {},
            onCloseClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Selected place panel - dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSelectedPlacePanelDark() {
    UniLifePlannerTheme {
        SelectedPlacePanel(
            place = UniversityPlacesDataSource.places.first(),
            onOpenPlaceInMapsClick = {},
            onCloseClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
