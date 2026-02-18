package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

/**
 * Domain model for a Comment on a post
 * Supports likes and nested replies up to 3 levels
 */
data class Comment(
    val id: String = "",
    val postId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderImageUrl: String? = null,
    val text: String = "",
    val likes: List<String> = emptyList(), // User IDs who liked this comment
    val likesCount: Long = 0,
    val replyCount: Long = 0,
    val parentCommentId: String? = null, // null for top-level comments
    val rootCommentId: String? = null, // The top-level comment ID for nested replies (null for top-level)
    val nestingLevel: Int = 0, // 0=top-level, 1=first reply, 2=second reply, 3=third reply (max)
    @ServerTimestamp val timestamp: Timestamp? = null,
    
    // UI-only fields (not stored in Firestore)
    @Exclude val replies: List<Comment> = emptyList(),
    @Exclude val isExpanded: Boolean = false,
    @Exclude val userInfo: PostUserInfo? = null // Enriched by repository
) {
    /**
     * Checks if this is a top-level comment (not a reply)
     */
    fun isTopLevel(): Boolean = parentCommentId == null && nestingLevel == 0
    
    /**
     * Checks if this comment has any replies
     */
    fun hasReplies(): Boolean = replyCount > 0
    
    /**
     * Checks if the comment is liked by specific user
     */
    fun isLikedBy(userId: String): Boolean = likes.contains(userId)
    
    /**
     * Checks if comment can have replies (max 3 levels)
     */
    fun canReply(): Boolean = nestingLevel < 3
    
    /**
     * Checks if text is valid
     */
    fun isValid(): Boolean = text.isNotBlank() && text.length <= PostConstants.Limits.MAX_COMMENT_LENGTH
}
