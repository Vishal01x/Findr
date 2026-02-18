package com.exa.android.reflekt.loopit.presentation.main.post.screen

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.google.firebase.Timestamp

/**
 * State for comment system
 * Manages comments, pagination, loading states, and user interactions
 */
data class CommentState(
    val postId: String? = null,
    val curPost : Post? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMoreComments: Boolean = true,
    
    // For adding new comment
    val newCommentText: String = "",
    val isAddingComment: Boolean = false,
    val addCommentError: String? = null,
    
    // For replies
    val expandedCommentIds: Set<String> = emptySet(),
    val loadingRepliesForComments: Set<String> = emptySet(),
    val replyingToComment: Comment? = null,
    val replyText: String = "",
    val isAddingReply: Boolean = false,
    
    // Pagination cursors
    val lastLikes: Long? = null,
    val lastTimestamp: Timestamp? = null,

    //Reset State
    val showDiscardDialog : Boolean = false,
    
    // For editing comment
    val editingComment: Comment? = null,
    val editCommentText: String = "",
    val isEditingComment: Boolean = false,
    
    // For deleting comment
    val deletingComment: Comment? = null,
    val isDeletingComment: Boolean = false
) {
    /**
     * Check if a comment's replies are expanded
     */
    fun isCommentExpanded(commentId: String): Boolean = commentId in expandedCommentIds
    
    /**
     * Check if replies are currently loading for a comment
     */
    fun isLoadingReplies(commentId: String): Boolean = commentId in loadingRepliesForComments
}


// Handle OneTime Ui Event
sealed interface CommentUiEvent {
    data object Dismiss : CommentUiEvent
    data object FocusInput : CommentUiEvent
}

