package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.BatchFeedbackRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementAdminRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.model.BatchReport
import com.exa.android.reflekt.loopit.presentation.main.placement.model.FeedbackStatus
import com.exa.android.reflekt.loopit.presentation.main.placement.model.FeedbackSubmission
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.AddBatchReportForm
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.BatchReviewEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.BatchReviewSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.BatchReviewState
import com.exa.android.reflekt.loopit.presentation.main.placement.model.SubmitFeedbackForm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchReviewViewModel @Inject constructor(
    private val repo: BatchFeedbackRepository,
    private val adminRepo: PlacementAdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BatchReviewState())
    val state: StateFlow<BatchReviewState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BatchReviewSideEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<BatchReviewSideEffect> = _effect.asSharedFlow()

    private var batchJob: Job? = null
    private var feedbackJob: Job? = null
    private var pendingJob: Job? = null

    fun onEvent(event: BatchReviewEvent) {
        when (event) {
            is BatchReviewEvent.Load              -> load(event.companyId)
            is BatchReviewEvent.SelectBatch       -> selectBatch(event.batchYear)

            // Submit sheet
            BatchReviewEvent.OpenSubmitSheet      -> _state.update { it.copy(showSubmitSheet = true, submitError = null) }
            BatchReviewEvent.CloseSubmitSheet     -> _state.update { it.copy(showSubmitSheet = false) }
            is BatchReviewEvent.BatchYearChanged  -> _state.update { it.copy(submitForm = it.submitForm.copy(batchYear = event.v)) }
            is BatchReviewEvent.RatingChanged     -> _state.update { it.copy(submitForm = it.submitForm.copy(rating = event.v)) }
            is BatchReviewEvent.ExperienceChanged -> _state.update { it.copy(submitForm = it.submitForm.copy(experience = event.v)) }
            is BatchReviewEvent.CurrentQuestionChanged -> _state.update { it.copy(submitForm = it.submitForm.copy(currentQuestion = event.v)) }
            BatchReviewEvent.AddQuestion          -> addQuestion()
            is BatchReviewEvent.RemoveQuestion    -> removeQuestion(event.index)
            is BatchReviewEvent.OtherInfoChanged  -> _state.update { it.copy(submitForm = it.submitForm.copy(otherInfo = event.v)) }
            BatchReviewEvent.ConfirmSubmit        -> submitFeedback()
            BatchReviewEvent.DismissSubmitError   -> _state.update { it.copy(submitError = null) }

            // Admin moderation
            is BatchReviewEvent.ApproveFeedback   -> moderateFeedback(event.feedbackId, FeedbackStatus.APPROVED)
            is BatchReviewEvent.RejectFeedback    -> moderateFeedback(event.feedbackId, FeedbackStatus.REJECTED)

            // Admin: add batch
            BatchReviewEvent.OpenAddBatchSheet    -> _state.update { it.copy(showAddBatchSheet = true) }
            BatchReviewEvent.CloseAddBatchSheet   -> _state.update { it.copy(showAddBatchSheet = false, addBatchForm = AddBatchReportForm()) }
            is BatchReviewEvent.AddBatchYearChanged       -> _state.update { it.copy(addBatchForm = it.addBatchForm.copy(batchYear = event.v)) }
            is BatchReviewEvent.AddShortlistedChanged     -> _state.update { it.copy(addBatchForm = it.addBatchForm.copy(shortlistedCount = event.v)) }
            is BatchReviewEvent.AddSelectedChanged        -> _state.update { it.copy(addBatchForm = it.addBatchForm.copy(selectedCount = event.v)) }
            is BatchReviewEvent.AddBatchQuestionChanged   -> _state.update { it.copy(addBatchForm = it.addBatchForm.copy(currentQuestion = event.v)) }
            BatchReviewEvent.AddBatchQuestion             -> addBatchQuestion()
            is BatchReviewEvent.RemoveBatchQuestion       -> removeBatchQuestion(event.index)
            is BatchReviewEvent.AddBatchOtherInfoChanged  -> _state.update { it.copy(addBatchForm = it.addBatchForm.copy(otherInfo = event.v)) }
            BatchReviewEvent.ConfirmAddBatch              -> saveBatchReport()
        }
    }

    // ─── Load ─────────────────────────────────────────────────────────────────

    private fun load(companyId: String) {
        _state.update { it.copy(companyId = companyId, isLoading = true) }

        viewModelScope.launch {
            // Check admin & login status
            val isAdmin = adminRepo.isCurrentUserAdmin()
            val uid = repo.getCurrentUserId()
            val isLoggedIn = uid != null
            val hasSubmitted = if (isLoggedIn) repo.hasUserSubmitted(companyId) else false

            _state.update {
                it.copy(
                    isAdmin = isAdmin,
                    isLoggedIn = isLoggedIn,
                    currentUserId = uid ?: "",
                    hasUserSubmitted = hasSubmitted
                )
            }

            // Prefill submit form batch year with selected batch if set
            val currentBatch = _state.value.selectedBatchYear
            if (currentBatch.isNotBlank()) {
                _state.update { it.copy(submitForm = it.submitForm.copy(batchYear = currentBatch)) }
            }

            // Admin: stream pending submissions
            if (isAdmin) {
                pendingJob?.cancel()
                pendingJob = viewModelScope.launch {
                    repo.getPendingFeedback(companyId).collect { pending ->
                        _state.update { it.copy(pendingFeedback = pending) }
                    }
                }
            }
        }

        // Stream batch reports
        batchJob?.cancel()
        batchJob = viewModelScope.launch {
            repo.getBatchReports(companyId).collect { reports ->
                val currentSelected = _state.value.selectedBatchYear
                val newSelected = if (currentSelected.isBlank() && reports.isNotEmpty())
                    reports.first().batchYear else currentSelected
                _state.update {
                    it.copy(
                        isLoading = false,
                        batchReports = reports,
                        selectedBatchYear = newSelected
                    )
                }
                // Auto-load feedback for the first batch
                if (newSelected != currentSelected && newSelected.isNotBlank()) {
                    loadFeedbackForBatch(companyId, newSelected)
                }
                if (reports.isEmpty()) _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun selectBatch(batchYear: String) {
        _state.update {
            it.copy(
                selectedBatchYear = batchYear,
                submitForm = it.submitForm.copy(batchYear = batchYear)
            )
        }
        val companyId = _state.value.companyId
        if (companyId.isNotBlank()) loadFeedbackForBatch(companyId, batchYear)
    }

    private fun loadFeedbackForBatch(companyId: String, batchYear: String) {
        feedbackJob?.cancel()
        feedbackJob = viewModelScope.launch {
            repo.getApprovedFeedback(companyId, batchYear).collect { feedback ->
                _state.update { it.copy(approvedFeedback = feedback) }
            }
        }
    }

    // ─── Submit Feedback ──────────────────────────────────────────────────────

    private fun addQuestion() {
        val q = _state.value.submitForm.currentQuestion.trim()
        if (q.isBlank()) return
        _state.update {
            it.copy(submitForm = it.submitForm.copy(
                questionsEncountered = it.submitForm.questionsEncountered + q,
                currentQuestion = ""
            ))
        }
    }

    private fun removeQuestion(index: Int) {
        _state.update {
            it.copy(submitForm = it.submitForm.copy(
                questionsEncountered = it.submitForm.questionsEncountered.toMutableList().also { l -> l.removeAt(index) }
            ))
        }
    }

    private fun submitFeedback() {
        val form = _state.value.submitForm
        if (form.batchYear.isBlank()) {
            _state.update { it.copy(submitError = "Please enter your batch year (e.g. 2025)") }
            return
        }
        if (form.rating == 0) {
            _state.update { it.copy(submitError = "Please select a rating (1–5 stars)") }
            return
        }
        if (form.experience.isBlank()) {
            _state.update { it.copy(submitError = "Please write your experience") }
            return
        }
        _state.update { it.copy(isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val submission = FeedbackSubmission(
                batchYear = form.batchYear,
                rating = form.rating,
                experience = form.experience,
                questionsEncountered = form.questionsEncountered,
                otherInfo = form.otherInfo
            )
            val result = repo.submitFeedback(_state.value.companyId, submission)
            if (result.isSuccess) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        showSubmitSheet = false,
                        hasUserSubmitted = true,
                        submitForm = SubmitFeedbackForm()
                    )
                }
                _effect.emit(BatchReviewSideEffect.ShowToast("✅ Feedback submitted! It will appear after admin review."))
            } else {
                _state.update {
                    it.copy(isSubmitting = false, submitError = result.exceptionOrNull()?.message ?: "Submission failed")
                }
            }
        }
    }

    // ─── Admin Moderation ─────────────────────────────────────────────────────

    private fun moderateFeedback(feedbackId: String, newStatus: FeedbackStatus) {
        _state.update { it.copy(isReviewing = true) }
        viewModelScope.launch {
            val result = repo.updateFeedbackStatus(_state.value.companyId, feedbackId, newStatus)
            _state.update { it.copy(isReviewing = false) }
            val msg = if (newStatus == FeedbackStatus.APPROVED) "✅ Feedback approved!" else "❌ Feedback rejected."
            _effect.emit(BatchReviewSideEffect.ShowToast(if (result.isSuccess) msg else "Failed: ${result.exceptionOrNull()?.message}"))
        }
    }

    // ─── Admin: Add Batch Report ──────────────────────────────────────────────

    private fun addBatchQuestion() {
        val q = _state.value.addBatchForm.currentQuestion.trim()
        if (q.isBlank()) return
        _state.update {
            it.copy(addBatchForm = it.addBatchForm.copy(
                questionsAsked = it.addBatchForm.questionsAsked + q,
                currentQuestion = ""
            ))
        }
    }

    private fun removeBatchQuestion(index: Int) {
        _state.update {
            it.copy(addBatchForm = it.addBatchForm.copy(
                questionsAsked = it.addBatchForm.questionsAsked.toMutableList().also { l -> l.removeAt(index) }
            ))
        }
    }

    private fun saveBatchReport() {
        val form = _state.value.addBatchForm
        if (form.batchYear.isBlank()) {
            _effect.tryEmit(BatchReviewSideEffect.ShowToast("Please enter a batch year"))
            return
        }
        _state.update { it.copy(isSavingBatch = true) }
        viewModelScope.launch {
            val report = BatchReport(
                batchYear        = form.batchYear.trim(),
                shortlistedCount = form.shortlistedCount.toIntOrNull() ?: 0,
                selectedCount    = form.selectedCount.toIntOrNull() ?: 0,
                questionsAsked   = form.questionsAsked,
                otherInfo        = form.otherInfo
            )
            val result = repo.addBatchReport(_state.value.companyId, report)
            _state.update { it.copy(isSavingBatch = false, showAddBatchSheet = result.isFailure, addBatchForm = if (result.isSuccess) AddBatchReportForm() else it.addBatchForm) }
            val msg = if (result.isSuccess) "✅ Batch report saved!" else "Failed: ${result.exceptionOrNull()?.message}"
            _effect.emit(BatchReviewSideEffect.ShowToast(msg))
        }
    }

    override fun onCleared() {
        super.onCleared()
        batchJob?.cancel()
        feedbackJob?.cancel()
        pendingJob?.cancel()
    }
}
