package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.theme.Spacing
import androidx.activity.compose.BackHandler
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.theme.CommentColor
import com.exa.android.reflekt.loopit.theme.LikeColorActive
import com.exa.android.reflekt.loopit.theme.ShareColor
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Modern social media-style Post Detail Screen
 * - Compact header with user info
 * - Text before images
 * - Better image sizing
 * - Tabs for Project/Event details
 * - Comments section
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBackClick: () -> Unit,
    onUserClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onManageRequestsClick: (String) -> Unit = {},
    postViewModel: PostDetailViewModel = hiltViewModel(),
    commentViewModel : CommentViewModel = hiltViewModel()
) {
    val postState by postViewModel.uiState.collectAsState()
    val commentState by commentViewModel.state.collectAsState()
    val currentUserId = postViewModel.currentUserId
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    var showDiscardDialog by remember { mutableStateOf(false) }

    val handleBack = {
        if (commentViewModel.hasUnsavedChanges()) {
            showDiscardDialog = true
        } else {
            onBackClick()
        }
    }

    BackHandler(enabled = true) {
        handleBack()
    }

    // Pager state for tabs (if PROJECT or EVENT)
    //val pagerState = rememberPagerState(pageCount = { 2 })

    var curTab by rememberSaveable { mutableIntStateOf(0) }
    val showTabs = postState.post?.type == PostType.PROJECT.displayName ||
            postState.post?.type == PostType.EVENT.displayName

    // Show toast messages
    LaunchedEffect(postState.toastMessage) {
        postState.toastMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            postViewModel.clearToast()
        }
    }

    LaunchedEffect(Unit) {
        commentViewModel.uiEvents.collect {event ->
            when(event){
                is CommentUiEvent.FocusInput ->{
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
                else ->{}
            }

        }
    }

    LaunchedEffect(postId) {
        commentViewModel.onEvent(CommentEvent.LoadComments(postId))
    }

    // Handle post deletion - navigate back when deleted
    LaunchedEffect(postState.postDeleted) {
        if (postState.postDeleted) {
            onBackClick()
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisible ->
                val total = listState.layoutInfo.totalItemsCount
                if (
                    lastVisible == total - 1 &&
                    commentState.hasMoreComments &&
                    !commentState.isLoadingMore
                ) {
                    commentViewModel.onEvent(CommentEvent.LoadMoreComments)
                }
            }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (postState.post?.type) {
                            PostType.PROJECT.displayName -> "Project"
                            PostType.EVENT.displayName -> "Event"
                            else -> "Post"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Show Edit/Delete buttons if user is author
                    if (postState.isAuthor && postState.post != null) {
                        IconButton(onClick = { onEditClick(postState.post!!.id) }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit Post",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { postViewModel.showDeleteConfirmation() }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Delete Post",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        IconButton(onClick = { onManageRequestsClick(postState.post!!.id) }) {
                            Icon(
                                Icons.Rounded.ManageAccounts,
                                contentDescription = "Manage",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },

        bottomBar = {
            if(curTab == 0) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    CommentInput(commentState, commentViewModel, focusRequester)
                }
            }
        },

        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                postState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                postState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = postState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { /* Retry */ },
                            modifier = Modifier.padding(top = Spacing.medium)
                        ) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    postState.post?.let { post ->
                        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {
                            // COMPACT USER HEADER (Social media style)
                            item {
//                                CompactUserHeader(
//                                    post = post,
//                                    onUserClick = { /* Navigate to user profile */ }
//                                )

                                // User Info Header
                                if (post.userInfo != null) {
                                    UserInfoHeader(
                                        userInfo = post.userInfo,
                                        postViews = post.viewsCount,
                                        timestamp = post.createdAt?.toDate()?.time
                                            ?: System.currentTimeMillis(),
                                        postType = post.type,
                                        onClick = {onUserClick(post.createdBy)}
                                    )
                                } else {
                                    // Fallback if user info not loaded
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(Spacing.medium),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Placeholder avatar
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    MaterialTheme.colorScheme.primary.copy(
                                                        alpha = 0.2f
                                                    )
                                                )
                                        )

                                        Spacer(modifier = Modifier.width(Spacing.small))

                                        Column {
                                            Text(
                                                text = post.createdByName.ifEmpty { "Unknown User" },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = post.type,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                            }

//                            if (!showTabs) {
//                                item {
//                                    OverviewTab(post, currentUserId, onImageClick = {index->
//                                        postViewModel.onImageClick(post.imageUrls, index, post.userInfo, post.description)
//                                    })
//                                }
//                            }

                            // TABS FOR PROJECT/EVENT (Overview + Details)
                            if (showTabs) {
                                item {
                                    TabRow(
                                        selectedTabIndex = curTab,
                                        containerColor = MaterialTheme.colorScheme.background
                                    ) {
                                        Tab(
                                            selected = curTab == 0,
                                            onClick = {
                                                curTab = 0
                                            },
                                            text = { Text("Overview") }
                                        )
                                        Tab(
                                            selected = curTab == 1,
                                            onClick = {
                                                curTab = 1
                                            },
                                            text = {
                                                Text(
                                                    if (post.type == PostType.PROJECT.displayName)
                                                        "Roles"
                                                    else
                                                        "Event Info"
                                                )
                                            }
                                        )
                                    }
                                }
                            }

//                                item {
//                                    HorizontalPager(
//                                        state = pagerState,
//                                        //modifier = Modifier.heightIn(min = 300.dp)
//                                    ) { page ->
//                                        when (page) {
//                                            0 ->
                                if (curTab == 0) {
                                    item {
                                        OverviewTab(
                                            post = post, currentUserId, onImageClick = { index ->
                                                postViewModel.onImageClick(
                                                    post.imageUrls,
                                                    index,
                                                    post.userInfo,
                                                    post.description
                                                )
                                            },
                                            onLikeClick = { postViewModel.onLikeClick(post.id, post.likes.contains(currentUserId)) },
                                            onCommentClick = { commentViewModel.onEvent(CommentEvent.FocusInput) },
                                            onShareClick = { TODO() }
                                        )
                                    }
                                } else {
                                    if (post.type == PostType.PROJECT.displayName) {
                                        item {
                                            val hasAppliedToAny = remember(postState.enrollmentStatuses, postState.isRegistered) {
                                                postState.enrollmentStatuses.values.any {
                                                    it == EnrollmentStatus.PENDING || it == EnrollmentStatus.ACCEPTED
                                                } || postState.isRegistered
                                            }

                                            ProjectDetailsTab(
                                                roles = post.projectRoles,
                                                enrollmentStatuses = postState.enrollmentStatuses,
                                                isAuthor = postState.isAuthor,
                                                onEnrollClick = { roleId ->
                                                    postViewModel.openEnrollmentDialog(roleId)
                                                },
                                                onToggleRoleClosure = { roleId, isClosed ->
                                                    postViewModel.toggleRoleClosure(roleId, isClosed)
                                                },
                                                hasAppliedToAny = hasAppliedToAny
                                            )
                                        }
                                    } else {
                                        item {
                                            val hasAppliedToAny = remember(postState.enrollmentStatuses, postState.isRegistered) {
                                                postState.enrollmentStatuses.values.any {
                                                    it == EnrollmentStatus.PENDING || it == EnrollmentStatus.ACCEPTED
                                                } || postState.isRegistered
                                            }

                                            post.eventDetails?.let { eventDetails ->
                                                ModernEventDetailsTab(
                                                    eventDetails = eventDetails,
                                                    eventTitle = post.title,
                                                    isRegistered = postState.isRegistered,
                                                    isAuthor = postState.isAuthor,
                                                    onRegisterClick = { postViewModel.openEventRegistrationDialog() },
                                                    onToggleClosure = { isClosed ->
                                                        postViewModel.toggleEventClosure(isClosed)
                                                    },
                                                    hasAppliedToAny = hasAppliedToAny
                                                )
                                            }
                                        }
                                    }
                                }


                            if(curTab == 0) {
//                            // COMMENTS SECTION
                                item {
                                    Column(Modifier.fillMaxWidth()) {
                                        CommentHeader(commentState.comments.size)
                                    }
                                }

                                commentListContent(commentState, currentUserId, commentViewModel, onUserClick)
                            }
                        }
                    }
                }
            }
        }


        // Dialogs
        if (postState.showEnrollmentDialog && postState.selectedRoleForEnrollment != null) {
            EnrollmentRequestDialog(
                role = postState.selectedRoleForEnrollment!!,
                onDismiss = { postViewModel.closeEnrollmentDialog() },
                onSubmit = { message ->
                    postViewModel.enrollInRole(postState.selectedRoleForEnrollment!!.id, message)
                },
                isLoading = postState.isProcessingEnrollment
            )
        }

        if (postState.showEventRegDialog && postState.post != null && postState.post!!.eventDetails != null) {
            EventRegistrationDialog(
                eventDetails = postState.post!!.eventDetails!!,
                eventTitle = postState.post!!.title,
                onDismiss = { postViewModel.closeEventRegistrationDialog() },
                onSubmit = { message ->
                    postViewModel.registerForEvent()
                    postViewModel.closeEventRegistrationDialog()
                },
                isLoading = postState.isProcessingEnrollment
            )
        }

        // Delete Confirmation Dialog
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
                    if (commentState.replyingToComment != null) {
                        commentViewModel.onEvent(CommentEvent.CancelReply)
                    }
                    if (commentState.newCommentText.isNotEmpty()) {
                        commentViewModel.onEvent(CommentEvent.UpdateCommentText(""))
                    }
                    showDiscardDialog = false
                    onBackClick()
                },
                onKeep = { showDiscardDialog = false }
            )
        }

        // Image Viewer with User Info and Caption
        if (postState.showImageViewer) {
            ImageViewer(
                images = postState.selectedImages,
                initialPage = postState.selectedImageIndex,
                user = postState.selectedImageUser,
                caption = postState.selectedImageCaption,
                onDismiss = postViewModel::onDismissImageViewer
            )
        }
    }
}


