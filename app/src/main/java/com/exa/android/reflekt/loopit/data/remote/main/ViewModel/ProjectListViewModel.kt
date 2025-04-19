package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProfileRepository
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProjectRepository
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.exa.android.reflekt.loopit.util.application.ProjectListState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val profileRepository: ProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListState())
    val state = _state.asStateFlow()

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
            ProjectListEvent.ToggleMyProjects -> {
                _state.update { it.copy(showMyProjectsOnly = !it.showMyProjectsOnly) }
                loadProjects()
            }
            ProjectListEvent.Refresh -> {
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

        }
    }

    private fun loadProjects(forceRefresh: Boolean = false) {
        val load="loading"
        _state.update { it.copy(isLoading = true, error = null) }
        Timber.tag(load).d("Loading projects")

        viewModelScope.launch {
            repository.getProjectsStream(
                searchQuery = _state.value.searchQuery,
                rolesFilter = _state.value.selectedRoles.toList(),
                tagsFilter = _state.value.selectedTags.toList(),
                showMyProjectsOnly = _state.value.showMyProjectsOnly,
                userId = auth.currentUser?.uid
            ).collect { result ->
                Timber.tag(load).d("Projects loaded")
                _state.update {
                    it.copy(
                        isLoading = false,
                        projects = result.getOrElse { emptyList() },
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    private fun loadFilters() {
        _state.update { it.copy(isLoading = true, error = null) }
        val load="loading"
        Timber.tag(load).d("Loading filters")

        viewModelScope.launch {
            val rolesResult = repository.getAvailableRoles()
            val tagsResult = repository.getAvailableTags()
            Timber.tag(load).d("Filters loaded")

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

    fun enrollInProject(projectId: String) {
        auth.currentUser?.uid?.let { userId ->
            _state.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                try {
                    val profile = profileRepository.getUserProfile(userId)
                    val userName = "${profile.firstName} ${profile.lastName}"
                    repository.enrollInProject(projectId, userId, userName)
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
}