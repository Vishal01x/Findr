package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.runtime.Composable
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DestructiveDialog

/**
 * Delete confirmation dialog for comments.
 * Delegates to the shared [DestructiveDialog].
 */
@Composable
fun DeleteCommentDialog(
    comment: Comment,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDeleting: Boolean
) {
    val preview = comment.text.take(100) + if (comment.text.length > 100) "…" else ""
    DestructiveDialog(
        title = "Delete Comment?",
        message = "Are you sure you want to delete this comment?\n\n\"$preview\"\n\nThis action cannot be undone.",
        confirmText = "Delete",
        isLoading = isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
