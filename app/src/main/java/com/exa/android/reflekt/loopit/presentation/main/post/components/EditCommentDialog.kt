package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.InputDialog

/**
 * Dialog for editing an existing comment.
 * Delegates to the shared [InputDialog].
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
    InputDialog(
        title = "Edit Comment",
        placeholder = "Enter your comment…",
        value = editText,
        onValueChange = onTextChange,
        confirmText = "Save",
        dismissText = "Cancel",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        isLoading = isLoading,
        maxLines = 5,
        icon = Icons.Outlined.Edit,
        modifier = Modifier
    )
}
