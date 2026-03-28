package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.runtime.Composable
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog

/**
 * Dialog shown when the user tries to leave with an unsaved comment.
 * Delegates to the shared [DiscardDialog].
 */
@Composable
fun DiscardCommentDialog(
    onConfirmDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    DiscardDialog(
        title = "Discard your comment?",
        message = "You've started writing a comment. If you leave now, it won't be saved.",
        discardText = "Discard",
        keepText = "Keep writing",
        onDiscard = onConfirmDiscard,
        onKeep = onDismiss
    )
}
