package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

/**
 * Domain model for a Post
 * Represents a post/project in the system with all its associated data
 */
data class Post(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val createdBy: String = "", // User ID
    val createdByName: String = "",
    val type: String = PostType.PROJECT.displayName,
    val links: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val fileUrls: List<String> = emptyList(), // New: Document/file attachments
    val videoUrl: String? = "",
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val sharesCount: Int = 0,
    val viewsCount: Int = 0, // New: Track post views
    val tags: List<String> = emptyList(),
    val rolesNeeded: List<String> = emptyList(), // Deprecated: Use projectRoles instead
    val projectRoles: List<ProjectRole> = emptyList(), // New: Detailed role requirements
    val eventDetails: EventDetails? = null, // New: Event-specific details
    val pollOptions: List<PollOption> = emptyList(), // New: Poll-specific options + votes
    val likes: List<String> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val enrolledPersons: Map<String, String> = emptyMap(),
    val requestedPersons: Map<String, String> = emptyMap(),
    val isAnonymous: Boolean = false,  // author identity hidden from feed
    
    // UI-only field (not stored in Firestore) - enriched by repository
    @Exclude val userInfo: PostUserInfo? = null
) {
    
    /**
     * Validates if the post has all required fields
     */
    fun isValid(): Boolean {
        return title.isNotBlank() && 
               title.length <= PostConstants.Limits.MAX_TITLE_LENGTH &&
               description.length <= PostConstants.Limits.MAX_DESCRIPTION_LENGTH
    }
    
    /**
     * Checks if a user has liked this post
     */
    fun isLikedBy(userId: String): Boolean {
        return likes.contains(userId)
    }
    
    /**
     * Checks if user is enrolled in this post/project
     */
    fun isUserEnrolled(userId: String): Boolean {
        return enrolledPersons.containsKey(userId)
    }
    
    /**
     * Checks if user has requested enrollment
     */
    fun hasUserRequested(userId: String): Boolean {
        return requestedPersons.containsKey(userId)
    }
    
    /**
     * Checks if user is the owner of this post
     */
    fun isOwnedBy(userId: String): Boolean {
        return createdBy == userId
    }

    /**
     * Total votes cast across all poll options.
     */
    fun totalPollVotes(): Int = pollOptions.sumOf { it.votes.size }

    /**
     * Index of the option [userId] voted for, or null if they haven't voted.
     */
    fun userPollVoteIndex(userId: String): Int? {
        if (userId.isBlank()) return null
        return pollOptions.indexOfFirst { it.isVotedBy(userId) }.takeIf { it >= 0 }
    }
}



