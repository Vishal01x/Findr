package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostUserInfo
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.CheckEnrollmentStatusUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.CheckRegistrationStatusUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.DeletePostUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.EnrollInRoleUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.RegisterForEventUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PostDetailUiState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val enrollmentStatuses: Map<String, EnrollmentStatus> = emptyMap(),
    val isRegistered: Boolean = false,
    val toastMessage: String? = null,
    val isAuthor: Boolean = false,

    // Dialog states
    val showEnrollmentDialog: Boolean = false,
    val selectedRoleForEnrollment: ProjectRole? = null,
    val showEventRegDialog: Boolean = false,
    val isProcessingEnrollment: Boolean = false,

    // Invite
    val showUserSearch: Boolean = false,

    // Event capacity
    val showCapacityDialog: Boolean = false,
    val isCapacityUpdating: Boolean = false,

    // Delete states
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val postDeleted: Boolean = false,

    // Image viewer state
    val showImageViewer: Boolean = false,
    val selectedImages: List<String> = emptyList(),
    val selectedImageIndex: Int = 0,
    val selectedImageUser : PostUserInfo? = null,
    val selectedImageCaption : String? = null,

    // Sheet / overlay states
    val showPostOptionsSheet: Boolean = false,
    val showShareSheet: Boolean = false,
    val showReportSheet: Boolean = false,
    val showRegistrationSuccess: Boolean = false,
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val enrollInRoleUseCase: EnrollInRoleUseCase,
    private val registerForEventUseCase: RegisterForEventUseCase,
    private val checkEnrollmentStatusUseCase: CheckEnrollmentStatusUseCase,
    private val checkRegistrationStatusUseCase: CheckRegistrationStatusUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["projectId"])
    val currentUserId: String = auth.currentUser?.uid ?: ""

    private val _uiState = MutableStateFlow(PostDetailUiState(isLoading = true))
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        loadPostAndObserve()
        incrementViewCount()
    }

    private fun incrementViewCount() {
        viewModelScope.launch {
            postRepository.incrementPostView(postId)
        }
    }

    private fun loadPostAndObserve() {
        viewModelScope.launch {
            // One-shot initial load to get enriched post (with userInfo)
            postRepository.getPostById(postId)
                .onSuccess { post ->
                    _uiState.update {
                        it.copy(
                            post = post,
                            isLoading = false,
                            isAuthor = post.createdBy == currentUserId
                        )
                    }
                    loadEnrollmentStatuses(post)
                    loadRegistrationStatus(post)
                    // Now subscribe to real-time updates, preserving userInfo from initial load
                    observePostUpdates(post.userInfo)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    private fun observePostUpdates(cachedUserInfo: PostUserInfo?) {
        viewModelScope.launch {
            postRepository.getProjectUpdates(postId).collect { result ->
                result.onSuccess { updatedPost ->
                    _uiState.update {
                        it.copy(
                            post = updatedPost.copy(userInfo = updatedPost.userInfo ?: cachedUserInfo),
                            isAuthor = updatedPost.createdBy == currentUserId
                        )
                    }
                }
            }
        }
    }

    fun refresh() { loadPost() }

    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPostById(postId)
                .onSuccess { post ->
                    _uiState.update {
                        it.copy(
                            post = post,
                            isLoading = false,
                            isAuthor = post.createdBy == currentUserId
                        )
                    }
                    loadEnrollmentStatuses(post)
                    loadRegistrationStatus(post)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    private fun loadEnrollmentStatuses(post: Post) {
        viewModelScope.launch {
            val statuses = mutableMapOf<String, EnrollmentStatus>()
            
            post.projectRoles.forEach { role ->
                checkEnrollmentStatusUseCase(postId, role.id, currentUserId)
                    .onSuccess { status ->
                        statuses[role.id] = status
                    }
            }
            
            _uiState.update { it.copy(enrollmentStatuses = statuses) }
        }
    }

    private fun loadRegistrationStatus(post: Post) {
        viewModelScope.launch {
            if (post.eventDetails != null) {
                checkRegistrationStatusUseCase(postId, currentUserId)
                    .onSuccess { isRegistered ->
                        _uiState.update { it.copy(isRegistered = isRegistered) }
                    }
            }
        }
    }

    fun openEnrollmentDialog(roleId: String) {
        val roles = _uiState.value.post?.projectRoles ?: emptyList()
        val role = roles.find { it.id == roleId } ?: roles.firstOrNull()
        _uiState.update {
            it.copy(
                showEnrollmentDialog = true,
                selectedRoleForEnrollment = role
            )
        }
    }
    
    fun closeEnrollmentDialog() {
        _uiState.update {
            it.copy(
                showEnrollmentDialog = false,
                selectedRoleForEnrollment = null
            )
        }
    }
    
    fun openEventRegistrationDialog() {
        _uiState.update { it.copy(showEventRegDialog = true) }
    }
    
    fun closeEventRegistrationDialog() {
        _uiState.update { it.copy(showEventRegDialog = false) }
    }

    fun hasUserAppliedToPost(): Boolean {
        // Check if user has any pending or accepted role enrollment
        val hasRoleEnrollment = _uiState.value.enrollmentStatuses.values.any { 
            it == EnrollmentStatus.PENDING || it == EnrollmentStatus.ACCEPTED 
        }
        
        // Check if user is registered for event
        val isEventRegistered = _uiState.value.isRegistered
        
        return hasRoleEnrollment || isEventRegistered
    }

    fun enrollInRole(roleId: String, message: String = "") {
        viewModelScope.launch {
            val post = _uiState.value.post ?: return@launch
            val role = post.projectRoles.find { it.id == roleId } ?: return@launch
            
            _uiState.update { it.copy(isProcessingEnrollment = true) }
            
            // Check "One Request Per User" policy
            if (hasUserAppliedToPost()) {
                showToast("You have already applied/enrolled in this post")
                _uiState.update { it.copy(isProcessingEnrollment = false) }
                return@launch
            }

            // Check if role is manually closed
            if (role.isManuallyClosed) {
                showToast("Applications for this role are closed")
                _uiState.update { it.copy(isProcessingEnrollment = false) }
                return@launch
            }
            
            // Check if role is full
            if (role.positionsFilled >= role.positionsNeeded) {
                showToast("Role is full")
                _uiState.update { it.copy(isProcessingEnrollment = false) }
                return@launch
            }
            
            // Enroll with message
            enrollInRoleUseCase(postId, roleId, message)
                .onSuccess {
                    showToast("Enrollment request sent!")
                    // Update status
                    _uiState.update { 
                        it.copy(
                            enrollmentStatuses = it.enrollmentStatuses + (roleId to EnrollmentStatus.PENDING),
                            isProcessingEnrollment = false,
                            showEnrollmentDialog = false,
                            selectedRoleForEnrollment = null
                        )
                    }
                }
                .onFailure { error ->
                    showToast(error.message ?: "Failed to enroll")
                    _uiState.update { it.copy(isProcessingEnrollment = false) }
                }
        }
    }

    fun registerForEvent() {
        viewModelScope.launch {
            val post = _uiState.value.post ?: return@launch
            val eventDetails = post.eventDetails ?: return@launch
            
            // Check "One Request Per User" policy
            if (hasUserAppliedToPost()) {
                showToast("You have already registered/applied for this post")
                return@launch
            }
            
            // Check if already registered
            if (_uiState.value.isRegistered) {
                showToast("Already registered for this event")
                return@launch
            }

            // Check if manually closed
            if (eventDetails.isManuallyClosed) {
                showToast("Registration is closed")
                return@launch
            }
            
            // Check if event is full
            if (eventDetails.isFull()) {
                showToast("Event is full")
                return@launch
            }
            
            // Check if registration is open
            if (!eventDetails.isRegistrationOpen()) {
                showToast("Registration deadline has passed")
                return@launch
            }
            
            // Register
            registerForEventUseCase(postId)
                .onSuccess {
                    _uiState.update { it.copy(isRegistered = true, showRegistrationSuccess = true) }
                    loadPost()
                }
                .onFailure { error ->
                    showToast(error.message ?: "Failed to register")
                }
        }
    }
    
    fun toggleEventClosure(isClosed: Boolean) {
        viewModelScope.launch {
            val post = _uiState.value.post ?: return@launch
            val updatedEvent = post.eventDetails?.copy(isManuallyClosed = isClosed)
            _uiState.update { it.copy(post = post.copy(eventDetails = updatedEvent)) }

            postRepository.updateEventClosureStatus(postId, isClosed)
                .onSuccess { showToast(if (isClosed) "Registration closed" else "Registration opened") }
                .onFailure {
                    showToast("Failed to update status")
                    _uiState.update { it.copy(post = post) } // revert
                }
        }
    }
    
    fun toggleRoleClosure(roleId: String, isClosed: Boolean) {
        viewModelScope.launch {
            val post = _uiState.value.post ?: return@launch
            val updatedRoles = post.projectRoles.map {
                if (it.id == roleId) it.copy(isManuallyClosed = isClosed) else it
            }
            _uiState.update { it.copy(post = post.copy(projectRoles = updatedRoles)) }

            postRepository.updateRoleClosureStatus(postId, roleId, isClosed)
                .onSuccess { showToast(if (isClosed) "Role closed" else "Role opened") }
                .onFailure {
                    showToast("Failed to update status")
                    _uiState.update { it.copy(post = post) } // revert
                }
        }
    }

    // ==================== DELETE POST ====================
    
    fun showDeleteConfirmation() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }
    
    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }
    
    fun deletePost() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            
            deletePostUseCase(postId)
                .onSuccess {
                    showToast("Post deleted successfully")
                    _uiState.update { 
                        it.copy(
                            showDeleteDialog = false,
                            isDeleting = false,
                            postDeleted = true
                        ) 
                    }
                }
                .onFailure { error ->
                    showToast(error.message ?: "Failed to delete post")
                    _uiState.update { 
                        it.copy(
                            showDeleteDialog = false,
                            isDeleting = false
                        ) 
                    }
                }
        }
    }


    /**
     * Open image viewer
     */
    fun onImageClick(images: List<String>, index: Int, user: PostUserInfo? = null, caption: String? = null) {
        _uiState.update {
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
        _uiState.update {
            it.copy(
                showImageViewer = false,
                selectedImages = emptyList(),
                selectedImageIndex = 0
            )
        }
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
     * Cast/change/retract a poll vote. observePostUpdates' live listener reflects the
     * updated vote counts back into [uiState] automatically, same as likes.
     */
    fun onVoteClick(optionIndex: Int) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid ?: return@launch
            val post = _uiState.value.post ?: return@launch
            postRepository.voteOnPoll(post.id, userId, optionIndex, post.pollOptions)
        }
    }

    // Deferred share payload — starts as soon as the sheet opens so the intent
    // fires instantly when the user taps "Share via…".
    private var sharePrepDeferred: kotlinx.coroutines.Deferred<com.exa.android.reflekt.loopit.core.utils.ShareData>? = null

    fun prepareShare(context: android.content.Context) {
        val post = _uiState.value.post ?: return
        sharePrepDeferred?.cancel()
        sharePrepDeferred = viewModelScope.async {
            com.exa.android.reflekt.loopit.core.utils.ShareUtils.prepareShareData(context, post)
        }
    }

    fun sharePost(context: android.content.Context) {
        viewModelScope.launch {
            showToast("Creating share preview, Please wait…")
            val post = _uiState.value.post ?: return@launch
            val data = sharePrepDeferred?.await()
                ?: com.exa.android.reflekt.loopit.core.utils.ShareUtils.prepareShareData(context, post)
            com.exa.android.reflekt.loopit.core.utils.ShareUtils.executeShare(context, data)
        }
    }

    // ==================== INVITE ====================

    fun onInviteClick() {
        _uiState.update { it.copy(showUserSearch = true) }
    }

    fun onUserSearchDismissed() {
        _uiState.update { it.copy(showUserSearch = false) }
    }

    // ==================== EVENT CAPACITY ====================

    fun showCapacityDialog() {
        _uiState.update { it.copy(showCapacityDialog = true) }
    }

    fun dismissCapacityDialog() {
        _uiState.update { it.copy(showCapacityDialog = false) }
    }

    fun updateEventCapacity(newCapacity: Int) {
        viewModelScope.launch {
            val currentCapacity = _uiState.value.post?.eventDetails?.registeredCount ?: 0
            if (newCapacity <= currentCapacity) {
                showToast("Capacity must be greater than current registrations ($currentCapacity)")
                return@launch
            }
            _uiState.update { it.copy(isCapacityUpdating = true, showCapacityDialog = false) }
            postRepository.updateEventCapacity(postId, newCapacity)
                .onSuccess { showToast("Capacity updated to $newCapacity") }
                .onFailure { showToast("Failed to update capacity") }
            _uiState.update { it.copy(isCapacityUpdating = false) }
        }
    }

    fun editRole(updatedRole: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole) {
        viewModelScope.launch {
            postRepository.updateProjectRole(postId, updatedRole)
                .onSuccess { showToast("Role updated") }
                .onFailure { showToast("Failed to update role") }
        }
    }

    // ==================== SHEET / DIALOG CONTROLS ====================

    fun openPostOptions() {
        _uiState.update { it.copy(showPostOptionsSheet = true) }
    }

    fun dismissPostOptions() {
        _uiState.update { it.copy(showPostOptionsSheet = false) }
    }

    fun openShareSheet() {
        _uiState.update { it.copy(showShareSheet = true) }
    }

    fun dismissShareSheet() {
        _uiState.update { it.copy(showShareSheet = false) }
    }

    fun openReportSheet() {
        _uiState.update { it.copy(showReportSheet = true) }
    }

    fun dismissReportSheet() {
        _uiState.update { it.copy(showReportSheet = false) }
    }

    fun dismissRegistrationSuccess() {
        _uiState.update { it.copy(showRegistrationSuccess = false) }
    }

    private fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }
}
