package com.example.unilifeplanner.ui.university

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeCard
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun UniversityScreen(
    onMenuClick: () -> Unit,
    onOpenPublicUniboImportClick: () -> Unit
) {
    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Universita",
                onMenuClick = onMenuClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier,
            contentPadding = PaddingValues(
                start = 20.dp,
                top = innerPadding.calculateTopPadding() + 20.dp,
                end = 20.dp,
                bottom = 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Universita",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )

            UniLifeCard {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Questa sezione usa solo dati pubblici disponibili sul sito UniBo. Non richiede username, password o accesso all'area riservata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            UniLifeCard {
                Icon(
                    imageVector = Icons.Filled.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Importa corsi da Universita di Bologna",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Scegli corso di laurea, anno accademico e campus per importare insegnamenti e lezioni pubbliche nel planner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(onClick = onOpenPublicUniboImportClick) {
                    Icon(
                        imageVector = Icons.Filled.CloudDownload,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Importa da UniBo")
                }
            }
        }
    }
}
