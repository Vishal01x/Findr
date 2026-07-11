package com.exa.android.reflekt.loopit.presentation.main.notification.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType
import com.exa.android.reflekt.loopit.presentation.main.notification.vm.NotificationViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToChat: ((String) -> Unit)? = null,
    onNavigateToProfile: ((String) -> Unit)? = null,
    onNavigateToPost: ((String) -> Unit)? = null
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val headerBg = MaterialTheme.colorScheme.surface
    val divider = LiquidColors.GlassBorder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBg)
                .drawBehind {
                    drawLine(divider, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                }
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = LiquidColors.TextPrimary
                    )
                }
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (unreadCount > 0) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(
                            text = "Mark all read",
                            style = MaterialTheme.typography.labelMedium,
                            color = LiquidColors.Primary
                        )
                    }
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = LiquidColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ── Category chips ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.small)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            NotificationCategory.entries.forEach { category ->
                NotifFilterChip(
                    label = category.displayName,
                    selected = selectedCategory == category,
                    onClick = { viewModel.setCategory(category) }
                )
            }
        }

        // ── Content ───────────────────────────────────────────────────────────
        if (notifications.isEmpty()) {
            NotificationEmptyState()
        } else {
            val (todayList, yesterdayList, earlierList) = groupNotifications(notifications)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (todayList.isNotEmpty()) {
                    item(key = "header_today") {
                        NotifSectionHeader("Today")
                    }
                    items(todayList, key = { it.id }) { notif ->
                        NotifItem(
                            notification = notif,
                            onClick = {
                                if (!notif.isRead) viewModel.markAsRead(notif.id)
                                handleNotifNavigation(notif, onNavigateToRoute, onNavigateToChat, onNavigateToProfile, onNavigateToPost)
                            },
                            onAcceptInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.acceptInvitation(notif) }} else null,
                            onRejectInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.rejectInvitation(notif) }} else null
                        )
                    }
                }
                if (yesterdayList.isNotEmpty()) {
                    item(key = "header_yesterday") {
                        NotifSectionHeader("Yesterday")
                    }
                    items(yesterdayList, key = { it.id }) { notif ->
                        NotifItem(
                            notification = notif,
                            onClick = {
                                if (!notif.isRead) viewModel.markAsRead(notif.id)
                                handleNotifNavigation(notif, onNavigateToRoute, onNavigateToChat, onNavigateToProfile, onNavigateToPost)
                            },
                            onAcceptInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.acceptInvitation(notif) }} else null,
                            onRejectInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.rejectInvitation(notif) }} else null
                        )
                    }
                }
                if (earlierList.isNotEmpty()) {
                    item(key = "header_earlier") {
                        NotifSectionHeader("Earlier")
                    }
                    items(earlierList, key = { it.id }) { notif ->
                        NotifItem(
                            notification = notif,
                            onClick = {
                                if (!notif.isRead) viewModel.markAsRead(notif.id)
                                handleNotifNavigation(notif, onNavigateToRoute, onNavigateToChat, onNavigateToProfile, onNavigateToPost)
                            },
                            onAcceptInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.acceptInvitation(notif) }} else null,
                            onRejectInvitation = if (notif.type == NotificationType.INVITATION) {{ viewModel.rejectInvitation(notif) }} else null
                        )
                    }
                }
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun NotifSectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = LiquidColors.TextSecondary,
        modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
    )
}

// ── Notification item ─────────────────────────────────────────────────────────

