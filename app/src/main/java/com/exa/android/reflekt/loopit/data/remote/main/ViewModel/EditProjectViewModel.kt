package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.ProjectRepository
import com.exa.android.reflekt.loopit.util.model.Project
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProjectState(
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val selectedRoles: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val availableRoles: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val requestedMembers: List<RequestedMember> = emptyList(),
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val isInitialLoadComplete: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val canSubmit: Boolean = false
)

data class RequestedMember(
    val id: String,
    val name: String
)

@HiltViewModel
class EditProjectViewModel @Inject constructor(
    private val repository: ProjectRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(EditProjectState())
    val state = _state.asStateFlow()

    init {
        loadAvailableFilters()
    }

    fun loadProject(projectId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.getProjectById(projectId)
            result.fold(
                onSuccess = { project ->
                    _state.update {
                        it.copy(
                            projectId = project.id,
                            title = project.title,
                            description = project.description,
                            selectedRoles = project.rolesNeeded.toSet(),
                            selectedTags = project.tags.toSet(),
                            requestedMembers = project.requestedPersons.map { (id, name) ->
                                RequestedMember(id, name)
                            },
                            isInitialLoadComplete = true,
                            isLoading = false,
                            canSubmit = validateForm(project.title, project.description)
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            error = e.message ?: "Failed to load project",
                            isLoading = false
                        )
                    }
                }
            )
        }
    }

    fun updateProject() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val currentState = state.value
            val currentUserId = auth.currentUser?.uid ?: run {
                _state.update { it.copy(
                    isLoading = false,
                    error = "User not authenticated"
                )}
                return@launch
            }

            val project = Project(
                id = currentState.projectId,
                title = currentState.title,
                description = currentState.description,
                rolesNeeded = currentState.selectedRoles.toList(),
                tags = currentState.selectedTags.toList(),
                createdBy = currentUserId,
                createdAt = null, // Will be preserved on server
                createdByName = "", // Will be preserved on server
                requestedPersons = currentState.requestedMembers.associate { it.id to it.name }
            )

            val result = repository.updateProject(project)
            _state.update {
                it.copy(
                    isLoading = false,
                    isSuccess = result.isSuccess,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun deleteProject() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val result = repository.deleteProject(state.value.projectId)
            _state.update {
                it.copy(
                    isLoading = false,
                    isSuccess = result.isSuccess,
                    error = result.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        val error = if (newTitle.isBlank()) "Title cannot be empty" else null
        _state.update {
            it.copy(
                title = newTitle,
                titleError = error,
                canSubmit = validateForm(newTitle, it.description)
            )
        }
    }

    fun onDescriptionChange(newDescription: String) {
        val error = if (newDescription.isBlank()) "Description cannot be empty" else null
        _state.update {
            it.copy(
                description = newDescription,
                descriptionError = error,
                canSubmit = validateForm(it.title, newDescription)
            )
        }
    }

    fun onRoleAdded(role: String) {
        _state.update {
            it.copy(
                selectedRoles = it.selectedRoles + role,
                canSubmit = validateForm(it.title, it.description)
            )
        }
    }

    fun onRoleRemoved(role: String) {
        _state.update {
            it.copy(
                selectedRoles = it.selectedRoles - role,
                canSubmit = validateForm(it.title, it.description)
            )
        }
    }

    fun onTagAdded(tag: String) {
        _state.update {
            it.copy(
                selectedTags = it.selectedTags + tag,
                canSubmit = validateForm(it.title, it.description)
            )
        }
    }

    fun onTagRemoved(tag: String) {
        _state.update {
            it.copy(
                selectedTags = it.selectedTags - tag,
                canSubmit = validateForm(it.title, it.description)
            )
        }
    }

    fun onNewRoleCreated(role: String) {
        viewModelScope.launch {
            val result = repository.addNewRole(role)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            availableRoles = it.availableRoles + role,
                            selectedRoles = it.selectedRoles + role,
                            canSubmit = validateForm(it.title, it.description)
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message ?: "Failed to add role") }
                }
            )
        }
    }

    fun onNewTagCreated(tag: String) {
        viewModelScope.launch {
            val result = repository.addNewTag(tag)
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            availableTags = it.availableTags + tag,
                            selectedTags = it.selectedTags + tag,
                            canSubmit = validateForm(it.title, it.description)
                        )
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(error = e.message ?: "Failed to add tag") }
                }
            )
        }
    }

    private fun loadAvailableFilters() {
        viewModelScope.launch {
            val rolesResult = repository.getAvailableRoles()
            val tagsResult = repository.getAvailableTags()

            _state.update {
                it.copy(
                    availableRoles = rolesResult.getOrElse { emptyList() },
                    availableTags = tagsResult.getOrElse { emptyList() },
                    error = rolesResult.exceptionOrNull()?.message
                        ?: tagsResult.exceptionOrNull()?.message
                )
            }
        }
    }

    private fun validateForm(title: String, description: String): Boolean {
        return title.isNotBlank() && description.isNotBlank()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}