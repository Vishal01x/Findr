package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.exa.android.reflekt.loopit.presentation.main.post.components.MarkdownText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import java.text.SimpleDateFormat
import java.util.Locale

import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

@Composable
internal fun EventDetailContent(
    post: Post,
    currentUserId: String = "",
    isRegistered: Boolean,
    isAuthor: Boolean,
    onRegisterClick: () -> Unit,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onChatClick: () -> Unit,
    onUserClick: () -> Unit,
    onManageClick: () -> Unit,
    onViewAllParticipants: () -> Unit = {},
    onToggleEventClosure: (Boolean) -> Unit = {},
    onUpdateCapacity: (Int) -> Unit = {},
    onOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val eventDetails = post.eventDetails ?: return
    val scrollState = rememberScrollState()
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    val isLiked = post.likes.contains(currentUserId)

    val bgBase = LiquidColors.BgBase

    Box(modifier = modifier.fillMaxSize().background(bgBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
        ) {
            HeroSection(
                imageUrl = post.imageUrls.firstOrNull(),
                isVirtual = eventDetails.isVirtual,
                category = post.tags.firstOrNull()
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = LiquidColors.TextPrimary,
                    lineHeight = 36.sp
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.CalendarToday,
                        contentDescription = null,
                        tint = LiquidColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buildDateTimeString(eventDetails),
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextSecondary
                    )
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = LiquidColors.Border)
                Spacer(Modifier.height(12.dp))

                HostRow(
                    post = post,
                    isAuthor = isAuthor,
                    onUserClick = onUserClick,
                    onManageClick = onManageClick
                )

                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = LiquidColors.Border)
                Spacer(Modifier.height(20.dp))

                UtilityButtonsRow(
                    eventDetails = eventDetails,
                    post = post,
                    context = context,
                    onChatClick = onChatClick
                )
                Spacer(Modifier.height(24.dp))

                AboutSection(
                    description = post.description,
                    isExpanded = isDescriptionExpanded,
                    onToggle = { isDescriptionExpanded = !isDescriptionExpanded }
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = if (eventDetails.isVirtual) "Event Link" else "Location",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary
                )
                Spacer(Modifier.height(12.dp))

                if (eventDetails.isVirtual) {
                    VirtualLocationCard(
                        eventDetails = eventDetails,
                        isRegistered = isRegistered
                    )
                } else {
                    PhysicalLocationCard(eventDetails = eventDetails, context = context)
                }

                Spacer(Modifier.height(24.dp))

                if (eventDetails.capacity != null) {
                    ParticipantsSection(
                        eventDetails = eventDetails,
                        isAuthor = isAuthor,
                        onViewAll = onViewAllParticipants
                    )
                    Spacer(Modifier.height(8.dp))
                }

                // Author-only controls: close registration + increase capacity
                if (isAuthor) {
                    AuthorEventControls(
                        eventDetails = eventDetails,
                        onToggleClosure = onToggleEventClosure,
                        onUpdateCapacity = onUpdateCapacity
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        FloatingHeader(
            isLiked = isLiked,
            onBackClick = onBackClick,
            onFavoriteClick = onFavoriteClick,
            onShareClick = onShareClick,
            onOptionsClick = onOptionsClick
        )

        if (!isAuthor && (isRegistered || eventDetails.isRegistrationOpen())) {
            EventBottomBar(
                eventDetails = eventDetails,
                isRegistered = isRegistered,
                isAuthor = isAuthor,
                onRegisterClick = onRegisterClick,
                context = context,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun HeroSection(imageUrl: String?, isVirtual: Boolean, category: String?) {
    val bgBase = LiquidColors.BgBase
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            StylizedIconHeader(category = category)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, bgBase)
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventBadge(text = if (isVirtual) "Virtual" else "Physical", isPrimary = true)
            if (!category.isNullOrBlank()) {
                EventBadge(text = category, isPrimary = false)
            }
        }
    }
}

@Composable
private fun FloatingHeader(
    isLiked: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onOptionsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlassIconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isLiked) Color(0xFFEF4444) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            GlassIconButton(onClick = onShareClick) {
                Icon(Icons.Rounded.Share, "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }
            GlassIconButton(onClick = onOptionsClick) {
                Icon(Icons.Rounded.MoreVert, "Options", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.3f))
            .clickableWithNoIndication(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
 fun EventBadge(text: String, isPrimary: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isPrimary) LiquidColors.Primary else Color.White.copy(alpha = 0.2f))
            .border(
                1.dp,
                if (isPrimary) Color.Transparent else Color.White.copy(alpha = 0.2f),
                RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 0.8.sp
        )
    }
}

private fun getCategoryIcon(category: String?): ImageVector {
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

@Composable
private fun StylizedIconHeader(category: String?) {
    val categoryIcon = getCategoryIcon(category)
    val surface = LiquidColors.Surface
    val bgBase = LiquidColors.BgBase
    val isDark = isSystemInDarkTheme()
    val dotColor = if (isDark) Color(0x2613B6EC) else Color(0x3513B6EC)

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(listOf(surface, bgBase)))
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 20.dp.toPx()
            val radius = 1.dp.toPx()
            val cols = (size.width / spacing).toInt() + 1
            val rows = (size.height / spacing).toInt() + 1
            repeat(cols + 1) { col ->
                repeat(rows + 1) { row ->
                    drawCircle(
                        color = dotColor,
                        radius = radius,
                        center = Offset(col * spacing, row * spacing)
                    )
                }
            }
        }
        Icon(
            imageVector = categoryIcon,
            contentDescription = null,
            tint = LiquidColors.Primary.copy(alpha = 0.08f),
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .rotate(15f)
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .rotate(3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(LiquidColors.Primary, Color(0xFF67E8F9))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.6f to Color.Transparent,
                            1f to bgBase
                        )
                    )
                )
        )
    }
}

