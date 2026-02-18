package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class UpdateCommentUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(newText : String, comment: Comment): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            if (comment.isTopLevel()) {
                postRepository.updateComment(comment.postId, comment.id, newText)
            } else {
                postRepository.updateReply(
                    newText,
                    comment.postId,
                    comment.rootCommentId,
                    comment.id,
                    currentUserId
                )
            }

        } catch (e: Exception) {
            Log.e("DeleteCommentUseCase", "Error adding comment", e)
            Result.failure(e)
        }
    }
}