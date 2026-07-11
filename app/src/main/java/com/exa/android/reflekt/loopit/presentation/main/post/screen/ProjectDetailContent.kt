package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import androidx.compose.ui.zIndex
import java.util.Date


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailContent(
    post: Post,
    currentUserId: String,
    isAuthor: Boolean,
    enrollmentStatuses: Map<String, EnrollmentStatus>,
    commentState: CommentState,
    commentViewModel: CommentViewModel,
    postViewModel: PostDetailViewModel,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit,
    onManageClick: () -> Unit,
    onShareClick: () -> Unit,
    onLikeClick: () -> Unit,
    onImageClick: (Int) -> Unit,
    onInviteClick: () -> Unit = {},
    onViewAllCollaborators: () -> Unit = {},
    onOptionsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val hasApplied = remember(enrollmentStatuses, post.enrolledPersons, currentUserId) {
        enrollmentStatuses.values.any { it == EnrollmentStatus.PENDING || it == EnrollmentStatus.ACCEPTED } ||
        post.isUserEnrolled(currentUserId) || post.hasUserRequested(currentUserId)
    }
    
    val allRolesClosed = post.projectRoles.isNotEmpty() && post.projectRoles.all { !it.isApplicationOpen() }
    val showStickyCta = isAuthor || (!hasApplied && !allRolesClosed)

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (lastVisible == total - 1 && commentState.hasMoreComments && !commentState.isLoadingMore)
                    commentViewModel.onEvent(CommentEvent.LoadMoreComments)
            }
    }
    LaunchedEffect(Unit) {
        commentViewModel.uiEvents.collect { event ->
            if (event is CommentUiEvent.FocusInput) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    val uiState by postViewModel.uiState.collectAsState()
    val pullState = rememberPullToRefreshState()
    val isDark = isSystemInDarkTheme()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            postViewModel.refresh()
            scope.launch {
                delay(2000)
                isRefreshing = false
            }
        },
        state = pullState,
        modifier = modifier.fillMaxSize(),
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = isRefreshing,
                containerColor = if (isDark) Color.Black else Color.White,
                color = LiquidColors.Primary,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    ) {
    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(bottom = if (showStickyCta) 104.dp else 24.dp),
            state = listState
        ) {

            // ── 1. Hero + nav + metadata (event-detail style) ──────────────────────────
            item {
                val ts = post.createdAt?.toDate()?.time ?: System.currentTimeMillis()
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                ) {
                    if (post.imageUrls.isNotEmpty()) {
                        HeroCarousel(
                            images = post.imageUrls,
                            onImageClick = onImageClick,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ProjectStylizedPlaceholder(category = post.tags.firstOrNull())
                    }
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.45f),
                                        Color.Transparent,
                                        LiquidColors.BgBase
                                    )
                                )
                            )
                    )
                    // Floating nav — back | like + share
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PdHeroNavBtn(Icons.AutoMirrored.Filled.ArrowBack, "Back", onClick = onBackClick)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            val isLiked = post.likes.contains(currentUserId)
                            PdHeroNavBtn(
                                icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                cd = "Like",
                                onClick = onLikeClick,
                                tint = if (isLiked) Color.Red else Color.White
                            )
                            PdHeroNavBtn(Icons.Rounded.Share, "Share", onClick = onShareClick)
                            PdHeroNavBtn(Icons.Rounded.MoreVert, "Options", onClick = onOptionsClick)
                        }
                    }
                    // Type + first-tag badges at bottom of hero
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventBadge(text = post.type, isPrimary = true)
                        post.tags.firstOrNull()?.let { EventBadge(text = it, isPrimary = false) }
                    }
                }
                // Metadata below hero (inline, no overlapping card)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
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
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = LiquidColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = com.exa.android.reflekt.loopit.core.utils.formatTimeAgo(ts),
                            style = MaterialTheme.typography.bodySmall,
                            color = LiquidColors.TextSecondary
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = LiquidColors.Border)
                    Spacer(Modifier.height(12.dp))
                    // Author row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(if (!post.isAnonymous) Modifier.clickableWithNoIndication { onUserClick(post.createdBy) } else Modifier),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (post.isAnonymous) {
                            AnonymousAvatar(size = 40.dp)
                        } else {
                            val avatarUrl = post.userInfo?.avatarUrl
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(2.dp, LiquidColors.Primary.copy(alpha = 0.30f), CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(LiquidColors.Primary.copy(alpha = 0.2f))
                                        .border(2.dp, LiquidColors.GlassBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = post.createdByName.firstOrNull()?.uppercase() ?: "?",
                                        color = LiquidColors.Primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Led by",
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
                                    chips.forEach { AcademicChip(it) }
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
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = LiquidColors.Border)
                }
            }

            // ── 2. About the Project ──────────────────────────────────────
            if (post.description.isNotBlank()) {
                item {
                    AboutSection(
                        text = post.description,
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 8.dp)
                    )
                }
            }

            // ── Links ──────────────────────────────────────────────
            if (post.links.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        post.links.forEach { link ->
                            LinkPreview(url = link, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // ── Files ──────────────────────────────────────────────
            if (post.fileUrls.isNotEmpty()) {
                item {
                    Box(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        FileAttachmentsList(post.fileUrls)
                    }
                }
            }

            // ── 3. Team Section (status summary + role cards) ──────────────
            if (post.projectRoles.isNotEmpty()) {
                item {
                    TeamSection(
                        roles = post.projectRoles,
                        enrollmentStatuses = enrollmentStatuses,
                        isAuthor = isAuthor,
                        hasApplied = hasApplied,
                        enrolledCount = post.enrolledPersons.size,
                        onViewAllClick = onViewAllCollaborators,
                        onEnrollClick = { id -> postViewModel.openEnrollmentDialog(id) },
                        onToggleClosure = { id, c -> postViewModel.toggleRoleClosure(id, c) },
                        onEditRole = { role -> postViewModel.editRole(role) },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 32.dp)
                    )
                }
            }

            // ── 5. Collaborators ───────────────────────────────────────────
            item {
                CollabSection(
                    post = post,
                    currentUserId = currentUserId,
                    isAuthor = isAuthor,
                    onInviteClick = onInviteClick,
                    onViewAll = onViewAllCollaborators,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                )
            }

            // ── 6. Discussion ──────────────────────────────────────────────
            item {
                Text(
                    text = "Discussion (${commentState.comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                )
            }
            commentListContent(commentState, currentUserId, commentViewModel, onUserClick)
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 16.dp)
                ) {
                    CommentInput(commentState, commentViewModel, focusRequester)
                }
            }
        }

        // ── Sticky "Enroll Now" CTA ────────────────────────────────────────
        if (showStickyCta) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, LiquidColors.BgBase, LiquidColors.BgBase),
                            startY = 0f, endY = 120f
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                val (ctaLabel, ctaIcon, ctaEnabled) = when {
                    isAuthor  -> Triple("Manage Requests", Icons.Rounded.ManageAccounts, true)
                    else      -> Triple("Enroll Now", Icons.Rounded.ArrowForward, true)
                }
                Button(
                    onClick = {
                        when {
                            isAuthor  -> onManageClick()
                            !hasApplied -> {
                                val role = post.projectRoles.firstOrNull()
                                if (role != null) postViewModel.openEnrollmentDialog(role.id)
                            }
                            else -> {}
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = ctaEnabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        ctaLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(ctaIcon, contentDescription = null, modifier = Modifier.size(22.dp))
                }
            }
        }

    }
    } // end PullToRefreshBox
}

