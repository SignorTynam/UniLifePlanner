package com.example.unilifeplanner.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeOutlinedButton
import com.example.unilifeplanner.ui.components.UniLifePrimaryButton
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun HomeScreen(
    onNavigateToCourses: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(title = "UniLife Planner")
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Benvenuto in UniLife Planner",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hai una panoramica pronta per corsi, esami e attivita della settimana.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    title = "Corsi totali",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    title = "Preferiti",
                    value = "0",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            SummaryCard(
                title = "Prossimo esame",
                value = "Nessun esame pianificato",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Azioni rapide",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            UniLifePrimaryButton(text = "Vai ai corsi", onClick = onNavigateToCourses)
            Spacer(modifier = Modifier.height(10.dp))
            UniLifeOutlinedButton(text = "Profilo", onClick = onNavigateToProfile)
            Spacer(modifier = Modifier.height(10.dp))
            UniLifeOutlinedButton(text = "Impostazioni", onClick = onNavigateToSettings)
            Spacer(modifier = Modifier.height(10.dp))
            UniLifeOutlinedButton(text = "Mappa", onClick = onNavigateToMap)
            Spacer(modifier = Modifier.height(10.dp))
            UniLifeOutlinedButton(text = "Statistiche", onClick = onNavigateToStatistics)
            Spacer(modifier = Modifier.height(20.dp))
            UniLifeOutlinedButton(text = "Logout", onClick = onLogout)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
