package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Get all registrations for an event
 */
class GetRegistrationsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<List<EventRegistration>> {
        return try {
            repository.getRegistrationsForEvent(postId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
