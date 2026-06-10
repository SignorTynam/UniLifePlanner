package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamFeedbackActions(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onSaveClick,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = "Salvataggio…",
                    modifier = Modifier.padding(start = 10.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(text = "Salva esito")
                }
            }
        }
        OutlinedButton(
            onClick = onDismissClick,
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Non chiedermelo più")
        }
    }
}

@Preview(name = "Feedback actions", showBackground = true)
@Composable
private fun PreviewExamFeedbackActions() {
    UniLifePlannerTheme {
        ExamFeedbackActions(
            isSaving = false,
            onSaveClick = {},
            onDismissClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Feedback actions - saving", showBackground = true)
@Composable
private fun PreviewExamFeedbackActionsSaving() {
    UniLifePlannerTheme {
        ExamFeedbackActions(
            isSaving = true,
            onSaveClick = {},
            onDismissClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
