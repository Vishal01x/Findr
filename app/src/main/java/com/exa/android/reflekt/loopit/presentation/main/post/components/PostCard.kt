package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.core.utils.openLinkIntent
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.openLink
import com.exa.android.reflekt.loopit.theme.*

/**
 * Comprehensive post card with multimedia support and interactions
 * Integrated with Firestore-backed Post domain model
 */
@Composable
fun PostCard(
    post: Post,
    currentUserId: String = "",
    onPostClick: () -> Unit = {},
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRepostClick: () -> Unit = {},
    onShareClick: () -> Unit,
    onImageClick: (Int) -> Unit = {},
    onUserClick: () -> Unit = {},
    onEnrollInRole: (String) -> Unit = {}, // New: Role enrollment
    onRegisterForEvent: () -> Unit = {}, // New: Event registration
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = if (isExpanded) Int.MAX_VALUE else 3
    
    Card(
        onClick = onPostClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp,
            pressedElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // User Info Header
            if (post.userInfo != null) {
                UserInfoHeader(
                    userInfo = post.userInfo,
                    postViews = post.viewsCount,
                    timestamp = post.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                    postType = post.type,
                    onClick = onUserClick
                )
            } else {
                // Fallback if user info not loaded
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Placeholder avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    )
                    
                    Spacer(modifier = Modifier.width(Spacing.small))
                    
                    Column {
                        Text(
                            text = post.createdByName.ifEmpty { "Unknown User" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = post.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Post Title (if present)
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                )
            }
            
            // Post Content/Description
            if (post.description.isNotBlank()) {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                ) {
                    Text(
                        text = post.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize()
                    )
                    
                    // Show "Read more" if content is long
                    if (post.description.length > 150) {
                        Text(
                            text = if (isExpanded) "Show less" else "Read more",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickable { isExpanded = !isExpanded }
                                .padding(top = Spacing.extraSmall)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Spacing.small))
                }
            }
            
            // Media Content (Images/Video)
            if (post.imageUrls.isNotEmpty() || post.videoUrl?.isNotBlank() == true) {
                MediaContent(
                    images = post.imageUrls,
                    videoUrl = post.videoUrl,
                    onImageClick = onImageClick,
                    modifier = Modifier.padding(bottom = Spacing.small)
                )
            }
            
            // Links
            if (post.links.isNotEmpty()) {
                post.links.forEach { link ->
                    LinkPreview(
                        url = link,
                        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall)
                    )
                }
            }

            //PDF (if available)
            if(post.fileUrls.isNotEmpty()){
                FileAttachmentsList(post.fileUrls)
            }

            // Tags - Only show when expanded
            if (isExpanded && post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    post.tags.take(5).forEachIndexed { index, tag ->
//                        val gradients = listOf(
//                            listOf(TagGradient1Start, TagGradient1End),
//                            listOf(TagGradient2Start, TagGradient2End),
//                            listOf(TagGradient3Start, TagGradient3End)
//                        )
//                        val gradient = gradients[index % gradients.size]
//
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
            
            // Enrollment/Registration Section (for PROJECT and EVENT posts)
            PostActionSection(
                post = post,
                currentUserId = currentUserId,
                onEnrollInRole = onEnrollInRole,
                onRegisterForEvent = onRegisterForEvent
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            
            // Interaction Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.small),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Like Button
                val isLiked = post.likes.contains(currentUserId)
                InteractionButton(
                    icon = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                    count = post.likesCount,
                    label = "Like",
                    isActive = isLiked,
                    activeColor = LikeColorActive,
                    onClick = onLikeClick
                )
                
                // Comment
                InteractionButton(
                    icon = Icons.Rounded.ChatBubbleOutline,
                    count = post.commentsCount,
                    label = "Comment",
                    activeColor = CommentColor,
                    onClick = onCommentClick
                )
                
//                // Repost
//                InteractionButton(
//                    icon = Icons.Rounded.Repeat,
//                    count = post.sharesCount,
//                    label = "Repost",
//                    activeColor = AccentDark,
//                    onClick = onRepostClick
//                )
//
                // Share
                InteractionButton(
                    icon = Icons.Rounded.Share,
                    count = 0,
                    label = "Share",
                    activeColor = ShareColor,
                    onClick = onShareClick
                )

                // View Count
//                InteractionButton(
//                    icon = Icons.Rounded.Visibility,
//                    count = post.viewsCount,
//                    label = "Views",
//                    activeColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                    isActive = false,
//                    onClick = {} // View count is typically non-interactive or leads to analytics
//                )
            }
        }
    }
}

@Composable
fun InteractionButton(
    icon: ImageVector,
    count: Int,
    label: String,
    isActive: Boolean = false,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        label = "buttonScale"
    )
    
    Surface(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            if (count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun LinkPreview(
    url: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { openLinkIntent(url, context) }
                .padding(Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Link,
                contentDescription = "Link",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(Spacing.small))
            
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}
