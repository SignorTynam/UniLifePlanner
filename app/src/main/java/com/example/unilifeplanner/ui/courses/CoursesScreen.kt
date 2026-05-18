package com.example.unilifeplanner.ui.courses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeEmptyState
import com.example.unilifeplanner.ui.components.UniLifeOutlinedButton
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun CoursesScreen(
    onAddCourseClick: () -> Unit,
    onDemoCourseClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Corsi",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCourseClick) {
                Text(text = "+")
            }
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = "Lista corsi",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            UniLifeEmptyState(
                title = "Nessun corso salvato",
                message = "Aggiungi il primo corso quando sara disponibile la gestione dati."
            )
            Spacer(modifier = Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Corso demo",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Placeholder navigabile per verificare il dettaglio corso.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    UniLifeOutlinedButton(
                        text = "Apri corso demo",
                        onClick = onDemoCourseClick
                    )
                }
            }
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
