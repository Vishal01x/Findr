package com.exa.android.reflekt.loopit.presentation.main.post.create

import android.net.Uri
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.FileAttachment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole

/**
 * State for the Create Post screen
 * Manages all input fields and UI state
 */
data class CreatePostState(
    // Post type selection
    val selectedPostType: PostType = PostType.PROJECT,

    // Edit mode
    val postId: String? = null,
    val isEditMode: Boolean = false,
    val isLoadingPost: Boolean = false,

    // Basic fields
    val title: String = "",
    val description: String = "",
    val links: List<String> = emptyList(),
    val currentLinkInput: String = "",

    // Media attachments (newly picked by user, local URIs)
    val selectedImages: List<Uri> = emptyList(),
    val selectedFiles: List<FileAttachment> = emptyList(),

    // Existing remote URLs (from the saved post, shown in edit mode)
    val existingImageUrls: List<String> = emptyList(),
    val existingFileUrls: List<String> = emptyList(),

    // Project-specific fields
    val projectRoles: List<ProjectRole> = emptyList(),
    val showAddRoleDialog: Boolean = false,
    val editingRole: ProjectRole? = null,

    // Event-specific fields
    val eventDetails: EventDetails? = null,
    val showEventDetailsDialog: Boolean = false,

    // Poll-specific fields (2-5 options)
    val pollOptions: List<String> = listOf("", ""),

    // Identity
    val isAnonymous: Boolean = false,

    // Link preview metadata (for Share-a-Post screen)
    val linkPreviewTitle: String? = null,
    val linkPreviewDescription: String? = null,
    val linkPreviewImageUrl: String? = null,
    val linkPreviewDomain: String? = null,
    val isLoadingLinkPreview: Boolean = false,

    // Upload state
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val error: String? = null,

    // Navigation state
    val showDiscardDialog: Boolean = false,
    val postCreatedSuccessfully: Boolean = false
) {
    /**
     * Checks if there are unsaved changes
     */
    fun hasUnsavedChanges(): Boolean {
        return title.isNotBlank() ||
                description.isNotBlank() ||
                links.isNotEmpty() ||
                selectedImages.isNotEmpty() ||
                selectedFiles.isNotEmpty() ||
                projectRoles.isNotEmpty() ||
                eventDetails != null ||
                pollOptions.any { it.isNotBlank() }
    }

    /**
     * Checks if post can be submitted
     */
    fun canSubmit(): Boolean {
        if (title.isBlank()) return false
        if (isUploading) return false

        // Type-specific validation
        return when (selectedPostType) {
            PostType.EVENT -> eventDetails?.isValid() == true
            PostType.POLL -> pollOptions.count { it.isNotBlank() } >= 2
            else -> true // Other types don't have strict requirements
        }
    }
}

/**
 * For Handling One Time Event in Ui
 */
sealed interface CreatePostUiEvent {
    object PopBack : CreatePostUiEvent
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : CreatePostUiEvent
}

