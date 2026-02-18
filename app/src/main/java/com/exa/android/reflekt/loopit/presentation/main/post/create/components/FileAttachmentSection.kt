package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.FileAttachment

/**
 * File Attachment Section Component
 * Displays selected files with add/remove functionality
 */
@Composable
fun FileAttachmentSection(
    selectedFiles: List<FileAttachment>,
    onAddFiles: () -> Unit,
    onRemoveFile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Files (${selectedFiles.size}/3)",
                style = MaterialTheme.typography.titleMedium
            )
            
            if (selectedFiles.size < 3) {
                FilledTonalButton(
                    onClick = onAddFiles,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add files", modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Files")
                }
            }
        }
        
        if (selectedFiles.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                selectedFiles.forEach { file ->
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
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = "File",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Column {
                                    Text(
                                        text = file.fileName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = file.getFormattedSize(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            IconButton(onClick = { onRemoveFile(file.id) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove file",
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
