package com.example.unilifeplanner.ui.navigation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun DrawerSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp, end = 16.dp)
        )
        content()
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview(name = "DrawerSection (light)", showBackground = true)
@Composable
private fun PreviewDrawerSection() {
    UniLifePlannerTheme {
        DrawerSection(title = "Studio") {
            ModernDrawerItem(
                label = "Corsi",
                icon = Icons.Filled.Home,
                selected = false,
                onClick = {}
            )
            ModernDrawerItem(
                label = "Lezioni",
                icon = Icons.Filled.Home,
                selected = true,
                onClick = {}
            )
        }
    }
}
