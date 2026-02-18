package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Check if user has registered for an event
 */
class CheckRegistrationStatusUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(
        postId: String,
        userId: String
    ): Result<Boolean> {
        return try {
            repository.isUserRegisteredForEvent(postId, userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
