package com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository

import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PollOption
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Reply
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Post/Project operations
 * Defines contract for data layer implementation
 */
interface PostRepository {

    // ==================== POST OPERATIONS ====================

    suspend fun createPost(post: Post, profile: profileUser): Result<Unit>

    fun getPostsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?,
        postType: String?
    ): Flow<Result<List<Post>>>

    suspend fun getPostById(projectId: String): Result<Post>

    suspend fun updatePost(postId : String, post: Post): Result<Unit>

    suspend fun deleteProject(projectId: String): Result<Unit>

    fun getProjectUpdates(projectId: String): Flow<Result<Post>>

    suspend fun toggleLike(projectId: String, userId: String, isCurrentlyLiked : Boolean): Result<Unit>

    /**
     * Cast (or change) a vote on a poll post. Voting the same option again removes the vote.
     * A user can only ever have one active vote across all options for a given poll.
     * [currentPollOptions] is the caller's locally-known poll state (from the currently
     * displayed [Post]) - passing it in lets the write skip a Firestore read entirely.
     */
    suspend fun voteOnPoll(
        postId: String,
        userId: String,
        optionIndex: Int,
        currentPollOptions: List<PollOption>
    ): Result<Unit>

    // ==================== ENROLLMENT OPERATIONS ====================

    suspend fun enrollInProject(
        project: Post,
        userId: String,
        userName: String,
        imageUrl: String?
    ): Result<Unit>

    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit>

    suspend fun acceptJoinRequest(
        project: Post,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit>

    suspend fun rejectJoinRequest(
        project: Post,
        userId: String,
        value: profileUser
    ): Result<Unit>

    // ==================== FILTER OPERATIONS ====================

    suspend fun getAvailableRoles(): Result<List<String>>

    suspend fun getAvailableTags(): Result<List<String>>

    suspend fun addNewRole(role: String): Result<Unit>

    suspend fun addNewTag(tag: String): Result<Unit>

    // ==================== COMMENT OPERATIONS ====================

    fun getComments(
        postId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>>

    suspend fun addComment(
        post: Post,
        profile: profileUser,
        comment: Comment
    ): Result<Unit>

    suspend fun updateComment(
        projectId: String,
        commentId: String,
        newText: String
    ): Result<Unit>

    suspend fun deleteComment(
        postId: String,
        commentId: String,
        userId: String
    ): Result<Unit>


    // ==================== EMBEDDED REPLY OPERATIONS (DEPRECATED) ====================

    suspend fun addEmbeddedReply(
        postId: String,
        commentId: String,
        reply: Reply
    ): Result<Unit>

    suspend fun updateEmbeddedReply(
        postId: String,
        commentId: String,
        newReplyText: String
    ): Result<Unit>

    suspend fun deleteEmbeddedReply(postId: String, commentId: String): Result<Unit>

    // ==================== NESTED COMMENT OPERATIONS ====================

    suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        replyId : String?,
        userId: String,
        isCurrentlyLiked : Boolean
    ): Result<Unit>

    suspend fun addReply(
        postId: String,
        profile: profileUser,
        reply: Comment,
        parentCommentId : String,
        parentCommentSenderId : String
    ): Result<Unit>

    fun getReplies(
        postId: String,
        rootCommentId : String?,
        parentCommentId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>>

    suspend fun updateReply(
        newReplyText: String,
        postId: String,
        rootCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit>

    suspend fun deleteReply(
        postId: String,
        rootCommentId: String?,
        parentCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit>

    // ==================== ENROLLMENT & REGISTRATION OPERATIONS ====================
    
    /**
     * Enroll in a project role
     */
    suspend fun enrollInRole(enrollment: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment): Result<Unit>
    
    /**
     * Register for an event
     */
    suspend fun registerForEvent(registration: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration): Result<Unit>
    
    /**
     * Get enrollments for a specific post
     */
    suspend fun getEnrollmentsForPost(postId: String): Result<List<com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment>>

    /**
     * Get registrations for an event
     */
    suspend fun getRegistrationsForEvent(postId: String): Result<List<EventRegistration>>

    /**
     * Update enrollment status (accept/reject)
     */
    suspend fun updateEnrollmentStatus(
        enrollmentId: String,
        status: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
    ): Result<Unit>
    
    /**
     * Check if user is enrolled in a role
     */
    suspend fun isUserEnrolledInRole(postId: String, roleId: String, userId: String): Result<Boolean>
    
    /**
     * Check if user is registered for event
     */
    /**
     * Check if user is registered for event
     */
    suspend fun isUserRegisteredForEvent(postId: String, userId: String): Result<Boolean>
    
    /**
     * Get user's own posts
     */
    fun getMyPosts(userId: String): Flow<Result<List<Post>>>
    
    /**
     * Delete a post
     */
    suspend fun deletePost(postId: String, userId: String): Result<Unit>

    /**
     * Increment view count for a post
     */
    suspend fun incrementPostView(postId: String): Result<Unit>
    
    /**
     * Get enrollment status for a specific role
     */
    suspend fun getEnrollmentStatus(postId: String, roleId: String, userId: String): Result<com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus>
    
    /**
     * Increment role filled count when accepting enrollment
     */
    suspend fun incrementRoleFilledCount(postId: String, roleId: String): Result<Unit>

    /**
     * Update manual closure status for an event
     */
    suspend fun updateEventClosureStatus(postId: String, isClosed: Boolean): Result<Unit>

    /**
     * Update manual closure status for a role
     */
    suspend fun updateRoleClosureStatus(postId: String, roleId: String, isClosed: Boolean): Result<Unit>

    /**
     * Update event capacity (author only)
     */
    suspend fun updateEventCapacity(postId: String, newCapacity: Int): Result<Unit>

    /**
     * Send a project invitation push notification to a user
     */
    suspend fun inviteUserToProject(
        postId: String,
        projectTitle: String,
        invitedUserId: String,
        invitedFcmToken: String,
        inviterName: String,
        roleName: String?
    ): Result<Unit>

    /**
     * Update a project role's editable fields (name, description, technology, positionsNeeded)
     * preserves positionsFilled and isManuallyClosed
     */
    suspend fun updateProjectRole(postId: String, updatedRole: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole): Result<Unit>
}
