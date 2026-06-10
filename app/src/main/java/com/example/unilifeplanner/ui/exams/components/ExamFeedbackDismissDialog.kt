package com.example.unilifeplanner.ui.exams.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamFeedbackDismissDialog(
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = "Non chiedere più l’esito?") },
        text = {
            Text(
                text = "Questo appello non verrà più mostrato come esito da registrare. Potrai comunque modificarlo dalla sezione Esami."
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmClick) {
                Text(text = "Conferma")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Annulla")
            }
        }
    )
}

@Preview(name = "Dismiss dialog", showBackground = true)
@Composable
private fun PreviewExamFeedbackDismissDialog() {
    UniLifePlannerTheme {
        ExamFeedbackDismissDialog(
            onDismissRequest = {},
            onConfirmClick = {}
        )
    }
}
