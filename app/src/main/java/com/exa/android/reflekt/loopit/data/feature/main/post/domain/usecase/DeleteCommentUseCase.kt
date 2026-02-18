package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(comment: Comment): Result<Unit> {
        return try {
            val curUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            if (comment.isTopLevel()) {
                postRepository.deleteComment(comment.postId, comment.id, curUserId)
            } else {
                postRepository.deleteReply(
                    comment.postId,
                    comment.rootCommentId,
                    comment.parentCommentId,
                    comment.id,
                    curUserId
                )
            }

        } catch (e: Exception) {
            Log.e("DeleteCommentUseCase", "Error adding comment", e)
            Result.failure(e)
        }
    }
}