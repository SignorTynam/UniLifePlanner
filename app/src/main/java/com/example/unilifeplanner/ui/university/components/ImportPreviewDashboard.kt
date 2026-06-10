package com.example.unilifeplanner.ui.university.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme
import com.example.unilifeplanner.university.publicimport.PublicImportPreview
import com.example.unilifeplanner.university.publicimport.PublicTeaching

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportPreviewDashboard(
    preview: PublicImportPreview,
    isImporting: Boolean,
    onImportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Anteprima import",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = preview.degreeProgram.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    preview.curriculum?.let { MiniPill(it.name) }
                    MiniPill(previewStudyYearLabel(preview.selectedStudyYear))
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PreviewMetricTile(Icons.Filled.School, preview.teachings.size.toString(), "Insegnamenti")
                    PreviewMetricTile(Icons.Filled.Schedule, preview.lessons.size.toString(), "Lezioni")
                    PreviewMetricTile(Icons.Filled.Event, preview.examAppeals.size.toString(), "Appelli")
                    PreviewMetricTile(Icons.Filled.WarningAmber, preview.warnings.size.toString(), "Avvisi")
                }
            }
        }

        if (preview.warnings.isNotEmpty()) {
            UniboImportWarningPanel(warnings = preview.warnings)
        }

        Text(
            text = "Insegnamenti trovati",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        preview.teachings.forEach { teaching ->
            TeachingPreviewItem(
                teaching = teaching,
                lessonsCount = preview.lessonsByTeachingExternalId[teaching.externalId].orEmpty().size,
                examAppealsCount = preview.examAppealsByTeachingExternalId[teaching.externalId].orEmpty().size
            )
        }

        Button(
            onClick = onImportClick,
            enabled = preview.teachings.isNotEmpty() && !isImporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Importazione…",
                    modifier = Modifier.padding(start = 10.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Importa nel planner",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun TeachingPreviewItem(
    teaching: PublicTeaching,
    lessonsCount: Int,
    examAppealsCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(
                text = teaching.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Docente: ${teaching.professor ?: "Non indicato"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MiniPill("CFU ${teaching.credits ?: 0}")
                teaching.studyYear?.let { MiniPill(previewStudyYearLabel(it)) }
                MiniPill(previewCompletenessLabel(lessonsCount, examAppealsCount))
            }
            Text(
                text = "$lessonsCount lezioni • $examAppealsCount appelli",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PreviewMetricTile(
    icon: ImageVector,
    value: String,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(name = "Import preview dashboard", showBackground = true, heightDp = 760)
@Composable
private fun PreviewImportPreviewDashboard() {
    UniLifePlannerTheme {
        ImportPreviewDashboard(
            preview = samplePreview(),
            isImporting = false,
            onImportClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Teaching preview item", showBackground = true)
@Composable
private fun PreviewTeachingPreviewItem() {
    UniLifePlannerTheme {
        TeachingPreviewItem(
            teaching = samplePreview().teachings.first(),
            lessonsCount = 2,
            examAppealsCount = 1,
            modifier = Modifier.padding(16.dp)
        )
    }
}
