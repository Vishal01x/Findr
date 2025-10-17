package com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Postt
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createProject(project: Postt, profile : profileUser): Result<Unit>
    fun getProjectsStream(
        searchQuery: String = "",
        rolesFilter: List<String> = emptyList(),
        tagsFilter: List<String> = emptyList(),
        showMyProjectsOnly: Boolean = false,
        userId: String? = null,
        postType: String?
    ): Flow<Result<List<Postt>>>
    suspend fun getProjectById(projectId: String): Result<Postt>
    suspend fun getAvailableRoles(): Result<List<String>>
    suspend fun getAvailableTags(): Result<List<String>>
    suspend fun updateProject(project: Postt, profile : profileUser): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
    suspend fun addNewRole(role: String): Result<Unit>
    suspend fun addNewTag(tag: String): Result<Unit>
    suspend fun enrollInProject(project: Postt, userId: String, userName: String, imageUrl : String?): Result<Unit>
    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit>
    suspend fun acceptJoinRequest(
        project : Postt,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit>
    suspend fun rejectJoinRequest(project : Postt, userId: String, value: profileUser): Result<Unit>
    fun getProjectUpdates(projectId: String): Flow<Result<Postt>>
    suspend fun addComment(project: Postt, profile: profileUser, comment: Comment): Result<Unit>
    suspend fun updateComment(projectId: String, commentId: String, newText: String): Result<Unit>
    suspend fun deleteComment(projectId: String, commentId: String): Result<Unit>
    suspend fun toggleLike(projectId: String, userId: String): Result<Unit>
}