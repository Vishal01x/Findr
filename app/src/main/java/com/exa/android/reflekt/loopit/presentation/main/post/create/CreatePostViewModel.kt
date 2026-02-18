package com.exa.android.reflekt.loopit.presentation.main.post.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.FileAttachment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.CreatePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for Create Post screen
 * Manages post creation state and coordinates with use cases
 */
@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CreatePostState())
    val state: StateFlow<CreatePostState> = _state.asStateFlow()

    private val _uiEvent = MutableSharedFlow<CreatePostUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()


    /**
     * Handle all user events
     */
    fun onEvent(event: CreatePostEvent) {
        when (event) {
            is CreatePostEvent.OnPostTypeSelected -> onPostTypeSelected(event.postType)
            is CreatePostEvent.OnTitleChanged -> onTitleChanged(event.title)
            is CreatePostEvent.OnDescriptionChanged -> onDescriptionChanged(event.description)
            is CreatePostEvent.OnLinkInputChanged -> onLinkInputChanged(event.link)
            is CreatePostEvent.OnAddLink -> onAddLink()
            is CreatePostEvent.OnRemoveLink -> onRemoveLink(event.link)
            is CreatePostEvent.OnImagesSelected -> onImagesSelected(event.uris)
            is CreatePostEvent.OnRemoveImage -> onRemoveImage(event.uri)
            is CreatePostEvent.OnFilesSelected -> onFilesSelected(event.uris)
            is CreatePostEvent.OnRemoveFile -> onRemoveFile(event.fileId)
            is CreatePostEvent.OnShowAddRoleDialog -> onShowAddRoleDialog()
            is CreatePostEvent.OnDismissAddRoleDialog -> onDismissAddRoleDialog()
            is CreatePostEvent.OnEditRole -> onEditRole(event.role)
            is CreatePostEvent.OnSaveRole -> onSaveRole(event.role)
            is CreatePostEvent.OnRemoveRole -> onRemoveRole(event.roleId)
            is CreatePostEvent.OnShowEventDetailsDialog -> onShowEventDetailsDialog()
            is CreatePostEvent.OnDismissEventDetailsDialog -> onDismissEventDetailsDialog()
            is CreatePostEvent.OnSaveEventDetails -> onSaveEventDetails(event.eventDetails)
            is CreatePostEvent.OnCreatePost -> onCreatePost()
            is CreatePostEvent.OnDismissError -> onDismissError()
            is CreatePostEvent.OnBackPressed -> onBackPressed()
            is CreatePostEvent.OnConfirmDiscard -> onConfirmDiscard()
            is CreatePostEvent.OnCancelDiscard -> onCancelDiscard()
        }
    }

    // ==================== POST TYPE ====================

    private fun onPostTypeSelected(postType: PostType) {
        _state.update { it.copy(selectedPostType = postType) }
    }

    // ==================== BASIC INPUT ====================

    private fun onTitleChanged(title: String) {
        _state.update { it.copy(title = title) }
    }

    private fun onDescriptionChanged(description: String) {
        _state.update { it.copy(description = description) }
    }

    // ==================== LINK MANAGEMENT ====================

    private fun onLinkInputChanged(link: String) {
        _state.update { it.copy(currentLinkInput = link) }
    }

    private fun onAddLink() {
        val currentInput = _state.value.currentLinkInput.trim()
        if (currentInput.isNotBlank() && !_state.value.links.contains(currentInput)) {
            _state.update {
                it.copy(
                    links = it.links + currentInput,
                    currentLinkInput = ""
                )
            }
        }
    }

    private fun onRemoveLink(link: String) {
        _state.update {
            it.copy(links = it.links.filter { l -> l != link })
        }
    }

    // ==================== IMAGE MANAGEMENT ====================

    private fun onImagesSelected(uris: List<Uri>) {
        _state.update {
            val newImages = (it.selectedImages + uris).distinct().take(5) // Max 5 images
            it.copy(selectedImages = newImages)
        }
    }

    private fun onRemoveImage(uri: Uri) {
        _state.update {
            it.copy(selectedImages = it.selectedImages.filter { img -> img != uri })
        }
    }

    // ==================== FILE MANAGEMENT ====================

    private fun onFilesSelected(uris: List<Pair<Uri, String>>) {
        _state.update { currentState ->
            val newFiles = uris.map { (uri, fileName) ->
                FileAttachment(
                    id = UUID.randomUUID().toString(),
                    fileName = fileName,
                    localUri = uri
                )
            }
            val allFiles = (currentState.selectedFiles + newFiles).take(3) // Max 3 files
            currentState.copy(selectedFiles = allFiles)
        }
    }

    private fun onRemoveFile(fileId: String) {
        _state.update {
            it.copy(selectedFiles = it.selectedFiles.filter { file -> file.id != fileId })
        }
    }

    // ==================== PROJECT ROLE MANAGEMENT ====================

    private fun onShowAddRoleDialog() {
        _state.update { it.copy(showAddRoleDialog = true, editingRole = null) }
    }

    private fun onDismissAddRoleDialog() {
        _state.update { it.copy(showAddRoleDialog = false, editingRole = null) }
    }

    private fun onEditRole(role: ProjectRole) {
        _state.update { it.copy(showAddRoleDialog = true, editingRole = role) }
    }

    private fun onSaveRole(role: ProjectRole) {
        _state.update { currentState ->
            val updatedRoles = if (currentState.editingRole != null) {
                // Edit existing role
                currentState.projectRoles.map { if (it.id == role.id) role else it }
            } else {
                // Add new role
                currentState.projectRoles + role
            }
            currentState.copy(
                projectRoles = updatedRoles,
                showAddRoleDialog = false,
                editingRole = null
            )
        }
    }

    private fun onRemoveRole(roleId: String) {
        _state.update {
            it.copy(projectRoles = it.projectRoles.filter { role -> role.id != roleId })
        }
    }

    // ==================== EVENT DETAILS MANAGEMENT ====================

    private fun onShowEventDetailsDialog() {
        _state.update { it.copy(showEventDetailsDialog = true) }
    }

    private fun onDismissEventDetailsDialog() {
        _state.update { it.copy(showEventDetailsDialog = false) }
    }

    private fun onSaveEventDetails(eventDetails: EventDetails) {
        _state.update {
            it.copy(
                eventDetails = eventDetails,
                showEventDetailsDialog = false
            )
        }
    }

    // ==================== POST CREATION ====================

    private fun onCreatePost() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isUploading = true, error = null) }

                val currentUser = auth.currentUser
                    ?: throw Exception("User not authenticated")

                // Get user profile
                val profile = try {
                    profileRepository.getUserProfile(currentUser.uid)
                } catch (e: Exception) {
                    throw Exception("Failed to load user profile: ${e.message}")
                }

                // Create post object
                val post = Post(
                    id = UUID.randomUUID().toString(),
                    title = _state.value.title.trim(),
                    description = _state.value.description.trim(),
                    type = _state.value.selectedPostType.displayName,
                    links = _state.value.links,
                    projectRoles = _state.value.projectRoles,
                    eventDetails = _state.value.eventDetails,
                    createdBy = currentUser.uid,
                    createdByName = profile.name
                )

                // Prepare file URIs with filenames
                val fileUris = _state.value.selectedFiles.mapNotNull { file ->
                    file.localUri?.let { uri -> uri to file.fileName }
                }

                // Create post with media uploads
                val result = createPostUseCase(
                    post = post,
                    profile = profile,
                    imageUris = _state.value.selectedImages,
                    fileUris = fileUris
                )

                result.fold(
                    onSuccess = {
                        _state.update {
                            it.copy(
                                isUploading = false,
                                postCreatedSuccessfully = true
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isUploading = false,
                                error = error.message ?: "Failed to create post"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    // ==================== ERROR HANDLING ====================

    private fun onDismissError() {
        _state.update { it.copy(error = null) }
    }

    // ==================== NAVIGATION ====================

    private fun onBackPressed() {
        if (_state.value.hasUnsavedChanges()) {
            _state.update { it.copy(showDiscardDialog = true) }
        } else {
            // Navigate back immediately if no changes
            viewModelScope.launch {
                _uiEvent.emit(CreatePostUiEvent.PopBack)
            }
        }
    }

    private fun onConfirmDiscard() {
        _state.update { it.copy(showDiscardDialog = false) }
        // Navigation will be handled by the screen
        viewModelScope.launch {
            _uiEvent.emit(CreatePostUiEvent.PopBack)
        }
    }

    private fun onCancelDiscard() {
        _state.update { it.copy(showDiscardDialog = false) }
    }
}
