package com.example.unilifeplanner.ui.exams.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PastExamsHeader(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Appelli passati",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Consulta gli esiti o registra quelli mancanti",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onToggle) {
                Text(text = if (isExpanded) "Nascondi" else "Mostra")
            }
        }
    }
}
