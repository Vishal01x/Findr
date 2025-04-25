package com.exa.android.reflekt.loopit.data.remote.main.Repository

import com.exa.android.reflekt.loopit.util.model.Project
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    suspend fun createProject(project: Project): Result<Unit>
    fun getProjectsStream(
        searchQuery: String = "",
        rolesFilter: List<String> = emptyList(),
        tagsFilter: List<String> = emptyList(),
        showMyProjectsOnly: Boolean = false,
        userId: String? = null
    ): Flow<Result<List<Project>>>
    suspend fun getProjectById(projectId: String): Result<Project>
    suspend fun getAvailableRoles(): Result<List<String>>
    suspend fun getAvailableTags(): Result<List<String>>
    suspend fun updateProject(project: Project): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
    suspend fun addNewRole(role: String): Result<Unit>
    suspend fun addNewTag(tag: String): Result<Unit>
    suspend fun enrollInProject(project: Project, userId: String, userName: String, imageUrl : String?): Result<Unit>
    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit>
    suspend fun acceptJoinRequest(projectId: String, userId: String, userName: String): Result<Unit>
    suspend fun rejectJoinRequest(projectId: String, userId: String): Result<Unit>
    fun getProjectUpdates(projectId: String): Flow<Result<Project>>
}