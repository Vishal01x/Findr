package com.exa.android.reflekt.loopit.data.feature.main.post.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val repository: PostRepository,
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var lastLikes: Long? = null
    private var lastTimestamp: Timestamp? = null
    private var isEndReached = false
    private val pageSize = 20L

    fun loadComments(postId : String) {
        if (isEndReached) return

        viewModelScope.launch {
            repository.getComments(
                postId = postId,
                lastLikes = lastLikes,
                lastTimestamp = lastTimestamp,
                limit = pageSize
            ).collect { response ->
                when (response) {
                    is Response.Loading -> _loading.value = true
                    is Response.Success -> {
                        val newComments = response.data
                        if (newComments.isEmpty()) {
                            isEndReached = true
                        } else {
                            _comments.value = _comments.value + newComments
                            // Update last cursor for pagination
                            val lastComment = newComments.last()
                            lastLikes = lastComment.likesCount
                            lastTimestamp = lastComment.timestamp
                        }
                        _loading.value = false
                    }
                    is Response.Error -> {
                        _error.value = response.message
                        _loading.value = false
                    }
                }
            }
        }
    }

    // Optional: call this when user scrolls near end
    fun loadNextPage(postId : String) {
        loadComments(postId)
    }
}
