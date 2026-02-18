package com.exa.android.reflekt.loopit.presentation.main.post.components

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentEvent
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentUiEvent
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CommentViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Bottom sheet for displaying and managing comments
 * Features: pagination, loading states, add comment, nested replies
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    postId: String,
    onDismiss: () -> Unit,
    onUserClick: (String) -> Unit,
    viewModel: CommentViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { newValue ->
            if (newValue == SheetValue.Hidden && viewModel.hasUnsavedChanges()) {
                viewModel.onEvent(CommentEvent.DismissRequested)
                false // keep sheet open
            } else {
                true
            }
        }
    )

    BackHandler(enabled = sheetState.isVisible) {
        viewModel.onEvent(CommentEvent.DismissRequested)
    }

//    LaunchedEffect(sheetState.isVisible) {
//        if (sheetState.isVisible) {
//            sheetState.expand()
//        }
//    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                CommentUiEvent.Dismiss -> onDismiss()
                else -> {}
            }
        }
    }

    // Discard confirmation dialog
    if (state.showDiscardDialog) {
        DiscardCommentDialog(
            onConfirmDiscard = { viewModel.onEvent(CommentEvent.DiscardConfirmed) },
            onDismiss = {
                viewModel.onEvent(CommentEvent.DiscardCancelled)
                coroutineScope.launch {
                    if (!sheetState.isVisible) {
                        sheetState.show()
                    }
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = { viewModel.onEvent(CommentEvent.DismissRequested) },
        modifier = modifier,
        sheetState = sheetState, //rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
         CommentSection(postId, viewModel, focusRequester, onUserClick)
    }
}

@Composable
fun CommentSection(
    postId: String,
    viewModel: CommentViewModel = hiltViewModel(),
    focusRequester: FocusRequester,
    onUserClick : (String) -> Unit
){
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    // Load comments when sheet opens
    LaunchedEffect(postId) {
        viewModel.onEvent(CommentEvent.LoadComments(postId))
    }

    // Detect when user scrolls to bottom for pagination
    LaunchedEffect(listState.canScrollForward, listState.isScrollInProgress) {
        if (!listState.canScrollForward && !listState.isScrollInProgress) {
            if (state.hasMoreComments && !state.isLoadingMore && state.comments.isNotEmpty()) {
                viewModel.onEvent(CommentEvent.LoadMoreComments)
            }
        }
    }
    
    // Edit Comment Dialog
    state.editingComment?.let { comment ->
        EditCommentDialog(
            comment = comment,
            editText = state.editCommentText,
            onTextChange = { viewModel.onEvent(CommentEvent.UpdateEditCommentText(it)) },
            onDismiss = { viewModel.onEvent(CommentEvent.CancelEditComment) },
            onConfirm = {
                viewModel.onEvent(CommentEvent.ConfirmEditComment(comment, state.editCommentText))
                        },
            isLoading = state.isEditingComment
        )
    }
    
    // Delete Comment Dialog
    state.deletingComment?.let { comment ->
        DeleteCommentDialog(
            comment = comment,
            onDismiss = { viewModel.onEvent(CommentEvent.CancelDeleteComment) },
            onConfirm = { viewModel.onEvent(CommentEvent.ConfirmDeleteComment(comment)) },
            isDeleting = state.isDeletingComment
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        CommentHeader(state.comments.size)
        // Comments list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(modifier = Modifier.fillMaxHeight(), state=listState) {
                commentListContent(state,currentUserId, viewModel, onUserClick)
            }
        }

        HorizontalDivider()

        CommentInput(state,viewModel, focusRequester)
    }
}