// ── Project hero badge ─────────────────────────────────────────────────────────
@Composable
private fun ProjectHeroBadge(text: String, isPrimary: Boolean) {
    val accent = Color(0xFF10B981)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isPrimary) accent else Color.White.copy(alpha = 0.2f))
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

// ── Project stylized placeholder (green accent) ────────────────────────────────
@Composable
private fun ProjectStylizedPlaceholder(category: String?) {
    val isDark = isSystemInDarkTheme()
    val dotColor = if (isDark) Color(0x2613B6EC) else Color(0x3513B6EC)
    val surface = LiquidColors.Surface
    val bgBase = LiquidColors.BgBase
    val icon = getProjectCategoryIcon(category)
    Box(modifier = Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(surface, bgBase))))
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 20.dp.toPx()
            val radius = 1.dp.toPx()
            val cols = (size.width / spacing).toInt() + 1
            val rows = (size.height / spacing).toInt() + 1
            repeat(cols + 1) { col ->
                repeat(rows + 1) { row ->
                    drawCircle(color = dotColor, radius = radius, center = Offset(col * spacing, row * spacing))
                }
            }
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LiquidColors.Primary.copy(alpha = 0.08f),
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 30.dp, y = 30.dp)
                .rotate(15f)
        )
        Box(
            modifier = Modifier.align(Alignment.Center).padding(top = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .rotate(3f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(LiquidColors.Primary, Color(0xFF67E8F9)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(0f to Color.Transparent, 0.6f to Color.Transparent, 1f to bgBase)
                    )
                )
        )
    }
}

