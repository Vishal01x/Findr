package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.core.utils.collectDescendantIds
import com.exa.android.reflekt.loopit.core.utils.updateRepliesRecursively
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing comment system state
 * Handles comments, replies, likes, and pagination
 */
@HiltViewModel
class CommentViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val getRepliesUseCase: GetRepliesUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val addReplyUseCase: AddReplyUseCase,
    private val updateCommentUseCase: UpdateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val toggleCommentLikeUseCase: ToggleCommentLikeUseCase,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CommentState())
    val state: StateFlow<CommentState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<CommentUiEvent>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val uiEvents = _uiEvents.asSharedFlow()

    private var currentPost: Post? = null

    /**
     * Handle comment events
     */
    fun onEvent(event: CommentEvent) {
        when (event) {
            is CommentEvent.LoadComments -> loadComments(event.postId)
            is CommentEvent.LoadMoreComments -> loadMoreComments()
            is CommentEvent.AddComment -> addComment(event.postId, event.text)
            is CommentEvent.ToggleCommentLike -> toggleCommentLike(event.comment)
            is CommentEvent.ToggleReplies -> toggleReplies(event.commentId)
            is CommentEvent.LoadReplies -> loadReplies(event.comment)
            is CommentEvent.StartReply -> startReply(event.comment)
            is CommentEvent.CancelReply -> cancelReply()
            is CommentEvent.AddReply -> addReply(event.parentComment, event.text)
            is CommentEvent.UpdateCommentText -> updateCommentText(event.text)
            is CommentEvent.UpdateReplyText -> updateReplyText(event.text)
            is CommentEvent.RetryLoadComments -> retryLoadComments()
            is CommentEvent.FocusInput -> focusInput()
            is CommentEvent.ClearError -> clearError()
            is CommentEvent.StartEditComment -> startEditComment(event.comment)
            is CommentEvent.CancelEditComment -> cancelEditComment()
            is CommentEvent.ConfirmEditComment -> confirmEditComment(event.comment, event.newText)
            is CommentEvent.UpdateEditCommentText -> updateEditCommentText(event.text)
            is CommentEvent.StartDeleteComment -> startDeleteComment(event.comment)
            is CommentEvent.CancelDeleteComment -> cancelDeleteComment()
            is CommentEvent.ConfirmDeleteComment -> confirmDeleteComment(event.comment)
            is CommentEvent.DismissRequested -> onDismissRequested()
            is CommentEvent.DiscardConfirmed -> onDiscardConfirmed()
            is CommentEvent.DiscardCancelled -> onDiscardCancelled()
        }
    }

    /**
     * Load initial comments for a post
     */
    private fun loadComments(postId: String) {
        viewModelScope.launch {

            _state.update {
                it.copy(
                    postId = postId,
                    isLoading = true,
                    error = null,
                    comments = emptyList(),
                    lastLikes = null,
                    lastTimestamp = null,
                    hasMoreComments = true
                )
            }

            getCommentsUseCase(
                postId = postId,
                lastLikes = null,
                lastTimestamp = null,
                limit = 20
            ).catch { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load comments"
                    )
                }
            }.collect { response ->
                when (response) {
                    is Response.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }
                    is Response.Success -> {
                        val comments = response.data ?: emptyList()
                        val lastComment = comments.lastOrNull()
                        
                        _state.update {
                            it.copy(
                                comments = comments,
                                isLoading = false,
                                error = null,
                                lastLikes = lastComment?.likesCount,
                                lastTimestamp = lastComment?.timestamp,
                                hasMoreComments = comments.size >= 20
                            )
                        }
                    }
                    is Response.Error -> {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = response.message ?: "Failed to load comments"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Load more comments (pagination)
     */
    private fun loadMoreComments() {
        val currentState = _state.value
        if (currentState.isLoadingMore || !currentState.hasMoreComments || currentState.postId == null) {
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }

            getCommentsUseCase(
                postId = currentState.postId,
                lastLikes = currentState.lastLikes,
                lastTimestamp = currentState.lastTimestamp,
                limit = 20
            ).catch { error ->
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        error = error.message ?: "Failed to load more comments"
                    )
                }
            }.collect { response ->
                when (response) {
                    is Response.Success -> {
                        val newComments = response.data ?: emptyList()
                        val lastComment = newComments.lastOrNull()
                        
                        _state.update {
                            it.copy(
                                comments = it.comments + newComments,
                                isLoadingMore = false,
                                lastLikes = lastComment?.likesCount,
                                lastTimestamp = lastComment?.timestamp,
                                hasMoreComments = newComments.size >= 20
                            )
                        }
                    }
                    is Response.Error -> {
                        _state.update {
                            it.copy(
                                isLoadingMore = false,
                                error = response.message ?: "Failed to load more comments"
                            )
                        }
                    }
                    is Response.Loading -> {
                        // Already set to loading
                    }
                }
            }
        }
    }

    /**
     * Add a new top-level comment
     */
    private fun addComment(postId : String?, text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return

        if(postId == null){
            _state.update {
                it.copy(error = "Failed to add comment")
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isAddingComment = true, addCommentError = null) }

            val result = addCommentUseCase(postId, trimmedText)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isAddingComment = false,
                            newCommentText = "",
                            addCommentError = null
                        )
                    }
                    // Reload comments to show new comment
                   // _state.value.postId?.let { postId -> loadComments(postId) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isAddingComment = false,
                            addCommentError = error.message ?: "Failed to add comment"
                        )
                    }
                }
            )
        }
    }

    /**
     * Toggle like on a comment
     */
    private fun toggleCommentLike(comment : Comment) {
        val postId = _state.value.postId ?: return

        viewModelScope.launch {
            val result = toggleCommentLikeUseCase(comment)
            
            if (result.isFailure) {
                Log.e("CommentViewModel", "Failed to toggle like: ${result.exceptionOrNull()?.message}")
            }
            // Note: The UI will update via Firestore real-time listener
        }
    }

    /**
     * Toggle expansion of replies for a comment
     */
    private fun toggleReplies(commentId: String) {
        val isExpanded = _state.value.isCommentExpanded(commentId)

        if (isExpanded) {
            // Collect all child ids recursively
            val descendantIds = _state.value.comments.collectDescendantIds(
                commentId
            )

            _state.update {
                it.copy(
                    expandedCommentIds =
                        it.expandedCommentIds -
                                descendantIds -
                                commentId
                )
            }

        } else {
            _state.update {
                it.copy(
                    expandedCommentIds =
                        it.expandedCommentIds + commentId
                )
            }
        }
    }

    /**
     * Load replies for a comment
     */
    private fun loadReplies(comment: Comment) {
        val postId = _state.value.postId ?: return
        
        viewModelScope.launch {
            _state.update {
                it.copy(loadingRepliesForComments = it.loadingRepliesForComments + comment.id)
            }

            getRepliesUseCase(
                postId = postId,
                rootCommentId = comment.rootCommentId,
                parentCommentId = comment.id,
                lastLikes = null,
                lastTimestamp = null,
                limit = 20
            ).catch { error ->
                _state.update {
                    it.copy(loadingRepliesForComments = it.loadingRepliesForComments - comment.id)
                }
            }.collect { response ->
                when (response) {
                    is Response.Success -> {
                        val replies = response.data ?: emptyList()
                        
                        _state.update { state ->
                            val updatedComments = state.comments.updateRepliesRecursively(
                                comment.id,
                                replies
                            )
                            state.copy(
                                comments = updatedComments,
                                loadingRepliesForComments = state.loadingRepliesForComments - comment.id,
                                expandedCommentIds = state.expandedCommentIds + comment.id
                            )
                        }
                    }
                    is Response.Error -> {
                        _state.update {
                            it.copy(loadingRepliesForComments = it.loadingRepliesForComments - comment.id)
                        }
                    }
                    is Response.Loading -> {
                        // Already loading
                    }
                }
            }
        }
    }

    /**
     * Start replying to a comment
     */
    private fun startReply(comment: Comment) {
        _state.update {
            it.copy(
                replyingToComment = comment,
                replyText = ""
            )
        }
    }

    /**
     * Cancel replying
     */
    private fun cancelReply() {
        _state.update {
            it.copy(
                replyingToComment = null,
                replyText = ""
            )
        }
    }

    /**
     * Add a reply to a comment
     */
    private fun addReply(parentComment: Comment, text: String) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) return

        val postId = _state.value.postId ?: return

        viewModelScope.launch {
            _state.update { it.copy(isAddingReply = true) }

            val result = addReplyUseCase(postId, parentComment, trimmedText)
            
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isAddingReply = false,
                            replyingToComment = null,
                            replyText = ""
                        )
                    }
                    // Reload replies for this comment
                    //loadReplies(parentComment)
                    toggleReplies(parentComment.id)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isAddingReply = false,
                            error = error.message ?: "Failed to add reply"
                        )
                    }
                }
            )
        }
    }

    private fun updateCommentText(text: String) {
        _state.update { it.copy(newCommentText = text) }
    }

    private fun updateReplyText(text: String) {
        _state.update { it.copy(replyText = text) }
    }

    private fun retryLoadComments() {
        _state.value.postId?.let { loadComments(it) }
    }

    private fun focusInput(){
        viewModelScope.launch {
            _uiEvents.emit(CommentUiEvent.FocusInput)
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null, addCommentError = null) }
    }

    // ==================== EDIT COMMENT LOGIC ====================

    private fun startEditComment(comment: Comment) {
        _state.update {
            it.copy(
                editingComment = comment,
                editCommentText = comment.text
            )
        }
    }

    private fun cancelEditComment() {
        _state.update {
            it.copy(
                editingComment = null,
                editCommentText = ""
            )
        }
    }

    private fun updateEditCommentText(text: String) {
        _state.update { it.copy(editCommentText = text) }
    }

    private fun confirmEditComment(comment : Comment, newText: String) {
        val postId = _state.value.postId ?: return
        val trimmedText = newText.trim()
        if (trimmedText.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isEditingComment = true) }
            val result = updateCommentUseCase(newText, comment)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isEditingComment = false,
                            editingComment = null,
                            editCommentText = ""
                        )
                    }
                    // Reload comments to show updated comment
                    //loadComments(postId)
                },
                onFailure = { error ->
                    Log.e("CommentViewModel", " FAILED updating embedded reply ", error)
                    _state.update {
                        it.copy(
                            isEditingComment = false,
                            error = error.message ?: "Failed to update comment"
                        )
                    }
                }
            )
        }
    }

    // ==================== DELETE COMMENT LOGIC ====================

    private fun startDeleteComment(comment: Comment) {
        _state.update { it.copy(deletingComment = comment) }
    }

    private fun cancelDeleteComment() {
        _state.update { it.copy(deletingComment = null) }
    }

    private fun confirmDeleteComment(comment: Comment) {
        val postId = _state.value.postId ?: return
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            _state.update { it.copy(isDeletingComment = true) }

            val result = deleteCommentUseCase(comment)

            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isDeletingComment = false,
                            deletingComment = null
                        )
                    }
                    // Reload comments to reflect deletion
                    //loadComments(postId)
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isDeletingComment = false,
                            error = error.message ?: "Failed to delete comment"
                        )
                    }
                }
            )
        }
    }

    /* ---------------- DISMISS LOGIC ---------------- */

    /**
     * Checks if there are any unsaved changes that would be lost on dismiss.
     * Includes: comment text, reply text, or modified edit text.
     */
    fun hasUnsavedChanges(): Boolean {
        val s = _state.value
        return s.replyingToComment != null ||
                s.newCommentText.isNotBlank() ||
                s.replyText.isNotBlank() ||
                // Check if editing and text has been modified
                (s.editingComment != null && s.editCommentText != s.editingComment.text)
    }

    /**
     * Called when user attempts to dismiss the bottom sheet.
     * Handles the dismiss flow properly:
     * 1. If deleting dialog is open -> close it first
     * 2. If editing dialog is open with changes -> show discard dialog
     * 3. If editing dialog is open without changes -> close it
     * 4. If there are unsaved changes -> show discard dialog
     * 5. Otherwise -> dismiss immediately
     */
    private fun onDismissRequested() {
        val currentState = _state.value

        when {
            // If delete dialog is open, just close it (no data loss)
            currentState.deletingComment != null -> {
                _state.update { it.copy(deletingComment = null) }
            }

            // If edit dialog is open
            currentState.editingComment != null -> {
                val hasEditChanges = currentState.editCommentText != currentState.editingComment.text
                if (hasEditChanges) {
                    // Show discard dialog for unsaved edit changes
                    _state.update { it.copy(showDiscardDialog = true) }
                } else {
                    // No changes, just close the edit dialog
                    _state.update {
                        it.copy(
                            editingComment = null,
                            editCommentText = ""
                        )
                    }
                }
            }

            // Check for other unsaved changes (comment text, reply)
            hasUnsavedChanges() -> {
                _state.update { it.copy(showDiscardDialog = true) }
            }

            // No unsaved changes, dismiss immediately
            else -> {
                resetState()
                dismiss()
            }
        }
    }

    /**
     * Called when user confirms discarding all unsaved changes.
     */
    private fun onDiscardConfirmed() {
        resetState()
        dismiss()
    }

    /**
     * Called when user cancels the discard dialog (wants to continue editing).
     */
    private fun onDiscardCancelled() {
        _state.update { it.copy(showDiscardDialog = false) }
    }

    private fun dismiss() {
        viewModelScope.launch {
            _uiEvents.emit(CommentUiEvent.Dismiss)
        }
    }

    private fun resetState() {
        _state.value = CommentState()
    }

    override fun onCleared() {
        super.onCleared()
        _state.value = CommentState()
    }
}

