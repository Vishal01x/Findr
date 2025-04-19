package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.util.Log
import com.exa.android.reflekt.loopit.util.model.Project
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
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
    private val auth: FirebaseAuth
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
            require(auth.currentUser != null) { "User must be authenticated" }
            require(project.title.isNotBlank()) { "Title cannot be empty" }

            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .set(project)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Firestore", "Error creating project", e)
            Result.failure(e)
        }
    }

    override fun getProjectsStream(
        searchQuery: String,
        rolesFilter: List<String>,
        tagsFilter: List<String>,
        showMyProjectsOnly: Boolean,
        userId: String?
    ): Flow<Result<List<Project>>> = flow {
        try {
            emit(Result.success(emptyList())) // Initial loading state

            var query = db.collection(PROJECTS_COLLECTION)
                .orderBy(Project.FIELD_CREATED_AT, Query.Direction.DESCENDING)

            if (searchQuery.isNotBlank()) {
                query = query
                    .whereGreaterThanOrEqualTo(Project.FIELD_TITLE, searchQuery)
                    .whereLessThanOrEqualTo(Project.FIELD_TITLE, "$searchQuery\uf8ff")
            }

            if (rolesFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny("rolesNeeded", rolesFilter)
            }else if (tagsFilter.isNotEmpty()) {
                query = query.whereArrayContainsAny("tags", tagsFilter)
            }
            if (showMyProjectsOnly && userId != null) {
                query = query.whereEqualTo("createdBy", userId)
            }

            val snapshot = query.get().await()
            val projects = snapshot.documents.mapNotNull { doc ->
                doc.toObject<Project>()?.copy(id = doc.id)
            }

            emit(Result.success(projects))
        } catch (e: Exception) {
            Log.e("Firestore", "Error getting projects", e)
            emit(Result.failure(e))
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

            db.collection(PROJECTS_COLLECTION)
                .document(project.id)
                .set(project, SetOptions.merge())
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
    override suspend fun enrollInProject(projectId: String, userId: String, userName: String): Result<Unit> {
        return try {
            require(auth.currentUser?.uid == userId) { "User must be authenticated" }

            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .get()
                .await() // Verify project exists

            // Second operation - the actual enrollment
            db.collection(PROJECTS_COLLECTION)
                .document(projectId)
                .update(
                    mapOf("requestedPersons.$userId" to userName)
                )
                .await()

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

}