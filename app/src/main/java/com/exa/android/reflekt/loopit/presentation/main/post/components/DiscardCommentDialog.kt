package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun DiscardCommentDialog(
    onConfirmDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Discard your comment?",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "You’ve started writing a comment. If you leave now, it won’t be saved.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmDiscard
            ) {
                Text(
                    text = "Discard",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep writing")
            }
        }
    )
}
