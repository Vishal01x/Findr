package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProjectRepository
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent.SelectPostType
import com.exa.android.reflekt.loopit.util.application.ProjectListState
import com.exa.android.reflekt.loopit.util.model.Comment
import com.exa.android.reflekt.loopit.util.model.Project
import com.google.firebase.Timestamp
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
class ProjectListViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListState())
    val state = _state.asStateFlow()

    private var projectsJob: Job? = null

    init {
        loadFilters()
        loadProjects()
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
                val comment = Comment(
                    id = UUID.randomUUID().toString(),
                    text = event.text,
                    senderId = auth.currentUser?.uid ?: return,
                    timestamp = Timestamp.now()
                )
                viewModelScope.launch {
                    repository.addComment(event.projectId, comment)
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
                                error = error.message ?: "Failed to load projects"
                            )
                        }
                    }
                )
            }
        }
    }


    private fun loadFilters() {
        _state.update { it.copy(isLoading = true, error = null) }
        val load="loading"
        //Timber.tag(load).d("Loading filters")

        viewModelScope.launch {
            val rolesResult = repository.getAvailableRoles()
            val tagsResult = repository.getAvailableTags()
            //Timber.tag(load).d("Filters loaded")

            _state.update {
                it.copy(
                    isLoading = false,
                    availableRoles = rolesResult.getOrElse { emptyList() },
                    availableTags = tagsResult.getOrElse { emptyList() },
                    error = rolesResult.exceptionOrNull()?.message
                        ?: tagsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun enrollInProject(project: Project) {
        auth.currentUser?.uid?.let { userId ->
            _state.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                try {
                    val profile = profileRepository.getUserProfile(userId)
                    val userName = profile.name
                    repository.enrollInProject(project, userId, userName, profile.imageUrl)
                    loadProjects() // Refresh the list
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = e.message) }
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
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            }
        }
    }
    fun acceptJoinRequest(projectId: String, userId: String, userName: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                repository.acceptJoinRequest(projectId, userId, userName)
                loadProjects()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }


    fun rejectJoinRequest(projectId: String, userId: String) {
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                repository.rejectJoinRequest(projectId, userId)
                loadProjects()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    fun addComment(projectId: String, text: String) {
        viewModelScope.launch {
            repository.addComment(projectId, Comment(
                id = UUID.randomUUID().toString(),
                text = text,
                senderId = auth.currentUser?.uid ?: return@launch,
                timestamp = Timestamp.now()
            ))
        }
    }

    fun deleteComment(projectId: String, commentId: String) {
        viewModelScope.launch {
            repository.deleteComment(projectId, commentId)
        }
    }
}