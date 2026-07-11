package com.exa.android.reflekt.loopit.presentation.main.post.components

import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.theme.LikeColorActive
import com.exa.android.reflekt.loopit.theme.Spacing
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.NotificationPermissionHandler

/**
 * Post statistics bar showing like, comment, and share counts with clickable actions.
 * Triggers the notification-permission flow on Like and Comment taps.
 */
@Composable
fun PostStatsBar(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLiked = post.isLikedBy(currentUserId)

    // Notification permission trigger — set true on like or comment action
    var notifPermissionTrigger by remember { mutableStateOf(false) }
    NotificationPermissionHandler(trigger = notifPermissionTrigger) {
        notifPermissionTrigger = false
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like Action
        PostStatAction(
            icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            count = maxOf(0, post.likesCount),
            label = "Like",
            isActive = isLiked,
            activeColor = LikeColorActive,
            onClick = {
                notifPermissionTrigger = true
                onLikeClick()
            },
            modifier = Modifier.weight(1f)
        )

        // Comment Action
        PostStatAction(
            icon = Icons.Rounded.ChatBubbleOutline,
            count = post.commentsCount,
            label = "Comment",
            onClick = {
                notifPermissionTrigger = true
                onCommentClick()
            },
            modifier = Modifier.weight(1f)
        )

        // Share Action
        PostStatAction(
            icon = Icons.Rounded.Share,
            count = post.sharesCount,
            label = "Share",
            onClick = onShareClick,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual stat action item with icon, count, and label
 */
@Composable
private fun PostStatAction(
    icon: ImageVector,
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    activeColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
    
    Column(
        modifier = modifier
            .clickableWithNoIndication(onClick = onClick)
            .padding(vertical = Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

/**
 * Format large numbers with K/M suffix
 */
private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}