@Composable
private fun OverviewTab(post: Post, curUserId : String, onImageClick : (Int) -> Unit, onLikeClick: () -> Unit, onCommentClick: () -> Unit, onShareClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.Center
    ) {
        // TITLE (if present)
        if (post.title.isNotBlank()) {
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = Spacing.small)
            )
        }

        // DESCRIPTION (Text before images)
        if (post.description.isNotBlank()) {
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = Spacing.small)
            )
        }


        //Links Preview
        if (post.links.isNotEmpty()) {
            //Spacer(modifier = Modifier.height(Spacing.small))
            post.links.forEach { link ->
                LinkPreview(
                    url = link,
                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.extraSmall)
                )
            }
        }

        // COMPACT IMAGE DISPLAY
        if (post.imageUrls.isNotEmpty()) {
            MediaContent(post.imageUrls, onImageClick = onImageClick)
        }

        //PDF (if available)
        if(post.fileUrls.isNotEmpty()){
            FileAttachmentsList(post.fileUrls)
        }

        // TAGS
        if (post.tags.isNotEmpty()) {
            CompactTagsRow(tags = post.tags)
        }

        Spacing.small

        // POST ACTIONS (Like, Comment, Share
        CompactInteractionRow (
            post = post,
            currentUserId = curUserId,
            onLikeClick = onLikeClick,
            onCommentClick = onCommentClick,
            onShareClick = onShareClick
        )

        HorizontalDivider(thickness = 8.dp, color = MaterialTheme.colorScheme.surfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompactTagsRow(tags: List<String>) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.forEach { tag ->
            SuggestionChip(
                onClick = { },
                label = { Text("#$tag", style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
private fun CompactInteractionRow(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.small),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Like Button
        val isLiked = post.likes.contains(currentUserId)
        InteractionButton(
            icon = if (isLiked) Icons.Rounded.ThumbUp else Icons.Rounded.ThumbUpOffAlt,
            count = post.likesCount,
            label = "Like",
            isActive = isLiked,
            activeColor = LikeColorActive,
            onClick = onLikeClick
        )

        // Comment
        InteractionButton(
            icon = Icons.Rounded.ChatBubbleOutline,
            count = post.commentsCount,
            label = "Comment",
            activeColor = CommentColor,
            onClick = onCommentClick
        )

//        // Repost
//        InteractionButton(
//            icon = Icons.Rounded.Repeat,
//            count = post.sharesCount,
//            label = "Repost",
//            activeColor = AccentDark,
//            onClick = onRepostClick
//        )

        // Share
        InteractionButton(
            icon = Icons.Rounded.Share,
            count = 0,
            label = "Share",
            activeColor = ShareColor,
            onClick = onShareClick
        )

//
//        // Views count
//        Row(
//            horizontalArrangement = Arrangement.spacedBy(4.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                Icons.Default.Visibility,
//                contentDescription = null,
//                modifier = Modifier.size(16.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = formatCount(post.viewsCount),
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
    }
}

@Composable
private fun CompactActionButton(
    icon: ImageVector,
    count: Int,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            if (count > 0) {
                Text(
                    text = formatCount(count),
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ProjectDetailsTab(
    roles: List<ProjectRole>,
    enrollmentStatuses: Map<String, EnrollmentStatus>,
    isAuthor: Boolean,
    onEnrollClick: (String) -> Unit,
    onToggleRoleClosure: (String, Boolean) -> Unit,
    hasAppliedToAny: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        roles.forEach { role ->
            RoleCardCompact(
                role = role,
                enrollmentStatus = enrollmentStatuses[role.id],
                isAuthor = isAuthor,
                onEnrollClick = { onEnrollClick(role.id) },
                onToggleClosure = { isClosed -> onToggleRoleClosure(role.id, isClosed) },
                hasAppliedToAny = hasAppliedToAny
            )
        }
    }
}

@Composable
private fun RoleCardCompact(
    role: ProjectRole,
    enrollmentStatus: EnrollmentStatus?,
    isAuthor: Boolean,
    onEnrollClick: () -> Unit,
    onToggleClosure: (Boolean) -> Unit,
    hasAppliedToAny: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(role.roleName, fontWeight = FontWeight.Bold)
                    role.technology?.let {
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                     Text(
                        "${role.positionsFilled}/${role.positionsNeeded}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (role.isManuallyClosed) {
                         Text(
                            "Closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Author Toggle
            if (isAuthor) {
                Spacer(modifier = Modifier.height(Spacing.small))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Accepting Applications", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = !role.isManuallyClosed,
                        onCheckedChange = { isOpen -> onToggleClosure(!isOpen) },
                        //scale = 0.8f
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.small))

            if (enrollmentStatus != null && enrollmentStatus != EnrollmentStatus.NOT_ENROLLED) {
                EnrollmentStatusBadge(status = enrollmentStatus)
            } else {
                 val isFull = role.positionsFilled >= role.positionsNeeded
                 val isClosed = role.isManuallyClosed
                 
                 val buttonText = when {
                     isClosed -> "Application Closed"
                     isFull -> "Role Full"
                     hasAppliedToAny -> "Already Applied"
                     else -> "Apply"
                 }
                 
                 val isEnabled = !isClosed && !isFull && !hasAppliedToAny

                Button(
                    onClick = onEnrollClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isEnabled,
                    colors = ButtonDefaults.buttonColors(
                        disabledContainerColor = if (isClosed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = if (isClosed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(buttonText)
                }
            }
        }
    }
}

@Composable
private fun ModernEventDetailsTab(
    eventDetails: EventDetails,
    eventTitle: String,
    isRegistered: Boolean,
    isAuthor: Boolean,
    onRegisterClick: () -> Unit,
    onToggleClosure: (Boolean) -> Unit,
    hasAppliedToAny: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Date & Time Card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = MaterialTheme.colorScheme.primary)
                Column {
                    Text(
                        "When",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatEventDate(eventDetails.eventDate),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    eventDetails.eventTime?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Location Card
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(Spacing.medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Icon(
                    if (eventDetails.isVirtual) Icons.Rounded.Laptop else Icons.Rounded.LocationOn,
                    null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(
                        "Where",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        eventDetails.getLocationDisplay(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Capacity
        if (eventDetails.capacity != null) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(Spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
                ) {
                    Icon(Icons.Rounded.People, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(
                            "Attendees",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${eventDetails.registeredCount} / ${eventDetails.capacity} registered",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        LinearProgressIndicator(
                            progress = { eventDetails.registeredCount.toFloat() / eventDetails.capacity },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }
            }
        }
        
        // Author Toggle
        if (isAuthor) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                 Row(
                    modifier = Modifier.padding(Spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                         Text(
                            "Registration Status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                         Text(
                            if (eventDetails.isManuallyClosed) "Closed" else "Open",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                             color = if (eventDetails.isManuallyClosed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    }
                    Switch(
                        checked = !eventDetails.isManuallyClosed,
                        onCheckedChange = { isOpen -> onToggleClosure(!isOpen) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Register Button
        val isFull = eventDetails.isFull()
        val isClosed = eventDetails.isManuallyClosed
        val isDeadlinePassed = !eventDetails.isRegistrationOpen() && !isClosed && !isFull
        
        val buttonText = when {
            isRegistered -> "Already Registered"
            isClosed -> "Registration Closed"
            isFull -> "Event Full"
            isDeadlinePassed -> "Registration Ended"
            hasAppliedToAny -> "Already Applied (One Request Limit)"
            else -> "Register for Event"
        }
        
        val isEnabled = !isRegistered && eventDetails.isRegistrationOpen() && !hasAppliedToAny

        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = isEnabled,
            colors = ButtonDefaults.buttonColors(
                disabledContainerColor = if (isClosed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = if (isClosed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text(buttonText)
        }
    }
}


// HELPER COMPOSABLE

@Composable
private fun CompactUserHeader(
    post: Post,
    onUserClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        val avatarUrl = post.userInfo?.avatarUrl
        if (avatarUrl != null) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "Profile",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.createdByName.firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = post.createdByName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimeAgo(post.createdAt?.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("•", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = post.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        IconButton(onClick = { /* More options */ }) {
            Icon(Icons.Default.MoreVert, "More")
        }
    }
}

@Composable
private fun CompactImageDisplay(
    images: List<String>,
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    AsyncImage(
        model = images.first(),
        contentDescription = "Post image",
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = 400.dp),
        contentScale = ContentScale.Crop
    )
}

// Helper functions
private fun formatTimeAgo(date: Date?): String {
    if (date == null) return ""
    val now = Date()
    val diff = now.time - date.time
    val hours = diff / (1000 * 60 * 60)
    val days = hours / 24
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        else -> "Just now"
    }
}

private fun formatCount(count: Int): String = when {
    count >= 1_000_000 -> "${count / 1_000_000}M"
    count >= 1_000 -> "${count / 1_000}K"
    else -> count.toString()
}

private fun formatEventDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "TBA"
    val date = timestamp.toDate()
    val formatter = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
