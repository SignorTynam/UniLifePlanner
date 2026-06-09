package com.example.unilifeplanner.ui.lessons.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun LessonEmptyState(
    title: String,
    message: String,
    primaryActionLabel: String?,
    onPrimaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (primaryActionLabel != null && onPrimaryAction != null) {
                if (primaryActionLabel == "Cancella filtri") {
                    OutlinedButton(onClick = onPrimaryAction) {
                        Text(text = primaryActionLabel)
                    }
                } else {
                    Button(onClick = onPrimaryAction) {
                        Text(text = primaryActionLabel)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Nessuna lezione")
@Composable
private fun LessonEmptyStateNoLessonsPreview() {
    UniLifePlannerTheme {
        LessonEmptyState(
            title = "Nessuna lezione programmata",
            message = "Aggiungi una lezione manualmente o importa i dati da UniBo.",
            primaryActionLabel = "Aggiungi lezione",
            onPrimaryAction = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Filtri attivi")
@Composable
private fun LessonEmptyStateFilteredPreview() {
    UniLifePlannerTheme {
        LessonEmptyState(
            title = "Nessuna lezione trovata",
            message = "Prova a modificare ricerca, giorno, corso o ordinamento.",
            primaryActionLabel = "Cancella filtri",
            onPrimaryAction = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dark")
@Composable
private fun LessonEmptyStateDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        LessonEmptyState(
            title = "Nessuna lezione programmata",
            message = "Aggiungi una lezione manualmente o importa i dati da UniBo.",
            primaryActionLabel = "Aggiungi lezione",
            onPrimaryAction = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
