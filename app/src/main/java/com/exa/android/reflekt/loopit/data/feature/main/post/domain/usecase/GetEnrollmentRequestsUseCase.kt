package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import javax.inject.Inject

/**
 * Get all enrollment requests for a post, grouped by role
 */
class GetEnrollmentRequestsUseCase @Inject constructor(
    private val repository: PostRepository
) {
    suspend operator fun invoke(postId: String): Result<Map<String, List<RoleEnrollment>>> {
        return try {
            val enrollments = repository.getEnrollmentsForPost(postId).getOrThrow()
            
            // Group by roleId
            val grouped = enrollments.groupBy { it.roleId }
            
            Result.success(grouped)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
