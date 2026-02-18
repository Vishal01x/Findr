package com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource

import android.util.Log
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Reply
import com.google.firebase.Timestamp
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
 * Remote data source for comment-related Firebase operations
 * Handles all Firestore comment CRUD operations
 */
@Singleton
class CommentRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val postsCollection get() = firestore.collection(PostConstants.Collections.PROJECTS)
    private val TAG = "CommentDataSource"
    /**
     * Generate a document ID for comment
     */
    fun generateCommentId(): String {
        return firestore.collection(PostConstants.Collections.PROJECTS)
            .document()
            .collection(PostConstants.Collections.COMMENTS)
            .document()
            .id
    }
    
    /**
     * Get comments stream with pagination
     */
    fun getComments(
        postId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>> = callbackFlow {
        trySend(Response.Loading)
        
        val baseQuery = postsCollection
            .document(postId)
            .collection(PostConstants.Collections.COMMENTS)
            .orderBy(PostConstants.CommentFields.LIKES_COUNT, Query.Direction.DESCENDING)
            .orderBy(PostConstants.CommentFields.TIMESTAMP, Query.Direction.DESCENDING)
        
        val query = if (lastLikes != null && lastTimestamp != null) {
            baseQuery.startAfter(lastLikes, lastTimestamp).limit(limit)
        } else {
            baseQuery.limit(limit)
        }
        
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.localizedMessage))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.toObjects(Comment::class.java)
                    .mapIndexed { index, comment ->
                        comment.copy(id = snapshot.documents[index].id)
                    }
                trySend(Response.Success(list))
            }
        }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Add comment
     */
    suspend fun addComment(postId: String, comment: Comment): Result<Unit> {
        return try {
            val commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(comment.id)
            
            commentRef.set(comment).await()
            
            // Increment comment count
            postsCollection
                .document(postId)
                .update(PostConstants.PostFields.COMMENT_COUNT, FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update comment
     */
    suspend fun updateComment(postId: String, commentId: String, newText: String): Result<Unit> {
        return try {
            postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)
                .update(mapOf(PostConstants.CommentFields.TEXT to newText))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete a comment and all its nested replies
     */
    suspend fun deleteComment(
        postId: String,
        commentId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)

            // Validate ownership
            val commentDoc = commentRef.get().await()
            val senderId = commentDoc.getString(PostConstants.CommentFields.SENDER_ID)

            if (senderId != userId) {
                return Result.failure(IllegalStateException("Only comment owner can delete"))
            }

            // Delete the comment (Firestore doesn't auto-delete subcollections, but we'll handle replies separately)
            commentRef.delete().await()

            // Decrement post comment count
            postsCollection
                .document(postId)
                .update(PostConstants.PostFields.COMMENT_COUNT, FieldValue.increment(-1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting comment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add embedded reply (DEPRECATED - use NestedCommentDataSource instead)
     */
    suspend fun addEmbeddedReply(postId: String, commentId: String, reply: Reply): Result<Unit> {
        return try {
            val commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(commentRef)
                // Note: Comment model no longer has embeddedReply field
                // This is kept for backwards compatibility but won't work with new Comment model
                val newData = mapOf(
                    PostConstants.CommentFields.EMBEDDED_REPLY to reply,
                    PostConstants.CommentFields.REPLY_COUNT to FieldValue.increment(1)
                )
                transaction.update(commentRef, newData)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update embedded reply
     */
    suspend fun updateEmbeddedReply(
        postId: String,
        commentId: String,
        newReplyText: String
    ): Result<Unit> {
        return try {
            postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)
                .update(mapOf("${PostConstants.CommentFields.EMBEDDED_REPLY}.${PostConstants.ReplyFields.TEXT}" to newReplyText))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete embedded reply (DEPRECATED - use NestedCommentDataSource instead)
     */
    suspend fun deleteEmbeddedReply(postId: String, commentId: String): Result<Unit> {
        return try {
            val commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)
            
            firestore.runTransaction { transaction ->
                // Note: Comment model no longer has embeddedReply field
                // This is kept for backwards compatibility
                val newData = mapOf(
                    PostConstants.CommentFields.EMBEDDED_REPLY to null,
                    PostConstants.CommentFields.REPLY_COUNT to FieldValue.increment(-1)
                )
                transaction.update(commentRef, newData)
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
