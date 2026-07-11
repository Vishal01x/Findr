package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.core.utils.formatTimeAgo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import com.exa.android.reflekt.loopit.theme.*
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.exa.android.reflekt.R

@Composable
fun UserInfoHeader(
    userInfo: PostUserInfo,
    postViews: Int,
    timestamp: Long,
    isAnonymous: Boolean = false,
    postType: String? = null,
    tagBadge: String? = null,
    showBio: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (!isAnonymous) Modifier.clickableWithNoIndication(onClick = onClick) else Modifier)
            .padding(top = Spacing.medium, start = Spacing.medium, end = Spacing.medium),
        verticalAlignment = Alignment.Top
    ) {
        // Avatar
        Box(modifier = Modifier.size(52.dp).padding(3.dp)) {
            if (isAnonymous) {
                AnonymousAvatar()
            } else {
                UserAvatar(userInfo.avatarUrl, userInfo.name, AvatarSize.Large)
            }
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(modifier = Modifier.weight(1f)) {
            // Name row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isAnonymous) "Anonymous" else userInfo.name.ifEmpty { "Anonymous" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAnonymous) LiquidColors.TextSecondary else LiquidColors.TextPrimary
                )
                if (!isAnonymous && userInfo.isVerified) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Academic chips (branch / year / section) or "Identity hidden" for anonymous
            if (isAnonymous) {
                Text(
                    text = "Identity hidden",
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidColors.TextSecondary,
                    modifier = Modifier.padding(top = Spacing.verySmall)
                )
            } else {
                val chips = listOfNotNull(
                    userInfo.branch?.ifBlank { null }?.let { abbreviateStream(it) },
                    userInfo.section?.ifBlank { null },
                    userInfo.year?.ifBlank { null }
                )
                if (chips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        chips.forEach { chip ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LiquidColors.Primary.copy(alpha = 0.10f))
                                    .border(1.dp, LiquidColors.Primary.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = LiquidColors.Primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Timestamp · views · type
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = Spacing.verySmall)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = "Time",
                        modifier = Modifier.size(13.dp),
                        tint = LiquidColors.TextSecondary
                    )
                    Text(
                        text = formatTimeAgo(timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextSecondary
                    )
                }

//                if (postViews > 0) {
//                    Text(
//                        text = " • ",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = LiquidColors.TextSecondary
//                    )
//                    Icon(
//                        Icons.Rounded.Visibility,
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                        contentDescription = "Views",
//                        modifier = Modifier.size(14.dp)
//                    )
//                    Spacer(modifier = Modifier.size(1.dp))
//                    Text(
//                        text = formatCount(postViews),
//                        style = MaterialTheme.typography.bodySmall,
//                        color = LiquidColors.TextSecondary
//                    )
//                }

//                if (postType != null && postType.isNotBlank()) {
//                    Text(
//                        text = " • ",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = LiquidColors.TextSecondary
//                    )
//                    Text(
//                        text = postType,
//                        style = MaterialTheme.typography.bodySmall,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
            }

            if (showBio && !isAnonymous && userInfo.bio != null) {
                Text(
                    text = userInfo.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = LiquidColors.TextSecondary.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2
                )
            }
        }

        // Trailing tag badge (first post tag, shown like project type badge)
        if (tagBadge != null) {
            Spacer(modifier = Modifier.width(Spacing.small))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .background(LiquidColors.Primary.copy(alpha = 0.08f))
                    .border(1.dp, LiquidColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(99.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Post", //tagBadge.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.Primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

/** Gray incognito avatar shown for anonymous posts. */
@Composable
fun AnonymousAvatar(size: androidx.compose.ui.unit.Dp = 46.dp) {
    val bg = if (androidx.compose.foundation.isSystemInDarkTheme()) Color(0xFF101D22) else Color(0xFFD1D5DB)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_anonymous_avatar),
            contentDescription = "Anonymous",
            modifier = Modifier.fillMaxSize()
        )
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${count / 1_000_000}M"
        count >= 1_000 -> "${count / 1_000}K"
        else -> count.toString()
    }
}
