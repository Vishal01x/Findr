package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.GetEnrollmentRequestsUseCase
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.UpdateEnrollmentStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageEnrollmentsState(
    val post: Post? = null,
    val enrollmentsByRole: Map<String, List<RoleEnrollment>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ManageEnrollmentsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val getEnrollmentRequestsUseCase: GetEnrollmentRequestsUseCase,
    private val updateEnrollmentStatusUseCase: UpdateEnrollmentStatusUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _state = MutableStateFlow(ManageEnrollmentsState(isLoading = true))
    val state: StateFlow<ManageEnrollmentsState> = _state.asStateFlow()

    init {
        loadPost()
        loadEnrollments()
    }

    private fun loadPost() {
        viewModelScope.launch {
            postRepository.getPostById(postId)
                .onSuccess { post ->
                    _state.update { it.copy(post = post) }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    private fun loadEnrollments() {
        viewModelScope.launch {
            getEnrollmentRequestsUseCase(postId)
                .onSuccess { enrollments ->
                    _state.update { 
                        it.copy(
                            enrollmentsByRole = enrollments,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun acceptEnrollment(enrollmentId: String, roleId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            updateEnrollmentStatusUseCase(
                enrollmentId = enrollmentId,
                postId = postId,
                roleId = roleId,
                status = EnrollmentStatus.ACCEPTED
            ).onSuccess {
                _state.update { it.copy(successMessage = "Enrollment accepted!") }
                loadEnrollments() // Reload to get updated list
                loadPost() // Reload post to get updated role counts
            }.onFailure { error ->
                _state.update { 
                    it.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun rejectEnrollment(enrollmentId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            updateEnrollmentStatusUseCase(
                enrollmentId = enrollmentId,
                postId = postId,
                roleId = "", // Not needed for rejection
                status = EnrollmentStatus.REJECTED
            ).onSuccess {
                _state.update { it.copy(successMessage = "Enrollment rejected") }
                loadEnrollments() // Reload to get updated list
            }.onFailure { error ->
                _state.update { 
                    it.copy(
                        error = error.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}
