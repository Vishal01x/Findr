package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
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
                OutlinedTextField(
                    value = roleName,
                    onValueChange = { roleName = it },
                    label = { Text("Role Name *") },
                    placeholder = { Text("e.g., Frontend Developer") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = technology,
                    onValueChange = { technology = it },
                    label = { Text("Technology/Skill") },
                    placeholder = { Text("e.g., React, Kotlin") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = positions,
                    onValueChange = { positions = it },
                    label = { Text("Positions Needed *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Role responsibilities...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
