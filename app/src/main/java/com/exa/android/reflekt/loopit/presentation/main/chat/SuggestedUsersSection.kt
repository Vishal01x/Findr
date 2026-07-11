package com.exa.android.reflekt.loopit.presentation.main.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import com.exa.android.reflekt.loopit.data.feature.main.suggestion.SuggestedUser
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.post.components.AvatarSize
import com.exa.android.reflekt.loopit.presentation.main.post.components.UserAvatar

@Composable
fun SuggestedUsersSection(
    suggestions: List<SuggestedUser>,
    onUserClick: (String) -> Unit
) {
    if (suggestions.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "PEOPLE YOU MAY KNOW",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = LiquidColors.TextSecondary,
                letterSpacing = 1.2.sp
            )
            Text(
                "Based on your skills",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.Primary,
                fontSize = 10.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(suggestions, key = { it.userId }) { user ->
                SuggestionCard(user = user, onClick = { onUserClick(user.userId) })
            }
        }
    }
}

@Composable
private fun SuggestionCard(user: SuggestedUser, onClick: () -> Unit) {
    val accentColor = LiquidColors.Primary

    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .clickableWithNoIndication { onClick() }
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar with gradient ring
        Box(modifier = Modifier.size(56.dp)) {
            UserAvatar(
                imageUrl = user.profileImageUrl,
                userName = user.name,
                size = AvatarSize.Pinned,
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(accentColor, accentColor.copy(alpha = 0.4f))
                        ),
                        shape = CircleShape
                    )
            )
            // Points badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (user.points >= 99) "99+" else "${user.points}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Name
        Text(
            text = user.name.split(" ").firstOrNull() ?: user.name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = LiquidColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Headline
        if (user.headline.isNotBlank()) {
            Text(
                text = user.headline,
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 10.sp
            )
        }

        // Matched skills chips (up to 2)
        val displaySkills = user.matchedSkills.take(2).ifEmpty { user.skills.take(2) }
        if (displaySkills.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                displaySkills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (user.matchedSkills.contains(skill))
                                    accentColor.copy(alpha = 0.12f)
                                else
                                    LiquidColors.GlassBorder.copy(alpha = 0.3f)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = skill.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (user.matchedSkills.contains(skill))
                                accentColor
                            else
                                LiquidColors.TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 9.sp,
                            fontWeight = if (user.matchedSkills.contains(skill))
                                FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
