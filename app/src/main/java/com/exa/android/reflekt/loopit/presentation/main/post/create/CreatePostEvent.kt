package com.exa.android.reflekt.loopit.presentation.main.post.create

import android.net.Uri
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.google.firebase.Timestamp

/**
 * Events for the Create Post screen
 * Represents all user actions
 */
sealed class CreatePostEvent {
    // Post type selection
    data class OnPostTypeSelected(val postType: PostType) : CreatePostEvent()
    
    // Basic input
    data class OnTitleChanged(val title: String) : CreatePostEvent()
    data class OnDescriptionChanged(val description: String) : CreatePostEvent()
    
    // Link management
    data class OnLinkInputChanged(val link: String) : CreatePostEvent()
    data object OnAddLink : CreatePostEvent()
    data class OnRemoveLink(val link: String) : CreatePostEvent()
    
    // Image management
    data class OnImagesSelected(val uris: List<Uri>) : CreatePostEvent()
    data class OnRemoveImage(val uri: Uri) : CreatePostEvent()
    
    // File management
    data class OnFilesSelected(val uris: List<Pair<Uri, String>>) : CreatePostEvent() // URI + filename
    data class OnRemoveFile(val fileId: String) : CreatePostEvent()
    
    // Project role management
    data object OnShowAddRoleDialog : CreatePostEvent()
    data object OnDismissAddRoleDialog : CreatePostEvent()
    data class OnEditRole(val role: ProjectRole) : CreatePostEvent()
    data class OnSaveRole(val role: ProjectRole) : CreatePostEvent()
    data class OnRemoveRole(val roleId: String) : CreatePostEvent()
    
    // Event details management
    data object OnShowEventDetailsDialog : CreatePostEvent()
    data object OnDismissEventDetailsDialog : CreatePostEvent()
    data class OnSaveEventDetails(val eventDetails: EventDetails) : CreatePostEvent()
    
    // Post actions
    data object OnCreatePost : CreatePostEvent()
    data object OnDismissError : CreatePostEvent()
    
    // Navigation
    data object OnBackPressed : CreatePostEvent()
    data object OnConfirmDiscard : CreatePostEvent()
    data object OnCancelDiscard : CreatePostEvent()
}
