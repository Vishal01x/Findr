package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Read-only tags display used in PostDetailScreen and PostCard.
 */
@Composable
fun TagsSection(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) return

    Column(modifier = modifier) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(Spacing.small))

        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            tags.forEach { tag ->
                TagChip(tag = tag)
            }
        }
    }
}

/**
 * Tag chip — read-only by default.
 *
 * @param onRemove  If provided, a close × icon is shown making the chip removable.
 *                  Used in [TagInputSection] inside EditPostScreen.
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (onRemove != null) 1f else 0.5f),
        tonalElevation = if (onRemove != null) 2.dp else 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                start = 12.dp,
                end = if (onRemove != null) 4.dp else 12.dp,
                top = 6.dp,
                bottom = 6.dp
            )
        ) {
            Text(
                text = "#$tag",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            if (onRemove != null) {
                Spacer(Modifier.width(2.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove tag",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Editable tag editor with removable chips + add-tag input.
 * Used in [EditPostScreen] and [CreatePostScreen].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputSection(
    tags: List<String>,
    onAddTag: (String) -> Unit,
    onRemoveTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun submit() {
        val trimmed = input.trim().removePrefix("#")
        if (trimmed.isNotBlank()) {
            onAddTag(trimmed)
            input = ""
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Tags",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Existing chips with × button
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                tags.forEach { tag ->
                    TagChip(tag = tag, onRemove = { onRemoveTag(tag) })
                }
            }
        }

        // Add-tag input row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.replace("\n", "") },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Add a tag…") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    submit()
                    keyboardController?.hide()
                })
            )
            FilledTonalIconButton(
                onClick = ::submit,
                enabled = input.trim().isNotBlank()
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add tag")
            }
        }
    }
}
