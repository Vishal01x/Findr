package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Use case to update an existing post
 */
class UpdatePostUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String, post: Post): Result<Unit> {
        return try {
            repository.updatePost(postId, post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
