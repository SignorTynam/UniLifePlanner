package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ExamGradeSection(
    grade: String,
    gradeError: String?,
    enabled: Boolean,
    onGradeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Voto",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Inserisci il voto, se disponibile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = grade,
                onValueChange = onGradeChange,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                placeholder = { Text(text = "Es. 28, 30, 30L") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Grade,
                        contentDescription = null
                    )
                },
                isError = gradeError != null,
                supportingText = {
                    Text(
                        text = gradeError ?: "Puoi lasciarlo vuoto se non vuoi registrarlo."
                    )
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Preview(name = "Grade section", showBackground = true)
@Composable
private fun PreviewExamGradeSection() {
    UniLifePlannerTheme {
        ExamGradeSection(
            grade = "30L",
            gradeError = null,
            enabled = true,
            onGradeChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Grade section - error dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewExamGradeSectionErrorDark() {
    UniLifePlannerTheme {
        ExamGradeSection(
            grade = "40",
            gradeError = "Il voto deve essere compreso tra 18 e 30.",
            enabled = true,
            onGradeChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
