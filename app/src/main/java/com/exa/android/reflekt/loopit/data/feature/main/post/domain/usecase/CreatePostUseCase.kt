package com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase

import android.net.Uri
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import javax.inject.Inject

/**
 * Use case for creating a new post
 * Handles validation, media upload, and post creation
 */
class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository,
    private val validatePostUseCase: ValidatePostUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase
) {
    /**
     * Creates a post with media uploads
     * @param post The post data to create
     * @param profile The user profile creating the post
     * @param imageUris List of local image URIs to upload
     * @param fileUris List of local file URIs to upload
     * @return Result with Unit on success or error message on failure
     */
    suspend operator fun invoke(
        post: Post,
        profile: profileUser,
        imageUris: List<Uri> = emptyList(),
        fileUris: List<Pair<Uri, String>> = emptyList() // Pair of URI and filename
    ): Result<Unit> {
        return try {
            // Step 1: Validate post data
            val validationResult = validatePostUseCase(post, imageUris.size, fileUris.size)
            if (validationResult.isFailure) {
                return validationResult
            }
            
            // Step 2: Upload images if any
            val imageUrls = if (imageUris.isNotEmpty()) {
                uploadMediaUseCase.uploadImages(imageUris, post.id)
                    .getOrElse { error ->
                        return Result.failure(Exception("Failed to upload images: ${error.message}"))
                    }
            } else {
                emptyList()
            }
            
            // Step 3: Upload files if any
            val fileUrls = if (fileUris.isNotEmpty()) {
                uploadMediaUseCase.uploadFiles(fileUris, post.id)
                    .getOrElse { error ->
                        return Result.failure(Exception("Failed to upload files: ${error.message}"))
                    }
            } else {
                emptyList()
            }
            
            // Step 4: Create post with uploaded URLs
            val postWithMedia = post.copy(
                imageUrls = imageUrls,
                fileUrls = fileUrls
            )
            
            postRepository.createPost(postWithMedia, profile)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to create post: ${e.message}"))
        }
    }
}
