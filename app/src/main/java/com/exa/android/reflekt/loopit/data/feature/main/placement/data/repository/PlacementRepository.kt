package com.exa.android.reflekt.loopit.data.feature.main.placement.data.repository

import com.exa.android.reflekt.loopit.data.feature.main.placement.data.datasource.PlacementDataSource
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.PlacementReview
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.VerificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementRepository @Inject constructor(
    private val dataSource: PlacementDataSource
) {
    fun getAllReviews(verifiedOnly: Boolean = true): Flow<List<PlacementReview>> {
        Timber.tag("PlacementRepo").d("Fetching all reviews (verifiedOnly=$verifiedOnly)")
        return dataSource.getAllReviews(verifiedOnly)
            .onStart { Timber.tag("PlacementRepo:Stream").d("Started listening to all reviews") }
            .catch { e ->
                Timber.tag("PlacementRepo:Error").e(e, "Error in getAllReviews flow")
                throw e
            }
    }

    fun getReviewsByCompany(companyName: String): Flow<List<PlacementReview>> {
        Timber.tag("PlacementRepo").d("Fetching reviews for company: $companyName")
        return dataSource.getReviewsByCompany(companyName)
            .onStart { Timber.tag("PlacementRepo:Stream").d("Started listening to reviews for $companyName") }
            .catch { e ->
                Timber.tag("PlacementRepo:Error").e(e, "Error in getReviewsByCompany flow for $companyName")
                throw e
            }
    }

    fun getReviewsByYear(year: Int): Flow<List<PlacementReview>> {
        Timber.tag("PlacementRepo").d("Fetching reviews for year: $year")
        return dataSource.getReviewsByYear(year)
            .onStart { Timber.tag("PlacementRepo:Stream").d("Started listening to reviews for year $year") }
            .catch { e ->
                Timber.tag("PlacementRepo:Error").e(e, "Error in getReviewsByYear flow for $year")
                throw e
            }
    }

    suspend fun submitReview(review: PlacementReview): Result<String> {
        Timber.tag("PlacementRepo").d("Submitting review for ${review.companyName} (${review.year})")
        return dataSource.submitReview(review).also { result ->
            if (result.isSuccess) {
                Timber.tag("PlacementRepo:Success").i("Review submitted: ${result.getOrNull()}")
            } else {
                Timber.tag("PlacementRepo:Error").e(result.exceptionOrNull(), "Failed to submit review")
            }
        }
    }

    suspend fun updateVerificationStatus(
        reviewId: String,
        status: VerificationStatus
    ): Result<Unit> {
        Timber.tag("PlacementRepo").d("Updating verification status: $reviewId -> ${status.name}")
        return dataSource.updateVerificationStatus(reviewId, status).also { result ->
            if (result.isSuccess) {
                Timber.tag("PlacementRepo:Success").i("Verification status updated for $reviewId")
            } else {
                Timber.tag("PlacementRepo:Error").e(result.exceptionOrNull(), "Failed to update verification")
            }
        }
    }

    suspend fun getReviewById(reviewId: String): Result<PlacementReview?> {
        Timber.tag("PlacementRepo").d("Fetching review by ID: $reviewId")
        return dataSource.getReviewById(reviewId).also { result ->
            if (result.isSuccess) {
                Timber.tag("PlacementRepo:Success").d("Review fetched: ${result.getOrNull()?.companyName}")
            } else {
                Timber.tag("PlacementRepo:Error").e(result.exceptionOrNull(), "Failed to fetch review $reviewId")
            }
        }
    }
}
