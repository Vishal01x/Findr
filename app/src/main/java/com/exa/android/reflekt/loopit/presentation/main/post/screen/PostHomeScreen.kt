package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.presentation.main.post.components.CommentBottomSheet
import com.exa.android.reflekt.loopit.presentation.main.post.components.ImageViewer
import com.exa.android.reflekt.loopit.presentation.main.post.components.PostCard
import com.exa.android.reflekt.loopit.presentation.main.post.components.TopHeaderBar
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.theme.Spacing
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Group
import androidx.compose.ui.platform.LocalContext
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.presentation.main.notification.vm.NotificationViewModel
import com.exa.android.reflekt.loopit.presentation.main.post.components.PostShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostHomeScreen(
    viewModel: PostHomeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onPostClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    onNotificationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid ?: ""
    val context = androidx.compose.ui.platform.LocalContext.current
    val density = LocalDensity.current

    val userProfileState by userViewModel.userProfileData.collectAsState()
    val unreadNotifCount by notificationViewModel.unreadCount.collectAsState()

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            userViewModel.getProfileData(currentUserId)
        }
    }

    val profileData = (userProfileState as? Response.Success)?.data
    val currentUserName = profileData?.profileHeader?.name?.takeIf { it.isNotBlank() }
        ?: currentUser?.displayName ?: "User"
    val currentUserPhoto = profileData?.profileHeader?.profileImageUrl?.takeIf { it.isNotBlank() }
        ?: currentUser?.photoUrl?.toString()

    // Track actual measured header height so content never hides underneath it
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val headerHeightDp by remember(headerHeightPx) {
        derivedStateOf { with(density) { headerHeightPx.toDp() } }
    }

    // Scroll-aware header visibility (pinned when search is active)
    var headerVisible by remember { mutableStateOf(true) }
    var scrollDelta by remember { mutableFloatStateOf(0f) }
    val effectiveHeaderVisible = headerVisible || state.isSearchActive

    // Back press exits search mode
    val activity = LocalActivity.current
    // Back press exits search mode, otherwise closes app
    BackHandler(enabled = true) {
        if (state.isSearchActive) {
            viewModel.deactivateSearch()
        } else {
            activity?.finish()
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0) {
                    if (scrollDelta > 0) scrollDelta = 0f
                    scrollDelta += delta
                    if (scrollDelta < -20f) headerVisible = false
                } else if (delta > 0) {
                    if (scrollDelta < 0) scrollDelta = 0f
                    scrollDelta += delta
                    if (scrollDelta > 20f) headerVisible = true
                }
                return Offset.Zero
            }
        }
    }

    val pullState = rememberPullToRefreshState()
    val isDark = isSystemInDarkTheme()
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .nestedScroll(nestedScrollConnection)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.refresh()
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
        // Feed content — sits beneath the floating header
        when {
            state.isLoading && state.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp)
                ) {
                    PostShimmer()
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeightDp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Text(
                            text = state.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = LiquidColors.Red
                        )
                        Button(onClick = { /* Retry */ }) {
                            Text("Retry")
                        }
                    }
                }
            }

            state.posts.isEmpty() -> {
                when {
                    state.isSearchingUsers -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = headerHeightDp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = LiquidColors.Primary)
                        }
                    }
                    state.userResults.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = headerHeightDp + 8.dp,
                                bottom = 130.dp,
                                start = 16.dp,
                                end = 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item(key = "people_header") {
                                Text(
                                    text = "People",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LiquidColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(state.userResults, key = { it.uid }) { user ->
                                UserSearchResultRow(
                                    user = user,
                                    onClick = { onUserClick(user.uid) }
                                )
                            }
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = headerHeightDp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyPostState(filter = state.selectedFilter)
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = headerHeightDp,
                        bottom = 130.dp
                    )
                ) {
                    items(
                        items = state.posts,
                        key = { it.id }
                    ) { post ->
                        PostCard(
                            post = post,
                            currentUserId = currentUserId,
                            // Tapping an option votes inline (consumed by PollOptionsSection's
                            // own clickable); tapping elsewhere on the card opens Post Detail.
                            onPostClick = { onPostClick(post.id) },
                            onLikeClick = {
                                viewModel.onLikeClick(
                                    post.id,
                                    post.likes.contains(currentUserId)
                                )
                            },
                            onCommentClick = { viewModel.onCommentClick(post.id) },
                            onRepostClick = { viewModel.onRepostClick(post.id) },
                            onShareClick = { viewModel.onShareClick(post.id) },
                            onImageClick = { index ->
                                viewModel.onImageClick(
                                    post.imageUrls,
                                    index,
                                    post.userInfo,
                                    post.description
                                )
                            },
                            onUserClick = { if (!post.isAnonymous) onUserClick(post.createdBy) },
                            onEnrollInRole = { roleId -> viewModel.enrollInRole(post.id, roleId) },
                            onApplyNowClick = { viewModel.openEnrollmentDialog(post) },
                            onRegisterForEvent = { viewModel.registerForEvent(post.id) },
                            onVote = { optionIndex -> viewModel.onVoteClick(post.id, optionIndex) }
                        )
                    }
                }
            }
        }
        } // end PullToRefreshBox

        // Header floats on top — measures its own height so content pads correctly
        TopHeaderBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            selectedFilter = state.selectedFilter,
            onFilterSelected = viewModel::onFilterSelected,
            isSearchActive = state.isSearchActive,
            onSearchActivate = viewModel::activateSearch,
            onSearchDeactivate = viewModel::deactivateSearch,
            userName = currentUserName,
            userProfileUrl = currentUserPhoto,
            unreadNotificationCount = unreadNotifCount,
            onProfileClick = { onUserClick(currentUserId) },
            onNotificationClick = onNotificationClick,
            visible = effectiveHeaderVisible,
            modifier = Modifier
                .align(Alignment.TopStart)
                .onGloballyPositioned { coords ->
                    headerHeightPx = coords.size.height
                }
        )

        if (state.showCommentSheet) {
            CommentBottomSheet(
                onDismiss = viewModel::onDismissCommentSheet,
                postId = state.selectedPostId ?: "",
                onUserClick = onUserClick
            )
        }

        if (state.showImageViewer) {
            ImageViewer(
                images = state.selectedImages,
                initialPage = state.selectedImageIndex,
                user = state.selectedImageUser,
                caption = state.selectedImageCaption,
                onDismiss = viewModel::onDismissImageViewer
            )
        }

        if (state.showEnrollmentDialog && state.selectedPostForEnrollment != null) {
            val userSkillsStr = profileData?.skill.orEmpty() + " " + (profileData?.profileHeader?.skills?.joinToString() ?: "")
            com.exa.android.reflekt.loopit.presentation.main.post.components.EnrollmentRequestDialog(
                roles = state.selectedPostForEnrollment!!.projectRoles,
                userProfileSkills = userSkillsStr,
                postTitle = state.selectedPostForEnrollment!!.title,
                onDismiss = viewModel::closeEnrollmentDialog,
                onSubmit = { roleId, message -> viewModel.enrollInRole(state.selectedPostForEnrollment!!.id, roleId, message) },
                isLoading = state.isProcessingEnrollment
            )
        }

        // ── Share sheet ───────────────────────────────────────────────────────
        if (state.showShareSheet && state.sharePost != null) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            // Start background prep (short link + bitmap) as soon as the sheet appears
            LaunchedEffect(Unit) { viewModel.prepareShare(context) }
            com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppShareSheet(
                onCopyLink = {
                    scope.launch {
                        val shortLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildShortFdlUrl(context, state.sharePost!!.id)
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(shortLink))
                        viewModel.closeShareSheet()
                    }
                },
                onSystemShare = { viewModel.executeShare(context) },
                onDismiss = { viewModel.closeShareSheet() }
            )
        }

    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }
}

