package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.runtime.Composable
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DestructiveDialog

@Composable
fun BlockUserDialog(
    onDismiss: () -> Unit,
    onConfirmBlock: () -> Unit
) {
    DestructiveDialog(
        title = "Block User",
        message = "Are you sure you want to block this user? They won't be able to message you.",
        confirmText = "Block",
        icon = Icons.Default.Block,
        onConfirm = onConfirmBlock,
        onDismiss = onDismiss
    )
}
