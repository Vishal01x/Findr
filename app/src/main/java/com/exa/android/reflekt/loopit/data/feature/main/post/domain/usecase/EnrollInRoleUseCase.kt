package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.exa.android.reflekt.loopit.fcm.sendProjectNotification
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.InAppNotificationHelper
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for enrolling in a project role
 * Handles the enrollment request creation
 */
class EnrollInRoleUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth,
    private val userRepository: com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
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
            
            // Get post to find author
            val post = postRepository.getPostById(postId).getOrNull()
            if (post != null && post.createdBy != currentUser.uid) {
                val deepLink = "findr://author_management/$postId"
                val fcm = userRepository.getUserFcm(post.createdBy)
                sendProjectNotification(
                    context = context,
                    receiverToken = fcm,
                    body = "${profile.name} has requested to enroll in your project",
                    title = post.title,
                    projectId = postId,
                    senderId = currentUser.uid,
                    imageUrl = profile.profilePicture,
                    link = deepLink
                )
                
                InAppNotificationHelper.sendNotification(
                    firestore = firestore,
                    recipientId = post.createdBy,
                    type = NotificationType.ENROLL_REQUEST,
                    category = NotificationCategory.SOCIAL,
                    title = "Enrollment Request",
                    message = "${profile.name} has requested to enroll in your project",
                    senderName = profile.name,
                    senderImageUrl = profile.profilePicture,
                    projectImageUrl = post.imageUrls.firstOrNull(),
                    senderId = profile.uid,
                    targetId = postId,
                    route = "author_management/$postId"
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to enroll: ${e.message}"))
        }
    }
}
