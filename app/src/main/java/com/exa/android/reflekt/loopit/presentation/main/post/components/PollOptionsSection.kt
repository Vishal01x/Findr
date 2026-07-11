package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

/**
 * Poll voting UI - shared between the feed's inline card and the Post Detail screen.
 * Tapping an option calls [onVote]; the caller is responsible for reflecting the new
 * vote count back into [post] (typically via a live Firestore listener).
 */
@Composable
fun PollOptionsSection(
    post: Post,
    currentUserId: String,
    onVote: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalVotes = post.totalPollVotes()
    val votedIndex = post.userPollVoteIndex(currentUserId)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        post.pollOptions.forEachIndexed { index, option ->
            val isSelected = index == votedIndex
            val fraction = if (totalVotes > 0) option.votes.size.toFloat() / totalVotes else 0f
            val animatedFraction by animateFloatAsState(
                targetValue = fraction,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "pollFraction"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LiquidColors.GlassSurface)
                    .border(
                        1.dp,
                        if (isSelected) LiquidColors.Primary else LiquidColors.GlassBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .clickableWithNoIndication { onVote(index) }
            ) {
                // Fill bar showing vote share
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedFraction.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isSelected) LiquidColors.Primary.copy(alpha = 0.25f)
                            else LiquidColors.Primary.copy(alpha = 0.10f)
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "Your vote",
                                tint = LiquidColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = LiquidColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                    if (totalVotes > 0) {
                        Text(
                            text = "${(fraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) LiquidColors.Primary else LiquidColors.TextSecondary
                        )
                    }
                }
            }
        }

        Text(
            text = if (totalVotes == 1) "1 vote" else "$totalVotes votes",
            style = MaterialTheme.typography.labelSmall,
            color = LiquidColors.TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
