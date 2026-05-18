package com.example.unilifeplanner.ui.statistics

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun StatisticsScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Statistiche",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = "Statistiche",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            UniLifeEmptyState(
                title = "Dati non ancora disponibili",
                message = "Le statistiche useranno corsi, esami e scadenze quando saranno persistiti."
            )
            Spacer(modifier = Modifier.height(20.dp))
            UniLifeOutlinedButton(
                text = "Back",
                onClick = onBackClick
            )
        }
    }
}