@Composable
private fun UserSearchResultRow(user: profileUser, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, LiquidColors.GlassBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePicture.isNotBlank()) {
                    AsyncImage(
                        model = user.profilePicture,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = user.name.firstOrNull()?.uppercase() ?: "?",
                        color = LiquidColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val subtitle = user.headline?.takeIf { it.isNotBlank() }
                    ?: user.email.takeIf { it.isNotBlank() }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = LiquidColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPostState(filter: PostType?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val icon = when (filter) {
            PostType.COLLABORATION -> Icons.Outlined.Group
            PostType.PROJECT -> Icons.Outlined.Code
            else -> Icons.Outlined.Article
        }
        val title = when (filter) {
            PostType.COLLABORATION -> "No Collaborations"
            PostType.PROJECT -> "No Projects"
            else -> "No Posts Yet"
        }
        val message = when (filter) {
            PostType.COLLABORATION -> "There are no collaboration requests right now. Be the first to start one!"
            PostType.PROJECT -> "There are no projects posted yet. Share your project to get started!"
            else -> "Your feed is empty. Follow more people or create a post to get started!"
        }

        Image(
            imageVector = icon,
            contentDescription = "Empty Posts",
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(LiquidColors.Primary.copy(alpha = 0.6f)),
            modifier = Modifier.size(96.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = LiquidColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = LiquidColors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
