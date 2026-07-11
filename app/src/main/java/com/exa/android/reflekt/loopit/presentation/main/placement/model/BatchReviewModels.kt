package com.exa.android.reflekt.loopit.presentation.main.placement.model

// ─── Batch Report (Admin-added, per batch year) ────────────────────────────────
data class BatchReport(
    val id: String = "",                        // Firestore doc ID (= batchYear string)
    val batchYear: String = "",                 // e.g. "2025", "2024"
    val shortlistedCount: Int = 0,              // # students shortlisted
    val selectedCount: Int = 0,                 // # students finally selected
    val questionsAsked: List<String> = emptyList(), // questions seen during the drive
    val otherInfo: String = "",                 // any other batch-specific info
    val createdBy: String = "",                 // admin UID
    val createdAt: Long = 0L
)

// ─── Feedback Submission (User-submitted) ─────────────────────────────────────
data class FeedbackSubmission(
    val id: String = "",
    val companyId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val batchYear: String = "",                 // self-declared by the submitter
    val rating: Int = 0,                        // 1–5 stars
    val experience: String = "",                // overall experience / review text
    val questionsEncountered: List<String> = emptyList(),
    val otherInfo: String = "",
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val submittedAt: Long = 0L,
    val reviewedAt: Long = 0L,
    val reviewedBy: String = ""                 // admin UID who reviewed
)

enum class FeedbackStatus { PENDING, APPROVED, REJECTED }

// ─── In-form state for submission UI ──────────────────────────────────────────
data class SubmitFeedbackForm(
    val batchYear: String = "",
    val rating: Int = 0,
    val experience: String = "",
    val questionsEncountered: List<String> = emptyList(),
    val currentQuestion: String = "",           // input for adding a question chip
    val otherInfo: String = ""
)
