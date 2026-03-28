package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField

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
    val titleOverLimit = title.length > PostConstants.Limits.MAX_TITLE_LENGTH
    val descOverLimit = description.length > PostConstants.Limits.MAX_DESCRIPTION_LENGTH

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Field
        AppTextField(
            value = title,
            onValueChange = onTitleChange,
            label = "Title *",
            placeholder = "Enter post title",
            leadingIcon = Icons.Default.Title,
            isError = titleOverLimit,
            errorMessage = if (titleOverLimit) "Title too long" else null,
            supportingText = {
                Text(
                    text = "${title.length}/${PostConstants.Limits.MAX_TITLE_LENGTH}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
        
        // Description Field
        AppTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = "Description",
            placeholder = "What's on your mind?",
            leadingIcon = Icons.Default.Description,
            singleLine = false,
            minLines = 4,
            maxLines = 8,
            isError = descOverLimit,
            errorMessage = if (descOverLimit) "Description too long" else null,
            supportingText = {
                Text(
                    text = "${description.length}/${PostConstants.Limits.MAX_DESCRIPTION_LENGTH}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        )
    }
}
