package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Article
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.exa.android.reflekt.loopit.presentation.main.post.components.MarkdownText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.abbreviateStream
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.ReportContentDialog
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.SuccessDialog
import com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppOptionsSheet
import com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppShareSheet
import com.exa.android.reflekt.loopit.presentation.main.component.sheet.BottomSheetOption
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.theme.Spacing
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    scrollTo: String = "",
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit = {},
    onChatClick: (userId: String, eventTitle: String) -> Unit = { _, _ -> },
    onEditClick: (String, String) -> Unit = { _, _ -> },
    onManageRequestsClick: (String) -> Unit = {},
    onInviteClick: (postId: String, title: String) -> Unit = { _, _ -> },
    onViewAllCollaborators: (postId: String, title: String) -> Unit = { _, _ -> },
    onViewAllParticipants: (postId: String, title: String) -> Unit = { _, _ -> },
    postViewModel: PostDetailViewModel = hiltViewModel(),
    commentViewModel: CommentViewModel = hiltViewModel()
) {
    val postState by postViewModel.uiState.collectAsState()
    val commentState by commentViewModel.state.collectAsState()
    val currentUserId = postViewModel.currentUserId
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var showDiscardDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val handleBack = {
        if (commentViewModel.hasUnsavedChanges()) showDiscardDialog = true
        else onBackClick()
    }

    BackHandler { handleBack() }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (postState.showEnrollmentDialog && postState.selectedRoleForEnrollment != null) {
        EnrollmentRequestDialog(
            roles = postState.post?.projectRoles ?: emptyList(),
            initialSelectedRoleId = postState.selectedRoleForEnrollment!!.id,
            postTitle = postState.post?.title ?: "",
            onDismiss = { postViewModel.closeEnrollmentDialog() },
            onSubmit = { roleId, message -> postViewModel.enrollInRole(roleId, message) },
            isLoading = postState.isProcessingEnrollment
        )
    }

    if (postState.showEventRegDialog && postState.post?.eventDetails != null) {
        EventRegistrationDialog(
            eventDetails = postState.post!!.eventDetails!!,
            eventTitle = postState.post!!.title,
            onDismiss = { postViewModel.closeEventRegistrationDialog() },
            onSubmit = { _ ->
                postViewModel.registerForEvent()
                postViewModel.closeEventRegistrationDialog()
            },
            isLoading = postState.isProcessingEnrollment
        )
    }

    if (postState.showDeleteDialog && postState.post != null) {
        DeleteConfirmationDialog(
            postTitle = postState.post!!.title,
            onDismiss = { postViewModel.dismissDeleteDialog() },
            onConfirm = { postViewModel.deletePost() },
            isDeleting = postState.isDeleting
        )
    }

    if (showDiscardDialog) {
        DiscardDialog(
            title = "Discard comment?",
            message = "You have an unsaved comment. Do you want to leave without posting?",
            discardText = "Discard",
            keepText = "Keep editing",
            onDiscard = {
                if (commentState.replyingToComment != null) commentViewModel.onEvent(CommentEvent.CancelReply)
                if (commentState.newCommentText.isNotEmpty()) commentViewModel.onEvent(CommentEvent.UpdateCommentText(""))
                showDiscardDialog = false
                onBackClick()
            },
            onKeep = { showDiscardDialog = false }
        )
    }

    if (postState.showImageViewer) {
        ImageViewer(
            images = postState.selectedImages,
            initialPage = postState.selectedImageIndex,
            user = postState.selectedImageUser,
            caption = postState.selectedImageCaption,
            onDismiss = postViewModel::onDismissImageViewer
        )
    }

    // ── Post Options Sheet ────────────────────────────────────────────────────
    if (postState.showPostOptionsSheet) {
        val postLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildFdlUrl(postState.post?.id ?: "")
        val authorOptions = if (postState.isAuthor) listOf(
            BottomSheetOption(
                icon = Icons.Rounded.Edit,
                label = "Edit Post",
                onClick = { postState.post?.let { onEditClick(it.id, it.type) } }
            ),
            BottomSheetOption(
                icon = Icons.Rounded.Delete,
                label = "Delete Post",
                isDestructive = true,
                onClick = { postViewModel.showDeleteConfirmation() }
            )
        ) else emptyList()
        val sharedOptions = listOf(
            BottomSheetOption(
                icon = Icons.Rounded.ContentCopy,
                label = "Copy Link",
                onClick = {
                    scope.launch {
                        val shortLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildShortFdlUrl(context, postState.post?.id ?: "")
                        clipboardManager.setText(AnnotatedString(shortLink))
                        postViewModel.clearToast()
                    }
                }
            )
        )
        val reportOption = if (!postState.isAuthor) listOf(
            BottomSheetOption(
                icon = Icons.Rounded.Flag,
                label = "Report Post",
                isDestructive = true,
                onClick = { postViewModel.openReportSheet() }
            )
        ) else emptyList()

        AppOptionsSheet(
            title = "Post Options",
            options = authorOptions + sharedOptions + reportOption,
            onDismiss = { postViewModel.dismissPostOptions() }
        )
    }

    // ── Share Sheet ───────────────────────────────────────────────────────────
    if (postState.showShareSheet) {
        // Kick off background prep (short-link + bitmap) as soon as the sheet
        // is visible so that "Share via…" fires the intent without any delay.
        LaunchedEffect(Unit) { postViewModel.prepareShare(context) }
        AppShareSheet(
            onCopyLink = {
                scope.launch {
                    val shortLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildShortFdlUrl(context, postState.post?.id ?: "")
                    clipboardManager.setText(AnnotatedString(shortLink))
                    postViewModel.dismissShareSheet()
                }
            },
            onSystemShare = { postViewModel.sharePost(context) },
            onDismiss = { postViewModel.dismissShareSheet() }
        )
    }

    // ── Report Dialog ─────────────────────────────────────────────────────────
    if (postState.showReportSheet) {
        ReportContentDialog(
            onDismissRequest = { postViewModel.dismissReportSheet() },
            onSubmit = { _ ->
                postViewModel.dismissReportSheet()
                postViewModel.dismissPostOptions()
            }
        )
    }

    // ── Registration Success Dialog ───────────────────────────────────────────
    if (postState.showRegistrationSuccess) {
        SuccessDialog(
            title = "Registration Confirmed!",
            message = "You're all set. We've sent a confirmation to your student address.",
            actionText = "View Event",
            onAction = { postViewModel.dismissRegistrationSuccess() },
            onDismissRequest = { postViewModel.dismissRegistrationSuccess() }
        )
    }


    // PROJECT posts → dedicated full-screen layout
    val currentPost = postState.post
    if (!postState.isLoading && currentPost?.type == PostType.PROJECT.displayName) {
        Box(Modifier.fillMaxSize()) {
            ProjectDetailContent(
                post = currentPost,
                currentUserId = currentUserId,
                isAuthor = postState.isAuthor,
                enrollmentStatuses = postState.enrollmentStatuses,
                commentState = commentState,
                commentViewModel = commentViewModel,
                postViewModel = postViewModel,
                onBackClick = { handleBack() },
                onUserClick = onUserClick,
                onManageClick = { onManageRequestsClick(currentPost.id) },
                onShareClick = { postViewModel.openShareSheet() },
                onLikeClick = { postViewModel.onLikeClick(currentPost.id, currentPost.likes.contains(currentUserId)) },
                onImageClick = { index ->
                    postViewModel.onImageClick(currentPost.imageUrls, index, currentPost.userInfo, currentPost.description)
                },
                onInviteClick = { onInviteClick(currentPost.id, currentPost.title) },
                onViewAllCollaborators = { onViewAllCollaborators(currentPost.id, currentPost.title) },
                onOptionsClick = { postViewModel.openPostOptions() }
            )
        }
        return
    }

    LaunchedEffect(postState.toastMessage) {
        postState.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            postViewModel.clearToast()
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
    LaunchedEffect(postId) { commentViewModel.onEvent(CommentEvent.LoadComments(postId)) }
    LaunchedEffect(postState.postDeleted) { if (postState.postDeleted) onBackClick() }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (lastVisible == total - 1 && commentState.hasMoreComments && !commentState.isLoadingMore) {
                    commentViewModel.onEvent(CommentEvent.LoadMoreComments)
                }
            }
    }

    // Scroll to the discussion/comments section when requested via deep link (?scrollTo=comments)
    val discussionIndex = remember(postState.post) {
        postState.post?.let { p ->
            var idx = 1 // hero item is always index 0
            if (p.description.isNotBlank()) idx++
            if (p.links.isNotEmpty()) idx++
            if (p.fileUrls.isNotEmpty()) idx++
            if (p.tags.isNotEmpty()) idx++
            idx++ // engagement bar
            idx   // discussion header
        } ?: Int.MAX_VALUE
    }
    var hasScrolledToComment by remember { mutableStateOf(false) }
    LaunchedEffect(scrollTo, discussionIndex, commentState.comments) {
        if (!hasScrolledToComment && scrollTo.isNotBlank() && discussionIndex != Int.MAX_VALUE) {
            if (scrollTo == "comments") {
                listState.animateScrollToItem(discussionIndex)
                hasScrolledToComment = true
            } else if (commentState.comments.isNotEmpty()) {
                val commentIndex = commentState.comments.indexOfFirst { topLevel ->
                    topLevel.id == scrollTo || topLevel.replies.any { it.id == scrollTo }
                }
                if (commentIndex != -1) {
                    listState.animateScrollToItem(discussionIndex + 1 + commentIndex)
                    hasScrolledToComment = true
                }
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    val isDark = isSystemInDarkTheme()
    var isRefreshing by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase).imePadding()) {
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
            modifier = Modifier.fillMaxSize(),
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
        when {
            postState.isLoading && postState.post == null -> {
                com.exa.android.reflekt.loopit.presentation.main.post.components.PostDetailShimmer()
            }
            postState.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(postState.error ?: "Error", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(Spacing.small))
                    Button(onClick = { postViewModel.refresh() }) { Text("Retry") }
                }
            }
            else -> postState.post?.let { post ->
                // EVENT → dedicated layout
                if (post.type == PostType.EVENT.displayName && post.eventDetails != null) {
                    val hasAppliedToAny = remember(postState.enrollmentStatuses, postState.isRegistered) {
                        postState.enrollmentStatuses.values.any {
                            it == EnrollmentStatus.PENDING || it == EnrollmentStatus.ACCEPTED
                        } || postState.isRegistered
                    }
                    EventDetailContent(
                        post = post,
                        currentUserId = currentUserId,
                        isRegistered = postState.isRegistered,
                        isAuthor = postState.isAuthor,
                        onRegisterClick = { postViewModel.openEventRegistrationDialog() },
                        onBackClick = { handleBack() },
                        onShareClick = { postViewModel.openShareSheet() },
                        onFavoriteClick = { postViewModel.onLikeClick(post.id, post.likes.contains(currentUserId)) },
                        onChatClick = { if (!post.isAnonymous) onChatClick(post.createdBy, post.title) },
                        onUserClick = { if (!post.isAnonymous) onUserClick(post.createdBy) },
                        onManageClick = { onManageRequestsClick(post.id) },
                        onViewAllParticipants = { onViewAllParticipants(post.id, post.title) },
                        onToggleEventClosure = { isClosed -> postViewModel.toggleEventClosure(isClosed) },
                        onUpdateCapacity = { newCapacity -> postViewModel.updateEventCapacity(newCapacity) },
                        onOptionsClick = { postViewModel.openPostOptions() },
                        modifier = Modifier.fillMaxSize()
                    )
                    return@let
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 160.dp)
                ) {
                    // ── 1. Hero + nav + metadata (event-detail style) ─────────────────────
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            if (post.imageUrls.isNotEmpty()) {
                                HeroCarousel(
                                    images = post.imageUrls,
                                    onImageClick = { index ->
                                        postViewModel.onImageClick(post.imageUrls, index, post.userInfo, post.description)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                PostStylizedPlaceholder(category = post.tags.firstOrNull(), postType = post.type)
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
                            // Floating nav
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PdHeroNavBtn(Icons.AutoMirrored.Filled.ArrowBack, "Back") { handleBack() }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    val isLiked = post.likes.contains(currentUserId)
                                    PdHeroNavBtn(
                                        icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        cd = "Like",
                                        tint = if (isLiked) Color.Red else Color.White
                                    ) { postViewModel.onLikeClick(post.id, isLiked) }
                                    PdHeroNavBtn(Icons.Rounded.Share, "Share") { postViewModel.openShareSheet() }
                                    PdHeroNavBtn(Icons.Rounded.MoreVert, "Options") { postViewModel.openPostOptions() }
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
                        // Metadata below hero
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
                                    text = com.exa.android.reflekt.loopit.core.utils.formatTimeAgo(post.createdAt?.toDate()?.time ?: System.currentTimeMillis()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LiquidColors.TextSecondary
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = LiquidColors.Border)
                            Spacer(Modifier.height(12.dp))
                            PostAuthorRow(
                                post = post,
                                onUserClick = { if (!post.isAnonymous) onUserClick(post.createdBy) }
                            )
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = LiquidColors.Border)
                        }
                    }

                    // ── 2. About / Description ────────────────────────────────
                    if (post.description.isNotBlank()) {
                        item {
                            PostAboutSection(
                                text = post.description,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 20.dp, bottom = 24.dp)
                            )
                        }
                    }

                    // ── 2b. Poll ──────────────────────────────────────────────
                    if (post.pollOptions.isNotEmpty()) {
                        item {
                            PollOptionsSection(
                                post = post,
                                currentUserId = currentUserId,
                                onVote = { optionIndex -> postViewModel.onVoteClick(optionIndex) },
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }

                    // ── 3. Links ──────────────────────────────────────────────
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

                    // ── 4. Files ──────────────────────────────────────────────
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

                    // ── 5. Tags ───────────────────────────────────────────────
                    if (post.tags.isNotEmpty()) {
                        item {
                            FlowRow(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                post.tags.forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            "#$tag",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ── 6. Engagement bar ─────────────────────────────────────
                    item {
                        PostEngagementBar(
                            post = post,
                            currentUserId = currentUserId,
                            onLikeClick = { postViewModel.onLikeClick(post.id, post.likes.contains(currentUserId)) },
                            onCommentClick = { commentViewModel.onEvent(CommentEvent.FocusInput) },
                            onLinkClick = { postViewModel.sharePost(context) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                    }

                    // ── 7. Discussion ─────────────────────────────────────────
                    item {
                        Text(
                            "Discussion (${commentState.comments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = LiquidColors.TextPrimary,
                            modifier = Modifier
                                .padding(horizontal = 20.dp)
                                .padding(top = 8.dp, bottom = 16.dp)
                        )
                    }
                    commentListContent(commentState, currentUserId, commentViewModel, onUserClick)
                }
            }
        }
        } // end PullToRefreshBox

        // ── Fixed comment input bar at the bottom ─────────────────────────────
        postState.post?.let { post ->
            if (post.type != PostType.EVENT.displayName) {
                Column(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(LiquidColors.BgBase)
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    CommentInput(commentState, commentViewModel, focusRequester)
                }
            }
        }
    }

}

// ── Hero nav button ───────────────────────────────────────────────────────────
@Composable
fun PdHeroNavBtn(icon: ImageVector, cd: String, tint: Color = Color.White, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.20f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), CircleShape)
            .clickableWithNoIndication(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, cd, tint = tint, modifier = Modifier.size(20.dp))
    }
}

