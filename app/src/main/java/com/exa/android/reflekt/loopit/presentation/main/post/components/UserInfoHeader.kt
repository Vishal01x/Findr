package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.core.utils.formatTimestamp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.theme.*
import com.google.firebase.Timestamp

/**
 * Enhanced user info header with better layout and verified badge
 * Integrated with PostUserInfo from Firestore
 */
@Composable
fun UserInfoHeader(
    userInfo: PostUserInfo,
    postViews: Int,
    timestamp: Long,
    postType: String? = null,
    showBio: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = Spacing.medium, start = Spacing.medium , end = Spacing.medium),
        verticalAlignment = Alignment.Top
    ) {
        // User Avatar with gradient border
        Box(
            modifier = Modifier
                .size(52.dp)
                // THIS ONE CAN BE USED FOR SHOWING TOP PROFILES OR LIVE IN FUTURE
//                .border(
//                    width = 2.dp,
//                    brush = Brush.linearGradient(
//                        colors = listOf(
//                            GradientPurpleStart,
//                            GradientPinkStart
//                        )
//                    ),
                //                   shape = CircleShape
                //              )
                .padding(3.dp)
        ) {

//            if (userInfo.avatarUrl != null) {
//                AsyncImage(
//                    model = userInfo.avatarUrl ?: "https://i.pravatar.cc/150?u=${userInfo.uid}",
//                    contentDescription = "${userInfo.name}'s avatar",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.surfaceVariant),
//                    contentScale = ContentScale.Crop
//                )
//            } else {
//                // Placeholder with initials
//                Text(
//                    text = getInitials(userInfo.name),
//                    style = MaterialTheme.typography.titleSmall,
//                    fontWeight = FontWeight.Bold,
//                    color = Color.White,
//                    modifier = Modifier.align(Alignment.Center)
//                )
//            }

            UserAvatar(userInfo.avatarUrl, userInfo.name, AvatarSize.Large)

        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        // User Info Column
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Name and verified badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = userInfo.name.ifEmpty { "Anonymous" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Verified badge
                if (userInfo.isVerified) {
                    Icon(
                        imageVector = Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2), // Twitter-like blue
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Course, year- STATIC FOR NOW
            Row(
                verticalAlignment = Alignment.CenterVertically,
               modifier = Modifier.padding(top = Spacing.verySmall)
            ) {
                // Static college info as requested
                Text(
                    text = "Computer Science", //Computer Science • 3rd Year",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // date, views, post type
            Row(
                verticalAlignment = Alignment.CenterVertically,
               modifier = Modifier.padding(top = Spacing.verySmall)
            ) {
//                Text(
//                    text = "@${userInfo.username}",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )


                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (postViews > 0) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Icon(
                        Icons.Rounded.Visibility,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = "Views",
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier.size(1.dp))

                    Text(
                        text = formatCount(postViews),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (postType != null && postType.isNotBlank()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = postType,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Optional bio
            if (showBio && userInfo.bio != null) {
                Text(
                    text = userInfo.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 2
                )
            }
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