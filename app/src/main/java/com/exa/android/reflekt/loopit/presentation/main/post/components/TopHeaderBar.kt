package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.theme.*
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
fun TopHeaderBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: PostType?,
    onFilterSelected: (PostType) -> Unit,
    isSearchActive: Boolean = false,
    onSearchActivate: () -> Unit = {},
    onSearchDeactivate: () -> Unit = {},
    visible: Boolean = true,
    userName: String = "User",
    userProfileUrl: String? = null,
    unreadNotificationCount: Int = 0,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slideProgress by animateFloatAsState(
        targetValue = if (visible) 0f else -1f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "header_slide"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "header_alpha"
    )

    val headerBg = MaterialTheme.colorScheme.surface
    val divider = LiquidColors.GlassBorder

    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = slideProgress * size.height
                this.alpha = alpha
            }
            .fillMaxWidth()
            .background(headerBg)
            .drawBehind {
                drawLine(
                    color = divider,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        if (isSearchActive) {
            SearchActiveHeader(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onBack = onSearchDeactivate
            )
        } else {
            NormalHeader(
                searchQuery = searchQuery,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
                onSearchActivate = onSearchActivate,
                userName = userName,
                userProfileUrl = userProfileUrl,
                unreadNotificationCount = unreadNotificationCount,
                onProfileClick = onProfileClick,
                onNotificationClick = onNotificationClick
            )
        }
    }
}

@Composable
private fun SearchActiveHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.small, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = LiquidColors.TextPrimary
            )
        }

        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.medium, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = LiquidColors.TextPrimary
                    ),
                    cursorBrush = SolidColor(LiquidColors.Primary),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search posts, people...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LiquidColors.TextSecondary
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = "Clear",
                    tint = LiquidColors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun NormalHeader(
    searchQuery: String,
    selectedFilter: PostType?,
    onFilterSelected: (PostType) -> Unit,
    onSearchActivate: () -> Unit,
    userName: String,
    userProfileUrl: String?,
    unreadNotificationCount: Int = 0,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium)
            .padding(top = Spacing.small, bottom = Spacing.small)
    ) {
        // Row 1: Logo on left, notification + avatar on right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiquidColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Findr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationCount > 0) {
                            Badge {
                                Text(
                                    text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notifications",
                            tint = LiquidColors.TextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clickable { onProfileClick() }
                        .padding(4.dp)
                ) {
                    UserAvatar(
                        imageUrl = userProfileUrl,
                        userName = userName,
                        size = AvatarSize.Small
                    )
                }
            }
        }

        // Row 2: Tappable search bar (activates search mode on click)
        SearchTapTarget(
            query = searchQuery,
            onClick = onSearchActivate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.small)
        )

        // Row 3: Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            PostType.values().forEach { type ->
                ModernFilterChip(
                    label = type.displayName,
                    selected = selectedFilter == type || (selectedFilter == null && type == PostType.ALL),
                    onClick = { onFilterSelected(type) }
                )
            }
        }
    }
}

@Composable
private fun SearchTapTarget(
    query: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = LiquidColors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(Spacing.small))
            Text(
                text = if (query.isNotEmpty()) query else "Search posts, people...",
                style = MaterialTheme.typography.bodyMedium,
                color = if (query.isNotEmpty()) LiquidColors.TextPrimary else LiquidColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ModernFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) LiquidColors.Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, LiquidColors.Primary) else null
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) LiquidColors.Primary else LiquidColors.TextSecondary
            )
        }
    }
}
