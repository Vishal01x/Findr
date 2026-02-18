package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case to toggle like on a comment or reply
 * Automatically gets current user ID
 */
class ToggleCommentLikeUseCase @Inject constructor(
    private val repository: PostRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(
        comment : Comment
    ): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val isCurrentlyLiked = comment.likes.contains(currentUserId)
            val commentId = if(comment.nestingLevel == 0)comment.id else comment.rootCommentId
            val replyId = if(comment.nestingLevel == 0)null else comment.id
            repository.toggleCommentLike(comment.postId, commentId ?: comment.id, replyId, currentUserId, isCurrentlyLiked)
        } catch (e: Exception) {
            Log.e("ToggleCommentLikeUseCase", "Error toggling comment like", e)
            Result.failure(e)
        }
    }
}
