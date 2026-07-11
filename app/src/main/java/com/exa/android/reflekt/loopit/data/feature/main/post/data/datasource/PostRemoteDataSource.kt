package com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for post-related Firebase operations
 * Handles all Firestore post CRUD operations
 */
@Singleton
class PostRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val postsCollection get() = firestore.collection(PostConstants.Collections.PROJECTS)
    
    /**
     * Create a new post in Firestore
     */
    suspend fun createPost(postId: String, postMap: Map<String, Any?>): Result<Unit> {
        return try {
            postsCollection
                .document(postId)
                .set(postMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get posts stream with filters
     */
    fun getPostsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?,
        typeFilter: String?
    ): Flow<Result<List<Map<String, Any?>>>> = callbackFlow {
        try {
            var query: Query = postsCollection
                .orderBy(PostConstants.PostFields.CREATED_AT, Query.Direction.DESCENDING)
            
            if (searchQuery.isNotBlank()) {
                val queryText = searchQuery.lowercase()
                query = query
                    .whereGreaterThanOrEqualTo(PostConstants.PostFields.TITLE_LOWER, queryText)
                    .whereLessThanOrEqualTo(PostConstants.PostFields.TITLE_LOWER, "$queryText\uf8ff")
            }
            
            if (!typeFilter.isNullOrEmpty()) {
                query = query.whereEqualTo(PostConstants.PostFields.TYPE, typeFilter)
            }
            
            if (rolesFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny(PostConstants.PostFields.ROLES_NEEDED, rolesFilter)
            } else if (tagsFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny(PostConstants.PostFields.TAGS, tagsFilter)
            }
            
            if (showMyProjectsOnly && userId != null) {
                query = query.whereEqualTo(PostConstants.PostFields.CREATED_BY, userId)
            }
            
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val posts = snapshot.documents.mapNotNull { doc ->
                        doc.data?.plus(PostConstants.PostFields.ID to doc.id)
                    }
                    trySend(Result.success(posts))
                }
            }
            
            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }
    
    /**
     * Get post by ID
     */
    suspend fun getPostById(postId: String): Result<Map<String, Any?>> {
        return try {
            val document = postsCollection
                .document(postId)
                .get()
                .await()
            
            document.data?.plus(PostConstants.PostFields.ID to document.id)
                ?.let { Result.success(it) }
                ?: Result.failure(Exception(PostConstants.ValidationMessages.POST_NOT_FOUND))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update post
     */
    suspend fun updatePost(postId: String, postMap: Map<String, Any?>): Result<Unit> {
        return try {
            postsCollection
                .document(postId)
                .update(postMap)
                //.set(postMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete post
     */
    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            postsCollection
                .document(postId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Toggle like on a post.
     * Direct field update (arrayUnion/arrayRemove + increment) instead of a transaction:
     * a transaction always forces a server round trip for its read before it can write, while
     * a plain update() is applied to Firestore's local cache immediately, so the existing
     * snapshot listener (getPostsStream/getProjectUpdates) reflects it instantly.
     */
    suspend fun toggleLike(
        postId: String,
        userId: String,
        isCurrentlyLiked: Boolean
    ): Result<Unit> {
        return try {
            val update = if (isCurrentlyLiked) {
                mapOf(
                    PostConstants.PostFields.LIKES to FieldValue.arrayRemove(userId),
                    PostConstants.PostFields.LIKES_COUNT to FieldValue.increment(-1)
                )
            } else {
                mapOf(
                    PostConstants.PostFields.LIKES to FieldValue.arrayUnion(userId),
                    PostConstants.PostFields.LIKES_COUNT to FieldValue.increment(1)
                )
            }
            postsCollection.document(postId).update(update).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Vote (or change vote / un-vote) on a poll post.
     * The caller (repository) recomputes the full [updatedPollOptions] from the locally-known
     * poll state, so this is a single direct field update with no preceding read - keeps the
     * write on the fast, locally-echoed path instead of a transaction, which always forces a
     * server round trip before it can apply anything.
     */
    suspend fun voteOnPoll(postId: String, updatedPollOptions: List<Map<String, Any?>>): Result<Unit> {
        return try {
            postsCollection.document(postId)
                .update(PostConstants.PostFields.POLL_OPTIONS, updatedPollOptions)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    /**
     * Enroll in project
     */
    suspend fun enrollInProject(projectId: String, userId: String, userName: String): Result<Unit> {
        return try {
            postsCollection
                .document(projectId)
                .update(mapOf("${PostConstants.PostFields.REQUESTED_PERSONS}.$userId" to userName))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Withdraw from project
     */
    suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit> {
        return try {
            postsCollection
                .document(projectId)
                .update(mapOf("${PostConstants.PostFields.REQUESTED_PERSONS}.$userId" to FieldValue.delete()))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Accept join request
     */
    suspend fun acceptJoinRequest(projectId: String, userId: String, userName: String): Result<Unit> {
        return try {
            val updates = mapOf(
                "${PostConstants.PostFields.ENROLLED_PERSONS}.$userId" to userName,
                "${PostConstants.PostFields.REQUESTED_PERSONS}.$userId" to FieldValue.delete()
            )
            postsCollection
                .document(projectId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reject join request
     */
    suspend fun rejectJoinRequest(projectId: String, userId: String): Result<Unit> {
        return try {
            postsCollection
                .document(projectId)
                .update("${PostConstants.PostFields.REQUESTED_PERSONS}.$userId", FieldValue.delete())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get project updates stream
     */
    fun getProjectUpdates(projectId: String): Flow<Result<Map<String, Any?>>> = callbackFlow {
        val listener = postsCollection
            .document(projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                
                snapshot?.data?.plus(PostConstants.PostFields.ID to snapshot.id)
                    ?.let { trySend(Result.success(it)) }
                    ?: trySend(Result.failure(Exception(PostConstants.ValidationMessages.POST_NOT_FOUND)))
            }
        
        awaitClose { listener.remove() }
    }
}
