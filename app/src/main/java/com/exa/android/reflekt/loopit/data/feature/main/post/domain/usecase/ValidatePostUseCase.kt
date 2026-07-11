package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import javax.inject.Inject

/**
 * Use case for validating post data before creation
 * Ensures all required fields are present and valid
 */
class ValidatePostUseCase @Inject constructor() {
    
    /**
     * Validates post data
     * @param post The post to validate
     * @param imageCount Number of images being uploaded
     * @param fileCount Number of files being uploaded
     * @return Result with Unit on success or error message on failure
     */
    operator fun invoke(
        post: Post,
        imageCount: Int = 0,
        fileCount: Int = 0
    ): Result<Unit> {
        // Validate title
        if (post.title.isBlank()) {
            return Result.failure(Exception(PostConstants.ValidationMessages.TITLE_REQUIRED))
        }
        
        if (post.title.length > PostConstants.Limits.MAX_TITLE_LENGTH) {
            return Result.failure(Exception(PostConstants.ValidationMessages.TITLE_TOO_LONG))
        }
        
        // Validate description length
        if (post.description.length > PostConstants.Limits.MAX_DESCRIPTION_LENGTH) {
            return Result.failure(Exception(PostConstants.ValidationMessages.DESCRIPTION_TOO_LONG))
        }
        
        // Validate image count
        if (imageCount > PostConstants.Limits.MAX_IMAGES) {
            return Result.failure(Exception("Maximum ${PostConstants.Limits.MAX_IMAGES} images allowed"))
        }
        
        // Validate file count
        if (fileCount > PostConstants.Limits.MAX_FILES) {
            return Result.failure(Exception("Maximum ${PostConstants.Limits.MAX_FILES} files allowed"))
        }
        
        // Validate links count
        if (post.links.size > PostConstants.Limits.MAX_LINKS) {
            return Result.failure(Exception("Maximum ${PostConstants.Limits.MAX_LINKS} links allowed"))
        }
        
        // Validate post type specific requirements
        when (PostType.fromDisplayName(post.type)) {
            PostType.PROJECT -> {
                // Project roles validation
                if (post.projectRoles.size > PostConstants.Limits.MAX_PROJECT_ROLES) {
                    return Result.failure(Exception("Maximum ${PostConstants.Limits.MAX_PROJECT_ROLES} roles allowed"))
                }
                
                // Validate each role
                post.projectRoles.forEach { role ->
                    if (!role.isValid()) {
                        return Result.failure(Exception("Invalid role: ${role.roleName}. Role name and positions are required."))
                    }
                }
            }
            
            PostType.EVENT -> {
                // Event details validation
                val eventDetails = post.eventDetails
                if (eventDetails == null) {
                    return Result.failure(Exception("Event details are required for event posts"))
                }

                if (!eventDetails.isValid()) {
                    return Result.failure(Exception("Invalid event details. Date and location/meeting link are required."))
                }
            }

            PostType.POLL -> {
                val validOptions = post.pollOptions.filter { it.text.isNotBlank() }
                if (validOptions.size < PostConstants.Limits.MIN_POLL_OPTIONS) {
                    return Result.failure(Exception("A poll needs at least ${PostConstants.Limits.MIN_POLL_OPTIONS} options"))
                }
                if (validOptions.size > PostConstants.Limits.MAX_POLL_OPTIONS) {
                    return Result.failure(Exception("Maximum ${PostConstants.Limits.MAX_POLL_OPTIONS} poll options allowed"))
                }
            }

            else -> {
                // No specific validation for other types
            }
        }
        
        return Result.success(Unit)
    }
}