private fun getProjectCategoryIcon(category: String?): ImageVector {
    return when (category?.lowercase()?.trim()) {
        "design", "ui/ux", "ux", "art", "creative" -> Icons.Rounded.Palette
        "web", "backend", "frontend", "fullstack", "development" -> Icons.Rounded.Code
        "mobile", "android", "ios" -> Icons.Rounded.PhoneAndroid
        "ai", "ml", "data", "research", "science" -> Icons.Rounded.Science
        "business", "finance", "startup", "entrepreneurship" -> Icons.Rounded.Business
        "social", "community", "networking" -> Icons.Rounded.Groups
        else -> Icons.Rounded.Code
    }
}

// ── Hero nav button (w-10 h-10 rounded-full bg-black/20 border border-white/10) ─
@Composable
private fun HeroNavBtn(icon: ImageVector, cd: String, onClick: () -> Unit, tint: Color = Color.White) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.20f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clickableWithNoIndication(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, cd, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ── Project header card ────────────────────────────────────────────────────────
@Composable
fun ProjectHeaderCard(post: Post, onUserClick: () -> Unit, modifier: Modifier = Modifier) {
    val ts = post.createdAt?.toDate()?.time ?: System.currentTimeMillis()
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(20.dp))
    ) {
        // Top glow accent
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(LiquidColors.Primary.copy(alpha = 0.06f), Color.Transparent)
                    )
                )
        )
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            // Badge + Time
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Type badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LiquidColors.Primary.copy(alpha = 0.12f))
                        .border(1.dp, LiquidColors.Primary.copy(alpha = 0.25f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = post.type.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.Primary,
                        letterSpacing = 0.8.sp
                    )
                }
                // Time ago
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Schedule, null, tint = LiquidColors.TextSecondary, modifier = Modifier.size(14.dp))
                    Text(
                        text = com.exa.android.reflekt.loopit.core.utils.formatTimeAgo(ts),
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Title
            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    lineHeight = 30.sp
                )
                Spacer(Modifier.height(12.dp))
            }

            // Divider + author row
            HorizontalDivider(color = LiquidColors.Border)
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!post.isAnonymous) Modifier.clickableWithNoIndication(onClick = onUserClick) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (post.isAnonymous) {
                    com.exa.android.reflekt.loopit.presentation.main.post.components.AnonymousAvatar(size = 40.dp)
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, LiquidColors.Primary.copy(alpha = 0.30f), CircleShape)
                    ) {
                        val avatarUrl = post.userInfo?.avatarUrl
                        if (!avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            UserAvatar(
                                imageUrl = null,
                                userName = post.userInfo?.name ?: post.createdByName,
                                size = AvatarSize.Medium,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = if (post.isAnonymous) "Anonymous" else post.userInfo?.name ?: post.createdByName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (post.isAnonymous) LiquidColors.TextSecondary else LiquidColors.TextPrimary
                    )
                    Text(
                        text = if (post.isAnonymous) "Identity hidden" else (post.userInfo?.bio
                            ?: post.userInfo?.username?.let { "@$it" }
                            ?: "Member"),
                        style = MaterialTheme.typography.labelSmall,
                        color = LiquidColors.TextSecondary
                    )
                }
            }
        }
    }
}

