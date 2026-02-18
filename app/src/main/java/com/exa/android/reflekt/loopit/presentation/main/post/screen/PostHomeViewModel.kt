package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.utils.ShareUtils
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.DeletePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.EnrollInRoleUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.RegisterForEventUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Post Home screen
 * Manages posts, interactions (like, comment, share)
 * Integrated with Firestore via PostRepository
 */
@HiltViewModel
class PostHomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val auth: FirebaseAuth,
    private val enrollInRoleUseCase: EnrollInRoleUseCase,
    private val registerForEventUseCase: RegisterForEventUseCase,
    private val deletePostUseCase: DeletePostUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PostHomeState())
    val state: StateFlow<PostHomeState> = _state.asStateFlow()
    val currentUserId = auth.currentUser?.uid ?: ""

    init {
        loadPosts()
    }

    /**
     * Load posts from Firestore - automatically enriched with user details
     */
    private fun loadPosts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            postRepository.getPostsStream(
                searchQuery = _state.value.searchQuery,
                rolesFilter = emptyList(),
                tagsFilter = emptyList(),
                showMyProjectsOnly = false,
                userId = null,
                postType = _state.value.selectedFilter?.displayName
            ).catch { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }.collect { result ->
                result.fold(
                    onSuccess = { posts ->
                        _state.update { it.copy(posts = posts, isLoading = false, error = null) }
                    },
                    onFailure = { error ->
                        _state.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            }
        }
    }

    /**
     * Update search query and reload posts
     */
    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        loadPosts()
    }

    /**
     * Update selected filter and reload posts
     */
    fun onFilterSelected(filter: PostType?) {
        _state.update { it.copy(selectedFilter = if (filter == PostType.ALL) null else filter) }
        loadPosts()
    }

    /**
     * Toggle like on a post
     */
    fun onLikeClick(postId: String, isCurrentlyLiked: Boolean) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            postRepository.toggleLike(postId, userId, isCurrentlyLiked)
        }
    }

    /**
     * Open comment sheet for a post
     */
    fun onCommentClick(postId: String) {
        viewModelScope.launch {
            _state.update { it.copy(selectedPostId = postId, showCommentSheet = true) }
            // Load comments when sheet opens - implementation can be added later
        }
    }

    /**
     * Close comment sheet
     */
    fun onDismissCommentSheet() {
        _state.update {
            it.copy(
                showCommentSheet = false,
                selectedPostId = null
            )
        }
    }


    fun onAddComment(content: String, parentCommentId: String? = null) {
        val postId = _state.value.selectedPostId ?: return

        viewModelScope.launch {
//            postRepository.addComment()
//            addCommentUseCase(postId, "current_user", content, parentCommentId)
        }
    }
    
    /**
     * Handle share action
     */
    fun onShareClick(postId: String, context: android.content.Context) {
        viewModelScope.launch {
            // Find the post in current state
            val post = _state.value.posts.find { it.id == postId } ?: return@launch
            
            // Share using ShareUtils
            ShareUtils.sharePost(context, post)
        }
    }

    /**
     * Handle repost action
     */
    fun onRepostClick(postId: String) {
        // TODO: Implement repost functionality
    }

    /**
     * Open image viewer
     */
    fun onImageClick(images: List<String>, index: Int, user: PostUserInfo? = null, caption: String? = null) {
        _state.update {
            it.copy(
                showImageViewer = true,
                selectedImages = images,
                selectedImageIndex = index,
                selectedImageUser = user,
                selectedImageCaption = caption
            )
        }
    }

    /**
     * Close image viewer
     */
    fun onDismissImageViewer() {
        _state.update {
            it.copy(
                showImageViewer = false,
                selectedImages = emptyList(),
                selectedImageIndex = 0
            )
        }
    }

    /**
     * Navigate to post details
     */
    fun onPostClick(postId: String) {
        // TODO: Navigate to post details screen
    }

    /**
     * Navigate to user profile
     */
    fun onUserClick(userId: String) {
        // TODO: Navigate to user profile screen
    }
    
    /**
     * Enroll in a project role
     */
    fun enrollInRole(postId: String, roleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            enrollInRoleUseCase(postId, roleId)
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        error = null
                    )}
                    // TODO: Show success message/snackbar
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
        }
    }
    
    /**
     * Register for an event
     */
    fun registerForEvent(postId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            registerForEventUseCase(postId)
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        error = null
                    )}
                    // TODO: Show success message/snackbar
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message
                    )}
                }
        }
    }


    /**
     * MY POST HANDLING
     */

    fun onDeleteClick(post: Post) {
        _state.update { it.copy(showDeleteDialog = true, postToDelete = post) }
    }

    fun onDismissDeleteDialog() {
        _state.update { it.copy(showDeleteDialog = false, postToDelete = null) }
    }

    fun onConfirmDelete() {
        val post = _state.value.postToDelete ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showDeleteDialog = false, postToDelete = null) }

            deletePostUseCase(post.id)
                .onSuccess {
                    // List will automatically update via Flow
                    _state.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }
}
