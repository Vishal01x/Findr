package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment

/**
 * Delete confirmation dialog for comments
 * Shows warning with comment preview
 */
@Composable
fun DeleteCommentDialog(
    comment: Comment,
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
                text = "Delete Comment?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this comment?\n\n\"${comment.text.take(100)}${if (comment.text.length > 100) "..." else ""}\"\n\nThis action cannot be undone.",
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
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
