package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType

/**
 * Post Type Selector Component
 * Horizontal scrollable row of chips for selecting post type
 */
@Composable
fun PostTypeSelector(
    selectedType: PostType,
    onTypeSelected: (PostType) -> Unit,
    modifier: Modifier = Modifier
) {
    val postTypes = listOf(
        PostType.POST,
        PostType.PROJECT,
        PostType.EVENT,
        PostType.HELP,
        PostType.COLLABORATION,
        PostType.OTHER
    )
    
    Column(modifier = modifier) {
        Text(
            text = "Post Type",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            postTypes.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.displayName) }
                )
            }
        }
    }
}
