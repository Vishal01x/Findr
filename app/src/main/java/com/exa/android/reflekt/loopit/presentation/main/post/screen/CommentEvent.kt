package com.exa.android.reflekt.loopit.presentation.main.post.screen

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post

/**
 * Events for comment system
 * Represents all possible user interactions with comments
 */
sealed class CommentEvent {
    /**
     * Load initial comments for a post
     */
    data class LoadComments(val postId: String) : CommentEvent()
    
    /**
     * Load more comments (pagination)
     */
    object LoadMoreComments : CommentEvent()
    
    /**
     * Add a new top-level comment
     */
    data class AddComment(val postId: String?, val text: String) : CommentEvent()
    
    /**
     * Toggle like on a comment or reply
     */
    data class ToggleCommentLike(val comment : Comment) : CommentEvent()
    
    /**
     * Toggle expansion of replies for a comment
     */
    data class ToggleReplies(val commentId: String) : CommentEvent()
    
    /**
     * Load replies for a specific comment
     */
    data class LoadReplies(val comment: Comment) : CommentEvent()
    
    /**
     * Start replying to a comment (show reply input)
     */
    data class StartReply(val comment: Comment) : CommentEvent()
    
    /**
     * Cancel replying (hide reply input)
     */
    object CancelReply : CommentEvent()
    
    /**
     * Add a reply to a comment
     */
    data class AddReply(val parentComment: Comment, val text: String) : CommentEvent()
    
    /**
     * Update the new comment text field
     */
    data class UpdateCommentText(val text: String) : CommentEvent()
    
    /**
     * Update the reply text field
     */
    data class UpdateReplyText(val text: String) : CommentEvent()
    
    /**
     * Retry loading comments after error
     */
    object RetryLoadComments : CommentEvent()

    /**
     * Focus and Show Keyboard
     */
    object FocusInput : CommentEvent()
    
    /**
     * Clear error message
     */
    object ClearError : CommentEvent()
    
    /**
     * Start editing a comment (show edit dialog)
     */
    data class StartEditComment(val comment: Comment) : CommentEvent()
    
    /**
     * Cancel editing (hide edit dialog)
     */
    object CancelEditComment : CommentEvent()
    
    /**
     * Confirm edit and save changes
     */
    data class ConfirmEditComment(val comment: Comment, val newText: String) : CommentEvent()
    
    /**
     * Update edit comment text field
     */
    data class UpdateEditCommentText(val text: String) : CommentEvent()
    
    /**
     * Start deleting a comment (show delete confirmation dialog)
     */
    data class StartDeleteComment(val comment: Comment) : CommentEvent()
    
    /**
     * Cancel delete (hide delete dialog)
     */
    object CancelDeleteComment : CommentEvent()
    
    /**
     * Confirm delete
     */
    data class ConfirmDeleteComment(val comment: Comment) : CommentEvent()
    
    // ==================== DISMISS EVENTS ====================
    
    /**
     * User requested to dismiss the comment sheet (drag down, back press, etc.)
     */
    object DismissRequested : CommentEvent()
    
    /**
     * User confirmed discarding unsaved changes
     */
    object DiscardConfirmed : CommentEvent()
    
    /**
     * User cancelled the discard dialog (wants to continue editing)
     */
    object DiscardCancelled : CommentEvent()
}
