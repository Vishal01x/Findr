package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case for managing enrollment requests
 * Allows post owners to accept or reject enrollment requests
 */
class ManageEnrollmentUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth
) {
    /**
     * Accept an enrollment request
     */
    suspend fun accept(
        postId: String,
        enrollmentId: String
    ): Result<Unit> {
        return updateStatus(postId, enrollmentId, EnrollmentStatus.ACCEPTED)
    }
    
    /**
     * Reject an enrollment request
     */
    suspend fun reject(
        postId: String,
        enrollmentId: String
    ): Result<Unit> {
        return updateStatus(postId, enrollmentId, EnrollmentStatus.REJECTED)
    }
    
    private suspend fun updateStatus(
        postId: String,
        enrollmentId: String,
        status: EnrollmentStatus
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Verify user owns the post
            val post = postRepository.getPostById(postId).getOrNull()
                ?: return Result.failure(Exception("Post not found"))
            
            if (post.createdBy != currentUser.uid) {
                return Result.failure(Exception("Only post owner can manage enrollments"))
            }
            
            postRepository.updateEnrollmentStatus(enrollmentId, status)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update enrollment: ${e.message}"))
        }
    }
}
