package com.exa.android.reflekt.loopit.data.feature.main.placement.data.datasource

import com.exa.android.reflekt.loopit.data.feature.main.placement.data.model.PlacementReviewDto
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.model.toDomain
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.model.toDto
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.PlacementReview
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.VerificationStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val collection = firestore.collection("placement_reviews")

    fun getAllReviews(verifiedOnly: Boolean = true): Flow<List<PlacementReview>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        val query: Query = collection.orderBy("year", Query.Direction.DESCENDING)
            .orderBy("visitDate", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val currentUserId = auth.currentUser?.uid
            val reviews = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(PlacementReviewDto::class.java)?.copy(id = doc.id)?.toDomain()
            }?.filter { review ->
                // Show verified reviews OR user's own reviews (regardless of status)
                if (verifiedOnly) {
                    review.verificationStatus == VerificationStatus.SAFE || 
                    (currentUserId != null && review.submittedBy == currentUserId)
                } else {
                    true
                }
            } ?: emptyList()

            trySend(reviews)
        }

        awaitClose { listener.remove() }
    }

    fun getReviewsByCompany(companyName: String): Flow<List<PlacementReview>> = callbackFlow {
        val listener = collection
            .whereEqualTo("companyName", companyName)
            .whereEqualTo("verificationStatus", VerificationStatus.SAFE.name)
            .orderBy("year", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PlacementReviewDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()

                trySend(reviews)
            }

        awaitClose { listener.remove() }
    }

    fun getReviewsByYear(year: Int): Flow<List<PlacementReview>> = callbackFlow {
        val listener = collection
            .whereEqualTo("year", year)
            .whereEqualTo("verificationStatus", VerificationStatus.SAFE.name)
            .orderBy("visitDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PlacementReviewDto::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()

                trySend(reviews)
            }

        awaitClose { listener.remove() }
    }

    suspend fun submitReview(review: PlacementReview): Result<String> {
        return try {
            val dto = review.toDto()
            val docRef = if (review.id.isEmpty()) {
                collection.add(dto).await()
            } else {
                collection.document(review.id).set(dto).await()
                collection.document(review.id)
            }
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVerificationStatus(
        reviewId: String,
        status: VerificationStatus
    ): Result<Unit> {
        return try {
            collection.document(reviewId).update(
                mapOf(
                    "verificationStatus" to status.name,
                    "verifiedAt" to com.google.firebase.Timestamp.now()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewById(reviewId: String): Result<PlacementReview?> {
        return try {
            val doc = collection.document(reviewId).get().await()
            val review = doc.toObject(PlacementReviewDto::class.java)?.copy(id = doc.id)?.toDomain()
            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
