package com.example.unilifeplanner.ui.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.PlaceType
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun MapFilterChips(
    selectedType: PlaceType?,
    onTypeSelected: (PlaceType?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterOptions = listOf<PlaceType?>(null) + PlaceType.entries

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(filterOptions) { type ->
            val label = type?.let(::placeTypePluralLabel) ?: "Tutti"
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(text = label) },
                modifier = Modifier.semantics {
                    contentDescription = "Filtro luogo $label"
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selectedType == type,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                )
            )
        }
    }
}

@Preview(name = "Map filters", showBackground = true)
@Composable
private fun PreviewMapFilterChips() {
    UniLifePlannerTheme {
        MapFilterChips(
            selectedType = PlaceType.LIBRARY,
            onTypeSelected = {}
        )
    }
}
