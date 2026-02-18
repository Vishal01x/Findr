package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Shimmer loading effect for comments
 * Shows animated placeholders while comments are loading
 */
@Composable
fun CommentShimmer(
    count: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        repeat(count) {
            CommentShimmerItem()
        }
    }
}

@Composable
fun CommentShimmerItem(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim, translateAnim),
        end = Offset(translateAnim + 200f, translateAnim + 200f)
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        // Avatar shimmer
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(brush)
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
        ) {
            // Name shimmer
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            // Comment text shimmer (2 lines)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(Spacing.extraSmall))

            // Action buttons shimmer
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
                
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Reply shimmer - smaller indent version
 */
@Composable
fun ReplyShimmer(
    count: Int = 2,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 52.dp), // Indent for replies
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        repeat(count) {
            CommentShimmerItem()
        }
    }
}