// ── About / Description section ───────────────────────────────────────────────
@Composable
private fun PostAboutSection(text: String, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(true) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            "About",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = LiquidColors.TextPrimary
        )
        Spacer(Modifier.height(12.dp))
        MarkdownText(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LiquidColors.TextSecondary,
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

// ── Engagement bar ────────────────────────────────────────────────────────────
@Composable
private fun PostEngagementBar(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLiked = post.likes.contains(currentUserId)
    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(color = LiquidColors.Border, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.large)) {
                Row(
                    modifier = Modifier
                        .clickableWithNoIndication(onClick = onLikeClick)
                        .padding(Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
                        contentDescription = "Like",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else LiquidColors.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = formatCount(maxOf(0, post.likesCount)),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isLiked) MaterialTheme.colorScheme.error else LiquidColors.TextSecondary
                    )
                }
                Row(
                    modifier = Modifier
                        .clickableWithNoIndication(onClick = onCommentClick)
                        .padding(Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = LiquidColors.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                    if (post.commentsCount > 0) {
                        Text(
                            text = formatCount(post.commentsCount),
                            style = MaterialTheme.typography.labelLarge,
                            color = LiquidColors.TextSecondary
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Rounded.Share,
                contentDescription = "Share",
                tint = LiquidColors.TextSecondary,
                modifier = Modifier
                    .size(22.dp)
                    .clickableWithNoIndication(onClick = onLinkClick)
            )
        }
        HorizontalDivider(color = LiquidColors.Border, thickness = 1.dp)
    }
}

// ── Post hero badge ───────────────────────────────────────────────────────────
@Composable
private fun PostHeroBadge(text: String, isPrimary: Boolean) {
    val accent = Color(0xFF8B5CF6)
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

// ── Post stylized placeholder ─────────────────────────────────────────────────
@Composable
private fun PostStylizedPlaceholder(category: String?, postType: String? = null) {
    val isDark = isSystemInDarkTheme()
    val dotColor = if (isDark) Color(0x2613B6EC) else Color(0x3513B6EC)
    val surface = LiquidColors.Surface
    val bgBase = LiquidColors.BgBase
    val icon = getPostCategoryIcon(category, postType)
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

private fun getPostCategoryIcon(category: String?, postType: String? = null): ImageVector {
    return when (category?.lowercase()?.trim()) {
        "design", "ui/ux", "ux", "art", "creative" -> Icons.Rounded.Palette
        "tech", "technology", "cs", "coding", "programming", "development" -> Icons.Rounded.Code
        "study", "education", "academic", "learning" -> Icons.Rounded.School
        "business", "finance", "startup", "entrepreneurship" -> Icons.Rounded.Business
        "sports", "fitness", "health", "wellness" -> Icons.Rounded.FitnessCenter
        "music" -> Icons.Rounded.MusicNote
        "science", "research" -> Icons.Rounded.Science
        "social", "community", "networking" -> Icons.Rounded.Groups
        else -> if (postType == PostType.POLL.displayName) Icons.Rounded.Poll else Icons.AutoMirrored.Rounded.Article
    }
}

// ── Post author row ───────────────────────────────────────────────────────────
@Composable
private fun PostAuthorRow(post: Post, onUserClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!post.isAnonymous) Modifier.clickableWithNoIndication(onClick = onUserClick) else Modifier),
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
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(LiquidColors.Primary.copy(alpha = 0.2f))
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
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
                text = "Posted by",
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
                    chips.forEach { PostAcademicChip(it) }
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
}

@Composable
private fun PostAcademicChip(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LiquidColors.Primary.copy(alpha = 0.10f))
            .border(1.dp, LiquidColors.Primary.copy(alpha = 0.20f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = LiquidColors.Primary,
            fontWeight = FontWeight.Medium
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun postDetailTimeAgo(timestamp: Timestamp?): String {
    if (timestamp == null) return "Recently"
    val diff = System.currentTimeMillis() - timestamp.toDate().time
    val mins = diff / 60_000
    val hours = mins / 60
    val days = hours / 24
    val weeks = days / 7
    val months = days / 30
    val years = days / 365
    return when {
        years >= 1  -> if (years == 1L) "1 year ago"  else "$years years ago"
        months >= 1 -> if (months == 1L) "1 month ago" else "$months months ago"
        weeks >= 1  -> if (weeks == 1L) "1 week ago"  else "$weeks weeks ago"
        days >= 1   -> "${days}d ago"
        hours >= 1  -> "${hours}h ago"
        mins >= 1   -> "${mins}m ago"
        else        -> "Just now"
    }
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000     -> "${count / 1_000}K"
    else               -> count.toString()
}

private fun formatEventDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "TBA"
    return SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(timestamp.toDate())
}
