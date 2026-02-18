package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Link Input Section Component
 * Allows adding and managing links
 */
@Composable
fun LinkInputSection(
    links: List<String>,
    currentInput: String,
    onInputChange: (String) -> Unit,
    onAddLink: () -> Unit,
    onRemoveLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Links (${links.size}/5)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentInput,
                onValueChange = onInputChange,
                label = { Text("Add link") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Link, contentDescription = null)
                }
            )
            
            IconButton(
                onClick = onAddLink,
                enabled = currentInput.isNotBlank() && links.size < 5
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add link")
            }
        }
        
        if (links.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                links.forEach { link ->
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
                            Text(
                                text = link,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            IconButton(onClick = { onRemoveLink(link) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove link",
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
