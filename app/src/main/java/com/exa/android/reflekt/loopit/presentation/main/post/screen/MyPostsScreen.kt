package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.liquidTopBarColors
import com.exa.android.reflekt.loopit.presentation.main.post.components.CommentBottomSheet
import com.exa.android.reflekt.loopit.presentation.main.post.components.ImageViewer
import com.exa.android.reflekt.loopit.presentation.main.post.components.PostCard
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    targetUserId: String? = null,
    onBackClick: () -> Unit,
    onEditPost: (String, String) -> Unit,
    onPostClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onCreatePost: () -> Unit = {},
    viewModel: PostHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    val actualUserId = targetUserId ?: viewModel.currentUserId
    val myPosts = state.posts.filter { it.createdBy == actualUserId }
    val userViewModel: com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel = hiltViewModel()
    val userProfileState by userViewModel.userProfileData.collectAsState()

    LaunchedEffect(actualUserId) {
        if (actualUserId.isNotEmpty()) {
            userViewModel.getProfileData(actualUserId)
        }
    }

    val profileData = (userProfileState as? com.exa.android.reflekt.loopit.core.network.Response.Success)?.data
    
    val isCurUser = targetUserId == null || targetUserId == viewModel.currentUserId
    val firstName = profileData?.profileHeader?.name?.split(" ")?.firstOrNull() ?: "User"
    val screenTitle = if (isCurUser) "My Posts" else "$firstName's Posts"

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PostHomeUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = screenTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${myPosts.size} published",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = liquidTopBarColors()
            )
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {

            if (state.isLoading && myPosts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && myPosts.isEmpty()) {
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (myPosts.isEmpty()) {
                EmptyMyPostsState(
                    onCreatePost = onCreatePost,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Spacing.medium)
                ) {
                    items(myPosts) { post ->
                        Column(
                            modifier = Modifier
                                .padding(horizontal = Spacing.medium, vertical = Spacing.small)
                                .clip(RoundedCornerShape(18.dp))
                                .background(LiquidColors.GlassSurface)
                                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(18.dp))
                                .padding(vertical = Spacing.small)
                        ) {
                            val isPoll = PostType.fromDisplayName(post.type) == PostType.POLL
                            PostCard(
                                post = post,
                                currentUserId = viewModel.currentUserId,
                                // Polls vote entirely inline - there is no poll detail screen to open
                                onPostClick = { if (!isPoll) onPostClick(post.id) },
                                onLikeClick = {
                                    viewModel.onLikeClick(
                                        post.id,
                                        post.likes.contains(viewModel.currentUserId)
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
                                onUserClick = {
                                    onUserClick(post.createdBy)
                                },
                                onEnrollInRole = { roleId ->
                                    viewModel.enrollInRole(post.id, roleId)
                                },
                                onApplyNowClick = { viewModel.openEnrollmentDialog(post) },
                                onRegisterForEvent = {
                                    viewModel.registerForEvent(post.id)
                                },
                                onVote = { optionIndex -> viewModel.onVoteClick(post.id, optionIndex) }
                            )

                            // Anonymous badge visible only to the post author
                            if (post.isAnonymous) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = Spacing.medium)
                                        .padding(bottom = Spacing.small),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VisibilityOff,
                                        contentDescription = null,
                                        tint = LiquidColors.TextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Posted anonymously — your identity is hidden from others",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = LiquidColors.TextSecondary
                                    )
                                }
                            }

                            if (isCurUser && post.createdBy == viewModel.currentUserId) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = Spacing.medium,
                                            vertical = Spacing.small
                                        ),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onEditPost(post.id, post.type) },
                                        modifier = Modifier.padding(end = Spacing.small)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit")
                                    }

                                    Button(
                                        onClick = { viewModel.onDeleteClick(post) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Comment Bottom Sheet
            if (state.showCommentSheet) {
                CommentBottomSheet(
                    onDismiss = viewModel::onDismissCommentSheet,
                    postId = state.selectedPostId ?: "",
                    onUserClick = onUserClick
                )
            }


            // Image Viewer with User Info and Caption
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
        }

        if (state.showDeleteDialog && state.postToDelete != null) {
            com.exa.android.reflekt.loopit.presentation.main.component.dialog.DestructiveDialog(
                title = "Delete Post?",
                message = "Are you sure you want to delete '${state.postToDelete?.title}'? This action cannot be undone.",
                confirmText = "Delete",
                onConfirm = viewModel::onConfirmDelete,
                onDismiss = viewModel::onDismissDeleteDialog
            )
        }

        if (state.showShareSheet && state.sharePost != null) {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            val postLink = com.exa.android.reflekt.loopit.core.utils.ShareUtils.buildFdlUrl(state.sharePost!!.id)
            LaunchedEffect(Unit) { viewModel.prepareShare(context) }
            com.exa.android.reflekt.loopit.presentation.main.component.sheet.AppShareSheet(
                onCopyLink = { clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(postLink)) },
                onSystemShare = { viewModel.executeShare(context) },
                onDismiss = { viewModel.closeShareSheet() }
            )
        }
    }

    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }
}

@Composable
private fun EmptyMyPostsState(
    onCreatePost: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = LiquidColors.Primary
    val isDark = LiquidColors.isDark

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated orbit + icon
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Spinning dashed ring
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
            ) {
                val stroke = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(12f, 10f), 0f
                    )
                )
                drawCircle(
                    color = primary.copy(alpha = 0.30f),
                    radius = size.minDimension / 2f - 4.dp.toPx(),
                    style = stroke
                )
            }

            // Soft glow behind icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(primary.copy(alpha = if (isDark) 0.08f else 0.10f))
            )

            Icon(
                imageVector = Icons.Outlined.Campaign,
                contentDescription = null,
                tint = primary.copy(alpha = 0.85f),
                modifier = Modifier.size(88.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Share your first story",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = LiquidColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Collaborate with peers by posting your ideas,\nevent invites, or technical bugs.",
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 22.sp
            ),
            color = LiquidColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // CTA button — dark in light mode, white in dark mode (matches HTML design)
        val btnBg = if (isDark) Color.White else Color(0xFF0F172A)
        val btnContent = if (isDark) Color(0xFF0F172A) else Color.White
        val btnHover = if (isDark) Color(0xFFF1F5F9) else Color(0xFF1E293B)

        Button(
            onClick = onCreatePost,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = btnBg,
                contentColor = btnContent
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Share your first story",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        }
    }
}
