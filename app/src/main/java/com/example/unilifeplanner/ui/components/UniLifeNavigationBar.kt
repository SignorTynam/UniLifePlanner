package com.example.unilifeplanner.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

data class UniLifeNavigationItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun UniLifeNavigationBar(
    items: List<UniLifeNavigationItem>
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = item.selected,
                onClick = item.onClick,
                icon = {},
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}
