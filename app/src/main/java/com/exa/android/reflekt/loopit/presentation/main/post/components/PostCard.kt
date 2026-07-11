package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.core.utils.formatTimeAgo
import com.exa.android.reflekt.loopit.core.utils.openLinkIntent
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import com.exa.android.reflekt.loopit.theme.*
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.component.AcademicChipsRow
import java.text.SimpleDateFormat
import java.util.Locale

// ═══════════════════════════════════════════════════════════
// Entry point — dispatches to the right card variant
// ═══════════════════════════════════════════════════════════

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
    onEnrollInRole: (String) -> Unit = {},
    onApplyNowClick: () -> Unit = {},
    onRegisterForEvent: () -> Unit = {},
    onVote: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val postType = PostType.fromDisplayName(post.type) ?: PostType.POST
    when (postType) {
        PostType.PROJECT, PostType.COLLABORATION -> ProjectPostCard(
            post = post,
            currentUserId = currentUserId,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onUserClick = onUserClick,
            onApplyNowClick = onApplyNowClick,
            modifier = modifier
        )
        PostType.EVENT -> EventPostCard(
            post = post,
            currentUserId = currentUserId,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onUserClick = onUserClick,
            onRegisterForEvent = onRegisterForEvent,
            modifier = modifier
        )
        else -> StandardPostCard(
            post = post,
            currentUserId = currentUserId,
            onPostClick = onPostClick,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick,
            onImageClick = onImageClick,
            onUserClick = onUserClick,
            onVote = onVote,
            modifier = modifier
        )
    }
}