// ── Team section (status summary + individual role cards) ─────────────────────
@Composable
private fun TeamSection(
    roles: List<ProjectRole>,
    enrollmentStatuses: Map<String, EnrollmentStatus>,
    isAuthor: Boolean,
    hasApplied: Boolean,
    enrolledCount: Int = 0,
    onViewAllClick: () -> Unit = {},
    onEnrollClick: (String) -> Unit,
    onToggleClosure: (String, Boolean) -> Unit,
    onEditRole: (ProjectRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val total  = roles.sumOf { it.positionsNeeded }
    val filled = roles.sumOf { it.positionsFilled }
    val open   = roles.count { it.isApplicationOpen() }

    var editingRole by remember { mutableStateOf<ProjectRole?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Status header (sharp, compact) ────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                "TEAM STATUS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = LiquidColors.TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                "$filled/$total",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(6.dp))
        // Thin sharp progress bar (no rounded corners = sharp edges)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = if (total > 0) filled.toFloat() / total else 0f)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (open > 0) "$open role${if (open != 1) "s" else ""} open" else "All roles filled",
                style = MaterialTheme.typography.labelSmall,
                color = LiquidColors.TextSecondary
            )
            if (enrolledCount > 0) {
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.Primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickableWithNoIndication(onClick = onViewAllClick)
                )
            }
        }

        if (enrolledCount > 0) {
            Spacer(Modifier.height(12.dp))
            val avatarColors = listOf(
                Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899),
                Color(0xFF14B8A6), Color(0xFFF59E0B)
            )
            val maxDisplay = 5
            val displayCount = minOf(enrolledCount, maxDisplay)
            val showExtra = enrolledCount > maxDisplay
            val avatarSize = 32
            val avatarStep = 22
            val totalWidth = (displayCount + if (showExtra) 1 else 0) * avatarStep + (avatarSize - avatarStep)
            val surface = LiquidColors.Surface
            val avatarExtra = LiquidColors.AvatarExtra

            Box(
                modifier = Modifier
                    .width(totalWidth.dp)
                    .height(avatarSize.dp)
                    .clickableWithNoIndication(onClick = onViewAllClick)
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
                            "+${enrolledCount - maxDisplay}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = LiquidColors.TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        // ── Role cards: active first, then inactive ────────────────────────
        // Non-authors don't see manually-closed roles
        val visibleRoles = if (isAuthor) roles else roles.filter { !it.isManuallyClosed }
        val activeRoles = visibleRoles.filter { it.isApplicationOpen() }
        val inactiveRoles = visibleRoles.filter { !it.isApplicationOpen() }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            activeRoles.forEachIndexed { index, role ->
                RoleCard(
                    role = role,
                    enrollmentStatus = enrollmentStatuses[role.id],
                    highlighted = index == 0,
                    isActive = true,
                    isAuthor = isAuthor,
                    hasApplied = hasApplied,
                    onEnroll = { onEnrollClick(role.id) },
                    onToggle = { closed -> onToggleClosure(role.id, closed) },
                    onEditClick = { editingRole = role }
                )
            }
            inactiveRoles.forEach { role ->
                RoleCard(
                    role = role,
                    enrollmentStatus = enrollmentStatuses[role.id],
                    highlighted = false,
                    isActive = false,
                    isAuthor = isAuthor,
                    hasApplied = hasApplied,
                    onEnroll = {},
                    onToggle = { closed -> onToggleClosure(role.id, closed) },
                    onEditClick = { editingRole = role }
                )
            }
        }
    }

    editingRole?.let { role ->
        AddRoleDialog(
            role = role,
            onDismiss = { editingRole = null },
            onSave = { newRole ->
                // preserve runtime fields that the form doesn't touch
                onEditRole(newRole.copy(
                    positionsFilled = role.positionsFilled,
                    isManuallyClosed = role.isManuallyClosed
                ))
                editingRole = null
            }
        )
    }
}

