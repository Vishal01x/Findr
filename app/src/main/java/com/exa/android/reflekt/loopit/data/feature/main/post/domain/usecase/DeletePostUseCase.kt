package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case for deleting a post
 * Only allows post owner to delete
 */
class DeletePostUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(postId: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            postRepository.deletePost(postId, currentUser.uid)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete post: ${e.message}"))
        }
    }
}
