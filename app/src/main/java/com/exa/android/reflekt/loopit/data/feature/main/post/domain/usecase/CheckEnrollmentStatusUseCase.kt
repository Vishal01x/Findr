package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Check if user has enrolled or has pending request for a specific role
 */
class CheckEnrollmentStatusUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(
        postId: String,
        roleId: String,
        userId: String
    ): Result<EnrollmentStatus> {
        return try {
            repository.getEnrollmentStatus(postId, roleId, userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
