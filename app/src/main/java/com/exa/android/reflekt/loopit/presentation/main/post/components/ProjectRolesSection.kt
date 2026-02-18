package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole

/**
 * Project Roles Section Component
 * Displays and manages project role requirements
 */
@Composable
fun ProjectRolesSection(
    roles: List<ProjectRole>,
    onAddRole: () -> Unit,
    onEditRole: (ProjectRole) -> Unit,
    onRemoveRole: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Roles Needed (${roles.size}/10)",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (roles.size < 10) {
                FilledTonalButton(
                    onClick = onAddRole,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add role", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Role")
                }
            }
        }
        
        if (roles.isEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No roles added yet. Add roles to specify team requirements.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Spacer(Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                roles.forEach { role ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = role.getDisplayText(),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                role.description?.let { desc ->
                                    if (desc.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            Row {
                                IconButton(onClick = { onEditRole(role) }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Edit role",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                IconButton(onClick = { onRemoveRole(role.id) }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove role",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
