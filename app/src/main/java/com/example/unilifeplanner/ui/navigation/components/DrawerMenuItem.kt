package com.example.unilifeplanner.ui.navigation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun ModernDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = null,
    supportingText: String? = null
) {
    val itemShape = RoundedCornerShape(16.dp)
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconContainerColor = if (selected) {
        MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val iconTint = if (selected) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderModifier = if (selected) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
            shape = itemShape
        )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .defaultMinSize(minHeight = 52.dp)
            .clip(itemShape)
            .then(borderModifier)
            .clickable(
                onClickLabel = label,
                onClick = onClick
            )
            .semantics { role = Role.Button },
        color = backgroundColor,
        shape = itemShape,
        tonalElevation = if (selected) 0.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconContainerColor),
                contentAlignment = Alignment.Center
            ) {
                if (badgeText != null) {
                    BadgedBox(
                        badge = {
                            Badge { Text(badgeText, style = MaterialTheme.typography.labelSmall) }
                        }
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (supportingText != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(0.dp))
        }
    }
}

@Preview(name = "Item - selected (light)", showBackground = true)
@Composable
private fun PreviewItemSelected() {
    UniLifePlannerTheme {
        ModernDrawerItem(
            label = "Home",
            icon = Icons.Filled.Home,
            selected = true,
            onClick = {}
        )
    }
}

@Preview(name = "Item - not selected (light)", showBackground = true)
@Composable
private fun PreviewItemNotSelected() {
    UniLifePlannerTheme {
        ModernDrawerItem(
            label = "Corsi",
            icon = Icons.Filled.School,
            selected = false,
            onClick = {}
        )
    }
}

@Preview(name = "Item - selected (dark)", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewItemSelectedDark() {
    UniLifePlannerTheme {
        ModernDrawerItem(
            label = "Home",
            icon = Icons.Filled.Home,
            selected = true,
            onClick = {}
        )
    }
}
