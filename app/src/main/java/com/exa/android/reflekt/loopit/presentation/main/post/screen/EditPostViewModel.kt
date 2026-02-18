package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.*
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.UpdatePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.UploadMediaUseCase
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditPostState(
    val originalPost: Post? = null,
    val title: String = "",
    val description: String = "",
    val type: String = PostType.POST.displayName,
    val tags: List<String> = emptyList(),
    val links: List<String> = emptyList(),
    val imageUrls: List<String> = emptyList(),
    val fileUrls: List<String> = emptyList(),
    val projectRoles: List<ProjectRole> = emptyList(),
    val eventDetails: EventDetails? = null,
    // New fields for media upload during edit
    val selectedNewImages: List<Uri> = emptyList(),
    val selectedNewFiles: List<Pair<Uri, String>> = emptyList(),
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val currentUser: profileUser? = null
)

@HiltViewModel
class EditPostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val updatePostUseCase: UpdatePostUseCase,
    private val uploadMediaUseCase: UploadMediaUseCase,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _state = MutableStateFlow(EditPostState(isLoading = true))
    val state: StateFlow<EditPostState> = _state.asStateFlow()

    init {
        loadPost()
    }

    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPostById(postId)
                .onSuccess { post ->
                    // Check if user is the author
                    if (post.createdBy != currentUserId) {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "You don't have permission to edit this post"
                            )
                        }
                        return@onSuccess
                    }

                    _state.update {
                        it.copy(
                            originalPost = post,
                            title = post.title,
                            description = post.description,
                            type = post.type,
                            tags = post.tags,
                            links = post.links,
                            imageUrls = post.imageUrls,
                            fileUrls = post.fileUrls,
                            projectRoles = post.projectRoles,
                            eventDetails = post.eventDetails,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load post"
                        )
                    }
                }
        }
    }

    fun updateTitle(title: String) {
        _state.update { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        _state.update { it.copy(description = description) }
    }

    fun updateType(type: String) {
        _state.update { it.copy(type = type) }
    }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && !_state.value.tags.contains(tag)) {
            _state.update { it.copy(tags = it.tags + tag) }
        }
    }

    fun removeTag(tag: String) {
        _state.update { it.copy(tags = it.tags - tag) }
    }

    fun addLink(link: String) {
        if (link.isNotBlank() && !_state.value.links.contains(link)) {
            _state.update { it.copy(links = it.links + link) }
        }
    }

    fun removeLink(link: String) {
        _state.update { it.copy(links = it.links - link) }
    }

    fun removeImage(url: String) {
        _state.update { it.copy(imageUrls = it.imageUrls - url) }
    }

    fun removeFile(url: String) {
        _state.update { it.copy(fileUrls = it.fileUrls - url) }
    }

    fun updateProjectRoles(roles: List<ProjectRole>) {
        _state.update { it.copy(projectRoles = roles) }
    }

//    fun removeTag(tag: String) {
//        _state.update { it.copy(tags = it.tags.filter { t -> t != tag }) }
//    }

//    fun removeLink(link: String) {
//        _state.update { it.copy(links = it.links.filter { l -> l != link }) }
//    }

    fun addNewImages(images: List<Uri>) {
        _state.update { it.copy(selectedNewImages = it.selectedNewImages + images) }
    }

    fun removeNewImage(uri: Uri) {
        _state.update { it.copy(selectedNewImages = it.selectedNewImages.filter { it != uri }) }
    }

//    fun removeImage(url: String) {
//        _state.update { it.copy(imageUrls = it.imageUrls.filter { it != url }) }
//    }

    fun addNewFiles(files: List<Pair<Uri, String>>) {
        _state.update { it.copy(selectedNewFiles = it.selectedNewFiles + files) }
    }

    fun removeNewFile(uri: Uri) {
        _state.update { it.copy(selectedNewFiles = it.selectedNewFiles.filter { it.first != uri }) }
    }

//    fun removeFile(url: String) {
//        _state.update { it.copy(fileUrls = it.fileUrls.filter { it != url }) }
//    }

    fun updateEventDetails(details: EventDetails?) {
        _state.update { it.copy(eventDetails = details) }
    }

    fun savePost() {
        viewModelScope.launch {
            val currentState = _state.value
            
            // Validation
            if (currentState.title.isBlank()) {
                _state.update { it.copy(error = "Title is required") }
                return@launch
            }
            
            if (currentState.description.isBlank()) {
                _state.update { it.copy(error = "Description is required") }
                return@launch
            }
            
            if (currentState.type == PostType.PROJECT.displayName && currentState.projectRoles.isEmpty()) {
                _state.update { it.copy(error = "At least one role is required for project posts") }
                return@launch
            }
            
            if (currentState.type == PostType.EVENT.displayName && currentState.eventDetails == null) {
                _state.update { it.copy(error = "Event details are required for event posts") }
                return@launch
            }

            _state.update { it.copy(isUploading = true, error = null) }

            val originalPost = currentState.originalPost ?: return@launch
            
            try {
                // Upload new images if any
                val newImageUrls = if (currentState.selectedNewImages.isNotEmpty()) {
                    uploadMediaUseCase.uploadImages(currentState.selectedNewImages, postId)
                        .getOrElse { error ->
                            _state.update { 
                                it.copy(
                                    isUploading = false, 
                                    error = "Failed to upload images: ${error.message}"
                                ) 
                            }
                            return@launch
                        }
                } else {
                    emptyList()
                }

                // Upload new files if any
                val newFileUrls = if (currentState.selectedNewFiles.isNotEmpty()) {
                    uploadMediaUseCase.uploadFiles(currentState.selectedNewFiles, postId)
                        .getOrElse { error ->
                            _state.update { 
                                it.copy(
                                    isUploading = false, 
                                    error = "Failed to upload files: ${error.message}"
                                ) 
                            }
                            return@launch
                        }
                } else {
                    emptyList()
                }

                // Merge with existing URLs (that weren't removed)
                val allImageUrls = currentState.imageUrls + newImageUrls
                val allFileUrls = currentState.fileUrls + newFileUrls
                
                // Create updated post
                val updatedPost = originalPost.copy(
                    title = currentState.title,
                    description = currentState.description,
                    type = currentState.type,
                    tags = currentState.tags,
                    links = currentState.links,
                    imageUrls = allImageUrls,
                    fileUrls = allFileUrls,
                    projectRoles = currentState.projectRoles,
                    eventDetails = currentState.eventDetails
                )

                // Update post in repository
                updatePostUseCase(postId, updatedPost)
                    .onSuccess {
                        _state.update { it.copy(isUploading = false, isSaved = true) }
                    }
                    .onFailure { error ->
                        _state.update { 
                            it.copy(
                                isUploading = false, 
                                error = error.message ?: "Failed to update post"
                            ) 
                        }
                    }
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

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
