package com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.repository.PlacementRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.PlacementReview
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.VerificationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class PlacementState(
    val reviews: List<PlacementReview> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedYear: Int? = null,
    val selectedCompany: String? = null,
    val groupedByYear: Map<Int, List<PlacementReview>> = emptyMap(),
    val expandedYears: Set<Int> = emptySet()
)

sealed class PlacementEvent {
    object LoadReviews : PlacementEvent()
    data class FilterByYear(val year: Int?) : PlacementEvent()
    data class FilterByCompany(val companyName: String?) : PlacementEvent()
    data class ToggleYearExpansion(val year: Int) : PlacementEvent()
    data class SubmitReview(val review: PlacementReview) : PlacementEvent()
    data class UpdateVerificationStatus(val reviewId: String, val status: VerificationStatus) : PlacementEvent()
}

@HiltViewModel
class PlacementViewModel @Inject constructor(
    private val repository: PlacementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementState())
    val state: StateFlow<PlacementState> = _state.asStateFlow()

    private val _submitState = mutableStateOf<SubmitState>(SubmitState.Idle)
    val submitState: State<SubmitState> = _submitState

    init {
        loadReviews()
    }

    fun onEvent(event: PlacementEvent) {
        when (event) {
            is PlacementEvent.LoadReviews -> loadReviews()
            is PlacementEvent.FilterByYear -> filterByYear(event.year)
            is PlacementEvent.FilterByCompany -> filterByCompany(event.companyName)
            is PlacementEvent.ToggleYearExpansion -> toggleYearExpansion(event.year)
            is PlacementEvent.SubmitReview -> submitReview(event.review)
            is PlacementEvent.UpdateVerificationStatus -> updateVerificationStatus(event.reviewId, event.status)
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            Timber.tag("PlacementVM:Load").d("Loading placement reviews (verified only)")
            try {
                repository.getAllReviews(verifiedOnly = true).collect { reviews ->
                    val grouped = reviews.groupBy { it.year }
                    Timber.tag("PlacementVM:Load").i("Loaded ${reviews.size} reviews across ${grouped.size} years")
                    _state.value = _state.value.copy(
                        reviews = reviews,
                        groupedByYear = grouped,
                        isLoading = false,
                        expandedYears = grouped.keys.take(1).toSet() // Expand first year by default
                    )
                }
            } catch (e: Exception) {
                Timber.tag("PlacementVM:Error").e(e, "Failed to load reviews")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load reviews"
                )
            }
        }
    }

    private fun filterByYear(year: Int?) {
        if (year == null) {
            Timber.tag("PlacementVM:Filter").d("Clearing year filter")
            _state.value = _state.value.copy(selectedYear = null)
            loadReviews()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, selectedYear = year)
            Timber.tag("PlacementVM:Filter").d("Filtering reviews by year: $year")
            try {
                repository.getReviewsByYear(year).collect { reviews ->
                    val grouped = reviews.groupBy { it.year }
                    Timber.tag("PlacementVM:Filter").i("Found ${reviews.size} reviews for year $year")
                    _state.value = _state.value.copy(
                        reviews = reviews,
                        groupedByYear = grouped,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.tag("PlacementVM:Error").e(e, "Failed to filter reviews by year: $year")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to filter reviews"
                )
            }
        }
    }

    private fun filterByCompany(companyName: String?) {
        if (companyName == null) {
            Timber.tag("PlacementVM:Filter").d("Clearing company filter")
            _state.value = _state.value.copy(selectedCompany = null)
            loadReviews()
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, selectedCompany = companyName)
            Timber.tag("PlacementVM:Filter").d("Filtering reviews by company: $companyName")
            try {
                repository.getReviewsByCompany(companyName).collect { reviews ->
                    val grouped = reviews.groupBy { it.year }
                    Timber.tag("PlacementVM:Filter").i("Found ${reviews.size} reviews for company: $companyName")
                    _state.value = _state.value.copy(
                        reviews = reviews,
                        groupedByYear = grouped,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.tag("PlacementVM:Error").e(e, "Failed to filter reviews by company: $companyName")
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to filter reviews"
                )
            }
        }
    }

    private fun toggleYearExpansion(year: Int) {
        val currentExpanded = _state.value.expandedYears
        val isExpanding = !currentExpanded.contains(year)
        Timber.tag("PlacementVM:UI").d("${if (isExpanding) "Expanding" else "Collapsing"} year: $year")
        _state.value = _state.value.copy(
            expandedYears = if (currentExpanded.contains(year)) {
                currentExpanded - year
            } else {
                currentExpanded + year
            }
        )
    }

    private fun submitReview(review: PlacementReview) {
        viewModelScope.launch {
            Timber.tag("PlacementVM:Submit").d("Submitting review for company: ${review.companyName}, year: ${review.year}")
            _submitState.value = SubmitState.Loading
            val result = repository.submitReview(review)
            _submitState.value = if (result.isSuccess) {
                val reviewId = result.getOrNull() ?: ""
                Timber.tag("PlacementVM:Submit").i("Review submitted successfully with ID: $reviewId")
                SubmitState.Success(reviewId)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to submit review"
                Timber.tag("PlacementVM:Error").e(result.exceptionOrNull(), "Failed to submit review for ${review.companyName}")
                SubmitState.Error(error)
            }
        }
    }

    private fun updateVerificationStatus(reviewId: String, status: VerificationStatus) {
        viewModelScope.launch {
            Timber.tag("PlacementVM:Verify").d("Updating verification status for review $reviewId to ${status.name}")
            val result = repository.updateVerificationStatus(reviewId, status)
            if (result.isSuccess) {
                Timber.tag("PlacementVM:Verify").i("Verification status updated successfully")
            } else {
                Timber.tag("PlacementVM:Error").e(result.exceptionOrNull(), "Failed to update verification status")
            }
        }
    }

    fun resetSubmitState() {
        _submitState.value = SubmitState.Idle
    }
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    data class Success(val reviewId: String) : SubmitState()
    data class Error(val message: String) : SubmitState()
}
