package com.exa.android.reflekt.loopit.data.feature.main.post.data.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Postt
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.core.network.ProjectListEvent
import com.exa.android.reflekt.loopit.core.network.ProjectListEvent.SelectPostType
import com.exa.android.reflekt.loopit.core.network.ProjectListState
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostListViewModel @Inject constructor(
    private val repository: PostRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListState())
    val state = _state.asStateFlow()

    private val curUserProfile =  MutableStateFlow(profileUser())

    val currentUserId = auth.currentUser?.uid

    private var projectsJob: Job? = null

    init {
        loadFilters()
        loadProjects()
        getProfile()
    }

    fun getProfile(){
        viewModelScope.launch {
            currentUserId?.let {
                curUserProfile.value = profileRepository.getUserProfile(currentUserId)
            }
        }
    }


    fun onEvent(event: ProjectListEvent) {
        when (event) {
            is ProjectListEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                loadProjects()
            }
            is ProjectListEvent.RoleSelected -> {
                _state.update { it.copy(
                    selectedRoles = it.selectedRoles + event.role
                )}
                loadProjects()
            }
            is ProjectListEvent.RoleDeselected -> {
                _state.update { it.copy(
                    selectedRoles = it.selectedRoles - event.role
                )}
                loadProjects()
            }
            is ProjectListEvent.TagSelected -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags + event.tag
                )}
                loadProjects()
            }
            is ProjectListEvent.TagDeselected -> {
                _state.update { it.copy(
                    selectedTags = it.selectedTags - event.tag
                )}
                loadProjects()
            }
            is ProjectListEvent.DeleteProject -> {
                viewModelScope.launch {
                    repository.deleteProject(event.projectId)
                    loadProjects() // Refresh the list after deletion
                }
            }
            is SelectPostType -> {
                _state.update { it.copy(selectedPostType = event.type) }
                loadProjects()
            }
            is ProjectListEvent.AddComment -> {
                viewModelScope.launch {
                    //repository.addComment(event.projectId, comment)
                    addComment(event.project, event.text)
                }
            }
            is ProjectListEvent.UpdateComment -> {
                viewModelScope.launch {
                    repository.updateComment(
                        event.projectId,
                        event.commentId,
                        event.newText
                    )
                }
            }
            is ProjectListEvent.DeleteComment -> {
                viewModelScope.launch {
                    repository.deleteComment(event.projectId, event.commentId)
                }
            }
            is ProjectListEvent.ToggleLike -> toggleLike(event.projectId)
            ProjectListEvent.ToggleMyProjects -> {
                _state.update { it.copy(showMyProjectsOnly = !it.showMyProjectsOnly) }
                loadProjects()
            }
            ProjectListEvent.Refresh -> {
                _state.update { it.copy(isRefreshing = true) }
                loadProjects(forceRefresh = true)
            }
            ProjectListEvent.ClearFilters -> {
                _state.update {
                    it.copy(
                        searchQuery = "",
                        selectedRoles = emptySet(),
                        selectedTags = emptySet()
                    )
                }
                loadProjects()
            }
            ProjectListEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }
    fun toggleLike(projectId: String) {
        auth.currentUser?.uid?.let { userId ->
            viewModelScope.launch {
                repository.toggleLike(projectId, userId)
            }
        }
    }

    fun loadProjects(forceRefresh: Boolean = false) {
        _state.update { it.copy(isLoading = true, error = null) }

        projectsJob?.cancel() // Cancel previous listener if exists
        projectsJob = viewModelScope.launch {
            repository.getProjectsStream(
                searchQuery = _state.value.searchQuery,
                rolesFilter = _state.value.selectedRoles.toList(),
                tagsFilter = _state.value.selectedTags.toList(),
                showMyProjectsOnly = _state.value.showMyProjectsOnly,
                userId = auth.currentUser?.uid,
                postType = _state.value.selectedPostType?.displayName
            ).collect { result ->
                result.fold(
                    onSuccess = { projects ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                projects = projects,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    },
                    onFailure = { error ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = "Failed to load projects"
                            )
                        }
                    }
                )
            }
        }
    }
    private val _emails = mutableStateOf<List<String>>(emptyList())
    val emails: State<List<String>> = _emails

    fun fetchAllUserEmails() {
        viewModelScope.launch {
            try {
                _emails.value = profileRepository.getAllUserEmails()
            } catch (e: Exception) {
                // Handle error if needed
                _state.update { it.copy(error = "Failed to fetch emails") }
            }
        }
    }


    private fun loadFilters() {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val rolesResult = repository.getAvailableRoles()
            val tagsResult = repository.getAvailableTags()

            _state.update {
                val hasError = rolesResult.isFailure || tagsResult.isFailure

                it.copy(
                    isLoading = false,
                    availableRoles = rolesResult.getOrElse { emptyList() },
                    availableTags = tagsResult.getOrElse { emptyList() },
                    error = if (hasError) "There is some error in loading filters" else null
                )
            }
        }
    }

    fun enrollInProject(project: Postt) {
        auth.currentUser?.uid?.let { userId ->
            _state.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                try {
                    val profile = curUserProfile.value
                    val userName = profile.name
                    repository.enrollInProject(project, userId, userName, profile.imageUrl)
                    loadProjects() // Refresh the list
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = "There is some error in enrolling project") }
                }
            }
        }
    }

    fun withdrawFromProject(projectId: String) {
        auth.currentUser?.uid?.let { userId ->
            _state.update { it.copy(isLoading = true, error = null) }

            viewModelScope.launch {
                try {
                    repository.withdrawFromProject(projectId, userId)
                    loadProjects() // Refresh the list
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = "There is some error in withdrawing project") }
                }
            }
        }
    }
    fun acceptJoinRequest(project : Postt, userId: String, userName: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val profileUser = curUserProfile.value
                repository.acceptJoinRequest(project, userId, userName, profileUser)
                loadProjects()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "There is some error in accepting join request") }
            }
        }
    }


    fun rejectJoinRequest(project: Postt, userId: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                repository.rejectJoinRequest(project, userId, curUserProfile.value)
                loadProjects()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "There is some error in rejecting join request") }
            }
        }
    }
    // In ProjectListViewModel
    suspend fun addComment(project : Postt, text: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Fetch user profile to get name
            val profile = profileRepository.getUserProfile(currentUser.uid)
            val userName = profile?.name ?: "Unknown"

            val comment = Comment(
                id = UUID.randomUUID().toString(),
                text = text,
                senderId = currentUser.uid,
                senderName = userName
            )

            repository.addComment(project, curUserProfile.value, comment)
        }
    }

    fun deleteComment(projectId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(projectId, commentId)
        }
    }
}