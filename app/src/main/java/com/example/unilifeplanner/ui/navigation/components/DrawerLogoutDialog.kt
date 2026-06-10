package com.example.unilifeplanner.ui.navigation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.unilifeplanner.ui.theme.UniLifePlannerTheme

@Composable
fun LogoutConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Uscire dall'account?",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "Verrai riportato alla schermata di accesso. I dati locali del planner resteranno salvati.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Logout",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Annulla",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Preview(name = "LogoutConfirmDialog (light)", showBackground = true)
@Composable
private fun PreviewLogoutDialog() {
    UniLifePlannerTheme {
        LogoutConfirmDialog(onDismiss = {}, onConfirm = {})
    }
}

@Preview(name = "LogoutConfirmDialog (dark)", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLogoutDialogDark() {
    UniLifePlannerTheme {
        LogoutConfirmDialog(onDismiss = {}, onConfirm = {})
    }
}
