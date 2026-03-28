package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.runtime.Composable
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DestructiveDialog

/**
 * Delete confirmation dialog used in PostDetailScreen / PostDetailViewModel.
 * Delegates to the shared [DestructiveDialog].
 */
@Composable
fun DeleteConfirmationDialog(
    postTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDeleting: Boolean
) {
    DestructiveDialog(
        title = "Delete Post?",
        message = "Are you sure you want to delete \"$postTitle\"? This action cannot be undone.",
        confirmText = "Delete",
        isLoading = isDeleting,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
