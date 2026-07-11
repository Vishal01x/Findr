package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentEvent
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentState
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentViewModel
import com.exa.android.reflekt.loopit.theme.Spacing
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CommentRoute(
    postId: String,
    viewModel: CommentViewModel,
    content: @Composable (
        state: CommentState,
        listState: LazyListState,
        viewModel: CommentViewModel
    ) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()


    content(state, listState, viewModel)
}

fun LazyListScope.commentListContent(
    state: CommentState,
    currentUserId: String,
    viewModel: CommentViewModel,
    onUserClick : (String) -> Unit
) {
    when {
        // Initial loading state
        state.isLoading && state.comments.isEmpty() -> {
            items(5) { CommentShimmerItem() }
        }

        // Error state
        state.error != null && state.comments.isEmpty() -> {
            item {
                CommentError(state.error, onClick = {
                    viewModel.onEvent(CommentEvent.RetryLoadComments)
                })
            }
        }

        // Empty state
        state.comments.isEmpty() -> {
            item {
                EmptyState()
            }
        }

        // Comments loaded
        else -> {
            items(
                items = state.comments,
                key = { it.id }
            ) { comment ->
                // Top-level comment
                RenderCommentWithReplies(
                    comment = comment,
                    state = state,
                    currentUserId = currentUserId,
                    viewModel = viewModel,
                    onUserClick = onUserClick
                )
            }

            // Loading more indicator
            if (state.isLoadingMore) {
                item {
                    LoadingMore()
                }
            }
        }
    }
}


@Composable
fun CommentHeader(commentCount: Int) {
    Text(
        text = if (commentCount > 0) "Comments ($commentCount)" else "Comments",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = LiquidColors.TextPrimary,
        modifier = Modifier.padding(Spacing.medium)
    )
    HorizontalDivider(color = LiquidColors.GlassBorder)
}

@Composable
fun CommentInput(state: CommentState, viewModel: CommentViewModel, focusRequester: FocusRequester) {
    //HorizontalDivider()

    if (state.replyingToComment != null) {
        ReplyingIndicator(
            name = state.replyingToComment.senderName,
            onCancel = { viewModel.onEvent(CommentEvent.CancelReply) }
        )
    }

    AddCommentReplyInput(
        text = if (state.replyingToComment != null)
            state.replyText else state.newCommentText,
        isReply = state.replyingToComment != null,
        isLoading = state.isAddingComment || state.isAddingReply,
        error = state.addCommentError,
        focusRequester = focusRequester,
        onTextChange = {
            viewModel.onEvent(
                if (state.replyingToComment == null)
                    CommentEvent.UpdateCommentText(it)
                else CommentEvent.UpdateReplyText(it)
            )
        },
        onSend = {
            viewModel.onEvent(
                if (state.replyingToComment == null)
                    CommentEvent.AddComment(state.postId, state.newCommentText)
                else CommentEvent.AddReply(
                    state.replyingToComment,
                    state.replyText
                )
            )
        }
    )
}


/**
 * Input field for adding a new comment
 */
@Composable
fun AddCommentReplyInput(
    text: String,
    onTextChange: (String) -> Unit,
    isReply: Boolean = false,
    focusRequester: FocusRequester,
    onSend: () -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(
                    horizontal = Spacing.medium,
                    vertical = Spacing.extraSmall
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                placeholder = {
                    Text(
                        if (isReply) "Add a reply…" else "Add a comment…",
                        color = LiquidColors.TextMuted
                    )
                },
                enabled = !isLoading,
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = LiquidColors.TextPrimary,
                    unfocusedTextColor = LiquidColors.TextPrimary,
                    focusedBorderColor = LiquidColors.Primary,
                    unfocusedBorderColor = LiquidColors.GlassBorder,
                    focusedContainerColor = LiquidColors.GlassSurface,
                    unfocusedContainerColor = LiquidColors.GlassSurface,
                    cursorColor = LiquidColors.Primary
                )
            )

            IconButton(
                onClick = onSend,
                enabled = text.isNotBlank() && !isLoading,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (text.isNotBlank()) LiquidColors.Primary else LiquidColors.GlassSurface,
                        CircleShape
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (text.isNotBlank()) Color.White else LiquidColors.TextSecondary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) Color.White else LiquidColors.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Input field for replying to a comment
 */
@Composable
private fun ReplyInput(
    replyText: String,
    onReplyTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        OutlinedTextField(
            value = replyText,
            onValueChange = onReplyTextChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Write a reply...") },
            enabled = !isLoading,
            maxLines = 2,
            shape = RoundedCornerShape(20.dp)
        )

        IconButton(
            onClick = onCancel,
            enabled = !isLoading
        ) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Cancel"
            )
        }

        IconButton(
            onClick = onSend,
            enabled = replyText.isNotBlank() && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send reply",
                    tint = if (replyText.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Recursive function to render a comment and all its nested replies
 * Each comment/reply gets its own event handlers with correct IDs
 */
@Composable
fun RenderCommentWithReplies(
    comment: Comment,
    state: CommentState,
    currentUserId: String,
    viewModel: CommentViewModel,
    onUserClick: (String) -> Unit
) {
    Column {
        CommentItem(
            comment = comment,
            currentUserId = currentUserId,
            onUserClick = onUserClick,
            onLikeClick = { viewModel.onEvent(CommentEvent.ToggleCommentLike(comment)) },
            onReplyClick = { viewModel.onEvent(CommentEvent.StartReply(comment)) },
            onToggleReplies = {
                if (state.isCommentExpanded(comment.id)) {
                    viewModel.onEvent(CommentEvent.ToggleReplies(comment.id))
                } else {
                    viewModel.onEvent(CommentEvent.LoadReplies(comment))
                }
            },
            onEditClick = { viewModel.onEvent(CommentEvent.StartEditComment(comment)) },
            onDeleteClick = { viewModel.onEvent(CommentEvent.StartDeleteComment(comment)) },
            isExpanded = state.isCommentExpanded(comment.id),
            isLoadingReplies = state.isLoadingReplies(comment.id)
        )
        if (state.isCommentExpanded(comment.id) && comment.replies.isNotEmpty()) {
            comment.replies.forEach { reply ->
                RenderCommentWithReplies(reply, state, currentUserId, viewModel, onUserClick)
            }
        }
    }
}


@Composable
fun ReplyingIndicator(name: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LiquidColors.Primary.copy(alpha = 0.07f))
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Replying to $name",
            style = MaterialTheme.typography.bodySmall,
            color = LiquidColors.Primary,
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onCancel) {
            Text("Cancel", color = LiquidColors.TextSecondary)
        }
    }
}


@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.ChatBubbleOutline,
            contentDescription = "No comments",
            tint = LiquidColors.Primary.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = "No comments yet",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = LiquidColors.TextPrimary
        )
        Text(
            text = "Be the first to comment!",
            style = MaterialTheme.typography.bodyMedium,
            color = LiquidColors.TextSecondary
        )
    }
}


@Composable
fun CommentError(error: String?, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = "Error",
            tint = LiquidColors.Red,
            modifier = Modifier.size(44.dp)
        )
        Text(
            text = error ?: "Failed to load comments",
            style = MaterialTheme.typography.bodyLarge,
            color = LiquidColors.Red
        )
        Spacer(Modifier.height(4.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = LiquidColors.Primary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Retry", color = Color.White)
        }
    }
}


@Composable
fun LoadingMore() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    }
}

