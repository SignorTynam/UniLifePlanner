package com.example.unilifeplanner.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun UniLifeProfileAvatar(
    profileImageUri: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    contentDescription: String = "Foto profilo"
) {
    val avatarModifier = modifier
        .size(size)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.primaryContainer)

    if (profileImageUri.isNullOrBlank()) {
        Box(
            modifier = avatarModifier,
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "Avatar profilo",
                modifier = Modifier.size(size * 0.7f),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    } else {
        AsyncImage(
            model = Uri.parse(profileImageUri),
            contentDescription = contentDescription,
            modifier = avatarModifier,
            contentScale = ContentScale.Crop
        )
    }
}
