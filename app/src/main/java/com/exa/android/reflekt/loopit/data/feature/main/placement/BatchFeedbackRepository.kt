package com.exa.android.reflekt.loopit.data.feature.main.placement

import com.exa.android.reflekt.loopit.presentation.main.placement.model.BatchReport
import com.exa.android.reflekt.loopit.presentation.main.placement.model.FeedbackStatus
import com.exa.android.reflekt.loopit.presentation.main.placement.model.FeedbackSubmission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BatchFeedbackRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val COMPANIES = "placement_companies"
        private const val BATCH_REPORTS = "batch_reports"
        private const val FEEDBACK = "feedback_submissions"
    }

    // ─── Current User Helpers ─────────────────────────────────────────────────

    fun getCurrentUserId(): String? = auth.currentUser?.uid
    fun getCurrentUserEmail(): String = auth.currentUser?.email ?: ""
    fun getCurrentUserName(): String = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: "Anonymous"

    // ─── Batch Reports (Admin) ────────────────────────────────────────────────

    /** Real-time stream of all batch reports for a company, ordered newest-first. */
    fun getBatchReports(companyId: String): Flow<List<BatchReport>> = callbackFlow {
        val listener = firestore
            .collection(COMPANIES).document(companyId)
            .collection(BATCH_REPORTS)
            .orderBy("batchYear", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val reports = snap?.documents?.mapNotNull { doc ->
                    doc.data?.let { fromBatchReportMap(it) }
                } ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    /** Admin: add or overwrite a batch report (doc ID = batchYear). */
    suspend fun addBatchReport(companyId: String, report: BatchReport): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not logged in"))
                val toSave = report.copy(createdBy = uid, createdAt = System.currentTimeMillis(), id = report.batchYear)
                firestore.collection(COMPANIES).document(companyId)
                    .collection(BATCH_REPORTS).document(report.batchYear)
                    .set(toBatchReportMap(toSave)).await()
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }

    // ─── Feedback (Students) ──────────────────────────────────────────────────

    /**
     * Real-time stream of APPROVED feedback for a company.
     * Optionally filtered by [batchYear] if non-null/blank.
     */
    /**
     * Real-time stream of APPROVED feedback for a company, capped at 20 items.
     * Ordered newest-first. Requires composite index: status ASC, submittedAt DESC.
     */
    fun getApprovedFeedback(companyId: String, batchYear: String? = null): Flow<List<FeedbackSubmission>> =
        callbackFlow {
            var query: Query = firestore
                .collection(COMPANIES).document(companyId)
                .collection(FEEDBACK)
                .whereEqualTo("status", FeedbackStatus.APPROVED.name)
            if (!batchYear.isNullOrBlank()) {
                query = query.whereEqualTo("batchYear", batchYear)
            }
            query = query
                .orderBy("submittedAt", Query.Direction.DESCENDING)
                .limit(20)
            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.let { d -> fromFeedbackMap(d) } } ?: emptyList()
                trySend(list)
            }
            awaitClose { listener.remove() }
        }

    /** Admin: real-time stream of all PENDING submissions for a company, capped at 20 items. */
    fun getPendingFeedback(companyId: String): Flow<List<FeedbackSubmission>> = callbackFlow {
        val listener = firestore
            .collection(COMPANIES).document(companyId)
            .collection(FEEDBACK)
            .whereEqualTo("status", FeedbackStatus.PENDING.name)
            .orderBy("submittedAt", Query.Direction.ASCENDING)
            .limit(20)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull { it.data?.let { d -> fromFeedbackMap(d) } } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Check if current user has already submitted feedback for this company. */
    suspend fun hasUserSubmitted(companyId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: return@withContext false
            val snap = firestore.collection(COMPANIES).document(companyId)
                .collection(FEEDBACK)
                .whereEqualTo("userId", uid)
                .limit(1)
                .get().await()
            !snap.isEmpty
        } catch (e: Exception) { false }
    }

    /** Submit new feedback (status = PENDING). */
    suspend fun submitFeedback(companyId: String, submission: FeedbackSubmission): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val uid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not logged in"))
                val ref = firestore.collection(COMPANIES).document(companyId)
                    .collection(FEEDBACK).document()
                val toSave = submission.copy(
                    id = ref.id,
                    companyId = companyId,
                    userId = uid,
                    userEmail = auth.currentUser?.email ?: "",
                    userName = auth.currentUser?.displayName ?: auth.currentUser?.email?.substringBefore("@") ?: "Anonymous",
                    status = FeedbackStatus.PENDING,
                    submittedAt = System.currentTimeMillis()
                )
                ref.set(toFeedbackMap(toSave)).await()
                Result.success(ref.id)
            } catch (e: Exception) { Result.failure(e) }
        }

    /**
     * Seeder-only: writes a feedback document as-is, preserving its [FeedbackSubmission.status].
     * Unlike [submitFeedback] this does NOT force PENDING — use only from [PlacementDataSeeder].
     */
    suspend fun seedFeedbackDirect(companyId: String, fb: FeedbackSubmission): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                firestore.collection(COMPANIES).document(companyId)
                    .collection(FEEDBACK).document(fb.id)
                    .set(toFeedbackMap(fb)).await()
                Result.success(Unit)
            } catch (e: Exception) { Result.failure(e) }
        }

    /** Admin: approve or reject a submission. */
    suspend fun updateFeedbackStatus(
        companyId: String,
        feedbackId: String,
        newStatus: FeedbackStatus
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val adminUid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not logged in"))
            firestore.collection(COMPANIES).document(companyId)
                .collection(FEEDBACK).document(feedbackId)
                .update(mapOf(
                    "status" to newStatus.name,
                    "reviewedAt" to System.currentTimeMillis(),
                    "reviewedBy" to adminUid
                )).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    // ─── Serialization ────────────────────────────────────────────────────────

    private fun toBatchReportMap(r: BatchReport): Map<String, Any?> = mapOf(
        "id"               to r.id,
        "batchYear"        to r.batchYear,
        "shortlistedCount" to r.shortlistedCount,
        "selectedCount"    to r.selectedCount,
        "questionsAsked"   to r.questionsAsked,
        "otherInfo"        to r.otherInfo,
        "createdBy"        to r.createdBy,
        "createdAt"        to r.createdAt
    )

    private fun fromBatchReportMap(m: Map<String, Any?>): BatchReport = BatchReport(
        id               = m["id"] as? String ?: "",
        batchYear        = m["batchYear"] as? String ?: "",
        shortlistedCount = (m["shortlistedCount"] as? Long)?.toInt() ?: 0,
        selectedCount    = (m["selectedCount"] as? Long)?.toInt() ?: 0,
        questionsAsked   = (m["questionsAsked"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        otherInfo        = m["otherInfo"] as? String ?: "",
        createdBy        = m["createdBy"] as? String ?: "",
        createdAt        = m["createdAt"] as? Long ?: 0L
    )

    private fun toFeedbackMap(f: FeedbackSubmission): Map<String, Any?> = mapOf(
        "id"                    to f.id,
        "companyId"             to f.companyId,
        "userId"                to f.userId,
        "userEmail"             to f.userEmail,
        "userName"              to f.userName,
        "batchYear"             to f.batchYear,
        "rating"                to f.rating,
        "experience"            to f.experience,
        "questionsEncountered"  to f.questionsEncountered,
        "otherInfo"             to f.otherInfo,
        "status"                to f.status.name,
        "submittedAt"           to f.submittedAt,
        "reviewedAt"            to f.reviewedAt,
        "reviewedBy"            to f.reviewedBy
    )

    private fun fromFeedbackMap(m: Map<String, Any?>): FeedbackSubmission = FeedbackSubmission(
        id                   = m["id"] as? String ?: "",
        companyId            = m["companyId"] as? String ?: "",
        userId               = m["userId"] as? String ?: "",
        userEmail            = m["userEmail"] as? String ?: "",
        userName             = m["userName"] as? String ?: "",
        batchYear            = m["batchYear"] as? String ?: "",
        rating               = (m["rating"] as? Long)?.toInt() ?: 0,
        experience           = m["experience"] as? String ?: "",
        questionsEncountered = (m["questionsEncountered"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
        otherInfo            = m["otherInfo"] as? String ?: "",
        status               = try { FeedbackStatus.valueOf(m["status"] as? String ?: "PENDING") } catch (e: Exception) { FeedbackStatus.PENDING },
        submittedAt          = m["submittedAt"] as? Long ?: 0L,
        reviewedAt           = m["reviewedAt"] as? Long ?: 0L,
        reviewedBy           = m["reviewedBy"] as? String ?: ""
    )
}
