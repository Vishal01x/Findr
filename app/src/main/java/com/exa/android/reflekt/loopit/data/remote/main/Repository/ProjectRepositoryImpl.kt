package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.loopit.util.model.Comment
import com.exa.android.reflekt.loopit.util.model.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ProjectRepository {

    private companion object {
        const val PROJECTS_COLLECTION = "projects"
        const val METADATA_DOCUMENT = "metadata"
        const val FILTERS_DOCUMENT = "projectFilters"
        const val ROLES_FIELD = "availableRoles"
        const val TAGS_FIELD = "availableTags"
    }

    override suspend fun createProject(project: Project): Result<Unit> {
        return try {
            Log.d("Firestore", "Creating project: $project")
            require(auth.currentUser != null) { "User must be authenticated" }
            require(project.title.isNotBlank()) { "Title cannot be empty" }

            val projectMap = project.toMap()


            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .set(projectMap)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error creating project", e)
            Result.failure(e)
        }
    }
    fun Project.toMap(): Map<String, Any?> {
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




    override fun getProjectsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?,
        typeFilter: String?
    ): Flow<Result<List<Project>>> = callbackFlow {
        try {
            var query: Query = db.collection(PROJECTS_COLLECTION)
                .orderBy(Project.FIELD_CREATED_AT, Query.Direction.DESCENDING)

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
                        doc.toObject<Project>()?.copy(id = doc.id)
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

    override suspend fun getProjectById(projectId: String): Result<Project> {
        return try {
            val document = db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .get()
                .await()

            document.toObject<Project>()?.copy(id = document.id)
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

    override suspend fun updateProject(project: Project): Result<Unit> {
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
            val project = getProjectById(projectId).getOrThrow()
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


    override suspend fun enrollInProject(project: Project, userId: String, userName: String, imageUrl : String?): Result<Unit> {
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
            sendPushNotification(
                context,
                userRepository.getUserFcm(project.createdBy),
                "$userName is requested to enroll in your project : ${project.title}",
                project.title,
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
    override suspend fun acceptJoinRequest(projectId: String, userId: String, userName: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            val updates = mapOf(
                "enrolledPersons.$userId" to userName,
                "requestedPersons.$userId" to FieldValue.delete()
            )

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error accepting join request", e)
            Result.failure(e)
        }
    }
    override suspend fun rejectJoinRequest(projectId: String, userId: String): Result<Unit> {
        return try {
            require(auth.currentUser != null) { "User must be authenticated" }

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .update("requestedPersons.$userId", FieldValue.delete())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error rejecting join request", e)
            Result.failure(e)
        }
    }
    override fun getProjectUpdates(projectId: String): Flow<Result<Project>> = callbackFlow {
        val listener = db.collection(PROJECTS_COLLECTION)
            .document(projectId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                snapshot?.toObject<Project>()?.let { project ->
                    trySend(Result.success(project.copy(id = snapshot.id)))
                } ?: trySend(Result.failure(Exception("Project not found")))
            }

        awaitClose { listener.remove() }
    }
    override suspend fun addComment(projectId: String, comment: Comment): Result<Unit> {
        return try {
            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .collection("comments")
                .document(comment.id)
                .set(comment)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun toggleLike(projectId: String, userId: String): Result<Unit> {
        return try {
            val projectRef = db.collection(PROJECTS_COLLECTION).document(projectId)
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

    override suspend fun updateComment(projectId: String, commentId: String, newText: String): Result<Unit> {
        return try {
            val projectRef = db.collection(PROJECTS_COLLECTION).document(projectId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(projectRef)
                val project = snapshot.toObject(Project::class.java)
                    ?: throw Exception("Project not found")
                val updatedComments = project.comments.map { comment ->
                    if (comment.id == commentId) comment.copy(text = newText) else comment
                }
                transaction.update(projectRef, "comments", updatedComments)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteComment(projectId: String, commentId: String): Result<Unit> {
        return try {
            val projectRef = db.collection(PROJECTS_COLLECTION).document(projectId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(projectRef)
                val project = snapshot.toObject(Project::class.java)
                    ?: throw Exception("Project not found")
                val updatedComments = project.comments.filter { it.id != commentId }
                transaction.update(projectRef, "comments", updatedComments)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}