package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment

/**
 * Dialog for editing a comment
 * Pre-fills with current comment text and validates input
 */
@Composable
fun EditCommentDialog(
    comment: Comment,
    editText: String,
    onTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = { 
            if (!isLoading) onDismiss() 
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Edit Comment",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = editText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Enter your comment...") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = editText.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
