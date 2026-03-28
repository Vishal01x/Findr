package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
import java.util.UUID

/**
 * Add/Edit Role Dialog Component
 * Dialog for adding or editing project roles
 */
@Composable
fun AddRoleDialog(
    role: ProjectRole?,
    onDismiss: () -> Unit,
    onSave: (ProjectRole) -> Unit
) {
    var roleName by remember { mutableStateOf(role?.roleName ?: "") }
    var technology by remember { mutableStateOf(role?.technology ?: "") }
    var positions by remember { mutableStateOf(role?.positionsNeeded?.toString() ?: "1") }
    var description by remember { mutableStateOf(role?.description ?: "") }
    
    val isValid = roleName.isNotBlank() && positions.toIntOrNull() != null && positions.toInt() > 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (role == null) "Add Role" else "Edit Role") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    label = "Role Name *",
                    placeholder = "e.g., Frontend Developer",
                    leadingIcon = Icons.Default.Work,
                    isError = roleName.isBlank(),
                )

                AppTextField(
                    value = technology,
                    onValueChange = { technology = it },
                    label = "Technology / Skill",
                    placeholder = "e.g., React, Kotlin",
                    leadingIcon = Icons.Default.Code,
                )

                AppTextField(
                    value = positions,
                    onValueChange = { positions = it },
                    label = "Positions Needed *",
                    placeholder = "1",
                    leadingIcon = Icons.Default.Group,
                    isError = positions.toIntOrNull() == null || positions.toInt() <= 0,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (Optional)",
                    placeholder = "Role responsibilities…",
                    leadingIcon = Icons.Default.Description,
                    singleLine = false,
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newRole = ProjectRole(
                        id = role?.id ?: UUID.randomUUID().toString(),
                        roleName = roleName.trim(),
                        technology = technology.trim().ifBlank { null },
                        positionsNeeded = positions.toIntOrNull() ?: 1,
                        description = description.trim().ifBlank { null }
                    )
                    onSave(newRole)
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