// ── About the Project ──────────────────────────────────────────────────────────
@Composable
private fun AboutSection(text: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "About the Project",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LiquidColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        MarkdownText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LiquidColors.TextPrimary,
            lineHeight = 20.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.animateContentSize()
        )
        if (text.length > 160) {
            Row(
                modifier = Modifier
                    .clickableWithNoIndication { expanded = !expanded }
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expanded) "Show Less" else "Read More",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    role: ProjectRole,
    enrollmentStatus: EnrollmentStatus?,
    highlighted: Boolean,
    isActive: Boolean = true,
    isAuthor: Boolean,
    hasApplied: Boolean,
    onEnroll: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onEditClick: () -> Unit = {}
) {
    val alpha = if (isActive) 1f else 0.45f
    val cardBg = when {
        !isActive -> LiquidColors.GlassSurface.copy(alpha = 0.5f)
        highlighted -> LiquidColors.Primary.copy(alpha = 0.07f)
        else -> LiquidColors.GlassSurface
    }
    val cardBorder = when {
        !isActive -> LiquidColors.GlassBorder.copy(alpha = 0.5f)
        highlighted -> LiquidColors.Primary.copy(alpha = 0.35f)
        else -> LiquidColors.GlassBorder
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Pulsing dot top-right for highlighted card
        if (highlighted) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Column {
            // Role name + status chip
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    role.roleName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                val (statusLabel, statusColor) = when {
                    role.isManuallyClosed -> "Closed" to Color(0xFFF59E0B)
                    role.isFilled() -> "Full" to MaterialTheme.colorScheme.error
                    else -> "Open" to Color(0xFF22C55E)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }

            if (!role.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(role.description, style = MaterialTheme.typography.labelSmall, color = LiquidColors.TextSecondary)
            }

            // Slot progress
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${role.positionsFilled}/${role.positionsNeeded} filled",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.TextSecondary
                )
                Text(
                    "${maxOf(0, role.remainingPositions())} remaining",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (role.remainingPositions() <= 0) MaterialTheme.colorScheme.error else LiquidColors.TextSecondary
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { if (role.positionsNeeded > 0) role.positionsFilled.toFloat() / role.positionsNeeded else 0f },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = when {
                    role.isFilled() -> MaterialTheme.colorScheme.error
                    role.positionsFilled > 0 -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            // Tech chips
            if (!role.technology.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    role.technology.split(",").take(4).forEach { tech ->
                        val chipBg = if (highlighted) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.background
                        val chipBorder = MaterialTheme.colorScheme.outlineVariant
                        val chipText = if (highlighted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(chipBg)
                                .border(1.dp, chipBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(tech.trim(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = chipText)
                        }
                    }
                }
            }

            // Author: toggle + edit controls
            if (isAuthor) {
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accepting Applications", style = MaterialTheme.typography.labelSmall, color = LiquidColors.TextSecondary)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit role",
                                tint = LiquidColors.TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Switch(
                            checked = !role.isManuallyClosed,
                            onCheckedChange = { open -> onToggle(!open) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(0.4f)
                            )
                        )
                    }
                }
            } else if (enrollmentStatus != null && enrollmentStatus != EnrollmentStatus.NOT_ENROLLED) {
                Spacer(Modifier.height(10.dp))
                EnrollmentStatusBadge(status = enrollmentStatus)
            }
        }
    }
}

// ── Collaborators ──────────────────────────────────────────────────────────────
@Composable
private fun CollabSection(
    post: Post,
    currentUserId: String,
    isAuthor: Boolean = false,
    onInviteClick: () -> Unit = {},
    onViewAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalCollabs = 1 + post.enrolledPersons.size
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Collaborators",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = LiquidColors.TextPrimary
                )
                Text(
                    "$totalCollabs member${if (totalCollabs != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = LiquidColors.TextSecondary
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isAuthor) {
                    IconButton(onClick = onInviteClick, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Rounded.PersonAdd,
                            contentDescription = "Invite collaborator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    "View All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickableWithNoIndication(onClick = onViewAll)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            item {
                CollabAvatar(
                    avatarUrl = post.userInfo?.avatarUrl,
                    name = post.userInfo?.name ?: post.createdByName,
                    label = if (post.createdBy == currentUserId) "You"
                    else post.userInfo?.name?.substringBefore(" ") ?: "Owner"
                )
            }
            items(post.enrolledPersons.entries.toList().take(5)) { (uid, role) ->
                CollabAvatar(
                    avatarUrl = null,
                    name = uid,
                    label = if (uid == currentUserId) "You" else role.substringBefore(" ").take(10).ifBlank { "Member" }
                )
            }
        }
    }
}

@Composable
private fun CollabAvatar(avatarUrl: String?, name: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
        ) {
            UserAvatar(imageUrl = avatarUrl, userName = name, size = AvatarSize.Large, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = LiquidColors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Academic info chip ─────────────────────────────────────────────────────────
@Composable
private fun AcademicChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Helper ─────────────────────────────────────────────────────────────────────
private fun projTimeAgo(date: Date?): String {
    if (date == null) return ""
    val diff   = Date().time - date.time
    val mins   = diff / 60_000
    val hours  = mins / 60
    val days   = hours / 24
    val weeks  = days / 7
    val months = days / 30
    val years  = days / 365
    return when {
        years  >= 1 -> if (years == 1L) "1 year ago"  else "${years} years ago"
        months >= 1 -> if (months == 1L) "1 month ago" else "${months} months ago"
        weeks  >= 1 -> if (weeks == 1L) "1 week ago"  else "${weeks} weeks ago"
        days   >= 1 -> "${days} days ago"
        hours  >= 1 -> "${hours}h ago"
        mins   >= 1 -> "${mins}m ago"
        else        -> "Just now"
    }
}
