package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.ManageEnrollmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnrollmentManagementState(
    val enrollments: List<RoleEnrollment> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EnrollmentManagementViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val manageEnrollmentUseCase: ManageEnrollmentUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String =
        savedStateHandle.get<String>("postId")
            ?: throw IllegalArgumentException("postId is required")

    private val _state = MutableStateFlow(EnrollmentManagementState())
    val state: StateFlow<EnrollmentManagementState> = _state.asStateFlow()

    init {
        loadEnrollments()
    }

    private fun loadEnrollments() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            postRepository.getEnrollmentsForPost(postId)
                .onSuccess { enrollments ->
                    _state.update {
                        it.copy(
                            enrollments = enrollments,
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

    fun acceptEnrollment(enrollmentId: String) {
        updateEnrollment(enrollmentId, EnrollmentStatus.ACCEPTED)
    }

    fun rejectEnrollment(enrollmentId: String) {
        updateEnrollment(enrollmentId, EnrollmentStatus.REJECTED)
    }

    private fun updateEnrollment(
        enrollmentId: String,
        status: EnrollmentStatus
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            val result = when (status) {
                EnrollmentStatus.ACCEPTED ->
                    manageEnrollmentUseCase.accept(postId, enrollmentId)
                EnrollmentStatus.REJECTED ->
                    manageEnrollmentUseCase.reject(postId, enrollmentId)
                else -> Result.failure(Exception("Invalid status"))
            }

            result.onSuccess {
                _state.update { current ->
                    current.copy(
                        enrollments = current.enrollments.map {
                            if (it.id == enrollmentId)
                                it.copy(status = status)
                            else it
                        },
                        isLoading = false
                    )
                }
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
}
