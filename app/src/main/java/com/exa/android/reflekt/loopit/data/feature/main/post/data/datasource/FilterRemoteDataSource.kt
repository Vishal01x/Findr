package com.exa.android.reflekt.loopit.data.feature.main.post.data.datasource

import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostConstants
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source for filter-related operations (roles and tags)
 * Handles metadata storage in Firestore
 */
@Singleton
class FilterRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val metadataDoc get() = firestore
        .collection(PostConstants.Collections.METADATA)
        .document(PostConstants.Documents.FILTERS)
    
    /**
     * Get available roles
     */
    suspend fun getAvailableRoles(): Result<List<String>> {
        return try {
            val doc = metadataDoc
                .get(Source.SERVER)
                .await()
            
            val roles = doc.get(PostConstants.FilterFields.AVAILABLE_ROLES) as? List<String> ?: emptyList()
            Result.success(roles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available tags
     */
    suspend fun getAvailableTags(): Result<List<String>> {
        return try {
            val doc = metadataDoc
                .get(Source.SERVER)
                .await()
            
            val tags = doc.get(PostConstants.FilterFields.AVAILABLE_TAGS) as? List<String> ?: emptyList()
            Result.success(tags)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add new role
     */
    suspend fun addNewRole(role: String): Result<Unit> {
        return try {
            metadataDoc
                .set(
                    mapOf(PostConstants.FilterFields.AVAILABLE_ROLES to FieldValue.arrayUnion(role)),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add new tag
     */
    suspend fun addNewTag(tag: String): Result<Unit> {
        return try {
            metadataDoc
                .set(
                    mapOf(PostConstants.FilterFields.AVAILABLE_TAGS to FieldValue.arrayUnion(tag)),
                    SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
