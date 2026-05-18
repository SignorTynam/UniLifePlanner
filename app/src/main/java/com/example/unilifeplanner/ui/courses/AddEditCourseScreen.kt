package com.example.unilifeplanner.ui.courses

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeOutlinedButton
import com.example.unilifeplanner.ui.components.UniLifePrimaryButton
import com.example.unilifeplanner.ui.components.UniLifeScreenContainer
import com.example.unilifeplanner.ui.components.UniLifeTextField
import com.example.unilifeplanner.ui.components.UniLifeTopBar

@Composable
fun AddEditCourseScreen(
    courseId: Int?,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit
) {
    var courseName by remember { mutableStateOf("") }
    val title = if (courseId == null) {
        "Aggiungi nuovo corso"
    } else {
        "Modifica corso ID: $courseId"
    }

    Scaffold(
        topBar = {
            UniLifeTopBar(
                title = "Corso",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        UniLifeScreenContainer(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Placeholder grafico: il salvataggio reale verra collegato nelle fasi dati.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))
            UniLifeTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = "Nome corso"
            )
            Spacer(modifier = Modifier.height(20.dp))
            UniLifePrimaryButton(
                text = "Salva",
                onClick = onSaveClick
            )
            Spacer(modifier = Modifier.height(10.dp))
            UniLifeOutlinedButton(
                text = "Back",
                onClick = onBackClick
            )
        }
    }
}
