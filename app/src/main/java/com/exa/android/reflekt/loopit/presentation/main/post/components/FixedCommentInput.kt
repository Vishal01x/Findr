package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.theme.Spacing
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.NotificationPermissionHandler
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.liquidTextFieldColors

/**
 * Fixed bottom comment input bar
 * Features:
 * - Always visible at the bottom
 * - User avatar
 * - Text input field
 * - Send button
 * - Reply indicator when replying to someone
 */
@Composable
fun FixedCommentInput(
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    userAvatarUrl: String?,
    userName: String,
    isLoading: Boolean = false,
    replyingToUserName: String? = null,
    onCancelReply: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Notification permission trigger — fires when user submits a comment
    var notifPermissionTrigger by remember { mutableStateOf(false) }
    NotificationPermissionHandler(trigger = notifPermissionTrigger) {
        notifPermissionTrigger = false
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Reply indicator
        if (replyingToUserName != null && onCancelReply != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Reply,
                            contentDescription = "Replying",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Replying to $replyingToUserName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Cancel reply",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
        
        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalAlignment = Alignment.Bottom
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (userAvatarUrl != null) {
                    AsyncImage(
                        model = userAvatarUrl,
                        contentDescription = "Your avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            
            // Text input field
            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp),
                placeholder = {
                    Text(
                        text = if (replyingToUserName != null) "Write a reply..." else "Add a comment...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                enabled = !isLoading,
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = liquidTextFieldColors(),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            
            // Send button
            IconButton(
                onClick = {
                    notifPermissionTrigger = true
                    onSendClick()
                },
                enabled = commentText.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (commentText.isNotBlank() && !isLoading) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (commentText.isNotBlank()) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
