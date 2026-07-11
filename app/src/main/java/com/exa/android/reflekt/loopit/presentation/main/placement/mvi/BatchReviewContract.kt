package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

import com.exa.android.reflekt.loopit.presentation.main.placement.model.BatchReport
import com.exa.android.reflekt.loopit.presentation.main.placement.model.FeedbackSubmission
import com.exa.android.reflekt.loopit.presentation.main.placement.model.SubmitFeedbackForm

// ─── State ────────────────────────────────────────────────────────────────────
data class BatchReviewState(
    val isLoading: Boolean = true,
    val companyId: String = "",

    // Batch reports (admin-added)
    val batchReports: List<BatchReport> = emptyList(),
    val selectedBatchYear: String = "",         // which batch chip is active

    // Approved public feedback for the selected batch
    val approvedFeedback: List<FeedbackSubmission> = emptyList(),

    // Whether the current user has already submitted feedback for this company
    val hasUserSubmitted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUserId: String = "",

    // Submit form
    val showSubmitSheet: Boolean = false,
    val submitForm: SubmitFeedbackForm = SubmitFeedbackForm(),
    val isSubmitting: Boolean = false,
    val submitError: String? = null,

    // Admin
    val isAdmin: Boolean = false,
    val pendingFeedback: List<FeedbackSubmission> = emptyList(),
    val isReviewing: Boolean = false,           // spinner while approving/rejecting

    // Admin: add batch report form
    val showAddBatchSheet: Boolean = false,
    val addBatchForm: AddBatchReportForm = AddBatchReportForm(),
    val isSavingBatch: Boolean = false,

    val error: String? = null
)

data class AddBatchReportForm(
    val batchYear: String = "",
    val shortlistedCount: String = "",
    val selectedCount: String = "",
    val currentQuestion: String = "",
    val questionsAsked: List<String> = emptyList(),
    val otherInfo: String = ""
)

// ─── Events ───────────────────────────────────────────────────────────────────
sealed class BatchReviewEvent {
    data class Load(val companyId: String) : BatchReviewEvent()
    data class SelectBatch(val batchYear: String) : BatchReviewEvent()

    // Submit feedback
    object OpenSubmitSheet : BatchReviewEvent()
    object CloseSubmitSheet : BatchReviewEvent()
    data class BatchYearChanged(val v: String) : BatchReviewEvent()
    data class RatingChanged(val v: Int) : BatchReviewEvent()
    data class ExperienceChanged(val v: String) : BatchReviewEvent()
    data class CurrentQuestionChanged(val v: String) : BatchReviewEvent()
    object AddQuestion : BatchReviewEvent()
    data class RemoveQuestion(val index: Int) : BatchReviewEvent()
    data class OtherInfoChanged(val v: String) : BatchReviewEvent()
    object ConfirmSubmit : BatchReviewEvent()
    object DismissSubmitError : BatchReviewEvent()

    // Admin: approve / reject
    data class ApproveFeedback(val feedbackId: String) : BatchReviewEvent()
    data class RejectFeedback(val feedbackId: String) : BatchReviewEvent()

    // Admin: add batch report
    object OpenAddBatchSheet : BatchReviewEvent()
    object CloseAddBatchSheet : BatchReviewEvent()
    data class AddBatchYearChanged(val v: String) : BatchReviewEvent()
    data class AddShortlistedChanged(val v: String) : BatchReviewEvent()
    data class AddSelectedChanged(val v: String) : BatchReviewEvent()
    data class AddBatchQuestionChanged(val v: String) : BatchReviewEvent()
    object AddBatchQuestion : BatchReviewEvent()
    data class RemoveBatchQuestion(val index: Int) : BatchReviewEvent()
    data class AddBatchOtherInfoChanged(val v: String) : BatchReviewEvent()
    object ConfirmAddBatch : BatchReviewEvent()
}

// ─── Side Effects ─────────────────────────────────────────────────────────────
sealed class BatchReviewSideEffect {
    data class ShowToast(val message: String) : BatchReviewSideEffect()
}
