package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.usecase.DeletePostUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyPostsState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,

)

@HiltViewModel
class MyPostsViewModel @Inject constructor(
    private val postRepository: PostRepository,

    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(MyPostsState())
    val state: StateFlow<MyPostsState> = _state.asStateFlow()
    
    val currentUserId = auth.currentUser?.uid ?: ""

    init {
        loadMyPosts()
    }

    private fun loadMyPosts() {
        if (currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            postRepository.getMyPosts(currentUserId)
                .catch { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                }
                .collect { result ->
                    result.fold(
                        onSuccess = { posts ->
                            _state.update { it.copy(posts = posts, isLoading = false) }
                        },
                        onFailure = { error ->
                            _state.update { it.copy(error = error.message, isLoading = false) }
                        }
                    )
                }
        }
    }

}
