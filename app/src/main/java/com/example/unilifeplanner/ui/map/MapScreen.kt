package com.example.unilifeplanner.ui.map

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
fun MapScreen(
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Mappa",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = "Mappa universitaria",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            UniLifeEmptyState(
                title = "Mappa non ancora disponibile",
                message = "Qui verranno mostrati luoghi universitari e punti di interesse."
            )
            Spacer(modifier = Modifier.height(20.dp))
            UniLifeOutlinedButton(
                text = "Back",
                onClick = onBackClick
            )
        }
    }
}
