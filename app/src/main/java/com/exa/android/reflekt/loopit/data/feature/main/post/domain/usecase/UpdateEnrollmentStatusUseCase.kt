package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Update enrollment status (accept or reject)
 * Also updates the post's role filled count when accepting
 */
class UpdateEnrollmentStatusUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(
        enrollmentId: String,
        postId: String,
        roleId: String,
        status: EnrollmentStatus
    ): Result<Unit> {
        return try {
            // Update enrollment status
            repository.updateEnrollmentStatus(enrollmentId, status).getOrThrow()
            
            // If accepting, increment the role's filled count
            if (status == EnrollmentStatus.ACCEPTED) {
                repository.incrementRoleFilledCount(postId, roleId).getOrThrow()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
