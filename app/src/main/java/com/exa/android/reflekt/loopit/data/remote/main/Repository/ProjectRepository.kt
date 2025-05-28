package com.exa.android.reflekt.loopit.data.remote.main.Repository

import com.exa.android.reflekt.loopit.util.model.Comment
import com.exa.android.reflekt.loopit.util.model.Project
import com.exa.android.reflekt.loopit.util.model.profileUser
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(project: Project, profile : profileUser): Result<Unit>
    fun getProjectsStream(
        searchQuery: String = "",
        rolesFilter: List<String> = emptyList(),
        tagsFilter: List<String> = emptyList(),
        showMyProjectsOnly: Boolean = false,
        userId: String? = null,
        postType: String?
    ): Flow<Result<List<Project>>>
    suspend fun getProjectById(projectId: String): Result<Project>
    suspend fun getAvailableRoles(): Result<List<String>>
    suspend fun getAvailableTags(): Result<List<String>>
    suspend fun updateProject(project: Project, profile : profileUser): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
    suspend fun addNewRole(role: String): Result<Unit>
    suspend fun addNewTag(tag: String): Result<Unit>
    suspend fun enrollInProject(project: Project, userId: String, userName: String, imageUrl : String?): Result<Unit>
    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit>
    suspend fun acceptJoinRequest(
        project : Project,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit>
    suspend fun rejectJoinRequest(project : Project,userId: String, value: profileUser): Result<Unit>
    fun getProjectUpdates(projectId: String): Flow<Result<Project>>
    suspend fun addComment(projectId: String, comment: Comment): Result<Unit>
    suspend fun updateComment(projectId: String, commentId: String, newText: String): Result<Unit>
    suspend fun deleteComment(projectId: String, commentId: String): Result<Unit>
    suspend fun toggleLike(projectId: String, userId: String): Result<Unit>
}