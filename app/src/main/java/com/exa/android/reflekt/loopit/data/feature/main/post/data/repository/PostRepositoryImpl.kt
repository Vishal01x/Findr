package com.exa.android.reflekt.loopit.data.feature.main.post.data.repository

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.loopit.core.network.Response
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.repository.UserRepository
import com.exa.android.reflekt.loopit.data.feature.main.post.data.repository.FirestoreUtils.generateDocumentId
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Comment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Reply
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.repository.PostRepository
import com.exa.android.reflekt.loopit.fcm.sendComment
import com.exa.android.reflekt.loopit.fcm.sendProjectNotification
import com.exa.android.reflekt.loopit.fcm.sendRequestUpdate
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : PostRepository {

    private companion object {
        const val PROJECTS_COLLECTION = "projects"
        const val COMMENT_COLLECTION = "comments"
        const val METADATA_DOCUMENT = "metadata"
        const val FILTERS_DOCUMENT = "projectFilters"
        const val ROLES_FIELD = "availableRoles"
        const val TAGS_FIELD = "availableTags"
    }


    override suspend fun createPost(post: Post, profile: profileUser): Result<Unit> {
        return try {
            Log.d("Firestore", "Creating project: $post")
            require(auth.currentUser != null) { "User must be authenticated" }
            require(post.title.isNotBlank()) { "Title cannot be empty" }

            val projectMap = post.toMap()


            db.collection(PROJECTS_COLLECTION)
                .document(post.id)
                .set(projectMap)
                .await()

            //sendPostNotification(context, Topics.Post,post, profile)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error creating project", e)
            Result.failure(e)
        }
    }

    fun Post.toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "description" to description,
            "rolesNeeded" to rolesNeeded,
            "tags" to tags,
            "createdAt" to createdAt,
            "createdBy" to createdBy,
            "createdByName" to createdByName,
            "enrolledPersons" to enrolledPersons,
            "requestedPersons" to requestedPersons,
            "type" to type,
            "imageUrls" to imageUrls,
            "links" to links,
            "title_lower" to title.lowercase()
        )
    }

    override fun getPostsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?,
        typeFilter: String?
    ): Flow<Result<List<Post>>> = callbackFlow {
        try {
            var query: Query = db.collection(PROJECTS_COLLECTION)
                .orderBy(Post.FIELD_CREATED_AT, Query.Direction.DESCENDING)

            if (searchQuery.isNotBlank()) {
                val queryText = searchQuery.lowercase()
                query = query
                    .whereGreaterThanOrEqualTo("title_lower", queryText)
                    .whereLessThanOrEqualTo("title_lower", "$queryText\uf8ff")
            }
            if (!typeFilter.isNullOrEmpty()) {
                query = query.whereEqualTo("type", typeFilter)
            }

            if (rolesFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny("rolesNeeded", rolesFilter)
            } else if (tagsFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny("tags", tagsFilter)
            }

            if (showMyProjectsOnly && userId != null) {
                query = query.whereEqualTo("createdBy", userId)
            }

            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error)).isSuccess
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val projects = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Post>()?.copy(id = doc.id)
                    }
                    trySend(Result.success(projects)).isSuccess
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e("Firestore", e.message.toString())
            trySend(Result.failure(e)).isSuccess
            close(e)
        }
    }

    override suspend fun getPostById(projectId: String): Result<Post> {
        return try {
            val document = db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .get()
                .await()

            document.toObject<Post>()?.copy(id = document.id)
                ?.let { Result.success(it) }
                ?: Result.failure(Exception("Project not found"))
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting project by ID", e)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableRoles(): Result<List<String>> {
        return try {
            val doc = db.collection(METADATA_DOCUMENT)
                .document(FILTERS_DOCUMENT)
                .get(Source.SERVER)
                .await()

            val roles = doc.get(ROLES_FIELD) as? List<String> ?: emptyList()
            Result.success(roles)
        } catch (e: Exception) {
             Log.e("Firestore", "Error getting available roles", e)
            Result.failure(e)
        }
    }

    override suspend fun getAvailableTags(): Result<List<String>> {
        return try {
            val doc = db.collection(METADATA_DOCUMENT)
                .document(FILTERS_DOCUMENT)
                .get(Source.SERVER)
                .await()

            val tags = doc.get(TAGS_FIELD) as? List<String> ?: emptyList()
            Result.success(tags)
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting available tags", e)
            Result.failure(e)
        }
    }

    override suspend fun updateProject(project: Post, profile: profileUser): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == project.createdBy) {
                "Only project owner can update"
            }

            val projectMap = project.toMap()


            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .set(projectMap)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error updating project", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
        return try {
            val project = getPostById(projectId).getOrThrow()
            require(auth.currentUser?.uid == project.createdBy) {
                "Only project owner can delete"
            }

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting project", e)
            Result.failure(e)
        }
    }

    override suspend fun addNewRole(role: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }
            require(role.isNotBlank()) { "Role cannot be empty" }

            // Create or update the document
            db.collection(METADATA_DOCUMENT)
                .document(FILTERS_DOCUMENT)
                .set(
                    mapOf(ROLES_FIELD to FieldValue.arrayUnion(role)),
                    SetOptions.merge() // This merges with existing document if it exists
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding role", e)
            Result.failure(e)
        }
    }

    override suspend fun addNewTag(tag: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }
            require(tag.isNotBlank()) { "Tag cannot be empty" }

            // Add to metadata document
            db.collection(METADATA_DOCUMENT)
                .document(FILTERS_DOCUMENT)
                .set(
                    mapOf(TAGS_FIELD to FieldValue.arrayUnion(tag)),
                    SetOptions.merge() // This merges with existing document if it exists
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error adding tag", e)
            Result.failure(e)
        }
    }


    override suspend fun enrollInProject(project: Post, userId: String, userName: String, imageUrl : String?): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == userId) { "User must be authenticated" }

            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .get()
                .await() // Verify project exists

            // Second operation - the actual enrollment
            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .update(
                    mapOf("requestedPersons.$userId" to userName)
                )
                .await()
            sendProjectNotification(
                context,
                userRepository.getUserFcm(project.createdBy),
                "$userName is requested to enroll in your project", // : ${project.title}",
                project.title,
                project.id,
                userId,
                imageUrl
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error enrolling in project", e)
            Result.failure(e)
        }
    }

    override suspend fun withdrawFromProject(projectId: String, userId: String): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == userId) { "User must be authenticated" }

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .get()
                .await()

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .update(
                    mapOf(
                        "requestedPersons.$userId" to FieldValue.delete()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error withdrawing from project", e)
            Result.failure(e)
        }
    }

    override suspend fun acceptJoinRequest(
        project : Post,
        userId: String,
        userName: String,
        profileUser: profileUser
    ): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            val updates = mapOf(
                "enrolledPersons.$userId" to userName,
                "requestedPersons.$userId" to FieldValue.delete()
            )

            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .update(updates)
                .await()

            userRepository.getUserFcm(userId)
                .let {
                    Log.d("FCM", "project request to - $it")
                    sendRequestUpdate(context, it,project,profileUser, "accepted")
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error accepting join request", e)
            Result.failure(e)
        }
    }


    override suspend fun rejectJoinRequest(project : Post, userId: String, value: profileUser): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .update("requestedPersons.$userId", FieldValue.delete())
                .await()
            userRepository.getUserFcm(userId)
                ?.let {fcm->
            sendRequestUpdate(context,fcm,project,value, "rejected")
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error rejecting join request", e)
            Result.failure(e)
        }
    }


    override fun getProjectUpdates(projectId: String): Flow<Result<Post>> = callbackFlow {
        val listener = db.collection(PROJECTS_COLLECTION)
            .document(projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                snapshot?.toObject<Post>()?.let { project ->
                    trySend(Result.success(project.copy(id = snapshot.id)))
                } ?: trySend(Result.failure(Exception("Project not found")))
            }

        awaitClose { listener.remove() }
    }

    override suspend fun toggleLike(postId: String, userId: String): Result<Unit> {
        return try {
            val projectRef = db.collection(PROJECTS_COLLECTION).document(postId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(projectRef)
                val currentLikes = snapshot.get("likes") as? List<String> ?: emptyList()
                val newLikes = if (currentLikes.contains(userId)) {
                    currentLikes - userId
                } else {
                    currentLikes + userId
                }
                transaction.update(projectRef, "likes", newLikes)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Comments - stored as subcollection under posts/{postId}/comments
    override fun getComments(
        postId: String,
        lastLikes: Long?,
        lastTimestamp: Timestamp?,
        limit: Long
    ): Flow<Response<List<Comment>>> =
        callbackFlow {
            trySend(Response.Loading)

            val baseQuery = db.collection(PROJECTS_COLLECTION)
                .document(postId)
                .collection(COMMENT_COLLECTION)
                .orderBy("likesCount", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.DESCENDING)

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
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Comment>()?.copy(id = doc.id) }
                    trySend(Response.Success(list))
                    Log.d("Post Issues", "Adding comment: $list")
                }
            }



            awaitClose { registration.remove() }
        }

    override suspend fun addComment(
        project: Post,
        profile: profileUser,
        comment: Comment
    ): Result<Unit> {
        return try {
            // Generate a comment ID if empty and save it inside the comment
            val commentId = comment.id.ifEmpty {
                generateDocumentId(PROJECTS_COLLECTION, COMMENT_COLLECTION)
            }

            // Assign the copy with ID back to a variable
            val commentWithId = comment.copy(id = commentId)

            val commentRef = db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .collection(COMMENT_COLLECTION)
                .document(commentId) // use the correct ID here

            Log.d("Post Firestore", "Adding comment: $commentWithId")

            // Save comment
            commentRef.set(commentWithId).await()

            // Increment comment count atomically
            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .update("commentCount", FieldValue.increment(1))
                .await()

            // Send notification if needed
            userRepository.getUserFcm(project.createdBy)?.let { fcm ->
                sendComment(context, fcm, project, profile, comment.text)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // For "single reply per comment" we embed the reply into the comment document (transactionally)
    override suspend fun addEmbeddedReply(postId: String, commentId: String, reply: Reply): Result<Unit> = try {
        val replyId = reply.replyId.ifEmpty { generateDocumentId(COMMENT_COLLECTION, "replies") }
        reply.copy(replyId = replyId)

        val commentRef = db.collection(PROJECTS_COLLECTION).document(postId).collection(
            COMMENT_COLLECTION).document(commentId)
        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val existing = snapshot.toObject(Comment::class.java)
            if (existing?.embeddedReply != null) {
                throw IllegalStateException("A reply already exists for this comment")
            }
            val newData = mapOf(
                "embeddedReply" to reply,
                "replyCount" to FieldValue.increment(1)
            )
            transaction.update(commentRef, newData)
        }.await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }


    // In future, if enabling nested replies: use replies subcollection under comment
    suspend fun addReplyDocument(postId: String, commentId: String, reply: Reply): Result<Unit> = try {
        val repliesColl = db.collection(PROJECTS_COLLECTION).document(postId)
            .collection(COMMENT_COLLECTION).document(commentId)
            .collection("replies")
        repliesColl.document(reply.replyId.ifEmpty { repliesColl.document().id }).set(reply).await()
// increment replyCount on comment
        db.collection(PROJECTS_COLLECTION).document(postId)
            .collection("comments").document(commentId)
            .update("replyCount", FieldValue.increment(1)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateComment(postId: String, commentId: String, newText: String): Result<Unit> {
        return try {
            // Make sure commentId exists
            if (commentId.isEmpty()) {
                return Result.failure(IllegalArgumentException("Comment ID cannot be empty"))
            }

            val commentRef = db.collection(PROJECTS_COLLECTION)
                .document(postId)
                .collection(COMMENT_COLLECTION)
                .document(commentId)

            commentRef.update(
                mapOf(
                    "text" to newText,
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateEmbeddedReply(
        postId: String,
        commentId: String,
        newReplyText : String
    ): Result<Unit> {
        return try {

            val commentRef = db.collection(PROJECTS_COLLECTION)
                .document(postId)
                .collection(COMMENT_COLLECTION)
                .document(commentId)

            commentRef.update(
                mapOf(
                    "embeddedReply.text" to newReplyText
                )
            ).await()


            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteComment(postId: String, commentId: String): Result<Unit> {
        return try {
            val commentRef = db.collection(PROJECTS_COLLECTION)
                .document(postId)
                .collection(COMMENT_COLLECTION)
                .document(commentId)

            // Delete the comment
            commentRef.delete().await()

            // Optionally, decrement the comment count atomically
            db.collection(PROJECTS_COLLECTION)
                .document(postId)
                .update("commentCount", FieldValue.increment(-1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun deleteEmbeddedReply(
        postId: String,
        commentId: String
    ): Result<Unit> = try {
        val commentRef = db.collection(PROJECTS_COLLECTION)
            .document(postId)
            .collection(COMMENT_COLLECTION)
            .document(commentId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val existing = snapshot.toObject(Comment::class.java)

            if (existing?.embeddedReply == null) {
                throw IllegalStateException("No reply exists for this comment")
            }

            val newData = mapOf(
                "embeddedReply" to null,
                "replyCount" to com.google.firebase.firestore.FieldValue.increment(-1)
            )

            transaction.update(commentRef, newData)
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }


}