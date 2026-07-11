package com.exa.android.reflekt.loopit.data.feature.main.post.data.repository

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource.CommentRemoteDataSource
import com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource.FilterRemoteDataSource
import com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource.NestedCommentDataSource
import com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource.PostRemoteDataSource
import com.exa.android.reflekt.loopit.data.feature.main.post.data.mapper.PostMapper
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PollOption
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RegistrationStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Reply
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.toPostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.fcm.sendComment
import com.exa.android.reflekt.loopit.fcm.sendProjectInvitation
import com.exa.android.reflekt.loopit.fcm.sendProjectNotification
import com.exa.android.reflekt.loopit.fcm.sendReply
import com.exa.android.reflekt.loopit.fcm.sendRequestUpdate
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.InAppNotificationHelper
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType as NotificationType
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.map
import java.util.HashMap

/**
 * Refactored PostRepository implementation
 * Now acts as an orchestration layer using data sources and mappers
 * Reduced from 644 lines to a clean, maintainable structure
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postDataSource: PostRemoteDataSource,
    private val commentDataSource: CommentRemoteDataSource,
    private val nestedCommentDataSource: NestedCommentDataSource,
    private val filterDataSource: FilterRemoteDataSource,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : PostRepository {

    // ==================== POST OPERATIONS ====================

    override suspend fun createPost(post: Post, profile: profileUser): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }
            require(post.isValid()) { "Invalid post data" }

            val postMap = PostMapper.toMap(post)
            postDataSource.createPost(post.id, postMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            Result.failure(e)
        }
    }

    override fun getPostsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?,
        postType: String?
    ): Flow<Result<List<Post>>> {
        // Pass empty query to Firestore — text search is done client-side after enrichment
        // so we can match against title, description, AND userInfo.name.
        return postDataSource.getPostsStream(
            searchQuery = "",
            rolesFilter = rolesFilter,
            tagsFilter = tagsFilter,
            showMyProjectsOnly = showMyProjectsOnly,
            userId = userId,
            typeFilter = postType
        ).map { result ->
            result.map { postMaps ->
                val posts = postMaps.mapNotNull { PostMapper.fromMap(it) }
                val enriched = enrichPostsWithUserInfo(posts)
                if (searchQuery.isBlank()) {
                    enriched
                } else {
                    val keywords = searchQuery.trim().lowercase()
                        .split("\\s+".toRegex()).filter { it.isNotBlank() }
                    enriched.filter { post ->
                        keywords.any { kw ->
                            post.title.lowercase().contains(kw) ||
                            post.description.lowercase().contains(kw) ||
                            post.userInfo?.name?.lowercase()?.contains(kw) == true
                        }
                    }
                }
            }
        }
    }

    override suspend fun getPostById(projectId: String): Result<Post> {
        return try {
            val result = postDataSource.getPostById(projectId)
            result.map { postMap ->
                val post = PostMapper.fromMap(postMap)
                // Enrich with user details
                enrichPostWithUserInfo(post)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting post by ID", e)
            Result.failure(e)
        }
    }

    override suspend fun updatePost(postId : String, post: Post): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == post.createdBy) {
                "Only project owner can update"
            }
            require(post.isValid()) { "Invalid post data" }

            val projectMap = PostMapper.toMap(post)
            postDataSource.updatePost(post.id, projectMap)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating project", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            val project = getPostById(projectId).getOrThrow()
            require(auth.currentUser?.uid == project.createdBy) {
                "Only project owner can delete"
            }

            postDataSource.deletePost(projectId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting project", e)
            Result.failure(e)
        }
    }

    override fun getProjectUpdates(projectId: String): Flow<Result<Post>> {
        return postDataSource.getProjectUpdates(projectId).map { result ->
            result.map { PostMapper.fromMap(it) }
        }
    }

    override suspend fun toggleLike(projectId: String, userId: String, isCurrentlyLiked : Boolean): Result<Unit> {
        return try {
            postDataSource.toggleLike(projectId, userId, isCurrentlyLiked)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling like", e)
            Result.failure(e)
        }
    }

    override suspend fun voteOnPoll(
        postId: String,
        userId: String,
        optionIndex: Int,
        currentPollOptions: List<PollOption>
    ): Result<Unit> {
        return try {
            if (optionIndex !in currentPollOptions.indices) {
                return Result.failure(Exception("Invalid poll option"))
            }

            // A user can only ever hold one active vote: strip them from every option,
            // then re-add to optionIndex - unless that's exactly where they already were,
            // which acts as an un-vote.
            val hadVoteHere = currentPollOptions[optionIndex].isVotedBy(userId)
            val updatedOptions = currentPollOptions.mapIndexed { index, option ->
                val newVotes = if (index == optionIndex && !hadVoteHere) {
                    option.votes + userId
                } else {
                    option.votes - userId
                }
                mapOf("text" to option.text, "votes" to newVotes)
            }

            postDataSource.voteOnPoll(postId, updatedOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error voting on poll", e)
            Result.failure(e)
        }
    }

    // ==================== ENROLLMENT OPERATIONS ====================

    override suspend fun enrollInProject(
        project: Post,
        userId: String,
        userName: String,
        imageUrl: String?
    ): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == userId) { "User must be authenticated" }

            val result = postDataSource.enrollInProject(project.id, userId, userName)
            
            if (result.isSuccess) {
                // Send notification
                sendProjectNotification(
                    context,
                    userRepository.getUserFcm(project.createdBy),
                    "$userName is requested to enroll in your project",
                    project.title,
                    project.id,
                    userId,
                    imageUrl
                )
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error enrolling in project", e)
            Result.failure(e)
        }
    }

    override suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == userId) { "User must be authenticated" }
            postDataSource.withdrawFromProject(projectId, userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error withdrawing from project", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptJoinRequest(
        project: Post,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            val result = postDataSource.acceptJoinRequest(project.id, userId, userName)
            
            if (result.isSuccess) {
                // Send notification
                userRepository.getUserFcm(userId)?.let { fcm ->
                    Log.d("FCM", "project request to - $fcm")
                    sendRequestUpdate(context, fcm, project, profileUser, "accepted")
                }
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error accepting join request", e)
            Result.failure(e)
        }
    }

    override suspend fun rejectJoinRequest(
        project: Post,
        userId: String,
        value: profileUser
    ): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            val result = postDataSource.rejectJoinRequest(project.id, userId)
            
            if (result.isSuccess) {
                // Send notification
                userRepository.getUserFcm(userId)?.let { fcm ->
                    sendRequestUpdate(context, fcm, project, value, "rejected")
                }
            }
            
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error rejecting join request", e)
            Result.failure(e)
        }
    }

    // ==================== FILTER OPERATIONS ====================

    override suspend fun getAvailableRoles(): Result<List<String>> {
        return try {
            filterDataSource.getAvailableRoles()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available roles", e)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableTags(): Result<List<String>> {
        return try {
            filterDataSource.getAvailableTags()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting available tags", e)
            Result.failure(e)
        }
    }

    override suspend fun addNewRole(role: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }
            require(role.isNotBlank()) { "Role cannot be empty" }
            filterDataSource.addNewRole(role)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding role", e)
            Result.failure(e)
        }
    }

    override suspend fun addNewTag(tag: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }
            require(tag.isNotBlank()) { "Tag cannot be empty" }
            filterDataSource.addNewTag(tag)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding tag", e)
            Result.failure(e)
        }
    }

    // ==================== COMMENT OPERATIONS ====================

    override fun getComments(
        postId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>> {
        return commentDataSource
            .getComments(postId, lastLikes, lastTimestamp, limit)
            .mapLatest { response ->
                when (response) {
                    is Response.Success -> {
                        val comments = response.data ?: emptyList()
                        val enriched = enrichCommentsWithUserInfo(comments)
                        Response.Success(enriched)
                    }
                    is Response.Error -> response
                    is Response.Loading -> response
                }
            }
    }


    override suspend fun addComment(
        post: Post,
        profile: profileUser,
        comment: Comment
    ): Result<Unit> {
        return try {
            // Generate comment ID if empty
            val commentId = comment.id.ifEmpty {
                commentDataSource.generateCommentId()
            }
            val commentWithId = comment.copy(id = commentId)

            val result = commentDataSource.addComment(post.id, commentWithId)

            if (result.isSuccess) {
                // Send notification
                val deepLink = "findr://project/${post.id}?scrollTo=${commentWithId.id}"
                userRepository.getUserFcm(post.createdBy)?.let { fcm ->
                    sendComment(context, fcm, post.id, profile, comment.text, link = deepLink)
                }
                InAppNotificationHelper.sendNotification(
                    firestore = firestore,
                    recipientId = post.createdBy,
                    type = NotificationType.COMMENT,
                    category = NotificationCategory.SOCIAL,
                    title = "${profile.name} commented on your post",
                    message = comment.text,
                    senderName = profile.name,
                    senderImageUrl = profile.profilePicture,
                    projectImageUrl = post.imageUrls.firstOrNull(),
                    senderId = profile.uid,
                    targetId = post.id,
                    route = "project_detail/${post.id}?scrollTo=${commentWithId.id}"
                )
            }

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            Result.failure(e)
        }
    }

    override suspend fun updateComment(
        projectId: String,
        commentId: String,
        newText: String
    ): Result<Unit> {
        return try {
            if (commentId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Comment ID cannot be empty"))
            }
            commentDataSource.updateComment(projectId, commentId, newText)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating comment", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(
        postId: String,
        commentId: String,
        userId: String
    ): Result<Unit> {
        return try {
            commentDataSource.deleteComment(postId, commentId, userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment", e)
            Result.failure(e)
        }
    }

    // ==================== EMBEDDED REPLY OPERATIONS (DEPRECATED) ====================

    override suspend fun addEmbeddedReply(
        postId: String,
        commentId: String,
        reply: Reply
    ): Result<Unit> {
        return try {
            val replyId = reply.replyId.ifEmpty {
                commentDataSource.generateCommentId()
            }
            val replyWithId = reply.copy(replyId = replyId)
            commentDataSource.addEmbeddedReply(postId, commentId, replyWithId)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding embedded reply", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEmbeddedReply(
        postId: String,
        commentId: String,
        newReplyText: String
    ): Result<Unit> {
        return try {
            commentDataSource.updateEmbeddedReply(postId, commentId, newReplyText)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating embedded reply", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteEmbeddedReply(postId: String, commentId: String): Result<Unit> {
        return try {
            commentDataSource.deleteEmbeddedReply(postId, commentId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting embedded reply", e)
            Result.failure(e)
        }
    }

    // ==================== COMMENT & REPLY LIKE OPERATIONS ====================

    override suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        replyId : String?,
        userId: String,
        isCurrentlyLiked : Boolean
    ): Result<Unit> {
        return try {
            nestedCommentDataSource.toggleCommentLike(postId, commentId, replyId, userId, isCurrentlyLiked)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling comment like", e)
            Result.failure(e)
        }
    }

    // ==================== NESTED REPLY OPERATIONS ====================

    override suspend fun addReply(
        postId: String,
        profile: profileUser,
        reply: Comment,
        parentCommentId : String,
        parentCommentSenderId : String
    ): Result<Unit> {
        return try {
           val result = nestedCommentDataSource.addReply(postId, parentCommentId, reply)

            if (result.isSuccess) {
                // Send notification
                val deepLink = "findr://project/$postId?scrollTo=${reply.id}"
                userRepository.getUserFcm(parentCommentSenderId)?.let { fcm ->
                    sendReply(context, fcm, postId, profile, reply.text, link = deepLink)
                }
                InAppNotificationHelper.sendNotification(
                    firestore = firestore,
                    recipientId = parentCommentSenderId,
                    type = NotificationType.COMMENT,
                    category = NotificationCategory.SOCIAL,
                    title = "${profile.name} replied to your comment",
                    message = reply.text,
                    senderName = profile.name,
                    senderImageUrl = profile.profilePicture,
                    senderId = profile.uid,
                    targetId = postId,
                    route = "project_detail/$postId?scrollTo=${reply.id}"
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reply", e)
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getReplies(
        postId: String,
        rootCommentId : String?,
        parentCommentId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>> {
        Log.d(TAG, "get replies called")
        return nestedCommentDataSource.getReplies(postId, rootCommentId,parentCommentId, lastLikes, lastTimestamp, limit)
            .mapLatest { response ->
                Log.d(TAG, response.toString())
                when (response) {
                    is Response.Success -> {
                        val replies = response.data
                        val enriched = enrichCommentsWithUserInfo(replies)
                        Response.Success(enriched)
                    }
                    is Response.Error -> {
                        Log.e(TAG, "Error loading replies", Throwable(response.message))
                        response
                    }

                    is Response.Loading -> response
                }
            }
    }

    override suspend fun updateReply(
        newReplyText: String,
        postId: String,
        rootCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {
            nestedCommentDataSource.updateReply(
                newReplyText,
                postId,
                rootCommentId,
                replyId,
                userId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reply", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteReply(
        postId: String,
        rootCommentId: String?,
        parentCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {
            nestedCommentDataSource.deleteReply(
                postId,
                rootCommentId,
                parentCommentId,
                replyId,
                userId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting embedded reply", e)
            Result.failure(e)
        }
    }


    // ==================== USER ENRICHMENT HELPERS ====================
    
    /**
     * Enrich a single post with user details
     */
    private suspend fun enrichPostWithUserInfo(post: Post): Post {
        return try {
            val userResult = userRepository.getUserInfoById(post.createdBy)
            val userInfo = userResult.getOrNull()?.toPostUserInfo()
            post.copy(userInfo = userInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching post with user info", e)
            post // Return post without enrichment if error
        }
    }
    
    /**
     * Enrich multiple posts with user details - batches user fetching for efficiency
     */
    private suspend fun enrichPostsWithUserInfo(posts: List<Post>): List<Post> {
        return try {
            if (posts.isEmpty()) return posts
            
            // Collect all unique user IDs from posts
            val userIds = posts.map { it.createdBy }.distinct()
            
            // Batch fetch all users
            val usersResult = userRepository.getUsersInfoByIds(userIds)
            val usersMap = usersResult.getOrNull() ?: emptyMap()
            
            // Map users to PostUserInfo
            val userInfoMap = usersMap.mapValues { it.value.toPostUserInfo() }
            
            // Enrich each post with its user info
            posts.map { post ->
                post.copy(userInfo = userInfoMap[post.createdBy])
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching posts with user info", e)
            posts // Return posts without enrichment if error
        }
    }
    
    /**
     * Enrich comments with user details
     */
    private suspend fun enrichCommentsWithUserInfo(comments: List<Comment>): List<Comment> {
        return try {
            if (comments.isEmpty()) return comments
            
            // Collect all unique user IDs from comments
            val userIds = comments.map { it.senderId }.distinct()
            
            // Batch fetch all users
            val usersResult = userRepository.getUsersInfoByIds(userIds)
            val usersMap = usersResult.getOrNull() ?: emptyMap()
            
            // Map users to PostUserInfo
            val userInfoMap = usersMap.mapValues { it.value.toPostUserInfo() }
            
            // Enrich each comment with its user info
            comments.map { comment ->
                comment.copy(userInfo = userInfoMap[comment.senderId])
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error enriching comments with user info", e)
            comments // Return comments without enrichment if error
        }
    }
    
    // ==================== ENROLLMENT & REGISTRATION OPERATIONS ====================
    
    override suspend fun enrollInRole(
        enrollment: RoleEnrollment
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            
            // Add enrollment document
            val enrollRef = firestore.collection(AppCollections.ENROLLMENTS).document()
            batch.set(enrollRef, enrollment)
            
            // Update requestedPersons on the post so the feed cards know the user has applied
            val postRef = firestore.collection(PostConstants.Collections.PROJECTS).document(enrollment.postId)
            batch.update(postRef, "requestedPersons.${enrollment.userId}", enrollment.roleId)
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error enrolling in role", e)
            Result.failure(e)
        }
    }
    
    override suspend fun registerForEvent(
        registration: EventRegistration
    ): Result<Unit> {
        return try {
            val postRef = firestore.collection(PostConstants.Collections.PROJECTS)
                .document(registration.postId)
            firestore.runTransaction { transaction ->
                val postSnap = transaction.get(postRef)
                @Suppress("UNCHECKED_CAST")
                val eventData = postSnap.get("eventDetails") as? Map<*, *>
                val isManuallyClosed = eventData?.get("isManuallyClosed") as? Boolean ?: false
                val capacity = (eventData?.get("capacity") as? Long)?.toInt()
                val registeredCount = (eventData?.get("registeredCount") as? Long)?.toInt() ?: 0
                if (isManuallyClosed) throw Exception("Registration is closed")
                if (capacity != null && registeredCount >= capacity) throw Exception("Event is full")

                val registrationRef = firestore.collection(AppCollections.REGISTRATIONS).document()
                transaction.set(registrationRef, registration)
                transaction.update(postRef, "eventDetails.registeredCount", FieldValue.increment(1))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering for event", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getEnrollmentsForPost(
        postId: String
    ): Result<List<RoleEnrollment>> {
        return try {
            val enrollments = firestore.collection(AppCollections.ENROLLMENTS)
                .whereEqualTo("postId", postId)
                .get()
                .await()
                .toObjects(RoleEnrollment::class.java)
            Result.success(enrollments)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enrollments", e)
            Result.failure(e)
        }
    }
    
//    override suspend fun getRegistrationsForEvent(
//        postId: String
//    ): Result<List<EventRegistration>> {
//        return try {
//            val registrations = firestore.collection(AppCollections.REGISTRATIONS)
//                .whereEqualTo("postId", postId)
//                .get()
//                .await()
//                .toObjects(EventRegistration::class.java)
//            Result.success(registrations)
//        } catch (e: Exception) {
//            Log.e(TAG, "Error getting registrations", e)
//            Result.failure(e)
//        }
//    }
    
    override suspend fun updateEnrollmentStatus(
        enrollmentId: String,
        status: EnrollmentStatus
    ): Result<Unit> {
        return try {
            val enrollmentRef = firestore.collection(AppCollections.ENROLLMENTS).document(enrollmentId)
            firestore.runTransaction { transaction ->
                // ── PHASE 1: ALL READS FIRST ──────────────────────────────────
                val enrollmentSnap = transaction.get(enrollmentRef)
                val postId = enrollmentSnap.getString("postId")
                val userId = enrollmentSnap.getString("userId")
                val roleName = enrollmentSnap.getString("roleName") ?: ""
                val roleId = enrollmentSnap.getString("roleId") ?: ""

                // Read postSnap up-front (before any writes) if we'll need it
                val postRef = if (!postId.isNullOrBlank()) {
                    firestore.collection(PostConstants.Collections.PROJECTS).document(postId)
                } else null
                val postSnap = postRef?.let { transaction.get(it) }

                // ── PHASE 2: ALL WRITES AFTER ─────────────────────────────────
                transaction.update(
                    enrollmentRef, mapOf(
                        "status" to status.name,
                        "respondedAt" to FieldValue.serverTimestamp()
                    )
                )

                if (postRef != null && postSnap != null && !userId.isNullOrBlank()) {
                    when (status) {
                        EnrollmentStatus.ACCEPTED -> {
                            transaction.update(postRef, "enrolledPersons.$userId", roleName)
                            transaction.update(postRef, "requestedPersons.$userId", FieldValue.delete())
                            // Atomically increment positionsFilled for the matching role
                            @Suppress("UNCHECKED_CAST")
                            val roles = postSnap.get(PostConstants.PostFields.PROJECT_ROLES)
                                    as? List<HashMap<String, Any>> ?: emptyList()
                            val updatedRoles = roles.map { role ->
                                if (role["id"] == roleId) {
                                    val newRole = HashMap(role)
                                    val filled = (newRole["positionsFilled"] as? Long)?.toInt() ?: 0
                                    newRole["positionsFilled"] = (filled + 1).toLong()
                                    newRole
                                } else role
                            }
                            transaction.update(postRef, PostConstants.PostFields.PROJECT_ROLES, updatedRoles)
                        }
                        EnrollmentStatus.REJECTED -> {
                            transaction.update(postRef, "enrolledPersons.$userId", FieldValue.delete())
                            transaction.update(postRef, "requestedPersons.$userId", FieldValue.delete())
                        }
                        else -> {}
                    }
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating enrollment status", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isUserEnrolledInRole(
        postId: String,
        roleId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            val enrollments = firestore.collection(AppCollections.ENROLLMENTS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("roleId", roleId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", EnrollmentStatus.ACCEPTED)
                .get()
                .await()
            Result.success(!enrollments.isEmpty)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking enrollment", e)
            Result.failure(e)
        }
    }
    
    override suspend fun isUserRegisteredForEvent(
        postId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            val registrations = firestore.collection(AppCollections.REGISTRATIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", RegistrationStatus.REGISTERED)
                .get()
                .await()
            Result.success(!registrations.isEmpty)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking registration", e)
            Result.failure(e)
        }
    }
    
    override fun getMyPosts(userId: String): Flow<Result<List<Post>>> {
        // Reuse existing getPostsStream which already returns Flow<Result<List<Post>>>
        return getPostsStream(
            searchQuery = "",
            rolesFilter = emptyList(),
            tagsFilter = emptyList(),
            showMyProjectsOnly = true,
            userId = userId,
            postType = null
        )
    }
    
    override suspend fun deletePost(postId: String, userId: String): Result<Unit> {
        return try {
            // Verify ownership
            val post = getPostById(postId).getOrNull()
            if (post?.createdBy != userId) {
                return Result.failure(Exception("Only post owner can delete"))
            }
            
            // TODO: Delete associated data (enrollments, registrations, comments)
            // TODO: Delete media files from storage
            
            // Use existing deleteProject method from data source
            deleteProject(postId)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            Result.failure(e)
        }
    }

    override suspend fun incrementPostView(postId: String): Result<Unit> {
        return try {
            postDataSource.updatePost(
                postId,
                mapOf(PostConstants.PostFields.VIEWS_COUNT to FieldValue.increment(1))
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing post view", e)
            Result.failure(e)
        }
    }

    override suspend fun getEnrollmentStatus(
        postId: String,
        roleId: String,
        userId: String
    ): Result<EnrollmentStatus> {
        return try {
            val enrollments = firestore.collection(AppCollections.ENROLLMENTS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("roleId", roleId)
                .whereEqualTo("userId", userId)
                .get()
                .await()
            
            if (enrollments.isEmpty) {
                return Result.success(EnrollmentStatus.NOT_ENROLLED)
            }
            
            val enrollment = enrollments.documents.first()
            val status = enrollment.getString("status") ?: "PENDING"
            
            val enrollmentStatus = when (status) {
                "PENDING" -> EnrollmentStatus.PENDING
                "ACCEPTED" -> EnrollmentStatus.ACCEPTED
                "REJECTED" -> EnrollmentStatus.REJECTED
                else -> EnrollmentStatus.NOT_ENROLLED
            }
            
            Result.success(enrollmentStatus)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting enrollment status", e)
            Result.failure(e)
        }
    }
    
    override suspend fun incrementRoleFilledCount(postId: String, roleId: String): Result<Unit> {
        return try {
            val postDoc = firestore.collection(PostConstants.Collections.PROJECTS)
                .document(postId)
                .get()
                .await()
            
            val postData = postDoc.data ?: return Result.failure(Exception("Post not found"))
            
            @Suppress("UNCHECKED_CAST")
            val rolesList = postData[PostConstants.PostFields.PROJECT_ROLES] as? List<Map<String, Any?>> ?: emptyList()
            
            val updatedRoles = rolesList.map { roleMap ->
                if (roleMap["id"] == roleId) {
                    val currentFilled = (roleMap["positionsFilled"] as? Long)?.toInt() ?: 0
                    roleMap.toMutableMap().apply {
                        put("positionsFilled", currentFilled + 1)
                    }
                } else {
                    roleMap
                }
            }
            
            firestore.collection(PostConstants.Collections.PROJECTS)
                .document(postId)
                .update(PostConstants.PostFields.PROJECT_ROLES, updatedRoles)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing role filled count", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getRegistrationsForEvent(postId: String): Result<List<EventRegistration>> {
        return try {
            val registrations = firestore.collection(AppCollections.REGISTRATIONS)
                .whereEqualTo("postId", postId)
                .whereEqualTo("status", "REGISTERED")
                .get()
                .await()

            val registrationList = registrations.documents.mapNotNull { doc ->
                try {
                    EventRegistration(
                        id = doc.id,
                        postId = doc.getString("postId") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        userProfilePic = doc.getString("userAvatar"),
                        status = RegistrationStatus.REGISTERED,
                        registeredAt = doc.getTimestamp("registeredAt") ?: Timestamp.now()
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing registration", e)
                    null
                }
            }

            Result.success(registrationList)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting registrations", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEventClosureStatus(postId: String, isClosed: Boolean): Result<Unit> {
        return try {
            val postRef = firestore.collection(PostConstants.Collections.PROJECTS).document(postId)
            postRef.update("eventDetails.isManuallyClosed", isClosed).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event closure status", e)
            Result.failure(e)
        }
    }

    override suspend fun updateRoleClosureStatus(postId: String, roleId: String, isClosed: Boolean): Result<Unit> {
        return try {
            val postRef = firestore.collection(PostConstants.Collections.PROJECTS).document(postId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                @Suppress("UNCHECKED_CAST")
                val roles = snapshot.get(PostConstants.PostFields.PROJECT_ROLES) as? List<HashMap<String, Any>> ?: emptyList()
                
                val updatedRoles = roles.map { role ->
                    if (role["id"] == roleId) {
                        val newRole = HashMap(role)
                        newRole["isManuallyClosed"] = isClosed
                        newRole
                    } else {
                        role
                    }
                }
                
                transaction.update(postRef, PostConstants.PostFields.PROJECT_ROLES, updatedRoles)
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating role closure status", e)
            Result.failure(e)
        }
    }

    override suspend fun updateEventCapacity(postId: String, newCapacity: Int): Result<Unit> {
        return try {
            firestore.collection(PostConstants.Collections.PROJECTS)
                .document(postId)
                .update("eventDetails.capacity", newCapacity)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating event capacity", e)
            Result.failure(e)
        }
    }

    override suspend fun inviteUserToProject(
        postId: String,
        projectTitle: String,
        invitedUserId: String,
        invitedFcmToken: String,
        inviterName: String,
        roleName: String?
    ): Result<Unit> {
        return try {
            sendProjectInvitation(context, invitedFcmToken, projectTitle, inviterName, roleName, postId)
            val body = if (roleName != null)
                "$inviterName invited you to join \"$projectTitle\" as $roleName"
            else
                "$inviterName invited you to join \"$projectTitle\""
            InAppNotificationHelper.sendNotification(
                firestore = firestore,
                recipientId = invitedUserId,
                type = NotificationType.INVITATION,
                category = NotificationCategory.PLACEMENTS,
                title = "You've been invited!",
                message = body,
                senderName = inviterName,
                senderId = auth.currentUser?.uid ?: "",
                targetId = postId
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending project invitation", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProjectRole(postId: String, updatedRole: ProjectRole): Result<Unit> {
        return try {
            val postRef = firestore.collection(PostConstants.Collections.PROJECTS).document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                @Suppress("UNCHECKED_CAST")
                val roles = snapshot.get(PostConstants.PostFields.PROJECT_ROLES) as? List<HashMap<String, Any>> ?: emptyList()
                val updatedRoles = roles.map { role ->
                    if (role["id"] == updatedRole.id) {
                        hashMapOf(
                            "id" to updatedRole.id,
                            "roleName" to updatedRole.roleName,
                            "description" to (updatedRole.description ?: ""),
                            "technology" to (updatedRole.technology ?: ""),
                            "positionsNeeded" to updatedRole.positionsNeeded.toLong(),
                            "positionsFilled" to (role["positionsFilled"] as? Long ?: 0L),
                            "isManuallyClosed" to (role["isManuallyClosed"] as? Boolean ?: false)
                        )
                    } else role
                }
                transaction.update(postRef, PostConstants.PostFields.PROJECT_ROLES, updatedRoles)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating project role", e)
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "PostRepository"
    }
}