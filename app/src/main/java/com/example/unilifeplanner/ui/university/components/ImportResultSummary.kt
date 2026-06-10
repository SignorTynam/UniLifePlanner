package com.example.unilifeplanner.ui.university.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.example.unilifeplanner.university.publicimport.PublicImportResult

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportResultSummary(
    result: PublicImportResult,
    onGoToCoursesClick: () -> Unit,
    onImportAnotherClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.44f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
                Text(
                    text = "Import completato",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "I dati sono stati aggiunti al planner.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResultMetric("Insegnamenti", result.importedTeachings, result.updatedTeachings)
                    ResultMetric("Lezioni", result.importedLessons, result.updatedLessons)
                    ResultMetric("Appelli", result.importedExamAppeals, result.updatedExamAppeals)
                }
                Button(
                    onClick = onGoToCoursesClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Vai ai corsi")
                }
                OutlinedButton(
                    onClick = onImportAnotherClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Importa un altro corso di laurea")
                }
            }
        }
        if (result.warnings.isNotEmpty()) {
            UniboImportWarningPanel(warnings = result.warnings)
        }
    }
}

@Composable
private fun ResultMetric(
    label: String,
    imported: Int,
    updated: Int
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "+$imported",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "$updated aggiornati",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Import result summary", showBackground = true)
@Composable
private fun PreviewImportResultSummary() {
    UniLifePlannerTheme {
        ImportResultSummary(
            result = sampleResult(),
            onGoToCoursesClick = {},
            onImportAnotherClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Import result summary - dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewImportResultSummaryDark() {
    UniLifePlannerTheme {
        ImportResultSummary(
            result = sampleResult(),
            onGoToCoursesClick = {},
            onImportAnotherClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
