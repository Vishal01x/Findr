package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

/**
 * Use case for registering for an event
 * Handles the event registration creation
 */
class RegisterForEventUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) {
    suspend operator fun invoke(
        postId: String
    ): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))
            
            // Get user profile for name and picture
            val profile = profileRepository.getUserProfile(currentUser.uid)
            
            // Create registration
            val registration = EventRegistration(
                postId = postId,
                userId = currentUser.uid,
                userName = profile.name,
                userProfilePic = profile.profilePicture
            )
            
            postRepository.registerForEvent(registration)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to register: ${e.message}"))
        }
    }
}
