package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.domain.exams.ExamFeedbackResult
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamResultSelector(
    selectedResult: ExamFeedbackResult,
    onResultSelected: (ExamFeedbackResult) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Com’è andato l’esame?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Scegli l’esito più adatto. Potrai modificarlo anche più avanti.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ExamFeedbackResult.entries.forEach { result ->
            ExamResultOptionTile(
                result = result,
                selected = selectedResult == result,
                enabled = enabled,
                onClick = { onResultSelected(result) }
            )
        }
    }
}

@Composable
fun ExamResultOptionTile(
    result: ExamFeedbackResult,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val iconContainerColor = if (selected) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.45f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(
                enabled = enabled,
                role = Role.RadioButton,
                onClickLabel = result.feedbackLabel(),
                onClick = onClick
            )
            .semantics {
                role = Role.RadioButton
                this.selected = selected
            },
        shape = shape,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = result.feedbackIcon(),
                    contentDescription = null,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(23.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = result.feedbackLabel(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.feedbackDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Result selector - passed", showBackground = true)
@Composable
private fun PreviewExamResultSelectorPassed() {
    UniLifePlannerTheme {
        ExamResultSelector(
            selectedResult = ExamFeedbackResult.PASSED,
            onResultSelected = {},
            enabled = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Result option - failed dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewExamResultOptionFailedDark() {
    UniLifePlannerTheme {
        ExamResultOptionTile(
            result = ExamFeedbackResult.FAILED,
            selected = true,
            enabled = true,
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
