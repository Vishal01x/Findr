package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.theme.*

/**
 * User avatar component with automatic placeholder generation
 * Displays user avatar image or generates a gradient placeholder with initials
 */
@Composable
fun UserAvatar(
    imageUrl: String?,
    userName: String,
    size: AvatarSize = AvatarSize.Medium,
    modifier: Modifier = Modifier
) {
    val initials = getInitials(userName)
    val gradientColors = getGradientForUser(userName)
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .then(
                if (imageUrl.isNullOrBlank()) {
                    Modifier.background(
                        Brush.linearGradient(gradientColors)
                    )
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrBlank()) {
            // Show initials placeholder
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = size.fontSize.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White
            )
        } else {
            // Show avatar image
            AsyncImage(
                model = imageUrl,
                contentDescription = "Avatar of $userName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Avatar size variants
 */
enum class AvatarSize(val dp: Dp, val fontSize: Int) {
    ExtraSmall(24.dp, 10),
    Small(32.dp, 12),
    Medium(40.dp, 16),
    Large(56.dp, 20),
    ExtraLarge(80.dp, 28)
}

/**
 * Extract initials from user name
 */
private fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return when {
        parts.size >= 2 -> "${parts[0].firstOrNull()?.uppercaseChar() ?: ""}${parts[1].firstOrNull()?.uppercaseChar() ?: ""}"
        parts.size == 1 && parts[0].length >= 2 -> "${parts[0][0].uppercaseChar()}${parts[0][1].uppercaseChar()}"
        parts.size == 1 -> parts[0].firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        else -> "?"
    }
}

/**
 * Get consistent gradient colors for a user based on their name
 * This ensures the same user always gets the same color scheme
 */
private fun getGradientForUser(name: String): List<Color> {
    val gradients = listOf(
//        listOf(GradientPurpleStart, GradientPurpleEnd),
//        listOf(GradientPinkStart, GradientPinkEnd),
//        listOf(GradientTealStart, GradientTealEnd),
//        listOf(GradientVibeStart, GradientVibeEnd),
//        listOf(TagGradient1Start, TagGradient1End),
//        listOf(TagGradient2Start, TagGradient2End),
//        listOf(TagGradient3Start, TagGradient3End)
        listOf(GradientPrimaryStart, GradientPrimaryEnd)
    )
    
    // Use hashCode to deterministically select a gradient
    val index = name.hashCode().let { if (it < 0) -it else it } % gradients.size
    return gradients[index]
}
