package com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DestructiveDialog

@Composable
fun DeleteMessageDialog(
    canDeleteForEveryone: Boolean,
    hasBeenDeleted: Boolean = false,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    DestructiveDialog(
        title = "Delete message?",
        message = if (canDeleteForEveryone && !hasBeenDeleted)
            "You can delete messages for everyone or just for yourself."
        else null,
        confirmText = "Delete",
        onConfirm = { onDelete(selectedOption ?: "Delete for Me") },
        onDismiss = onCancel,
        extraContent = if (canDeleteForEveryone && !hasBeenDeleted) {
            {
                val options = listOf("Delete for Me", "Delete for Everyone")
                options.forEach { option ->
                    SelectableOption(
                        text = option,
                        isSelected = selectedOption == option,
                        onSelect = { selectedOption = option }
                    )
                }
            }
        } else null
    )
}

@Composable
fun SelectableOption(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onSelect)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
        )
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
    }
}

/** Legacy shim — kept for any remaining call sites that used CustomButton directly. */
@Composable
fun CustomButton(text: String, isPrimary: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    if (isPrimary) {
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            shape = MaterialTheme.shapes.medium
        ) { Text(text = text) }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium
        ) { Text(text = text) }
    }
}
