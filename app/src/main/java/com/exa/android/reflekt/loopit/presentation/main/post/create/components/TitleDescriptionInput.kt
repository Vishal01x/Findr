package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants

/**
 * Title and Description Input Component
 * Text fields for post title and description with character counters
 */
@Composable
fun TitleDescriptionInput(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Field
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Title *") },
            placeholder = { Text("Enter post title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = title.length > PostConstants.Limits.MAX_TITLE_LENGTH,
            supportingText = {
                Text(
                    text = "${title.length}/${PostConstants.Limits.MAX_TITLE_LENGTH}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        
        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Description") },
            placeholder = { Text("What's on your mind?") },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            maxLines = 8,
            isError = description.length > PostConstants.Limits.MAX_DESCRIPTION_LENGTH,
            supportingText = {
                Text(
                    text = "${description.length}/${PostConstants.Limits.MAX_DESCRIPTION_LENGTH}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}
