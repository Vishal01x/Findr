package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BlockUserDialog(
    onDismiss: () -> Unit,
    onConfirmBlock: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
//            Text(
//                text = "Block User",
//                style = MaterialTheme.typography.titleLarge,
//                color = Color.Black
//            )
        },
        text = {
            Text(
                text = "Are you sure you want to block this user?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirmBlock) {
                Text(
                    text = "Block",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancel",
                    color = Color.Black,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    )
}