@Composable
private fun NotifItem(
    notification: Notification,
    onClick: () -> Unit,
    onAcceptInvitation: (() -> Unit)? = null,
    onRejectInvitation: (() -> Unit)? = null
) {
    val unreadBg = LiquidColors.Primary.copy(alpha = 0.06f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (!notification.isRead) unreadBg else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.medium, vertical = 12.dp)
    ) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar + type badge
        Box(modifier = Modifier.size(48.dp)) {
            NotifAvatar(
                imageUrl = notification.senderImageUrl,
                name = notification.senderName ?: notification.title,
                modifier = Modifier.size(48.dp)
            )
            // Type icon badge
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(notifTypeColor(notification.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = notifTypeIcon(notification.type),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    color = LiquidColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = formatRelativeTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.TextSecondary
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodySmall,
                color = LiquidColors.TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (!notification.isRead) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(8.dp)
                    .background(LiquidColors.Primary, CircleShape)
            )
        }
    }
    if (onAcceptInvitation != null && onRejectInvitation != null) {
        Row(
            modifier = Modifier
                .padding(start = 60.dp, top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRejectInvitation,
                modifier = Modifier.weight(1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, LiquidColors.TextSecondary.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("Decline", style = MaterialTheme.typography.labelMedium, color = LiquidColors.TextSecondary)
            }
            Button(
                onClick = onAcceptInvitation,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                Text("Accept", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    }
}

// ── Avatar ────────────────────────────────────────────────────────────────────

@Composable
private fun NotifAvatar(imageUrl: String?, name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(LiquidColors.Primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                color = LiquidColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// ── Filter chip ───────────────────────────────────────────────────────────────

@Composable
private fun NotifFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) LiquidColors.Primary.copy(alpha = 0.15f) else LiquidColors.GlassSurface,
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

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun NotificationEmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Notifications,
                    contentDescription = null,
                    tint = LiquidColors.Primary,
                    modifier = Modifier.size(40.dp)
                )
            }
            Text(
                text = "You're all caught up!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = LiquidColors.TextPrimary
            )
            Text(
                text = "New activity will show up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = LiquidColors.TextSecondary
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private data class GroupedNotifications(
    val today: List<Notification>,
    val yesterday: List<Notification>,
    val earlier: List<Notification>
)

private fun groupNotifications(list: List<Notification>): GroupedNotifications {
    val now = Calendar.getInstance()
    val todayCal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    val yesterdayCal = (todayCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    val today = list.filter { it.timestamp >= todayCal.timeInMillis }
    val yesterday = list.filter { it.timestamp >= yesterdayCal.timeInMillis && it.timestamp < todayCal.timeInMillis }
    val earlier = list.filter { it.timestamp < yesterdayCal.timeInMillis }
    return GroupedNotifications(today, yesterday, earlier)
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        diff < 172_800_000L -> "Yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

private fun notifTypeIcon(type: NotificationType): ImageVector = when (type) {
    NotificationType.MESSAGE -> Icons.Rounded.ChatBubble
    NotificationType.COMMENT -> Icons.AutoMirrored.Rounded.Comment
    NotificationType.ENROLL_REQUEST -> Icons.Rounded.PersonAdd
    NotificationType.ENROLL_ACCEPT -> Icons.Rounded.CheckCircle
    NotificationType.ENROLL_REJECT -> Icons.Rounded.Cancel
    NotificationType.INVITATION -> Icons.Rounded.MailOutline
    NotificationType.POST -> Icons.AutoMirrored.Rounded.Article
    NotificationType.EVENT -> Icons.Rounded.Event
    NotificationType.VIEW -> Icons.Rounded.Visibility
    NotificationType.ALERT -> Icons.Rounded.Notifications
}

private fun notifTypeColor(type: NotificationType): Color = when (type) {
    NotificationType.MESSAGE -> Color(0xFF13B6EC)
    NotificationType.COMMENT -> Color(0xFF8B5CF6)
    NotificationType.ENROLL_REQUEST -> Color(0xFF22C55E)
    NotificationType.ENROLL_ACCEPT -> Color(0xFF10B981) // Emerald 500
    NotificationType.ENROLL_REJECT -> Color(0xFFEF4444) // Red 500
    NotificationType.INVITATION -> Color(0xFF6366F1)
    NotificationType.POST -> Color(0xFFF59E0B)
    NotificationType.EVENT -> Color(0xFFEC4899)
    NotificationType.VIEW -> Color(0xFF14B8A6)
    NotificationType.ALERT -> Color(0xFFF43F5E)
}

private val NotificationCategory.displayName: String
    get() = when (this) {
        NotificationCategory.ALL -> "All"
        NotificationCategory.PLACEMENTS -> "Placements"
        NotificationCategory.SOCIAL -> "Social"
        NotificationCategory.EVENTS -> "Events"
    }

private fun handleNotifNavigation(
    notif: Notification,
    onRoute: (String) -> Unit,
    onChat: ((String) -> Unit)?,
    onProfile: ((String) -> Unit)?,
    onPost: ((String) -> Unit)?
) {
    val route = notif.route
    if (!route.isNullOrBlank()) {
        if (route.startsWith("myapp://chat")) {
            val uid = notif.senderId
            if (onChat != null && !uid.isNullOrBlank()) onChat(uid)
        } else if (route.startsWith("myapp://profile")) {
            val uid = notif.senderId
            if (onProfile != null && !uid.isNullOrBlank()) onProfile(uid)
        } else if (route.startsWith("myapp://project") || route.startsWith("myapp://comments")) {
            val postId = notif.targetId
            if (onPost != null && !postId.isNullOrBlank()) onPost(postId)
        } else {
            onRoute(route)
        }
        return
    }
    
    when (notif.type) {
        NotificationType.MESSAGE -> {
            val uid = notif.senderId
            if (onChat != null && !uid.isNullOrBlank()) onChat(uid)
            else notif.route?.let { onRoute(it) }
        }
        NotificationType.VIEW -> {
            val uid = notif.senderId
            if (onProfile != null && !uid.isNullOrBlank()) onProfile(uid)
            else notif.route?.let { onRoute(it) }
        }
        NotificationType.COMMENT, NotificationType.POST,
        NotificationType.ENROLL_REQUEST, NotificationType.ENROLL_ACCEPT, NotificationType.ENROLL_REJECT, 
        NotificationType.INVITATION, NotificationType.EVENT -> {
            val postId = notif.targetId
            if (onPost != null && !postId.isNullOrBlank()) onPost(postId)
            else notif.route?.let { onRoute(it) }
        }
        NotificationType.ALERT -> notif.route?.let { onRoute(it) }
    }
}
