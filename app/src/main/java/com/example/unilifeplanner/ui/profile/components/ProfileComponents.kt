package com.example.unilifeplanner.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeProfileAvatar
import com.example.unilifeplanner.domain.model.ThemeMode
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileHeroHeader(
    firstName: String,
    lastName: String,
    email: String,
    university: String,
    degreeCourse: String,
    profileImageUri: String?,
    onChooseImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Profilo studente" }
    val chips = listOf(university, degreeCourse)
        .mapNotNull { value -> value.trim().takeIf { it.isNotEmpty() } }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                UniLifeProfileAvatar(
                    profileImageUri = profileImageUri,
                    size = 112.dp,
                    contentDescription = "Foto profilo"
                )
                Surface(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shadowElevation = 3.dp
                ) {
                    IconButton(onClick = onChooseImageClick) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Seleziona foto profilo",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (email.isNotBlank()) {
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "Gestisci i tuoi dati studente",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f)
                )
            }

            if (chips.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chips.forEach { chip ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileStudentFormSection(
    firstName: String,
    lastName: String,
    email: String,
    university: String,
    degreeCourse: String,
    academicYear: String,
    isSaving: Boolean,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onUniversityChange: (String) -> Unit,
    onDegreeCourseChange: (String) -> Unit,
    onAcademicYearChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Dati studente",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Queste informazioni personalizzano la tua esperienza nell'app.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        ProfileFormGroup(title = "Identità") {
            ProfileInputField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = "Nome",
                icon = Icons.Filled.Person,
                enabled = !isSaving
            )
            ProfileInputField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = "Cognome",
                icon = Icons.Filled.Person,
                enabled = !isSaving
            )
            ProfileInputField(
                value = email,
                onValueChange = {},
                label = "Email",
                icon = Icons.Filled.Email,
                readOnly = true,
                enabled = !isSaving,
                supportingText = "Email dell'account"
            )
        }

        ProfileFormGroup(title = "Percorso universitario") {
            ProfileInputField(
                value = university,
                onValueChange = onUniversityChange,
                label = "Università",
                icon = Icons.Filled.School,
                enabled = !isSaving
            )
            ProfileInputField(
                value = degreeCourse,
                onValueChange = onDegreeCourseChange,
                label = "Corso di laurea",
                icon = Icons.Filled.AutoStories,
                enabled = !isSaving
            )
            ProfileInputField(
                value = academicYear,
                onValueChange = onAcademicYearChange,
                label = "Anno accademico",
                icon = Icons.Filled.CalendarMonth,
                enabled = !isSaving
            )
        }
    }
}

@Composable
private fun ProfileFormGroup(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun ProfileInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    supportingText: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        label = { Text(text = label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        supportingText = supportingText?.let { text ->
            { Text(text = text) }
        },
        colors = if (readOnly) {
            OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            OutlinedTextFieldDefaults.colors()
        }
    )
}

@Composable
fun ProfileAccountSection(
    email: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.32f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (email.isNotBlank()) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f),
                contentColor = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = "Account attivo",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = "I dati sono salvati localmente per personalizzare l'app.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileActionsSection(
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onSaveClick,
        enabled = !isSaving,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text = "Salva modifiche")
        }
    }
}

@Composable
fun ProfileDangerZone(
    isSaving: Boolean,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Accesso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Puoi uscire dall'account in qualsiasi momento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(
                onClick = onLogoutClick,
                enabled = !isSaving,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Logout",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ProfileLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = "Caricamento profilo...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileHeroHeaderPreview() {
    UniLifePlannerTheme {
        ProfileHeroHeader(
            firstName = "Mario",
            lastName = "Rossi",
            email = "mario.rossi@example.com",
            university = "Università di Bologna",
            degreeCourse = "Ingegneria e scienze informatiche",
            profileImageUri = null,
            onChooseImageClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileStudentFormSectionPreview() {
    UniLifePlannerTheme {
        ProfileStudentFormSection(
            firstName = "Mario",
            lastName = "Rossi",
            email = "mario.rossi@example.com",
            university = "Università di Bologna",
            degreeCourse = "Ingegneria e scienze informatiche",
            academicYear = "2025/2026",
            isSaving = false,
            onFirstNameChange = {},
            onLastNameChange = {},
            onUniversityChange = {},
            onDegreeCourseChange = {},
            onAcademicYearChange = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileAccountSectionPreview() {
    UniLifePlannerTheme {
        ProfileAccountSection(
            email = "mario.rossi@example.com",
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileActionsSectionPreview() {
    UniLifePlannerTheme {
        ProfileActionsSection(
            isSaving = false,
            onSaveClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDangerZonePreview() {
    UniLifePlannerTheme {
        ProfileDangerZone(
            isSaving = false,
            onLogoutClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileLongNamePreview() {
    UniLifePlannerTheme {
        ProfileHeroHeader(
            firstName = "Alessandro Massimiliano",
            lastName = "Rossi Bianchi Verdi",
            email = "alessandro.rossibianchiverdi@example.com",
            university = "Università di Bologna",
            degreeCourse = "Laurea Magistrale a Ciclo Unico in Medicina e Chirurgia con percorso internazionale",
            profileImageUri = null,
            onChooseImageClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileLoadingStatePreview() {
    UniLifePlannerTheme {
        ProfileLoadingState(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(20.dp)
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileDarkPreview() {
    UniLifePlannerTheme(themeMode = ThemeMode.DARK) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProfileHeroHeader(
                    firstName = "Mario",
                    lastName = "Rossi",
                    email = "mario.rossi@example.com",
                    university = "Università di Bologna",
                    degreeCourse = "Ingegneria e scienze informatiche",
                    profileImageUri = null,
                    onChooseImageClick = {}
                )
                ProfileAccountSection(email = "mario.rossi@example.com")
            }
        }
    }
}
