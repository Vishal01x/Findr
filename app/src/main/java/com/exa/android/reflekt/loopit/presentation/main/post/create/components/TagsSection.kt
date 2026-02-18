package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Tags Section component
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
        
        // Simple row layout for tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            tags.take(5).forEach { tag ->
                TagChip(tag = tag)
            }
            if (tags.size > 5) {
                TagChip(tag = "+${tags.size - 5}")
            }
        }
    }
}

/**
 * Single tag chip
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
