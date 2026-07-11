package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShortlistedUsersState(
    val users: List<Pair<profileUser, String>> = emptyList(), // user + role/label
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ShortlistedUsersViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val postId: String = savedStateHandle["postId"] ?: ""
    val mode: String = savedStateHandle["mode"] ?: "project"
    val title: String = savedStateHandle["title"] ?: ""

    private val _state = MutableStateFlow(ShortlistedUsersState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            if (mode == "event") {
                loadEventParticipants()
            } else {
                loadProjectCollaborators()
            }
        }
    }

    private suspend fun loadProjectCollaborators() {
        postRepository.getPostById(postId).onSuccess { post ->
            val userIds = (listOf(post.createdBy) + post.enrolledPersons.keys).distinct()
            userRepository.getUsersInfoByIds(userIds).onSuccess { userMap ->
                val pairs = userIds.mapNotNull { uid ->
                    val user = userMap[uid] ?: return@mapNotNull null
                    val label = when (uid) {
                        post.createdBy -> "Owner"
                        else -> post.enrolledPersons[uid] ?: "Collaborator"
                    }
                    user to label
                }
                _state.update { it.copy(users = pairs, isLoading = false) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }.onFailure { e ->
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    private suspend fun loadEventParticipants() {
        postRepository.getRegistrationsForEvent(postId).onSuccess { regs ->
            val accepted = regs.filter { it.status == com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RegistrationStatus.REGISTERED || it.status == com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RegistrationStatus.ATTENDED }
            val userIds = accepted.map { it.userId }.distinct()
            userRepository.getUsersInfoByIds(userIds).onSuccess { userMap ->
                val pairs = userIds.mapNotNull { uid ->
                    val user = userMap[uid] ?: return@mapNotNull null
                    user to "Registered"
                }
                _state.update { it.copy(users = pairs, isLoading = false) }
            }.onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }.onFailure { e ->
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }
}
