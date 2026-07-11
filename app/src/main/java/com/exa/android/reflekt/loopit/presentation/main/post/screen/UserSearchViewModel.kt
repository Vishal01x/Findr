package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.utils.ShareUtils
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.ChatRepository
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserSearchState(
    val query: String = "",
    val results: List<profileUser> = emptyList(),
    val isLoading: Boolean = false,
    val invitedUserIds: Set<String> = emptySet(),
    val toastMessage: String? = null,
    val showEmailInvite: Boolean = false
)

@HiltViewModel
class UserSearchViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val chatRepository: ChatRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val postId: String = checkNotNull(savedStateHandle["postId"])
    val projectTitle: String = savedStateHandle.get<String>("title") ?: ""

    private val _state = MutableStateFlow(UserSearchState())
    val state: StateFlow<UserSearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query, showEmailInvite = false) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300)
            _state.update { it.copy(isLoading = true) }
            userRepository.searchUsersByQuery(query)
                .onSuccess { users ->
                    _state.update {
                        it.copy(
                            results = users,
                            isLoading = false,
                            showEmailInvite = users.isEmpty() && query.contains("@")
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    fun inviteUser(user: profileUser) {
        if (_state.value.invitedUserIds.contains(user.uid)) return
        viewModelScope.launch {
            val inviterName = userRepository.getUserInfoById(auth.currentUser?.uid ?: "")
                .getOrNull()?.name ?: "Someone"
            val fcmToken = userRepository.getUserFcm(user.uid) ?: run {
                _state.update { it.copy(toastMessage = "Could not reach ${user.name}") }
                return@launch
            }
            postRepository.inviteUserToProject(
                postId = postId,
                projectTitle = projectTitle,
                invitedUserId = user.uid,
                invitedFcmToken = fcmToken,
                inviterName = inviterName,
                roleName = null
            ).onSuccess {
//                val projectLink = ShareUtils.buildFdlUrl(postId)
//                val inviteMessage = "Hey! $inviterName is inviting you to join \"$projectTitle\" on Findr.\n\nCheck it out: $projectLink"
//                chatRepository.createChatAndSendMessage(
//                    userId2 = user.uid,
//                    isCurUserBlocked = false,
//                    text = inviteMessage,
//                    replyTo = null,
//                    media = null,
//                    receiverToken = fcmToken
//                )
                _state.update {
                    it.copy(
                        invitedUserIds = it.invitedUserIds + user.uid,
                        toastMessage = "Invitation sent to ${user.name}"
                    )
                }
            }.onFailure {
                _state.update { it.copy(toastMessage = "Failed to send invitation") }
            }
        }
    }

    fun openEmailInvite(context: Context) {
        val email = _state.value.query.trim()
        val body = buildString {
            append("Hey! I'd like you to join \"$projectTitle\" on Findr.")
            append("\n\nFindr helps students and professionals connect for projects and events.")
            append("\n\nDownload the app: https://play.google.com/store/apps/details?id=com.exa.android.reflekt")
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, "Join \"$projectTitle\" on Findr!")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        runCatching { context.startActivity(Intent.createChooser(intent, "Send invite via")) }
    }

    fun clearToast() {
        _state.update { it.copy(toastMessage = null) }
    }
}
