package com.exa.android.reflekt.loopit.core.network

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post


data class ProjectListState(
    val projects: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedRoles: Set<String> = emptySet(),
    val availableRoles: List<String> = emptyList(),
    val selectedTags: Set<String> = emptySet(),
    val availableTags: List<String> = emptyList(),
    val showMyProjectsOnly: Boolean = false,
    val isRefreshing: Boolean = false,
    val selectedPostType: PostType? = null,
    val likedProjects: Set<String> = emptySet()
)

sealed class PostListEvent {
    data class SearchQueryChanged(val query: String) : PostListEvent()
    data class RoleSelected(val role: String) : PostListEvent()
    data class RoleDeselected(val role: String) : PostListEvent()
    data class TagSelected(val tag: String) : PostListEvent()
    data class TagDeselected(val tag: String) : PostListEvent()
    data class DeleteProject(val projectId: String) : PostListEvent()
    object Refresh : PostListEvent()
    object ToggleMyProjects : PostListEvent()
    object ClearFilters : PostListEvent()
    object ClearError : PostListEvent()
    data class SelectPostType(val type: PostType?) : PostListEvent()
    data class ToggleLike(val projectId: String) : PostListEvent()
    data class AddComment(val project : Post, val text: String) : PostListEvent()
    data class UpdateComment(val projectId: String, val commentId: String, val newText: String) : PostListEvent()
    data class DeleteComment(val projectId: String, val commentId: String) : PostListEvent()
}