// ═══════════════════════════════════════════════════════════
// PROJECT card
// ═══════════════════════════════════════════════════════════

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectPostCard(
    post: Post,
    currentUserId: String,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
    onApplyNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hasApplied = post.hasUserRequested(currentUserId) || post.isUserEnrolled(currentUserId)
    val isAuthor = post.createdBy == currentUserId
    val openRoles = post.projectRoles.filter { it.isApplicationOpen() }
    val totalRemaining = post.projectRoles.sumOf { maxOf(0, it.remainingPositions()) }
    var isDescExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onPostClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .border(1.dp, LiquidColors.Primary.copy(alpha = 0.22f), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidColors.GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Header: avatar + name + badge
            ProjectCardHeader(post = post, onUserClick = onUserClick)

            // Title
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.padding(
                        start = Spacing.medium,
                        end = Spacing.medium,
                        bottom = Spacing.extraSmall
                    )
                )
            }

            // Description (3 lines, expandable)
            if (post.description.isNotBlank()) {
                MarkdownText(
                    text = post.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = LiquidColors.TextSecondary,
                    maxLines = if (isDescExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(horizontal = Spacing.medium)
                        .animateContentSize()
                )
                if (post.description.length > 150) {
                    Text(
                        text = if (isDescExpanded) "Show less" else "Read more",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickableWithNoIndication { isDescExpanded = !isDescExpanded }
                            .padding(horizontal = Spacing.medium, vertical = Spacing.verySmall)
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            // Links (shown BEFORE open roles)
            if (post.links.isNotEmpty()) {
                post.links.forEach { link ->
                    LinkPreview(
                        url = link,
                        modifier = Modifier.padding(
                            horizontal = Spacing.medium,
                            vertical = Spacing.verySmall
                        )
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            // File attachments (shown BEFORE open roles)
            if (post.fileUrls.isNotEmpty()) {
                FileAttachmentsList(fileUrls = post.fileUrls)
                Spacer(modifier = Modifier.height(Spacing.small))
            }

            // Open Roles chips
            if (openRoles.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium)
                ) {
                    Text(
                        text = "OPEN ROLES",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidColors.TextSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(Spacing.extraSmall))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        openRoles.take(4).forEach { role ->
                            RoleChip(role = role)
                        }
                        if (post.projectRoles.size > 4) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LiquidColors.GlassSurface)
                                    .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+${post.projectRoles.size - 4} more",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LiquidColors.TextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.medium))
            }

            // Footer divider + slots + Apply Now
            HorizontalDivider(
                color = LiquidColors.GlassBorder,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                when {
                    isAuthor -> Text(
                        text = "You're leading",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidColors.TextSecondary
                    )
                    hasApplied -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = LiquidColors.Primary
                        )
                        Text(
                            text = "Application sent",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidColors.Primary
                        )
                    }
                    totalRemaining > 0 -> Text(
                        text = "$totalRemaining slot${if (totalRemaining != 1) "s" else ""} remaining",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidColors.TextSecondary
                    )
                    else -> Text(
                        text = "Positions filled",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidColors.TextSecondary
                    )
                }

                when {
                    isAuthor -> Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(LiquidColors.GlassSurface)
                            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Managing",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.TextSecondary
                        )
                    }
                    else -> Button(
                        onClick = {
                            when {
                                openRoles.isEmpty() -> android.widget.Toast.makeText(context, "Spots filled or opening is closed", android.widget.Toast.LENGTH_SHORT).show()
                                else -> onApplyNowClick()
                            }
                        },
                        enabled = !hasApplied,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (openRoles.isNotEmpty()) LiquidColors.Primary else LiquidColors.GlassSurface,
                            contentColor = if (openRoles.isNotEmpty()) Color.White else LiquidColors.TextSecondary,
                            disabledContainerColor = LiquidColors.GlassSurface,
                            disabledContentColor = LiquidColors.TextSecondary
                        ),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text(
                            text = if (hasApplied) "Applied" else "Enroll Now",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (!hasApplied && openRoles.isNotEmpty()) Color.White else LiquidColors.TextSecondary
                        )
                        if (!hasApplied && openRoles.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Action bar
            HorizontalDivider(
                color = LiquidColors.Border,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            PostActionBar(
                post = post,
                currentUserId = currentUserId,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick
            )
        }
    }
}

@Composable
private fun ProjectCardHeader(post: Post, onUserClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!post.isAnonymous) Modifier.clickableWithNoIndication(onClick = onUserClick) else Modifier)
            .padding(Spacing.medium),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(44.dp)) {
            if (post.isAnonymous) {
                AnonymousAvatar(size = 44.dp)
            } else {
                UserAvatar(
                    imageUrl = post.userInfo?.avatarUrl,
                    userName = post.userInfo?.name ?: post.createdByName,
                    size = AvatarSize.Medium
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.small))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (post.isAnonymous) "Anonymous" else post.userInfo?.name ?: post.createdByName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (post.isAnonymous) LiquidColors.TextSecondary else LiquidColors.TextPrimary
            )
            if (!post.isAnonymous && (post.userInfo?.branch?.isNotBlank() == true || post.userInfo?.section?.isNotBlank() == true || post.userInfo?.year?.isNotBlank() == true)) {
                Spacer(modifier = Modifier.height(4.dp))
                AcademicChipsRow(
                    branch = post.userInfo?.branch,
                    section = post.userInfo?.section,
                    year = post.userInfo?.year
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 1.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(13.dp),
                        tint = LiquidColors.TextSecondary
                    )
                    Text(
                        text = formatTimeAgo(post.createdAt?.toDate()?.time ?: System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextSecondary
                    )
                }
//                if (post.viewsCount > 0) {
//                    Text("·", style = MaterialTheme.typography.bodySmall, color = LiquidColors.TextSecondary)
//                    Icon(
//                        Icons.Rounded.Visibility,
//                        contentDescription = "Views",
//                        modifier = Modifier.size(13.dp),
//                        tint = LiquidColors.TextSecondary
//                    )
//                    Text(
//                        text = formatCount(post.viewsCount),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = LiquidColors.TextSecondary
//                    )
//                }
            }
        }
        Spacer(modifier = Modifier.width(Spacing.small))
        val badgeLabel = when (PostType.fromDisplayName(post.type)) {
            PostType.COLLABORATION -> "Collab"
            else -> "Project"
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(99.dp))
                .background(LiquidColors.Primary.copy(alpha = 0.08f))
                .border(1.dp, LiquidColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(99.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = badgeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.Primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun RoleChip(role: ProjectRole) {
    val remaining = role.remainingPositions()
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(LiquidColors.Primary.copy(alpha = 0.06f))
            .border(1.dp, LiquidColors.Primary.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
            .padding(horizontal = 11.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(LiquidColors.Primary)
        )
        Text(
            text = role.roleName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = LiquidColors.TextPrimary
        )
        if (remaining > 0) {
            Text(
                text = "· $remaining left",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.Primary.copy(alpha = 0.75f)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// EVENT card
// ═══════════════════════════════════════════════════════════

@Composable
private fun EventPostCard(
    post: Post,
    currentUserId: String,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onUserClick: () -> Unit,
    onRegisterForEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ed = post.eventDetails
    val isAuthor = post.createdBy == currentUserId
    // User has registered if they are in enrolledPersons map
    val hasRegistered = post.enrolledPersons.containsKey(currentUserId)
    val canRegister = !isAuthor && !hasRegistered && (ed?.isRegistrationOpen() ?: false)

    Card(
        onClick = onPostClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidColors.GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Cover image with gradient + category badge + title overlay
            EventCoverImage(post = post)

            // Date box + time / location
            if (ed != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ed.eventDate?.let { ts -> EventDateBox(ts) }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                    ) {
                        ed.eventTime?.let { time ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = LiquidColors.TextSecondary
                                )
                                Text(
                                    text = time,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LiquidColors.TextPrimary
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                        ) {
                            Icon(
                                if (ed.isVirtual) Icons.Rounded.Laptop else Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = LiquidColors.TextSecondary
                            )
                            Text(
                                text = ed.getLocationDisplay(),
                                style = MaterialTheme.typography.bodySmall,
                                color = LiquidColors.TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Description
//            if (post.description.isNotBlank()) {
//                Text(
//                    text = post.description,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = LiquidColors.TextSecondary,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier
//                        .padding(horizontal = Spacing.medium)
//                        .padding(bottom = Spacing.small)
//                )
//            }

            // Footer: capacity progress + Register button
            HorizontalDivider(
                color = LiquidColors.GlassBorder,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )

            // Capacity progress bar with shimmer when filling
            if (ed != null && ed.capacity != null && ed.capacity > 0) {
                val registeredCount = ed.registeredCount
                val capacity = ed.capacity
                val progress = (registeredCount.toFloat() / capacity).coerceIn(0f, 1f)
                val isFull = ed.isFull()

                // Shimmer state — triggers once when registeredCount changes
                var shimmerActive by remember { mutableStateOf(false) }
                LaunchedEffect(registeredCount) {
                    if (registeredCount > 0) {
                        shimmerActive = true
                        kotlinx.coroutines.delay(1800)
                        shimmerActive = false
                    }
                }
                val shimmerTranslate = rememberInfiniteTransition(label = "shimmer")
                val shimmerX by shimmerTranslate.animateFloat(
                    initialValue = -200f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "shimmerX"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium)
                        .padding(top = Spacing.small)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "$registeredCount/$capacity attending",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFull) MaterialTheme.colorScheme.error else LiquidColors.TextSecondary
                        )
                        if (isFull) {
                            Text("Slots Full!", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        } else {
                            Text("${capacity - registeredCount} left", style = MaterialTheme.typography.labelSmall, color = LiquidColors.TextSecondary)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(LiquidColors.GlassBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (shimmerActive) {
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                if (isFull) MaterialTheme.colorScheme.error else LiquidColors.Primary,
                                                Color.White.copy(alpha = 0.6f),
                                                if (isFull) MaterialTheme.colorScheme.error else LiquidColors.Primary
                                            ),
                                            startX = shimmerX,
                                            endX = shimmerX + 200f
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(
                                                if (isFull) MaterialTheme.colorScheme.error else LiquidColors.Primary,
                                                if (isFull) MaterialTheme.colorScheme.error else LiquidColors.Primary
                                            )
                                        )
                                    }
                                )
                        )
                    }
                }
                Spacer(Modifier.height(Spacing.small))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Icon(
                        Icons.Rounded.People,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = LiquidColors.TextSecondary
                    )
                    Text(
                        text = "${ed?.registeredCount ?: 0} attending",
                        style = MaterialTheme.typography.labelMedium,
                        color = LiquidColors.TextSecondary
                    )
                }

                when {
                    isAuthor -> {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LiquidColors.GlassSurface)
                                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = LiquidColors.TextSecondary
                            )
                            Text(
                                text = "You're hosting",
                                style = MaterialTheme.typography.labelMedium,
                                color = LiquidColors.TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    hasRegistered -> {
                        // Already registered — disabled success state
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF22C55E).copy(alpha = 0.10f))
                                .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.30f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF22C55E)
                            )
                            Text(
                                text = "Registered",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    else -> {
                        OutlinedButton(
                            onClick = {
                                when {
                                    canRegister -> onRegisterForEvent()
                                    ed?.isFull() == true -> android.widget.Toast.makeText(context, "Slots are full", android.widget.Toast.LENGTH_SHORT).show()
                                    else -> android.widget.Toast.makeText(context, "Registration is closed", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = canRegister,
                            border = BorderStroke(
                                1.dp,
                                if (canRegister) LiquidColors.Primary else LiquidColors.GlassBorder
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (canRegister) LiquidColors.Primary else LiquidColors.TextSecondary
                            )
                        ) {
                            Text(
                                text = when {
                                    canRegister -> "Register"
                                    ed?.isManuallyClosed == true -> "Closed"
                                    ed?.isFull() == true -> "Full"
                                    else -> "Closed"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun EventCoverImage(post: Post) {
    val category = post.tags.firstOrNull()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
    ) {
        if (post.imageUrls.isNotEmpty()) {
            AsyncImage(
                model = post.imageUrls[0],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = 0.65f },
                contentScale = ContentScale.Crop
            )
        } else {
            EventCoverPlaceholder(category = category)
        }

        // Gradient overlay: transparent top → dark bottom (readable text in both cases)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                    )
                )
        )

        // Category badge + event title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            val category = post.tags.firstOrNull()
            if (category != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EventDateBox(eventDate: com.google.firebase.Timestamp) {
    val date = remember(eventDate) { eventDate.toDate() }
    val monthStr = remember(date) {
        SimpleDateFormat("MMM", Locale.getDefault()).format(date).uppercase()
    }
    val dayStr = remember(date) {
        SimpleDateFormat("d", Locale.getDefault()).format(date)
    }

    Column(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = monthStr,
            style = MaterialTheme.typography.labelSmall,
            color = LiquidColors.Primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = dayStr,
            style = MaterialTheme.typography.headlineSmall,
            color = LiquidColors.TextPrimary,
            fontWeight = FontWeight.Bold,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun EventCoverPlaceholder(category: String?) {
    val isDark = isSystemInDarkTheme()
    val surface = if (isDark) Color(0xFF16262C) else Color(0xFFFFFFFF)
    val bgBase = if (isDark) Color(0xFF101D22) else Color(0xFFF1F5F9)
    val dotColor = if (isDark) Color(0x2613B6EC) else Color(0x3513B6EC)
    val primary = LiquidColors.Primary
    val icon = getEventCategoryIcon(category)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(surface, bgBase)))
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 18.dp.toPx()
            val radius = 1.dp.toPx()
            val cols = (size.width / spacing).toInt() + 2
            val rows = (size.height / spacing).toInt() + 2
            repeat(cols) { col ->
                repeat(rows) { row ->
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = Offset(col * spacing, row * spacing)
                    )
                }
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = primary.copy(alpha = 0.07f),
            modifier = Modifier
                .size(88.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 14.dp, y = 14.dp)
                .rotate(15f)
        )
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .rotate(3f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(colors = listOf(primary, Color(0xFF67E8F9)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

private fun getEventCategoryIcon(category: String?): ImageVector {
    return when (category?.lowercase()?.trim()) {
        "design", "ui/ux", "ux", "art", "creative" -> Icons.Rounded.Palette
        "tech", "technology", "cs", "coding", "programming", "development" -> Icons.Rounded.Code
        "study", "education", "academic", "learning" -> Icons.Rounded.School
        "business", "finance", "startup", "entrepreneurship" -> Icons.Rounded.Business
        "sports", "fitness", "health", "wellness" -> Icons.Rounded.FitnessCenter
        "music" -> Icons.Rounded.MusicNote
        "science", "research" -> Icons.Rounded.Science
        "social", "community", "networking" -> Icons.Rounded.Groups
        else -> Icons.Rounded.Event
    }
}

// ═══════════════════════════════════════════════════════════
// STANDARD card (Post, Bug/Help, Other)
// ═══════════════════════════════════════════════════════════

@Composable
private fun StandardPostCard(
    post: Post,
    currentUserId: String,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onImageClick: (Int) -> Unit,
    onUserClick: () -> Unit,
    onVote: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val maxLines = if (isExpanded) Int.MAX_VALUE else 3

    Card(
        onClick = onPostClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LiquidColors.GlassSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // User Info Header
            val firstTag = post.tags.firstOrNull()
            if (post.isAnonymous) {
                UserInfoHeader(
                    userInfo = post.userInfo ?: PostUserInfo(),
                    postViews = post.viewsCount,
                    timestamp = post.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                    postType = post.type,
                    tagBadge = firstTag,
                    isAnonymous = true,
                    onClick = {}
                )
            } else if (post.userInfo != null) {
                UserInfoHeader(
                    userInfo = post.userInfo,
                    postViews = post.viewsCount,
                    timestamp = post.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
                    postType = post.type,
                    tagBadge = firstTag,
                    isAnonymous = false,
                    onClick = onUserClick
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
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

            // Title
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                )
            }

            // Description
            if (post.description.isNotBlank()) {
                Column(modifier = Modifier.padding(horizontal = Spacing.medium)) {
                    MarkdownText(
                        text = post.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LiquidColors.TextPrimary,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.animateContentSize()
                    )
                    if (post.description.length > 150) {
                        Text(
                            text = if (isExpanded) "Show less" else "Read more",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clickableWithNoIndication { isExpanded = !isExpanded }
                                .padding(top = Spacing.extraSmall)
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.small))
                }
            }

            // Poll — voting happens right here, inline, no detail screen needed
            if (post.pollOptions.isNotEmpty()) {
                PollOptionsSection(
                    post = post,
                    currentUserId = currentUserId,
                    onVote = onVote,
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                )
            }

            // Media — single image gets padding + smaller height
            if (post.imageUrls.isNotEmpty() || post.videoUrl?.isNotBlank() == true) {
                MediaContent(
                    images = post.imageUrls,
                    videoUrl = post.videoUrl,
                    onImageClick = onImageClick,
                    compactSingleImage = true,
                    modifier = Modifier.padding(bottom = Spacing.small)
                )
            }

            // Links
            if (post.links.isNotEmpty()) {
                post.links.forEach { link ->
                    LinkPreview(
                        url = link,
                        modifier = Modifier.padding(
                            horizontal = Spacing.medium,
                            vertical = Spacing.extraSmall
                        )
                    )
                }
            }

            // File attachments
            if (post.fileUrls.isNotEmpty()) {
                FileAttachmentsList(post.fileUrls)
            }

            // Tags
            if (isExpanded && post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.medium, vertical = Spacing.small),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    post.tags.take(5).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.onSurfaceVariant)
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

            // Action bar
            HorizontalDivider(
                color = LiquidColors.Border,
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
            PostActionBar(
                post = post,
                currentUserId = currentUserId,
                onLikeClick = onLikeClick,
                onCommentClick = onCommentClick,
                onShareClick = onShareClick
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// Poll voting UI lives in PollOptionsSection.kt (shared with PostDetailScreen)
// ═══════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════
// Shared action bar
// ═══════════════════════════════════════════════════════════

@Composable
private fun PostActionBar(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    val isLiked = post.likes.contains(currentUserId)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.small),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        InteractionButton(
            icon = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
            count = maxOf(0, post.likesCount),
            label = "Like",
            isActive = isLiked,
            activeColor = LikeColorActive,
            triggerPop = isLiked,
            onClick = onLikeClick
        )
        InteractionButton(
            icon = Icons.Rounded.ChatBubbleOutline,
            count = post.commentsCount,
            label = "Comment",
            activeColor = CommentColor,
            onClick = onCommentClick
        )
        InteractionButton(
            icon = Icons.Rounded.Share,
            count = 0,
            label = "Share",
            activeColor = ShareColor,
            onClick = onShareClick
        )
    }
}

// ═══════════════════════════════════════════════════════════
// Public reusable components
// ═══════════════════════════════════════════════════════════

@Composable
fun InteractionButton(
    icon: ImageVector,
    count: Int,
    label: String,
    isActive: Boolean = false,
    activeColor: Color,
    triggerPop: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pop animation: briefly scale up to 1.35f then settle at 1.1f when active
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
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
                tint = if (isActive) activeColor else LiquidColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            if (count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) activeColor else LiquidColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun LinkPreview(
    url: String,
    modifier: Modifier = Modifier,
    viewModel: com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.viewModel.MetaDataViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val linkState by viewModel.getLinkState(url).collectAsState(initial = com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.viewModel.LinkState.Loading)

    when (val state = linkState) {
        is com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.viewModel.LinkState.Loading -> {
            OriginalLinkPreview(url, modifier)
        }
        is com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.viewModel.LinkState.Success -> {
            state.metadata?.let { meta ->
                Box(modifier = modifier.clickableWithNoIndication { 
                    openLinkIntent(url, context)
                }) {
                    com.exa.android.reflekt.loopit.presentation.main.post.create.SharedLinkPreviewCard(
                        isLoading = false,
                        title = meta.title,
                        description = meta.description,
                        imageUrl = meta.imageUrl,
                        domain = meta.domain,
                        url = url,
                        onDismiss = null
                    )
                }
            } ?: run {
                OriginalLinkPreview(url, modifier)
            }
        }
        is com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.linkPreview.viewModel.LinkState.Error -> {
            OriginalLinkPreview(url, modifier)
        }
    }
}

@Composable
private fun OriginalLinkPreview(
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
                .clickableWithNoIndication { openLinkIntent(url, context) }
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
