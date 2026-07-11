package com.exa.android.reflekt.loopit.data.feature.main.placement

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.exa.android.reflekt.loopit.presentation.main.placement.model.PlacedStudent
import com.exa.android.reflekt.loopit.presentation.main.placement.model.BatchStatistics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacedStudentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private const val TARGET_COLLEGE_NAME = "lnct"
        private const val STUDENTS_PAGE_LIMIT = 100L
        private const val PAGE_SIZE = 50L
        private val KEY_COLLEGE_DOC_ID = stringPreferencesKey("placement_college_doc_id")
    }

    data class StudentsPage(
        val students: List<PlacedStudent>,
        val lastDocument: com.google.firebase.firestore.DocumentSnapshot?,
        val hasMore: Boolean
    )

    // ─── College Doc ID — Disk-first, then Network ────────────────────────────
    //
    // Resolving the college doc ID requires a Firestore query (≈300 ms on first
    // cold start). We persist the resolved ID in DataStore so every subsequent
    // session reads it from disk (≈1 ms) and skips the extra network round-trip.

    private val _collegeDocId = MutableStateFlow<String?>(null)
    val collegeDocId: StateFlow<String?> = _collegeDocId.asStateFlow()

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        // Warm the college doc ID eagerly: DataStore first, Firestore as fallback.
        repoScope.launch { warmCollegeDocId() }
    }

    private suspend fun warmCollegeDocId() {
        // Fast path: DataStore (protobuf file on disk, ~1 ms)
        val cached = dataStore.data.first()[KEY_COLLEGE_DOC_ID]
        if (!cached.isNullOrBlank()) {
            _collegeDocId.value = cached
            return
        }
        // Slow path: Firestore network query (first install only)
        resolveAndPersistCollegeDocId()
    }

    private suspend fun resolveAndPersistCollegeDocId() {
        if (_collegeDocId.value != null) return
        try {
            val query = firestore.collection("placement_info")
                .whereEqualTo("collegeName", TARGET_COLLEGE_NAME)
                .limit(1)
                .get()
                .await()
            val docId = query.documents.firstOrNull()?.id ?: return
            _collegeDocId.value = docId
            // Persist so next session reads from disk
            dataStore.edit { prefs -> prefs[KEY_COLLEGE_DOC_ID] = docId }
        } catch (_: Exception) { /* will retry on next call */ }
    }

    /** Returns the college doc ID, resolving it if not yet cached. */
    private suspend fun getCollegeDocId(): String? {
        _collegeDocId.value?.let { return it }
        resolveAndPersistCollegeDocId()
        return _collegeDocId.value
    }

    // ─── Students — Server-Side Filters ───────────────────────────────────────

    /**
     * Streams placed students with filters pushed to Firestore where possible.
     *
     * Server-side: batchYear, type, packageLpa range, single-company exact match.
     * Client-side: multi-company selection (Firestore whereIn supports ≤ 30 values).
     *
     * Requires composite indexes:
     *  • placed_students: (batchYear ASC, packageLpa DESC)
     *  • placed_students: (type ASC, packageLpa DESC)
     *  • placed_students: (batchYear ASC, type ASC, packageLpa DESC)
     */
    fun getFilteredStudents(
        batchYear: Int? = null,
        company: String? = null,
        type: String? = null,        // "FTE" | "Intern" | null = all
        isAdmin: Boolean = false,
        packageRange: ClosedFloatingPointRange<Float>? = null,
        selectedCompanies: List<String> = emptyList()
    ): Flow<List<PlacedStudent>> = callbackFlow {
        val collegeDocId = getCollegeDocId()
        if (collegeDocId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val collectionRef = firestore
            .collection("placement_info")
            .document(collegeDocId)
            .collection("placed_students")

        // Build server-side query — layer filters to match available indexes.
        // We always order by packageLpa DESC so the most competitive placements
        // appear first regardless of applied filters.
        var query: Query = collectionRef

        // Equality filters first (Firestore requirement for compound queries)
        if (batchYear != null) {
            query = query.whereEqualTo("batchYear", batchYear)
        }
        if (!type.isNullOrBlank()) {
            query = query.whereEqualTo("type", type)
        }

        // Push package range to server when both bounds are meaningful
        val effectiveRange = packageRange
        if (effectiveRange != null &&
            !(effectiveRange.start == 0f && effectiveRange.endInclusive >= 100f)
        ) {
            query = query
                .whereGreaterThanOrEqualTo("packageLpa", effectiveRange.start.toDouble())
                .whereLessThanOrEqualTo("packageLpa", effectiveRange.endInclusive.toDouble())
        }

        // Push single-company exact match to server; multi-select stays in-memory
        if (!company.isNullOrBlank() && selectedCompanies.isEmpty()) {
            val trimmed = company.trim()
            val cases = listOf(
                trimmed,
                trimmed.lowercase(),
                trimmed.uppercase(),
                trimmed.lowercase().replaceFirstChar { it.uppercase() }
            ).distinct()
            
            if (cases.size == 1) {
                query = query.whereEqualTo("company", cases.first())
            } else {
                query = query.whereIn("company", cases)
            }
        }

        query = query
            .orderBy("packageLpa", Query.Direction.DESCENDING)
            .limit(STUDENTS_PAGE_LIMIT)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            var list = snap?.documents?.mapNotNull { doc ->
                doc.data?.let { fromMap(it, isAdmin) }
            } ?: emptyList()

            // Remaining in-memory filter: multi-company selection
            if (selectedCompanies.isNotEmpty()) {
                val lowerCompanies = selectedCompanies.map { it.lowercase().trim() }
                list = list.filter { lowerCompanies.contains(it.company.lowercase().trim()) }
            }

            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // ─── Paginated Fetch (cursor-based) ───────────────────────────────────────

    /**
     * Fetches one page of placed students starting after [afterDocument] (null = first page).
     * Returns [StudentsPage] with the last Firestore document (for next-page cursor) and
     * whether another page likely exists.
     */
    suspend fun fetchStudentsPage(
        batchYear: Int? = null,
        company: String? = null,
        type: String? = null,
        isAdmin: Boolean = false,
        packageRange: ClosedFloatingPointRange<Float>? = null,
        selectedCompanies: List<String> = emptyList(),
        afterDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    ): StudentsPage {
        val collegeDocId = getCollegeDocId() ?: return StudentsPage(emptyList(), null, false)

        val collectionRef = firestore
            .collection("placement_info")
            .document(collegeDocId)
            .collection("placed_students")

        var query: Query = collectionRef

        if (batchYear != null) query = query.whereEqualTo("batchYear", batchYear)
        if (!type.isNullOrBlank()) query = query.whereEqualTo("type", type)

        val effectiveRange = packageRange
        if (effectiveRange != null &&
            !(effectiveRange.start == 0f && effectiveRange.endInclusive >= 100f)
        ) {
            query = query
                .whereGreaterThanOrEqualTo("packageLpa", effectiveRange.start.toDouble())
                .whereLessThanOrEqualTo("packageLpa", effectiveRange.endInclusive.toDouble())
        }

        if (!company.isNullOrBlank() && selectedCompanies.isEmpty()) {
            query = query.whereEqualTo("company", company.trim())
        }

        query = query.orderBy("packageLpa", Query.Direction.DESCENDING).limit(PAGE_SIZE)

        if (afterDocument != null) {
            query = query.startAfter(afterDocument)
        }

        return try {
            val snap = query.get().await()
            var list = snap.documents.mapNotNull { doc ->
                doc.data?.let { fromMap(it, isAdmin) }
            }
            if (selectedCompanies.isNotEmpty()) {
                val lower = selectedCompanies.map { it.lowercase().trim() }
                list = list.filter { lower.contains(it.company.lowercase().trim()) }
            }
            StudentsPage(
                students = list,
                lastDocument = snap.documents.lastOrNull(),
                hasMore = snap.documents.size.toLong() == PAGE_SIZE
            )
        } catch (e: Exception) {
            StudentsPage(emptyList(), null, false)
        }
    }

    // ─── Batch Years ───────────────────────────────────────────────────────────

    /** One-shot fetch of distinct batch years from pre-computed statistics docs. */
    suspend fun getAvailableBatches(): List<Int> = try {
        val collegeDocId = getCollegeDocId() ?: return emptyList()
        val snap = firestore
            .collection("placement_info")
            .document(collegeDocId)
            .collection("placement_statistics")
            .get()
            .await()
        snap.documents.mapNotNull { it.id.toIntOrNull() }
            .distinct().sortedDescending()
    } catch (_: Exception) { emptyList() }

    // ─── Batch Statistics — One-shot (no live listener) ───────────────────────
    //
    // Batch statistics are pre-computed documents that change infrequently.
    // Using a snapshot listener for them opened an extra persistent connection
    // and caused an additional network round-trip on every filter change.
    // A one-shot .get().await() is sufficient and much cheaper.

    /**
     * One-shot fetch of batch statistics for a specific year, or an aggregate
     * of all years when [batchYear] is null.
     */
    suspend fun getBatchStatistics(batchYear: Int?): BatchStatistics? {
        return try {
            val collegeDocId = getCollegeDocId() ?: return null
            val statsCollection = firestore
                .collection("placement_info")
                .document(collegeDocId)
                .collection("placement_statistics")

            if (batchYear != null) {
                val snap = statsCollection.document(batchYear.toString()).get().await()
                snap.data?.let { statsFromMap(it) }
            } else {
                // Aggregate across all batch documents
                val snap = statsCollection.get().await()
                if (snap.isEmpty) return null

                var totalPlaced = 0
                var highestPackage = 0.0
                var totalLpaSum = 0.0
                val topCompanyScores = mutableMapOf<String, Int>()
                val allTopStudents = mutableListOf<PlacedStudent>()
                val allPackageDistribution = mutableMapOf<String, Int>()

                snap.documents.forEach { doc ->
                    val s = statsFromMap(doc.data ?: emptyMap())
                    totalPlaced += s.totalPlaced
                    totalLpaSum += s.averagePackage * s.totalPlaced
                    if (s.highestPackage > highestPackage) highestPackage = s.highestPackage
                    topCompanyScores[s.topCompany] =
                        (topCompanyScores[s.topCompany] ?: 0) + s.totalPlaced
                    allTopStudents.addAll(s.topStudents)
                    s.packageDistribution.forEach { (k, v) ->
                        allPackageDistribution[k] = (allPackageDistribution[k] ?: 0) + v
                    }
                }

                allTopStudents.sortByDescending { it.packageLpa }
                val overallTopCompany = topCompanyScores.maxByOrNull { it.value }?.key ?: ""
                val avgPackage = if (totalPlaced > 0) totalLpaSum / totalPlaced else 0.0

                BatchStatistics(
                    batchYear = 0,
                    totalPlaced = totalPlaced,
                    highestPackage = highestPackage,
                    averagePackage = avgPackage,
                    topCompany = overallTopCompany,
                    packageDistribution = allPackageDistribution,
                    topStudents = allTopStudents.take(5)
                )
            }
        } catch (_: Exception) { null }
    }

    /** One-shot fetch of all batch statistics sorted by year, for analytics screen. */
    suspend fun getAllBatchStatistics(): List<BatchStatistics> {
        return try {
            val collegeDocId = getCollegeDocId() ?: return emptyList()
            val snap = firestore
                .collection("placement_info")
                .document(collegeDocId)
                .collection("placement_statistics")
                .get()
                .await()
            snap.documents
                .mapNotNull { it.data?.let { d -> statsFromMap(d) } }
                .filter { it.batchYear > 0 }
                .sortedBy { it.batchYear }
        } catch (_: Exception) { emptyList() }
    }

    // ─── Serialization ────────────────────────────────────────────────────────

    private fun fromMap(m: Map<String, Any?>, isAdmin: Boolean): PlacedStudent = PlacedStudent(
        id             = m["id"] as? String ?: "",
        name           = m["name"] as? String ?: "",
        company        = m["company"] as? String ?: "",
        companyId      = m["companyId"] as? String ?: "",
        role           = m["role"] as? String ?: "",
        packageLpa     = (m["packageLpa"] as? Number)?.toDouble() ?: 0.0,
        packageDisplay = m["packageDisplay"] as? String ?: "",
        batchYear      = (m["batchYear"] as? Long)?.toInt() ?: 0,
        type           = m["type"] as? String ?: "FTE",
        branch         = m["branch"] as? String ?: "",
        linkedinUrl    = m["linkedinUrl"] as? String ?: "",
        email          = if (isAdmin) m["email"] as? String ?: "" else "",
        phone          = if (isAdmin) m["phone"] as? String ?: "" else "",
        hasPhone       = (m["phone"] as? String)?.isNotBlank() == true
    )

    fun toFirestoreMap(s: PlacedStudent): Map<String, Any?> = mapOf(
        "id"             to s.id,
        "name"           to s.name,
        "company"        to s.company,
        "companyId"      to s.companyId,
        "role"           to s.role,
        "packageLpa"     to s.packageLpa,
        "packageDisplay" to s.packageDisplay,
        "batchYear"      to s.batchYear,
        "type"           to s.type,
        "branch"         to s.branch,
        "linkedinUrl"    to s.linkedinUrl,
        "email"          to s.email,
        "phone"          to s.phone,
        "hasPhone"       to s.hasPhone
    )

    private fun statsFromMap(m: Map<String, Any?>): BatchStatistics {
        val studentsList =
            (m["topStudents"] as? List<Map<String, Any?>>)?.map { fromMap(it, false) } ?: emptyList()
        val distMap =
            (m["packageDistribution"] as? Map<String, Number>)?.mapValues { it.value.toInt() }
                ?: emptyMap()
        return BatchStatistics(
            batchYear       = (m["batchYear"] as? Long)?.toInt() ?: 0,
            totalPlaced     = (m["totalPlaced"] as? Long)?.toInt() ?: 0,
            highestPackage  = (m["highestPackage"] as? Number)?.toDouble() ?: 0.0,
            averagePackage  = (m["averagePackage"] as? Number)?.toDouble() ?: 0.0,
            above4LpaCount  = (m["above4LpaCount"] as? Long)?.toInt() ?: 0,
            above8LpaCount  = (m["above8LpaCount"] as? Long)?.toInt() ?: 0,
            topCompany      = m["topCompany"] as? String ?: "",
            packageDistribution = distMap,
            topStudents     = studentsList
        )
    }

    suspend fun getLiveEvents(): Flow<List<com.exa.android.reflekt.loopit.presentation.main.placement.model.LiveEventModel>> = callbackFlow {
        val collegeDocId = getCollegeDocId()
        if (collegeDocId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore
            .collection("placement_info")
            .document(collegeDocId)
            .collection("live_events")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val events = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(com.exa.android.reflekt.loopit.presentation.main.placement.model.LiveEventModel::class.java)
                } ?: emptyList()

                trySend(events)
            }

        awaitClose { listener.remove() }
    }
}
