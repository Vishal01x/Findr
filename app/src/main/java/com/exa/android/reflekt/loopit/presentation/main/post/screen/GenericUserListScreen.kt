package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidTopBar
import com.exa.android.reflekt.loopit.presentation.main.post.components.AnonymousAvatar

// ── Data model ────────────────────────────────────────────────────────────────

/**
 * UI-layer data class representing a user entry in any generic user list
 * (event participants, project members, role holders, etc.).
 */
data class UserListItem(
    val userId: String,
    val userName: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val rank: String? = null,
    val section: String? = null,
    val passout: String? = null,
    val resume: String? = null
)

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * A reusable screen that renders an arbitrary list of [UserListItem]s with a
 * LiquidTopBar, dark-mode-friendly theming, and per-item click handling.
 *
 * @param title       Top-bar title, e.g. "Participants" or "Team Members".
 * @param users       The list of users to display.
 * @param onBackClick Invoked when the user presses the navigation/back icon.
 * @param onUserClick Invoked with the [UserListItem.userId] when a row is tapped.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericUserListScreen(
    title: String,
    users: List<UserListItem>,
    onBackClick: () -> Unit,
    onUserClick: (userId: String) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = LiquidColors.BgBase,
        topBar = {
            LiquidTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LiquidColors.TextPrimary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (users.isEmpty()) {
            EmptyUsersPlaceholder(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Count chip header
                item(key = "count_chip") {
                    UserCountChip(count = users.size)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // User rows
                items(items = users, key = { it.userId }) { user ->
                    UserListRow(
                        user = user,
                        onClick = { onUserClick(user.userId) }
                    )
                }

                item(key = "bottom_spacer") {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── Internal subcomponents ────────────────────────────────────────────────────

/** Small pill showing total member count. */
@Composable
private fun UserCountChip(count: Int) {
    val primary = LiquidColors.Primary
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(primary.copy(alpha = 0.12f))
            .border(1.dp, primary.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$count ${if (count == 1) "member" else "members"}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = primary
        )
    }
}

/**
 * Card row for a single user.
 * Displays avatar, name, bio (1 line), and meta chips (rank / section / passout).
 */
@Composable
private fun UserListRow(
    user: UserListItem,
    onClick: () -> Unit
) {
    val surface       = LiquidColors.Surface
    val border        = LiquidColors.Border
    val primary       = LiquidColors.Primary
    val textPrimary   = LiquidColors.TextPrimary
    val textSecondary = LiquidColors.TextSecondary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            UserRowAvatar(
                avatarUrl = user.avatarUrl,
                userName  = user.userName,
                accent    = primary
            )

            // Name + bio + meta
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.userName.ifBlank { "Unknown" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!user.bio.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        color = textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                val metaParts = listOfNotNull(user.rank, user.section, user.passout)
                if (metaParts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(5.dp))
                    MetaChipsRow(parts = metaParts)
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = textSecondary.copy(alpha = 0.50f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Circular avatar. Priority:
 *  1. AsyncImage when [avatarUrl] is non-blank
 *  2. Gradient circle with initial letter
 *  3. AnonymousAvatar fallback
 */
@Composable
private fun UserRowAvatar(
    avatarUrl: String?,
    userName: String,
    accent: Color,
    size: Dp = 48.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.5.dp, accent.copy(alpha = 0.30f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when {
            !avatarUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = userName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            userName.isNotBlank() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    accent.copy(alpha = 0.35f),
                                    accent.copy(alpha = 0.12f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        fontSize = (size.value * 0.40f).sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }
            else -> {
                AnonymousAvatar(size = size)
            }
        }
    }
}

/** A horizontal row of small tag chips separated by dot dividers. */
@Composable
private fun MetaChipsRow(parts: List<String>) {
    val textSecondary = LiquidColors.TextSecondary
    val chipBg        = LiquidColors.BgBase
    val chipBorder    = LiquidColors.Border

    Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        parts.forEachIndexed { index, part ->
            if (index > 0) {
                // Dot separator
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(textSecondary.copy(alpha = 0.35f))
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(chipBg)
                    .border(1.dp, chipBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = part,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary,
                    maxLines = 1
                )
            }
        }
    }
}

/** Friendly empty-state view shown when the user list is empty. */
@Composable
private fun EmptyUsersPlaceholder(modifier: Modifier = Modifier) {
    val primary       = LiquidColors.Primary
    val textPrimary   = LiquidColors.TextPrimary
    val textSecondary = LiquidColors.TextSecondary

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Decorative avatar circle
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(primary.copy(alpha = 0.08f))
                .border(1.dp, primary.copy(alpha = 0.20f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👥", fontSize = 32.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "No members yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Members will appear here once they join.",
            fontSize = 13.sp,
            color = textSecondary
        )
    }
}
