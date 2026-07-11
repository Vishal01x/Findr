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
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.UpdatePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.feature.main.link_data.metadata.repository.LinkMetadataRepository
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
    private val updatePostUseCase: UpdatePostUseCase,
    private val postRepository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val linkMetadataRepository: LinkMetadataRepository,
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
            is CreatePostEvent.OnPollOptionChanged -> onPollOptionChanged(event.index, event.text)
            is CreatePostEvent.OnAddPollOption -> onAddPollOption()
            is CreatePostEvent.OnRemovePollOption -> onRemovePollOption(event.index)
            is CreatePostEvent.OnCreatePost -> onCreatePost()
            is CreatePostEvent.OnDismissError -> onDismissError()
            is CreatePostEvent.OnBackPressed -> onBackPressed()
            is CreatePostEvent.OnConfirmDiscard -> onConfirmDiscard()
            is CreatePostEvent.OnCancelDiscard -> onCancelDiscard()
            is CreatePostEvent.OnToggleAnonymous -> onToggleAnonymous()
            is CreatePostEvent.OnClearState -> onClearState()
            is CreatePostEvent.OnLoadPost -> onLoadPost(event.postId)
        }
    }

    // ==================== EDIT MODE ====================

    private fun onLoadPost(postId: String) {
        // Avoid double-loading
        if (_state.value.isEditMode && _state.value.postId == postId) return
        viewModelScope.launch {
            _state.update { it.copy(isLoadingPost = true, postId = postId, isEditMode = true) }
            postRepository.getPostById(postId)
                .onSuccess { post ->
                    val pollOptions = if (post.pollOptions.isNotEmpty())
                        post.pollOptions.map { it.text }
                    else listOf("", "")
                    _state.update {
                        it.copy(
                            isLoadingPost = false,
                            title = post.title,
                            description = post.description,
                            links = post.links,
                            projectRoles = post.projectRoles,
                            eventDetails = post.eventDetails,
                            pollOptions = pollOptions,
                            isAnonymous = post.isAnonymous,
                            existingImageUrls = post.imageUrls,
                            existingFileUrls = post.fileUrls,
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoadingPost = false, error = err.message ?: "Failed to load post") }
                }
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
            // Fetch preview for the first link added
            if (_state.value.links.size == 1) {
                fetchLinkPreview(currentInput)
            }
        }
    }

    private fun fetchLinkPreview(url: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingLinkPreview = true) }
            linkMetadataRepository.getMetadata(url)
                .onSuccess { meta ->
                    _state.update {
                        it.copy(
                            isLoadingLinkPreview = false,
                            linkPreviewTitle = meta.title,
                            linkPreviewDescription = meta.description,
                            linkPreviewImageUrl = meta.imageUrl,
                            linkPreviewDomain = meta.domain
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoadingLinkPreview = false) }
                }
        }
    }

    private fun onRemoveLink(link: String) {
        _state.update {
            val newLinks = it.links.filter { l -> l != link }
            it.copy(
                links = newLinks,
                // Clear preview if the linked URL was the preview source
                linkPreviewTitle = if (newLinks.isEmpty()) null else it.linkPreviewTitle,
                linkPreviewDescription = if (newLinks.isEmpty()) null else it.linkPreviewDescription,
                linkPreviewImageUrl = if (newLinks.isEmpty()) null else it.linkPreviewImageUrl,
                linkPreviewDomain = if (newLinks.isEmpty()) null else it.linkPreviewDomain,
            )
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

    // ==================== POLL OPTION MANAGEMENT ====================

    private fun onPollOptionChanged(index: Int, text: String) {
        _state.update { current ->
            val updated = current.pollOptions.toMutableList()
            if (index in updated.indices) updated[index] = text
            current.copy(pollOptions = updated)
        }
    }

    private fun onAddPollOption() {
        _state.update { current ->
            if (current.pollOptions.size >= 5) current
            else current.copy(pollOptions = current.pollOptions + "")
        }
    }

    private fun onRemovePollOption(index: Int) {
        _state.update { current ->
            if (current.pollOptions.size <= 2) current
            else current.copy(pollOptions = current.pollOptions.filterIndexed { i, _ -> i != index })
        }
    }

    // ==================== POST CREATION / UPDATE ====================

    private fun onCreatePost() {
        // Validation for EVENT type
        if (_state.value.selectedPostType == PostType.EVENT) {
            val ed = _state.value.eventDetails
            if (ed == null || ed.eventDate == null || ed.eventTime.isNullOrBlank()) {
                viewModelScope.launch {
                    _uiEvent.emit(CreatePostUiEvent.ShowSnackbar("Please select a date and time for the event", isError = true))
                }
                return
            }
        }

        if (_state.value.isEditMode) {
            onUpdatePost()
        } else {
            onCreateNewPost()
        }
    }

    private fun onCreateNewPost() {
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
                val authorName = if (_state.value.isAnonymous) "Anonymous" else profile.name
                val pollOptions = if (_state.value.selectedPostType == PostType.POLL) {
                    _state.value.pollOptions
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PollOption(text = it) }
                } else emptyList()
                val post = Post(
                    id = UUID.randomUUID().toString(),
                    title = _state.value.title.trim(),
                    description = _state.value.description.trim(),
                    type = _state.value.selectedPostType.displayName,
                    links = _state.value.links,
                    projectRoles = _state.value.projectRoles,
                    eventDetails = _state.value.eventDetails,
                    pollOptions = pollOptions,
                    createdBy = currentUser.uid,
                    createdByName = authorName,
                    isAnonymous = _state.value.isAnonymous
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
                        _uiEvent.emit(CreatePostUiEvent.ShowSnackbar("Post created successfully! 🎉"))
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isUploading = false,
                                error = error.message ?: "Failed to create post"
                            )
                        }
                        _uiEvent.emit(CreatePostUiEvent.ShowSnackbar(error.message ?: "Failed to create post", isError = true))
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isUploading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
                _uiEvent.emit(CreatePostUiEvent.ShowSnackbar(e.message ?: "An error occurred", isError = true))
            }
        }
    }

    private fun onUpdatePost() {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val postId = currentState.postId ?: return@launch
                _state.update { it.copy(isUploading = true, error = null) }

                // Fetch the original post to preserve read-only fields
                val originalPost = postRepository.getPostById(postId).getOrElse { err ->
                    _state.update { it.copy(isUploading = false, error = err.message ?: "Failed to load post") }
                    return@launch
                }

                val pollOptions = if (currentState.selectedPostType == PostType.POLL) {
                    currentState.pollOptions
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .map { com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PollOption(text = it) }
                } else originalPost.pollOptions

                val updatedPost = originalPost.copy(
                    title = currentState.title.trim(),
                    description = currentState.description.trim(),
                    links = currentState.links,
                    projectRoles = currentState.projectRoles,
                    eventDetails = currentState.eventDetails,
                    pollOptions = pollOptions,
                    isAnonymous = currentState.isAnonymous,
                    // keep existing remote images + newly uploaded ones will be appended below
                    imageUrls = currentState.existingImageUrls,
                    fileUrls = currentState.existingFileUrls,
                )

                updatePostUseCase(postId, updatedPost)
                    .onSuccess {
                        _state.update { it.copy(isUploading = false, postCreatedSuccessfully = true) }
                        _uiEvent.emit(CreatePostUiEvent.ShowSnackbar("Post updated successfully! ✅"))
                    }
                    .onFailure { err ->
                        _state.update { it.copy(isUploading = false, error = err.message ?: "Failed to update post") }
                        _uiEvent.emit(CreatePostUiEvent.ShowSnackbar(err.message ?: "Failed to update post", isError = true))
                    }
            } catch (e: Exception) {
                _state.update { it.copy(isUploading = false, error = e.message ?: "An error occurred") }
                _uiEvent.emit(CreatePostUiEvent.ShowSnackbar(e.message ?: "An error occurred", isError = true))
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
        onClearState()
        // Navigation will be handled by the screen
        viewModelScope.launch {
            _uiEvent.emit(CreatePostUiEvent.PopBack)
        }
    }

    private fun onCancelDiscard() {
        _state.update { it.copy(showDiscardDialog = false) }
    }

    private fun onToggleAnonymous() {
        _state.update { it.copy(isAnonymous = !it.isAnonymous) }
    }

    private fun onClearState() {
        _state.update { CreatePostState(selectedPostType = it.selectedPostType) }
    }
}