@Composable
private fun HostRow(
    post: Post,
    isAuthor: Boolean,
    onUserClick: () -> Unit,
    onManageClick: () -> Unit
) {
    val border = LiquidColors.Border
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .then(if (!post.isAnonymous) Modifier.clickableWithNoIndication(onClick = onUserClick) else Modifier)
        ) {
            if (post.isAnonymous) {
                com.exa.android.reflekt.loopit.presentation.main.post.components.AnonymousAvatar(size = 40.dp)
            } else if (post.userInfo?.avatarUrl != null) {
                AsyncImage(
                    model = post.userInfo.avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(2.dp, border, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Primary.copy(alpha = 0.2f))
                        .border(2.dp, border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.createdByName.firstOrNull()?.uppercase() ?: "?",
                        color = LiquidColors.Primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Hosted by",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.TextSecondary
                )
                if (post.isAnonymous) {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.TextPrimary
                    )
                } else {
                    val chips = listOfNotNull(
                        post.userInfo?.branch?.ifBlank { null }?.let { abbreviateStream(it) },
                        post.userInfo?.section?.ifBlank { null },
                        post.userInfo?.year?.ifBlank { null }
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = post.userInfo?.name ?: post.createdByName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.TextPrimary
                        )
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
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                    val bio = post.userInfo?.bio
                    if (!bio.isNullOrBlank()) {
                        Text(
                            text = bio,
                            style = MaterialTheme.typography.labelSmall,
                            fontStyle = FontStyle.Italic,
                            color = LiquidColors.TextSecondary.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        if (isAuthor) {
            TextButton(
                onClick = onManageClick,
                colors = ButtonDefaults.textButtonColors(contentColor = LiquidColors.Primary)
            ) {
                Text("Manage", fontWeight = FontWeight.SemiBold)
            }
        } else {
            Box(
                modifier = Modifier
                    .background(LiquidColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp)
            ) {
                TextButton(
                    onClick = { },
                    colors = ButtonDefaults.textButtonColors(contentColor = LiquidColors.Primary)
                ) {
                    Text("Follow", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun UtilityButtonsRow(
    eventDetails: EventDetails,
    post: Post,
    context: Context,
    onChatClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        UtilityButton(
            icon = Icons.Rounded.EventAvailable,
            label = "Add to Cal",
            modifier = Modifier.weight(1f),
            onClick = { addEventToCalendar(context, post, eventDetails) }
        )
        UtilityButton(
            icon = Icons.Rounded.Share,
            label = "Share",
            modifier = Modifier.weight(1f),
            onClick = {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "Check out: ${post.title}")
                }
                context.startActivity(Intent.createChooser(intent, "Share Event"))
            }
        )
        UtilityButton(
            icon = Icons.Rounded.Forum,
            label = "Chat",
            modifier = Modifier.weight(1f),
            onClick = onChatClick
        )
    }
}

@Composable
private fun UtilityButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val surface = LiquidColors.Surface
    val border = LiquidColors.Border
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(LiquidColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = LiquidColors.Primary, modifier = Modifier.size(20.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LiquidColors.UtilityLabel,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AboutSection(
    description: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Text(
        text = "About Event",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = LiquidColors.TextPrimary
    )
    Spacer(Modifier.height(8.dp))
    MarkdownText(
        text = description,
        style = MaterialTheme.typography.bodyMedium,
        color = LiquidColors.TextSecondary,
        lineHeight = 20.sp,
        maxLines = if (isExpanded) Int.MAX_VALUE else 4,
        overflow = TextOverflow.Ellipsis
    )
    TextButton(
        onClick = onToggle,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = if (isExpanded) "Show less" else "Read more",
            color = LiquidColors.Primary,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun VirtualLocationCard(eventDetails: EventDetails, isRegistered: Boolean) {
    val clipboardManager = LocalClipboardManager.current
    val surface = LiquidColors.Surface
    val border = LiquidColors.Border
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surface,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Videocam,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Virtual Event",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.TextPrimary
                    )
                    Text(
                        text = if (isRegistered && eventDetails.meetingLink != null)
                            eventDetails.meetingLink
                        else
                            "Link available after registration",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (isRegistered && eventDetails.meetingLink != null) {
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString(eventDetails.meetingLink)) }
                ) {
                    Icon(
                        Icons.Rounded.ContentCopy,
                        contentDescription = "Copy link",
                        tint = LiquidColors.Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PhysicalLocationCard(eventDetails: EventDetails, context: Context) {
    val surface = LiquidColors.Surface
    val border = LiquidColors.Border
    val mapBg = LiquidColors.MapPlaceholder
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surface,
        border = BorderStroke(1.dp, border)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(mapBg)
            ) {
                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = LiquidColors.Primary,
                    modifier = Modifier.size(40.dp).align(Alignment.Center)
                )
                val locationQuery = eventDetails.getLocationDisplay()
                TextButton(
                    onClick = {
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(locationQuery)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(surface.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                ) {
                    Text(
                        "Open Maps",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                }
            }
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(LiquidColors.Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.LocationOn, null, tint = LiquidColors.Primary, modifier = Modifier.size(20.dp))
                }
                Column {
                    eventDetails.venue?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LiquidColors.TextPrimary
                        )
                    }
                    eventDetails.location?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall, color = LiquidColors.TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    eventDetails: EventDetails,
    isAuthor: Boolean = false,
    onViewAll: () -> Unit = {}
) {
    val capacity = eventDetails.capacity ?: return
    val registered = eventDetails.registeredCount
    val remaining = eventDetails.remainingCapacity() ?: 0
    val isFull = eventDetails.isFull()
    val fillRatio = (registered.toFloat() / capacity).coerceIn(0f, 1f)

    // Shimmer triggers when registeredCount changes
    var shimmerActive by remember { mutableStateOf(false) }
    LaunchedEffect(registered) {
        if (registered > 0) {
            shimmerActive = true
            kotlinx.coroutines.delay(1800)
            shimmerActive = false
        }
    }
    val shimmerTransition = rememberInfiniteTransition(label = "slot_shimmer")
    val shimmerX by shimmerTransition.animateFloat(
        initialValue = -300f, targetValue = 900f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerX"
    )

    val surface = LiquidColors.Surface
    val border = LiquidColors.Border
    val progressTrack = LiquidColors.ProgressTrack
    val avatarExtra = LiquidColors.AvatarExtra

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Participants",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LiquidColors.TextPrimary
        )
        // Show "View All" to everyone if there are registered users
        if (registered > 0) {
            Text(
                "View All",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.Primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickableWithNoIndication(onClick = onViewAll)
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surface,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (registered > 0) {
                val avatarColors = listOf(
                    Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899),
                    Color(0xFF14B8A6), Color(0xFFF59E0B)
                )
                val maxDisplay = 5
                val displayCount = minOf(registered, maxDisplay)
                val showExtra = registered > maxDisplay
                val avatarSize = 32
                val avatarStep = 22
                val totalWidth = (displayCount + if (showExtra) 1 else 0) * avatarStep + (avatarSize - avatarStep)
                Box(
                    modifier = Modifier
                        .width(totalWidth.dp)
                        .height(avatarSize.dp)
                        .clickableWithNoIndication(onClick = onViewAll)
                ) {
                    for (index in (displayCount - 1) downTo 0) {
                        Box(
                            modifier = Modifier
                                .size(avatarSize.dp)
                                .offset(x = (index * avatarStep).dp)
                                .zIndex((displayCount - index).toFloat())
                                .clip(CircleShape)
                                .background(avatarColors[index % avatarColors.size])
                                .border(2.dp, surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (index + 1).toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (showExtra) {
                        Box(
                            modifier = Modifier
                                .size(avatarSize.dp)
                                .offset(x = (displayCount * avatarStep).dp)
                                .clip(CircleShape)
                                .background(avatarExtra)
                                .border(2.dp, surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+${registered - maxDisplay}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = LiquidColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$registered registered",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = LiquidColors.UtilityLabel
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(progressTrack)
                ) {
                    val barBrush = if (shimmerActive) {
                        Brush.horizontalGradient(
                            colors = listOf(
                                LiquidColors.Primary.copy(alpha = 0.4f),
                                LiquidColors.Primary,
                                Color.White.copy(alpha = 0.7f),
                                LiquidColors.Primary,
                                LiquidColors.Primary.copy(alpha = 0.4f)
                            ),
                            startX = shimmerX,
                            endX = shimmerX + 400f
                        )
                    } else {
                        Brush.horizontalGradient(listOf(LiquidColors.Primary, LiquidColors.Primary))
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fillRatio)
                            .background(barBrush)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (isFull) "Event is full" else "$remaining spots left",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFull) MaterialTheme.colorScheme.error else LiquidColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun AuthorEventControls(
    eventDetails: EventDetails,
    onToggleClosure: (Boolean) -> Unit,
    onUpdateCapacity: (Int) -> Unit
) {
    val isClosed = eventDetails.isManuallyClosed
    val currentCapacity = eventDetails.capacity ?: 0

    var showCapacityDialog by remember { mutableStateOf(false) }
    var capacityInput by remember { mutableStateOf(currentCapacity.toString()) }

    val surface = LiquidColors.Surface
    val border = LiquidColors.Border

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        border = BorderStroke(1.dp, border),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Registration Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            // Close / Reopen toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isClosed) "Registration Closed" else "Registration Open",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isClosed) "Tap to reopen registrations" else "Tap to close registrations",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextMuted
                    )
                }
                Switch(
                    checked = !isClosed,
                    onCheckedChange = { isOpen -> onToggleClosure(!isOpen) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = LiquidColors.Primary
                    )
                )
            }
            HorizontalDivider(color = border)
            // Increase capacity button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Event Capacity",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (currentCapacity > 0) "$currentCapacity total slots" else "Unlimited",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextMuted
                    )
                }
                OutlinedButton(
                    onClick = {
                        capacityInput = currentCapacity.toString()
                        showCapacityDialog = true
                    },
                    border = BorderStroke(1.dp, LiquidColors.Primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = LiquidColors.Primary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Update", color = LiquidColors.Primary, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    if (showCapacityDialog) {
        AlertDialog(
            onDismissRequest = { showCapacityDialog = false },
            title = { Text("Update Capacity") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Set total event capacity. Must be ≥ current registrations (${eventDetails.registeredCount}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = LiquidColors.TextMuted
                    )
                    OutlinedTextField(
                        value = capacityInput,
                        onValueChange = { v -> if (v.all { it.isDigit() } && v.length <= 5) capacityInput = v },
                        label = { Text("Capacity") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newCap = capacityInput.toIntOrNull() ?: 0
                        if (newCap >= eventDetails.registeredCount) {
                            onUpdateCapacity(newCap)
                            showCapacityDialog = false
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCapacityDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EventBottomBar(
    eventDetails: EventDetails,
    isRegistered: Boolean,
    isAuthor: Boolean,
    onRegisterClick: () -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val bgBase = LiquidColors.BgBase
    val border = LiquidColors.Border
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = bgBase.copy(alpha = 0.97f),
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isRegistered) {
                Column {
                    Text(
                        text = "PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Free",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                }
            }

            AnimatedContent(
                targetState = isRegistered,
                label = "event_cta",
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f)
            ) { registered ->
                if (!registered) {
                    val isFull = eventDetails.isFull()
                    val isClosed = eventDetails.isManuallyClosed
                    val canRegister = eventDetails.isRegistrationOpen()
                    val buttonColor = if (canRegister) LiquidColors.Primary else border

                    Button(
                        onClick = {
                            when {
                                canRegister -> onRegisterClick()
                                isFull -> android.widget.Toast.makeText(context, "Slots are full", android.widget.Toast.LENGTH_SHORT).show()
                                else -> android.widget.Toast.makeText(context, "Registration is closed", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Text(
                            text = when {
                                isClosed -> "Registration Closed"
                                isFull -> "Event Full"
                                else -> "Register for Event"
                            },
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (canRegister) {
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    if (eventDetails.isVirtual && eventDetails.meetingLink != null) {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(eventDetails.meetingLink))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(Icons.Rounded.Videocam, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Join Meeting",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = LiquidColors.Primary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, LiquidColors.Primary.copy(alpha = 0.3f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Rounded.CheckCircle,
                                        null,
                                        tint = LiquidColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Registered",
                                        fontWeight = FontWeight.SemiBold,
                                        color = LiquidColors.Primary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            val locationQuery = eventDetails.getLocationDisplay()
                            Button(
                                onClick = {
                                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(locationQuery)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
                                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 16.dp)
                            ) {
                                Icon(Icons.Rounded.Map, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Get Directions",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildDateTimeString(eventDetails: EventDetails): String {
    val datePart = eventDetails.eventDate?.let {
        SimpleDateFormat("MMM dd", Locale.getDefault()).format(it.toDate())
    } ?: ""
    val timePart = eventDetails.eventTime ?: ""
    return listOf(datePart, timePart).filter { it.isNotBlank() }.joinToString(", ")
}

private fun addEventToCalendar(context: Context, post: Post, eventDetails: EventDetails) {
    val startMs = buildStartTimeMs(eventDetails)
    val endMs = startMs?.plus(60 * 60 * 1000L)

    val description = buildString {
        if (post.description.isNotBlank()) append(post.description)
        if (eventDetails.isVirtual && !eventDetails.meetingLink.isNullOrBlank()) {
            if (isNotEmpty()) append("\n\n")
            append("Join: ${eventDetails.meetingLink}")
        }
    }

    fun calendarExtras(intent: Intent) {
        intent.putExtra(android.provider.CalendarContract.Events.TITLE, post.title)
        intent.putExtra(android.provider.CalendarContract.Events.DESCRIPTION, description)
        intent.putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, eventDetails.getLocationDisplay())
        startMs?.let { intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
        endMs?.let { intent.putExtra(android.provider.CalendarContract.EXTRA_EVENT_END_TIME, it) }
    }

    // 1. Google Calendar app (resolveActivity is unreliable on API 30+ without <queries>)
    try {
        val gcalIntent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            `package` = "com.google.android.calendar"
            calendarExtras(this)
        }
        context.startActivity(gcalIntent)
        return
    } catch (_: android.content.ActivityNotFoundException) {}

    // 2. Google Calendar web — opens in any browser
    try {
        val url = buildGoogleCalendarUrl(post, eventDetails, startMs, endMs, description)
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        return
    } catch (_: android.content.ActivityNotFoundException) {}

    // 3. Any system calendar app
    try {
        val sysIntent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            calendarExtras(this)
        }
        context.startActivity(sysIntent)
    } catch (_: android.content.ActivityNotFoundException) {
        android.widget.Toast.makeText(context, "No calendar app found", android.widget.Toast.LENGTH_SHORT).show()
    }
}

private fun buildStartTimeMs(eventDetails: EventDetails): Long? {
    val date = eventDetails.eventDate?.toDate() ?: return null
    val cal = java.util.Calendar.getInstance()
    cal.time = date
    eventDetails.eventTime?.let { timeStr ->
        parseHourMinute(timeStr)?.let { (h, m) ->
            cal.set(java.util.Calendar.HOUR_OF_DAY, h)
            cal.set(java.util.Calendar.MINUTE, m)
            cal.set(java.util.Calendar.SECOND, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
        }
    }
    return cal.timeInMillis
}

private fun parseHourMinute(timeStr: String): Pair<Int, Int>? {
    listOf("HH:mm", "h:mm a", "hh:mm a", "H:mm").forEach { pattern ->
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            val parsed = sdf.parse(timeStr) ?: return@forEach
            val c = java.util.Calendar.getInstance().apply { time = parsed }
            return c.get(java.util.Calendar.HOUR_OF_DAY) to c.get(java.util.Calendar.MINUTE)
        } catch (_: Exception) {}
    }
    return null
}

private fun buildGoogleCalendarUrl(
    post: Post,
    eventDetails: EventDetails,
    startMs: Long?,
    endMs: Long?,
    description: String
): String {
    val sb = StringBuilder("https://calendar.google.com/calendar/r/eventedit?")
    sb.append("text=${Uri.encode(post.title)}")
    if (description.isNotBlank()) sb.append("&details=${Uri.encode(description)}")
    val loc = eventDetails.getLocationDisplay()
    if (loc.isNotBlank()) sb.append("&location=${Uri.encode(loc)}")
    if (startMs != null && endMs != null) {
        val fmt = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.US)
        fmt.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sb.append("&dates=${fmt.format(java.util.Date(startMs))}/${fmt.format(java.util.Date(endMs))}")
    }
    return sb.toString()
}
