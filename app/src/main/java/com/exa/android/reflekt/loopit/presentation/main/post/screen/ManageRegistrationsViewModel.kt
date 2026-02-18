package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.GetRegistrationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageRegistrationsState(
    val post: Post? = null,
    val registrations: List<EventRegistration> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ManageRegistrationsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val getRegistrationsUseCase: GetRegistrationsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = checkNotNull(savedStateHandle["postId"])

    private val _state = MutableStateFlow(ManageRegistrationsState(isLoading = true))
    val state: StateFlow<ManageRegistrationsState> = _state.asStateFlow()

    init {
        loadPost()
        loadRegistrations()
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

    private fun loadRegistrations() {
        viewModelScope.launch {
            getRegistrationsUseCase(postId)
                .onSuccess { registrations ->
                    _state.update { 
                        it.copy(
                            registrations = registrations,
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
}
