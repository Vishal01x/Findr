package com.exa.android.reflekt.loopit.util.application

import com.exa.android.reflekt.loopit.util.model.Project


data class ProjectListState(
    val projects: List<Project> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedRoles: Set<String> = emptySet(),
    val availableRoles: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val showMyProjectsOnly: Boolean = false,
    val isRefreshing: Boolean = false
)

sealed class ProjectListEvent {
    data class SearchQueryChanged(val query: String) : ProjectListEvent()
    data class RoleSelected(val role: String) : ProjectListEvent()
    data class RoleDeselected(val role: String) : ProjectListEvent()
    data class TagSelected(val tag: String) : ProjectListEvent()
    data class TagDeselected(val tag: String) : ProjectListEvent()
    data class DeleteProject(val projectId: String) : ProjectListEvent()
    object Refresh : ProjectListEvent()
    object ToggleMyProjects : ProjectListEvent()
    object ClearFilters : ProjectListEvent()
    object ClearError : ProjectListEvent()
}