package com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource

import android.util.Log
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
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
 * Remote data source for nested comment operations  
 * Supports 3-level nesting with likes
 */
@Singleton
class NestedCommentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val postsCollection get() = firestore.collection(PostConstants.Collections.PROJECTS)
    
    companion object {
        private const val TAG = "NestedCommentDataSource"
        private const val REPLIES_COLLECTION = "replies"
    }
    
    /**
     * Generate a unique comment ID
     */
    fun generateCommentId(): String {
        return postsCollection.document().collection(PostConstants.Collections.COMMENTS).document().id
    }
    
    /**
     * Get top-level comments for a post
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
                trySend(Response.Error(error.localizedMessage ?: "Unknown error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                trySend(Response.Success(list))
            }
        }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Get replies for a comment (supports nested replies)
     */
    fun getReplies(
        postId: String,
        rootCommentId : String?,
        parentCommentId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>> = callbackFlow {
        trySend(Response.Loading)
        
        val baseQuery = postsCollection
            .document(postId)
            .collection(PostConstants.Collections.COMMENTS)
            .document(rootCommentId ?: parentCommentId)
            .collection(REPLIES_COLLECTION)
            .whereEqualTo("parentCommentId", parentCommentId)
            .orderBy(PostConstants.CommentFields.LIKES_COUNT, Query.Direction.DESCENDING)
            .orderBy(PostConstants.CommentFields.TIMESTAMP, Query.Direction.DESCENDING)
        
        val query = if (lastLikes != null && lastTimestamp != null) {
            baseQuery.startAfter(lastLikes, lastTimestamp).limit(limit)
        } else {
            baseQuery.limit(limit)
        }
        
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Response.Error(error.localizedMessage ?: "Unknown error"))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Comment::class.java)?.copy(id = doc.id)
                }
                trySend(Response.Success(list))
            }
        }
        
        awaitClose { registration.remove() }
    }
    
    /**
     * Add a top-level comment
     */
    suspend fun addComment(postId: String, comment: Comment): Result<Unit> {
        return try {
            val commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(comment.id)
            
            // Set likes count from likes array
            val commentData = comment.copy(likesCount = comment.likes.size.toLong())
            commentRef.set(commentData).await()
            
            // Increment post comment count
            postsCollection
                .document(postId)
                .update(PostConstants.PostFields.COMMENT_COUNT, FieldValue.increment(1))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding comment", e)
            Result.failure(e)
        }
    }
    
    /**
     * Add a reply to a comment (supports up to 3 levels of nesting)
     * All replies are stored under the root (top-level) comment's replies subcollection
     */
    suspend fun addReply(
        postId: String,
        parentCommentId: String,
        reply: Comment
    ): Result<Unit> {
        return try {
            val rootRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(reply.rootCommentId ?: parentCommentId)

            val replyRef = rootRef
                .collection(REPLIES_COLLECTION)
                .document(reply.id)

            // Set likes count from likes array
            val replyData = reply.copy(
                likesCount = reply.likes.size.toLong(),
                parentCommentId = parentCommentId
            )

            // Save reply
            replyRef
                .set(replyData)
                .await()

            // Increment parent reply count
            if(reply.nestingLevel == 1) {
                rootRef
                    .update(
                        PostConstants.CommentFields.REPLY_COUNT,
                        FieldValue.increment(1)
                    )
                    .await()
            }else{
                rootRef.collection(REPLIES_COLLECTION)
                    .document(parentCommentId)
                    .update(
                        PostConstants.CommentFields.REPLY_COUNT,
                        FieldValue.increment(1)
                    )
                    .await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding reply", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle like on a comment
     */
    suspend fun toggleCommentLike(
        postId: String,
        commentId: String,
        replyId : String?,
        userId: String,
        isCurrentlyLiked : Boolean,
    ): Result<Unit> {
        return try {
            var commentRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(commentId)

            replyId?.let {
                commentRef = commentRef.collection(PostConstants.Collections.REPLIES)
                    .document(replyId)
            }

//            firestore.runTransaction { transaction ->
//                val snapshot = transaction.get(commentRef)
//                val currentLikes = snapshot.get(PostConstants.CommentFields.LIKES) as? List<String> ?: emptyList()
//
//                val newLikes = if (currentLikes.contains(userId)) {
//                    currentLikes - userId
//                } else {
//                    currentLikes + userId
//                }
//
//                transaction.update(
//                    commentRef,
//                    mapOf(
//                        PostConstants.CommentFields.LIKES to newLikes,
//                        PostConstants.CommentFields.LIKES_COUNT to newLikes.size.toLong()
//                    )
//                )
//            }.await()

            val update = if (isCurrentlyLiked) {
                mapOf(
                    PostConstants.CommentFields.LIKES to FieldValue.arrayRemove(userId),
                    PostConstants.CommentFields.LIKES_COUNT to FieldValue.increment(-1)
                )
            } else {
                mapOf(
                    PostConstants.CommentFields.LIKES to FieldValue.arrayUnion(userId),
                    PostConstants.CommentFields.LIKES_COUNT to FieldValue.increment(1)
                )
            }

            commentRef.update(update).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling comment like", e)
            Result.failure(e)
        }
    }
    
    /**
     * Toggle like on a reply (handles nested replies)
     */
    suspend fun toggleReplyLike(
        postId: String,
        parentCommentId: String,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val replyRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(parentCommentId)
                .collection(REPLIES_COLLECTION)
                .document(replyId)
            
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(replyRef)
                val currentLikes = snapshot.get(PostConstants.CommentFields.LIKES) as? List<String> ?: emptyList()
                
                val newLikes = if (currentLikes.contains(userId)) {
                    currentLikes - userId
                } else {
                    currentLikes + userId
                }
                
                transaction.update(
                    replyRef,
                    mapOf(
                        PostConstants.CommentFields.LIKES to newLikes,
                        PostConstants.CommentFields.LIKES_COUNT to newLikes.size.toLong()
                    )
                )
            }.await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling reply like", e)
            Result.failure(e)
        }
    }
    
    /**
     * Delete a reply
     */
    suspend fun deleteReply(
        postId: String,
        rootCommentId: String?,
        parentCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {

            if(rootCommentId == null)return Result.failure(Exception("No path to delete"))

            val replyRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(rootCommentId)
                .collection(REPLIES_COLLECTION)
                .document(replyId)
            
            // Validate ownership
            val replyDoc = replyRef.get().await()
            val senderId = replyDoc.getString(PostConstants.CommentFields.SENDER_ID)
            
            if (senderId != userId) {
                return Result.failure(IllegalStateException("Only reply owner can delete"))
            }
            
            replyRef.delete().await()

            if(rootCommentId == parentCommentId){ // for the first reply need to reduce count from root
                // Decrement parent reply count
                postsCollection
                    .document(postId)
                    .collection(PostConstants.Collections.COMMENTS)
                    .document(rootCommentId)
                    .update(PostConstants.CommentFields.REPLY_COUNT, FieldValue.increment(-1))
                    .await()
            }else {
                // Decrement parent reply count
                postsCollection
                    .document(postId)
                    .collection(PostConstants.Collections.COMMENTS)
                    .document(rootCommentId)
                    .collection(REPLIES_COLLECTION)
                    .document(parentCommentId ?: rootCommentId)
                    .update(PostConstants.CommentFields.REPLY_COUNT, FieldValue.increment(-1))
                    .await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting reply", e)
            Result.failure(e)
        }
    }

    /**
     * Edit a reply
     */
    suspend fun updateReply(
        newText : String,
        postId: String,
        rootCommentId: String?,
        replyId: String,
        userId: String
    ): Result<Unit> {
        return try {
            if(rootCommentId == null)
                return Result.failure(IllegalStateException("No path to update"))

            val replyRef = postsCollection
                .document(postId)
                .collection(PostConstants.Collections.COMMENTS)
                .document(rootCommentId)
                .collection(REPLIES_COLLECTION)
                .document(replyId)

            // Validate ownership
            val replyDoc = replyRef.get().await()
            val senderId = replyDoc.getString(PostConstants.CommentFields.SENDER_ID)

            if (senderId != userId) {
                return Result.failure(IllegalStateException("Only reply owner can update"))
            }

            replyRef.update(mapOf(PostConstants.ReplyFields.TEXT to newText))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating reply", e)
            Result.failure(e)
        }
    }
}
