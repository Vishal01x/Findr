package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.Context
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.InAppNotificationHelper
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.exa.android.reflekt.loopit.fcm.sendProjectNotification
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class AuthorManagementViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(AuthorManagementState())
    val state: StateFlow<AuthorManagementState> = _state.asStateFlow()

    fun loadEnrollments(postId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            postRepository.getPostById(postId).onSuccess { post ->
                _state.update { it.copy(post = post) }
            }

            postRepository.getEnrollmentsForPost(postId)
                .onSuccess { enrollments ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            pendingEnrollments  = enrollments.filter { it.status == EnrollmentStatus.PENDING },
                            acceptedEnrollments = enrollments.filter { it.status == EnrollmentStatus.ACCEPTED },
                            rejectedEnrollments = enrollments.filter { it.status == EnrollmentStatus.REJECTED }
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load enrollments"
                    )}
                }
        }
    }

    fun acceptEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingIds = it.processingIds + enrollmentId) }
            postRepository.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.ACCEPTED)
                .onSuccess {
                    _state.update { currentState ->
                        val enrollment = currentState.pendingEnrollments.find { it.id == enrollmentId }
                        if (enrollment != null) {
                            
                            // Send notification
                            val deepLink = "findr://project/${enrollment.postId}"
                            viewModelScope.launch {
                                userRepository.getUserFcm(enrollment.userId)?.let { fcm ->
                                    val currentProfile = userRepository.getUserInfoById(currentState.post?.createdBy ?: "").getOrNull()
                                    sendProjectNotification(
                                        context,
                                        fcm,
                                        "Your enrollment request was accepted!",
                                        currentState.post?.title ?: "",
                                        enrollment.postId,
                                        currentState.post?.createdBy ?: "",
                                        currentProfile?.profilePicture,
                                        link = deepLink
                                    )
                                }
                                val currentProfile = userRepository.getUserInfoById(currentState.post?.createdBy ?: "").getOrNull()
                                InAppNotificationHelper.sendNotification(
                                    firestore = firestore,
                                    recipientId = enrollment.userId,
                                    type = NotificationType.ENROLL_ACCEPT,
                                    category = NotificationCategory.SOCIAL,
                                    title = "Request Accepted",
                                    message = "Your enrollment request for ${currentState.post?.title} was accepted!",
                                    senderName = currentProfile?.name ?: "Author",
                                    senderImageUrl = currentProfile?.profilePicture,
                                    projectImageUrl = currentState.post?.imageUrls?.firstOrNull(),
                                    senderId = currentState.post?.createdBy ?: "",
                                    targetId = enrollment.postId,
                                    route = "project_detail/${enrollment.postId}"
                                )
                            }
                            
                            val updatedRoles = currentState.post?.projectRoles?.map { role ->
                                if (role.id == enrollment.roleId) {
                                    role.copy(positionsFilled = role.positionsFilled + 1)
                                } else role
                            } ?: emptyList()
                            val updatedPost = currentState.post?.copy(projectRoles = updatedRoles)

                            currentState.copy(
                                pendingEnrollments  = currentState.pendingEnrollments.filter { it.id != enrollmentId },
                                acceptedEnrollments = currentState.acceptedEnrollments + enrollment.copy(status = EnrollmentStatus.ACCEPTED),
                                processingIds = currentState.processingIds - enrollmentId,
                                message = "Request accepted",
                                post = updatedPost
                            )
                        } else currentState.copy(processingIds = currentState.processingIds - enrollmentId)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        processingIds = it.processingIds - enrollmentId,
                        message = "Failed to accept: ${error.message}"
                    )}
                }
        }
    }

    fun rejectEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingIds = it.processingIds + enrollmentId) }
            postRepository.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.REJECTED)
                .onSuccess {
                    _state.update { currentState ->
                        val enrollment = currentState.pendingEnrollments.find { it.id == enrollmentId }
                        if (enrollment != null) {
                            
                            // Send notification
                            val deepLink = "findr://project/${enrollment.postId}"
                            viewModelScope.launch {
                                userRepository.getUserFcm(enrollment.userId)?.let { fcm ->
                                    val currentProfile = userRepository.getUserInfoById(currentState.post?.createdBy ?: "").getOrNull()
                                    sendProjectNotification(
                                        context,
                                        fcm,
                                        "Your enrollment request was rejected",
                                        currentState.post?.title ?: "",
                                        enrollment.postId,
                                        currentState.post?.createdBy ?: "",
                                        currentProfile?.profilePicture,
                                        link = deepLink
                                    )
                                }
                                val currentProfile = userRepository.getUserInfoById(currentState.post?.createdBy ?: "").getOrNull()
                                InAppNotificationHelper.sendNotification(
                                    firestore = firestore,
                                    recipientId = enrollment.userId,
                                    type = NotificationType.ENROLL_REJECT,
                                    category = NotificationCategory.SOCIAL,
                                    title = "Request Rejected",
                                    message = "Your enrollment request for ${currentState.post?.title} was rejected.",
                                    senderName = currentProfile?.name ?: "Author",
                                    senderImageUrl = currentProfile?.profilePicture,
                                    projectImageUrl = currentState.post?.imageUrls?.firstOrNull(),
                                    senderId = currentState.post?.createdBy ?: "",
                                    targetId = enrollment.postId,
                                    route = "project_detail/${enrollment.postId}"
                                )
                            }
                            
                            currentState.copy(
                                pendingEnrollments  = currentState.pendingEnrollments.filter { it.id != enrollmentId },
                                rejectedEnrollments = currentState.rejectedEnrollments + enrollment.copy(status = EnrollmentStatus.REJECTED),
                                processingIds = currentState.processingIds - enrollmentId,
                                message = "Request rejected"
                            )
                        } else currentState.copy(processingIds = currentState.processingIds - enrollmentId)
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(
                        processingIds = it.processingIds - enrollmentId,
                        message = "Failed to reject: ${error.message}"
                    )}
                }
        }
    }

    fun selectTab(tabIndex: Int) { _state.update { it.copy(selectedTab = tabIndex) } }
    fun clearMessage() { _state.update { it.copy(message = null) } }
}

data class AuthorManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val post: Post? = null,
    val pendingEnrollments:  List<RoleEnrollment> = emptyList(),
    val acceptedEnrollments: List<RoleEnrollment> = emptyList(),
    val rejectedEnrollments: List<RoleEnrollment> = emptyList(),
    val processingIds: Set<String> = emptySet(),
    val message: String? = null
) {
    val pendingCount: Int  get() = pendingEnrollments.size
    val acceptedCount: Int get() = acceptedEnrollments.size
    val rejectedCount: Int get() = rejectedEnrollments.size
    val spotsFilled: Int   get() = post?.projectRoles?.sumOf { it.positionsFilled } ?: 0
    val spotsTotal:  Int   get() = post?.projectRoles?.sumOf { it.positionsNeeded } ?: 0

    fun getAllEnrollments() = pendingEnrollments + acceptedEnrollments + rejectedEnrollments
}
