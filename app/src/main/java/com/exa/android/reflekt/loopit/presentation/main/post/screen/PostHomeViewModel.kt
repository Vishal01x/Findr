package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.utils.ShareUtils
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.DeletePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.EnrollInRoleUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.RegisterForEventUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/** One-shot UI events emitted by PostHomeViewModel */
sealed interface PostHomeUiEvent {
    data class ShowSnackbar(val message: String, val isError: Boolean = false) : PostHomeUiEvent
}

/**
 * ViewModel for the Post Home screen
 * Manages posts, interactions (like, comment, share)
 * Integrated with Firestore via PostRepository
 */
@HiltViewModel
class PostHomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth,
    private val enrollInRoleUseCase: EnrollInRoleUseCase,
    private val registerForEventUseCase: RegisterForEventUseCase,
    private val deletePostUseCase: DeletePostUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PostHomeState())
    val state: StateFlow<PostHomeState> = _state.asStateFlow()
    val currentUserId = auth.currentUser?.uid ?: ""

    private val _uiEvent = MutableSharedFlow<PostHomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var userSearchJob: Job? = null

    init {
        loadPosts()
    }

    /**
     * Load posts from Firestore - automatically enriched with user details
     */
    fun refresh() { loadPosts(forceReload = true) }

    private fun loadPosts(forceReload: Boolean = false) {
        val queryAtLaunch = _state.value.searchQuery
        val currentPosts = _state.value.posts
        
        if (!forceReload && currentPosts.isNotEmpty()) {
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            postRepository.getPostsStream(
                searchQuery = queryAtLaunch,
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
                        // If no post results and the query hasn't changed, search users
                        val currentQuery = _state.value.searchQuery
                        if (posts.isEmpty() && currentQuery.isNotBlank() && currentQuery == queryAtLaunch) {
                            triggerUserSearch(currentQuery)
                        } else if (posts.isNotEmpty()) {
                            userSearchJob?.cancel()
                            _state.update { it.copy(userResults = emptyList(), isSearchingUsers = false) }
                        }
                    },
                    onFailure = { error ->
                        _state.update { it.copy(isLoading = false, error = error.message) }
                    }
                )
            }
        }
    }

    private fun triggerUserSearch(query: String) {
        userSearchJob?.cancel()
        userSearchJob = viewModelScope.launch {
            _state.update { it.copy(isSearchingUsers = true) }
            userRepository.searchUsersByQuery(query)
                .onSuccess { users ->
                    if (isActive) _state.update { it.copy(userResults = users, isSearchingUsers = false) }
                }
                .onFailure {
                    if (isActive) _state.update { it.copy(isSearchingUsers = false) }
                }
        }
    }

    /**
     * Update search query and reload posts
     */
    fun onSearchQueryChange(query: String) {
        userSearchJob?.cancel()
        _state.update { it.copy(searchQuery = query, userResults = emptyList(), isSearchingUsers = false) }
        loadPosts(forceReload = true)
    }

    fun activateSearch() {
        _state.update { it.copy(isSearchActive = true) }
    }

    fun deactivateSearch() {
        userSearchJob?.cancel()
        _state.update { it.copy(
            isSearchActive = false,
            searchQuery = "",
            userResults = emptyList(),
            isSearchingUsers = false
        )}
        loadPosts(forceReload = true)
    }

    /**
     * Update selected filter and reload posts
     */
    fun onFilterSelected(filter: PostType?) {
        _state.update { it.copy(selectedFilter = if (filter == PostType.ALL) null else filter) }
        loadPosts(forceReload = true)
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
     * Cast/change/retract a poll vote. Firestore's live snapshot listener (via getPostsStream)
     * reflects the updated vote counts back into [state] automatically, same as likes.
     */
    fun onVoteClick(postId: String, optionIndex: Int) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val post = _state.value.posts.find { it.id == postId } ?: return@launch
            postRepository.voteOnPoll(postId, userId, optionIndex, post.pollOptions)
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
    
    // ── Share sheet ───────────────────────────────────────────────────────────

    private var sharePrepDeferred: kotlinx.coroutines.Deferred<com.exa.android.reflekt.loopit.core.utils.ShareData>? = null

    fun onShareClick(postId: String) {
        val post = _state.value.posts.find { it.id == postId } ?: return
        _state.update {
            it.copy(
                showShareSheet = true,
                sharePost = post
            )
        }
    }

    fun prepareShare(context: android.content.Context) {
        val post = _state.value.sharePost ?: return
        sharePrepDeferred?.cancel()
        sharePrepDeferred = viewModelScope.async {
            ShareUtils.prepareShareData(context, post)
        }
    }

    fun executeShare(context: android.content.Context) {
        viewModelScope.launch {
            showToast("Creating share preview…")
            val post = _state.value.sharePost ?: return@launch
            val data = sharePrepDeferred?.await()
                ?: ShareUtils.prepareShareData(context, post)
            ShareUtils.executeShare(context, data)
        }
    }

    fun closeShareSheet() {
        _state.update { it.copy(showShareSheet = false, sharePost = null) }
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
     * Open enrollment dialog
     */
    fun openEnrollmentDialog(post: Post) {
        _state.update {
            it.copy(
                showEnrollmentDialog = true,
                selectedPostForEnrollment = post
            )
        }
    }

    /**
     * Close enrollment dialog
     */
    fun closeEnrollmentDialog() {
        _state.update {
            it.copy(
                showEnrollmentDialog = false,
                selectedPostForEnrollment = null
            )
        }
    }

    /**
     * Enroll in a project role
     */
    fun enrollInRole(postId: String, roleId: String, message: String = "") {
        viewModelScope.launch {
            _state.update { it.copy(isProcessingEnrollment = true) }
            
            enrollInRoleUseCase(postId, roleId, message)
                .onSuccess {
                    showToast("Enrollment request sent!")
                    _state.update { it.copy(
                        isProcessingEnrollment = false,
                        showEnrollmentDialog = false,
                        selectedPostForEnrollment = null,
                        error = null
                    )}
                }
                .onFailure { error ->
                    showToast(error.message ?: "Failed to enroll")
                    _state.update { it.copy(
                        isProcessingEnrollment = false,
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
                    _uiEvent.emit(PostHomeUiEvent.ShowSnackbar("\"${post.title}\" deleted successfully"))
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                    _uiEvent.emit(PostHomeUiEvent.ShowSnackbar(error.message ?: "Failed to delete post", isError = true))
                }
        }
    }

    fun showToast(message: String) {
        _state.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
}
