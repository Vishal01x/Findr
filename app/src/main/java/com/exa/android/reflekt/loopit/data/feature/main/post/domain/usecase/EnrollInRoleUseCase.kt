package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case for enrolling in a project role
 * Handles the enrollment request creation
 */
class EnrollInRoleUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(
        postId: String,
        roleId: String,
        message: String? = null
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Get user profile for name and picture
            val profile = profileRepository.getUserProfile(currentUser.uid)
            
            // Create enrollment request
            val enrollment = RoleEnrollment(
                postId = postId,
                roleId = roleId,
                userId = currentUser.uid,
                userName = profile.name,
                userProfilePic = profile.profilePicture,
                message = message
            )
            
            postRepository.enrollInRole(enrollment)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to enroll: ${e.message}"))
        }
    }
}
