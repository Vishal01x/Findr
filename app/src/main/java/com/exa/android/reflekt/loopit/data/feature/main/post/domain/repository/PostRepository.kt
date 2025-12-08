package com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository

import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Reply
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createPost(project: Post, profile : profileUser): Result<Unit>
    fun getPostsStream(
        searchQuery: String = "",
        rolesFilter: List<String> = emptyList(),
        tagsFilter: List<String> = emptyList(),
        showMyProjectsOnly: Boolean = false,
        userId: String? = null,
        postType: String?
    ): Flow<Result<List<Post>>>
    suspend fun getPostById(projectId: String): Result<Post>
    suspend fun getAvailableRoles(): Result<List<String>>
    suspend fun getAvailableTags(): Result<List<String>>
    suspend fun updateProject(project: Post, profile : profileUser): Result<Unit>
    suspend fun deleteProject(projectId: String): Result<Unit>
    suspend fun addNewRole(role: String): Result<Unit>
    suspend fun addNewTag(tag: String): Result<Unit>
    suspend fun enrollInProject(project: Post, userId: String, userName: String, imageUrl : String?): Result<Unit>
    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit>
    suspend fun acceptJoinRequest(
        project : Post,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit>
    suspend fun rejectJoinRequest(project : Post, userId: String, value: profileUser): Result<Unit>
    fun getProjectUpdates(projectId: String): Flow<Result<Post>>
    suspend fun toggleLike(projectId: String, userId: String): Result<Unit>
    suspend fun addComment(project: Post, profile: profileUser, comment: Comment): Result<Unit>
    suspend fun updateComment(projectId: String, commentId: String, newText: String): Result<Unit>
    suspend fun deleteComment(projectId: String, commentId: String): Result<Unit>
    suspend fun addEmbeddedReply(postId: String, commentId: String, reply: Reply): Result<Unit>
    suspend fun updateEmbeddedReply(
        postId: String,
        commentId: String,
        newReplyText: String
    ): Result<Unit>

    suspend fun deleteEmbeddedReply(postId: String, commentId: String): Result<Unit>
    fun getComments(
        postId: String,
        lastLikes: Long? = null,
        lastTimestamp: Timestamp? = null,
        limit: Long = 20
    ): Flow<Response<List<Comment>>>
}