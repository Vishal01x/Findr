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
    
    // Basic fields
    val title: String = "",
    val description: String = "",
    val links: List<String> = emptyList(),
    val currentLinkInput: String = "",
    
    // Media attachments
    val selectedImages: List<Uri> = emptyList(),
    val selectedFiles: List<FileAttachment> = emptyList(),
    
    // Project-specific fields
    val projectRoles: List<ProjectRole> = emptyList(),
    val showAddRoleDialog: Boolean = false,
    val editingRole: ProjectRole? = null,
    
    // Event-specific fields
    val eventDetails: EventDetails? = null,
    val showEventDetailsDialog: Boolean = false,
    
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
                eventDetails != null
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
            else -> true // Other types don't have strict requirements
        }
    }
}

/**
 * For Handling One Time Event in Ui
 */
sealed interface CreatePostUiEvent{
    object PopBack: CreatePostUiEvent
}

