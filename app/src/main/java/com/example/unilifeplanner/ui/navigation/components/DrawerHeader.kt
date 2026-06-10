package com.example.unilifeplanner.ui.navigation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.unilifeplanner.ui.components.UniLifeProfileAvatar
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ModernDrawerHeader(
    firstName: String,
    lastName: String,
    email: String,
    profileImageUri: String?,
    appVersion: String,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = listOf(firstName.trim(), lastName.trim())
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { "Studente" }
    val displayEmail = email.trim().ifBlank { "Menu navigazione" }

    val headerShape = RoundedCornerShape(20.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(headerShape)
            .clickable(
                onClickLabel = "Apri profilo",
                onClick = onProfileClick
            )
            .semantics {
                contentDescription = "Apri profilo"
                role = Role.Button
            }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = headerShape
            ),
        shape = headerShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UniLifeProfileAvatar(
                    profileImageUri = profileImageUri,
                    size = 68.dp,
                    contentDescription = "Apri profilo"
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = displayEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DrawerPill(text = "Studente")
                Text(
                    text = "v$appVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun DrawerPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(name = "Header - profilo completo (light)", showBackground = true)
@Composable
private fun PreviewDrawerHeaderFull() {
    UniLifePlannerTheme {
        ModernDrawerHeader(
            firstName = "Mario",
            lastName = "Rossi",
            email = "mario.rossi@studio.unibo.it",
            profileImageUri = null,
            appVersion = "1.1.17",
            onProfileClick = {}
        )
    }
}

@Preview(name = "Header - senza nome / foto (dark)", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerHeaderEmpty() {
    UniLifePlannerTheme {
        ModernDrawerHeader(
            firstName = "",
            lastName = "",
            email = "",
            profileImageUri = null,
            appVersion = "1.1.17",
            onProfileClick = {}
        )
    }
}
