package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Author Management Screen
 * Handles loading and managing enrollment/registration requests
 */
@HiltViewModel
class AuthorManagementViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AuthorManagementState())
    val state: StateFlow<AuthorManagementState> = _state.asStateFlow()
    
    /**
     * Load all enrollments for a post
     */
    fun loadEnrollments(postId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            postRepository.getEnrollmentsForPost(postId)
                .onSuccess { enrollments ->
                    _state.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            pendingEnrollments = enrollments.filter { it.status == EnrollmentStatus.PENDING },
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
    
    /**
     * Accept an enrollment request
     */
    fun acceptEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingIds = it.processingIds + enrollmentId) }
            
            postRepository.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.ACCEPTED)
                .onSuccess {
                    // Move from pending to accepted
                    _state.update { currentState ->
                        val enrollment = currentState.pendingEnrollments.find { it.id == enrollmentId }
                        if (enrollment != null) {
                            val updatedEnrollment = enrollment.copy(status = EnrollmentStatus.ACCEPTED)
                            currentState.copy(
                                pendingEnrollments = currentState.pendingEnrollments.filter { it.id != enrollmentId },
                                acceptedEnrollments = currentState.acceptedEnrollments + updatedEnrollment,
                                processingIds = currentState.processingIds - enrollmentId,
                                message = "Request accepted successfully"
                            )
                        } else {
                            currentState.copy(processingIds = currentState.processingIds - enrollmentId)
                        }
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
    
    /**
     * Reject an enrollment request
     */
    fun rejectEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(processingIds = it.processingIds + enrollmentId) }
            
            postRepository.updateEnrollmentStatus(enrollmentId, EnrollmentStatus.REJECTED)
                .onSuccess {
                    // Move from pending to rejected
                    _state.update { currentState ->
                        val enrollment = currentState.pendingEnrollments.find { it.id == enrollmentId }
                        if (enrollment != null) {
                            val updatedEnrollment = enrollment.copy(status = EnrollmentStatus.REJECTED)
                            currentState.copy(
                                pendingEnrollments = currentState.pendingEnrollments.filter { it.id != enrollmentId },
                                rejectedEnrollments = currentState.rejectedEnrollments + updatedEnrollment,
                                processingIds = currentState.processingIds - enrollmentId,
                                message = "Request rejected"
                            )
                        } else {
                            currentState.copy(processingIds = currentState.processingIds - enrollmentId)
                        }
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
    
    /**
     * Select a tab
     */
    fun selectTab(tabIndex: Int) {
        _state.update { it.copy(selectedTab = tabIndex) }
    }
    
    /**
     * Clear status message
     */
    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

/**
 * State for Author Management Screen
 */
data class AuthorManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0,
    val pendingEnrollments: List<RoleEnrollment> = emptyList(),
    val acceptedEnrollments: List<RoleEnrollment> = emptyList(),
    val rejectedEnrollments: List<RoleEnrollment> = emptyList(),
    val processingIds: Set<String> = emptySet(),
    val message: String? = null
) {
    val pendingCount: Int get() = pendingEnrollments.size
    val acceptedCount: Int get() = acceptedEnrollments.size
    val rejectedCount: Int get() = rejectedEnrollments.size
    
    fun getAllEnrollments() = pendingEnrollments + acceptedEnrollments + rejectedEnrollments
}
