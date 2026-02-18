package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to add a reply to an existing comment
 * Handles nesting level validation (max 3 levels)
 */
class AddReplyUseCase @Inject constructor(
    private val repository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(
        postId: String,
        parentComment: Comment,
        replyText: String
    ): Result<Unit> {
        return try {
            // Validate nesting level
            if (!parentComment.canReply()) {
                return Result.failure(Exception("Maximum nesting level (3) reached"))
            }

            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            // Get current user profile
            val profileResult = userRepository.getUserInfoById(currentUserId)
            val profile = profileResult.getOrNull()
                ?: return Result.failure(Exception("Failed to get user profile"))

            // Create reply comment
            // Determine root comment ID:
            // - If parent is top-level (nestingLevel 0), root is the parent itself
            // - If parent is a reply (nestingLevel > 0), inherit its rootCommentId
            val rootId = if (parentComment.nestingLevel == 0) {
                parentComment.id
            } else {
                parentComment.rootCommentId ?: parentComment.id
            }
            
            val reply = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                senderId = currentUserId,
                senderName = profile.name,
                senderImageUrl = profile.profilePicture.ifEmpty { null },
                text = replyText,
                likes = emptyList(),
                likesCount = 0,
                replyCount = 0,
                parentCommentId = parentComment.id,
                rootCommentId = rootId,
                nestingLevel = parentComment.nestingLevel + 1,
                timestamp = Timestamp.now()
            )

            repository.addReply(
                postId = postId,
                profile = profile,
                reply = reply,
                parentCommentId = parentComment.id,
                parentCommentSenderId =  parentComment.senderId
            )
        } catch (e: Exception) {
            Log.e("AddReplyUseCase", "Error adding reply", e)
            Result.failure(e)
        }
    }
}
