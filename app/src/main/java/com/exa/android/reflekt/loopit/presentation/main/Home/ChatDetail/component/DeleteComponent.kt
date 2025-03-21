package com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun DeleteMessageDialog(
    canDeleteForEveryone: Boolean,
    hasBeenDeleted: Boolean = false,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(text = "Delete message?", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column {
                if (canDeleteForEveryone && !hasBeenDeleted) {
                    Text(text = "You can delete messages for everyone or just for yourself.")
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (canDeleteForEveryone && !hasBeenDeleted) {
                    val options = listOf("Delete for Me", "Delete for Everyone")
                    options.forEach { option ->
                        SelectableOption(
                            text = option,
                            isSelected = selectedOption == option,
                            onSelect = { selectedOption = option }
                        )
                    }
                }
            }
        },
        confirmButton = {
            CustomButton(
                text = "Delete",
                isPrimary = true,
                enabled = selectedOption != null || !canDeleteForEveryone || hasBeenDeleted,
                onClick = {
                    val deleteOption = selectedOption ?: "Delete for Me"
                    onDelete(deleteOption)
                }
            )
        },
        dismissButton = {
            CustomButton(text = "Cancel", isPrimary = false, onClick = onCancel)
        }
    )
}

@Composable
fun SelectableOption(text: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelect
            )
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
        ) {
            Text(text = text)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            border = ButtonDefaults.outlinedButtonBorder,
            colors = ButtonDefaults.outlinedButtonColors()
        ) {
            Text(text = text)
        }
    }
}
