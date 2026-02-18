package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

/**
 * Use case to add a top-level comment to a post
 * Automatically gets current user info and creates comment
 */
class AddCommentUseCase @Inject constructor(
    private val repository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(
        postId: String,
        commentText: String
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            // Get current user profile
            val profileResult = userRepository.getUserInfoById(currentUserId)
            val profile = profileResult.getOrNull()
                ?: return Result.failure(Exception("Failed to get user profile"))


            // Load post first
            val postResult = repository.getPostById(postId)
            val post = postResult.getOrNull()
                ?: return Result.failure(Exception("Failed to load Post"))

            // Create comment
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                postId = post.id,
                senderId = currentUserId,
                senderName = profile.name,
                senderImageUrl = profile.profilePicture.ifEmpty { null },
                text = commentText,
                likes = emptyList(),
                likesCount = 0,
                replyCount = 0,
                parentCommentId = null,
                nestingLevel = 0,
                timestamp = Timestamp.now()
            )

            repository.addComment(post, profile, comment)
        } catch (e: Exception) {
            Log.e("AddCommentUseCase", "Error adding comment", e)
            Result.failure(e)
        }
    }
}
