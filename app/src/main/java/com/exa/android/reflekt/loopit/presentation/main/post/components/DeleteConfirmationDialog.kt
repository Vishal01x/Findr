package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

/**
 * Delete confirmation dialog composable
 * Used for confirming post deletion with loading state
 */
@Composable
fun DeleteConfirmationDialog(
    postTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDeleting: Boolean
) {
    AlertDialog(
        onDismissRequest = { 
            if (!isDeleting) onDismiss() 
        },
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = "Delete Post?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$postTitle\"? This action cannot be undone."
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}
