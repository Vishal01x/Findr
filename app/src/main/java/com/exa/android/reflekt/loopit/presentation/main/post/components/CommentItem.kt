package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.exa.android.reflekt.loopit.core.utils.LinkUtils
import com.exa.android.reflekt.loopit.core.utils.openLinkIntent
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.theme.*
import com.google.firebase.auth.FirebaseAuth

/**
 * Comment item component with user info, text, likes, and reply button
 * Supports nested replies up to 3 levels
 */
@Composable
fun CommentItem(
    comment: Comment,
    currentUserId: String = FirebaseAuth.getInstance().currentUser?.uid ?: "",
    onUserClick : (String) -> Unit,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onToggleReplies: () -> Unit,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    isExpanded: Boolean = false,
    isLoadingReplies: Boolean = false,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    var isExpandedText by remember { mutableStateOf(false) }
    val maxLines = if (isExpandedText) Int.MAX_VALUE else 3
    
    // Check if current user is the author
    val isAuthor = comment.senderId == currentUserId

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = if (comment.nestingLevel > 0) (comment.nestingLevel * 40).dp else 0.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // User Avatar
            Box(
                modifier = Modifier
                    .size(if (comment.nestingLevel > 0) 32.dp else 40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable{
                        onUserClick(comment.senderId)
                    }
            ) {
                if (comment.userInfo?.avatarUrl != null || comment.senderImageUrl != null) {
                    AsyncImage(
                        model = comment.userInfo?.avatarUrl ?: comment.senderImageUrl,
                        contentDescription = "${comment.userInfo?.name}'s avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Placeholder with initials
                    Text(
                        text = getInitials(comment.userInfo?.name ?: comment.senderName),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                // User name and timestamp
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.small)
                    ) {
                        Row(
                            //modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.clickable{
                                onUserClick(comment.senderId)
                            }

                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = comment.userInfo?.name ?: comment.senderName,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = formatTimestamp(
                                        comment.timestamp?.toDate()?.time ?: System.currentTimeMillis()
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            // Action menu for author
                            if (isAuthor) {
                                CommentActionMenu(
                                    onEditClick = onEditClick,
                                    onDeleteClick = onDeleteClick
                                )
                            }
                        }

                        // Comment text
                        val annotatedText = buildAnnotatedString {
                            var lastIndex = 0

                            LinkUtils.findUrls(comment.text).forEach { urlSpan ->
                                if (urlSpan.start > lastIndex) {
                                    append(comment.text.substring(lastIndex, urlSpan.start))
                                }

                                pushStyle(
                                    SpanStyle(
                                        color = LiquidColors.Primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                                append(comment.text.substring(urlSpan.start, urlSpan.end))
                                pop()

                                addStringAnnotation(
                                    tag = "URL",
                                    annotation = urlSpan.url,
                                    start = urlSpan.start,
                                    end = urlSpan.end
                                )

                                lastIndex = urlSpan.end
                            }

                            if (lastIndex < comment.text.length) {
                                append(comment.text.substring(lastIndex))
                            }
                        }

                        ClickableText(
                            text = annotatedText,
                            style = MaterialTheme.typography.bodyMedium.copy(color = LiquidColors.TextSecondary),
                            maxLines = maxLines,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .animateContentSize(),
                            onClick = { offset ->
                                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        openLinkIntent(annotation.item, context)
                                    }
                            }
                        )

                        // Show "Read more" if content is long
                        if (comment.text.length > 150) {
                            Text(
                                text = if (isExpandedText) "Show less" else "Read more",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier
                                    .clickable { isExpandedText = !isExpandedText }
                                    .padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.padding(start = Spacing.small, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like button
                    val isLiked = comment.isLikedBy(currentUserId)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clickable(onClick = onLikeClick)
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            //imageVector = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                            imageVector = if (!isLiked) Icons.Rounded.FavoriteBorder else Icons.Rounded.Favorite,
                            contentDescription = "Like",
                            tint = if (isLiked) LikeColorActive else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )

                        if (comment.likesCount > 0) {
                            Text(
                                text = comment.likesCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isLiked) LikeColorActive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Reply button
                    /*if (comment.canReply()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .clickable(onClick = onReplyClick)
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Reply,
                                contentDescription = "Reply",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = "Reply",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }*/

                    if (comment.canReply()) {
                        Text(
                            text = "Reply",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onReplyClick() }
                        )
                    }

                    // View replies button
                    if (comment.hasReplies()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.clickable { onToggleReplies() }
                        ) {
                            if (comment.nestingLevel <= 0) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = "Replies",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = if (isExpanded) {
                                    "Hide ${comment.replyCount} ${if (comment.replyCount.toInt() == 1) "reply" else "replies"}"
                                } else {
                                    "View ${comment.replyCount} ${if (comment.replyCount.toInt() == 1) "reply" else "replies"}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        /*// Show replies if expanded
        if (isExpanded) {
            AnimatedVisibility(
                visible = comment.hasReplies(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.small),
                    modifier = Modifier.padding(top = Spacing.small)
                ) {
                    if (isLoadingReplies) {
                        ReplyShimmer(count = 2)
                    } else {
                        // Note: CommentBottomSheet creates the callbacks fresh for each comment
                        // So we just recursively call CommentItem - it will render correctly
                        comment.replies.forEach { reply ->
                            CommentItem(
                                comment = reply,
                                currentUserId = currentUserId,
                                onLikeClick = onLikeClick,
                                onReplyClick = onReplyClick,
                                onToggleReplies = onToggleReplies,
                                isExpanded = isExpanded,
                                isLoadingReplies = isLoadingReplies
                            )
                        }
                    }
                }
            }
        }
       */
    }
}

/**
 * Get initials from name for avatar placeholder
 */
private fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${
            parts[1].firstOrNull()?.uppercaseChar() ?: ""
        }"

        parts.size == 1 && parts[0].length >= 2 -> "${parts[0][0].uppercaseChar()}${parts[0][1].uppercaseChar()}"
        parts.size == 1 -> parts[0].firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        else -> "?"
    }
}

/**
 * Format timestamp to relative time
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m"
        diff < 86400_000 -> "${diff / 3600_000}h"
        diff < 604800_000 -> "${diff / 86400_000}d"
        else -> "${diff / 604800_000}w"
    }
}
