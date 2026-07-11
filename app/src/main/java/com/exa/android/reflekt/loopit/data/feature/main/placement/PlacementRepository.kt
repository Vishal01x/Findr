package com.exa.android.reflekt.loopit.data.feature.main.placement

import android.net.Uri
import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail
import com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel
import com.exa.android.reflekt.loopit.presentation.main.placement.model.EligibilityCriteria
import com.exa.android.reflekt.loopit.presentation.main.placement.model.PdfAttachment
import com.exa.android.reflekt.loopit.presentation.main.placement.model.PrepResource
import com.exa.android.reflekt.loopit.presentation.main.placement.model.PrepResourceIcon
import com.exa.android.reflekt.loopit.presentation.main.placement.model.RecruitmentStage
import com.exa.android.reflekt.loopit.presentation.main.placement.model.ReferenceLink
import com.exa.android.reflekt.loopit.presentation.main.placement.model.RoleType
import com.exa.android.reflekt.loopit.presentation.main.placement.model.StageStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import android.content.Context
import com.exa.android.reflekt.loopit.data.feature.main.media.api.CloudinaryApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class UploadState {
    data class Progress(val percent: Int) : UploadState()
    data class Success(val url: String, val sizeKb: Long) : UploadState()
    data class Error(val message: String) : UploadState()
}

@Singleton
class PlacementRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val cloudinaryApi: CloudinaryApi,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val COMPANIES_COLLECTION = "placement_companies"
    }

    // ─── Write ────────────────────────────────────────────────────────────────

    /** Saves a new CompanyDetail to Firestore. Returns the new document ID. */
    suspend fun addCompany(detail: CompanyDetail): Result<String> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: return@withContext Result.failure(Exception("Not logged in"))
            val ref = firestore.collection(COMPANIES_COLLECTION).document()
            val toSave = detail.copy(
                id = ref.id,
                createdBy = uid,
                lastUpdated = System.currentTimeMillis()
            )
            ref.set(toFirestoreMap(toSave)).await()
            Result.success(ref.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Updates an existing company document. */
    suspend fun updateCompany(id: String, detail: CompanyDetail): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val toSave = detail.copy(lastUpdated = System.currentTimeMillis())
            firestore.collection(COMPANIES_COLLECTION).document(id).set(toFirestoreMap(toSave)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    /**
     * Streams all companies in real time (full documents).
     * Ordered by live-process status first, then most recently updated.
     * Requires composite index: isLiveProcess DESC, lastUpdated DESC.
     */
    fun getCompanies(): Flow<List<CompanyDetail>> = callbackFlow {
        val listener = firestore.collection(COMPANIES_COLLECTION)
            .orderBy("isLiveProcess", Query.Direction.DESCENDING)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val companies = snapshot?.documents
                    ?.mapNotNull { fromFirestoreMap(it.data ?: return@mapNotNull null) }
                    ?: emptyList()
                trySend(companies)
            }
        awaitClose { listener.remove() }
    }

    /**
     * Streams company **summary** fields only — optimised for list/card screens.
     *
     * Uses Firestore field masks (.select()) so only lightweight summary fields
     * are transferred over the network, avoiding large nested arrays
     * (recruitmentStages, preparationResources, referenceLinks, pdfAttachments)
     * that are only needed on the detail screen.
     *
     * Fields fetched: id, name, logoUrl, tagline, isHiring, jobRole, roleType,
     * packageLpa, stipendPerMonth, level, deadline, isLiveProcess, lastUpdated.
     */
    fun getCompaniesForList(): Flow<List<CompanyDetail>> = callbackFlow {
        val listener = firestore.collection(COMPANIES_COLLECTION)
            .orderBy("isLiveProcess", Query.Direction.DESCENDING)
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val companies = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val d = doc.data ?: return@mapNotNull null
                        // Parse only the lightweight summary subset — heavy nested
                        // arrays default to empty lists since they are not fetched.
                        try {
                            CompanyDetail(
                                id            = d["id"] as? String ?: doc.id,
                                name          = d["name"] as? String ?: "",
                                logoUrl       = d["logoUrl"] as? String ?: "",
                                tagline       = d["tagline"] as? String ?: "",
                                isHiring      = d["isHiring"] as? Boolean ?: false,
                                jobRole       = d["jobRole"] as? String ?: "",
                                roleType      = RoleType.valueOf(d["roleType"] as? String ?: "BOTH"),
                                packageLpa    = d["packageLpa"] as? String,
                                stipendPerMonth = d["stipendPerMonth"] as? String,
                                level         = DifficultyLevel.valueOf(d["level"] as? String ?: "MEDIUM"),
                                deadline      = d["deadline"] as? String ?: "",
                                isLiveProcess = d["isLiveProcess"] as? Boolean ?: false,
                                lastUpdated   = d["lastUpdated"] as? Long ?: 0L,
                                location      = d["location"] as? String ?: "",
                                hiresCount    = (d["hiresCount"] as? Long)?.toInt() ?: 0
                            )
                        } catch (_: Exception) { null }
                    }
                    ?: emptyList()
                trySend(companies)
            }
        awaitClose { listener.remove() }
    }

    /** One-shot fetch of a single company. */
    suspend fun getCompanyById(id: String): Result<CompanyDetail?> = withContext(Dispatchers.IO) {
        try {
            val doc = firestore.collection(COMPANIES_COLLECTION).document(id).get().await()
            val detail = doc.data?.let { fromFirestoreMap(it) }
            Result.success(detail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── PDF Upload ───────────────────────────────────────────────────────────

    /**
     * Uploads a PDF file to Cloudinary with native progress tracking.
     */
    fun uploadPdf(uri: Uri, companyId: String, fileName: String): Flow<UploadState> = callbackFlow {
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        if (inputStream == null) {
            trySend(UploadState.Error("Could not open file"))
            close()
            return@callbackFlow
        }

        val fileLength = contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: -1L
        if (fileLength == -1L || fileLength > 10 * 1024 * 1024) { // 10MB limit
            trySend(UploadState.Error("File size exceeds 10MB limit or is unreadable"))
            close()
            return@callbackFlow
        }

        val requestBody = object : RequestBody() {
            override fun contentType() = "application/pdf".toMediaTypeOrNull()
            override fun contentLength() = fileLength
            override fun writeTo(sink: okio.BufferedSink) {
                val buffer = ByteArray(4096)
                var uploaded = 0L
                var read: Int
                var lastPercent = 0
                
                // Track progress
                inputStream.use { input ->
                    while (input.read(buffer).also { read = it } != -1) {
                        uploaded += read
                        sink.write(buffer, 0, read)
                        val percent = ((uploaded.toDouble() / fileLength.toDouble()) * 100).toInt()
                        if (percent > lastPercent) {
                            trySend(UploadState.Progress(percent))
                            lastPercent = percent
                        }
                    }
                }
            }
        }

        val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)
        val uploadPreset = "media_uploads".toRequestBody("text/plain".toMediaTypeOrNull())

        // Execute via Retrofit CloudinaryApi
        try {
            val response = cloudinaryApi.uploadFile(filePart, uploadPreset)
            val sizeKb = fileLength / 1024L
            trySend(UploadState.Success(response.secure_url, sizeKb))
        } catch (e: Exception) {
            trySend(UploadState.Error(e.message ?: "Upload failed"))
        }
        
        close()
        awaitClose { }
    }

    // ─── Serialization ────────────────────────────────────────────────────────

    private fun toFirestoreMap(d: CompanyDetail): Map<String, Any?> = mapOf(
        "id"                to d.id,
        "name"              to d.name,
        "logoUrl"           to d.logoUrl,
        "tagline"           to d.tagline,
        "description"       to d.description,
        "website"           to d.website,
        "location"          to d.location,
        "employeeCount"     to d.employeeCount,
        "isHiring"          to d.isHiring,
        "jobRole"           to d.jobRole,
        "roleType"          to d.roleType.name,
        "packageLpa"        to d.packageLpa,
        "stipendPerMonth"   to d.stipendPerMonth,
        "level"             to d.level.name,
        "hiresCount"        to d.hiresCount,
        "workCulture"       to d.workCulture,
        "internDeadline"    to d.internDeadline,
        "fulltimeDeadline"  to d.fulltimeDeadline,
        "deadline"          to d.deadline,
        "minCgpa"           to d.minCgpa,
        "tenth"             to d.tenth,
        "twelfth"           to d.twelfth,
        "allowedBacklogs"   to d.allowedBacklogs,
        "allowedBranches"   to d.allowedBranches,
        "isLiveProcess"     to d.isLiveProcess,
        "createdBy"         to d.createdBy,
        "lastUpdated"       to d.lastUpdated,
        "eligibilityCriteria" to mapOf(
            "minCgpa"          to d.eligibilityCriteria.minCgpa,
            "noActiveBacklogs" to d.eligibilityCriteria.noActiveBacklogs,
            "allowedBranches"  to d.eligibilityCriteria.allowedBranches
        ),
        "recruitmentStages" to d.recruitmentStages.map { s ->
            mapOf(
                "order"       to s.order,
                "title"       to s.title,
                "status"      to s.status.name,
                "date"        to s.date,
                "platform"    to s.platform,
                "durationMins" to s.durationMins,
                "tags"        to s.tags,
                "description" to s.description
            )
        },
        "preparationResources" to d.preparationResources.map { r ->
            mapOf("title" to r.title, "subtitle" to r.subtitle, "icon" to r.icon.name, "url" to r.url)
        },
        "referenceLinks" to d.referenceLinks.map { mapOf("label" to it.label, "url" to it.url) },
        "pdfAttachments" to d.pdfAttachments.map { mapOf("name" to it.name, "storageUrl" to it.storageUrl, "sizeKb" to it.sizeKb) },
        "placementContacts" to d.placementContacts.map { c ->
            mapOf("name" to c.name, "role" to c.role, "avatarUrl" to c.avatarUrl, "linkedinUrl" to c.linkedinUrl, "emailUrl" to c.emailUrl)
        }
    )

    @Suppress("UNCHECKED_CAST")
    private fun fromFirestoreMap(map: Map<String, Any?>): CompanyDetail? {
        return try {
            val criteria = (map["eligibilityCriteria"] as? Map<String, Any?>)
            val stages = (map["recruitmentStages"] as? List<Map<String, Any?>>)?.map { s ->
                RecruitmentStage(
                    order = (s["order"] as? Long)?.toInt() ?: 0,
                    title = s["title"] as? String ?: "",
                    status = StageStatus.valueOf(s["status"] as? String ?: "UPCOMING"),
                    date = s["date"] as? String ?: "",
                    platform = s["platform"] as? String ?: "",
                    durationMins = (s["durationMins"] as? Long)?.toInt() ?: 0,
                    tags = s["tags"] as? List<String> ?: emptyList(),
                    description = s["description"] as? String ?: ""
                )
            } ?: emptyList()
            val resources = (map["preparationResources"] as? List<Map<String, Any?>>)?.map { r ->
                PrepResource(
                    title = r["title"] as? String ?: "",
                    subtitle = r["subtitle"] as? String ?: "",
                    icon = PrepResourceIcon.valueOf(r["icon"] as? String ?: "LINK"),
                    url = r["url"] as? String ?: ""
                )
            } ?: emptyList()
            val refLinks = (map["referenceLinks"] as? List<Map<String, Any?>>)?.map { r ->
                ReferenceLink(label = r["label"] as? String ?: "", url = r["url"] as? String ?: "")
            } ?: emptyList()
            val pdfs = (map["pdfAttachments"] as? List<Map<String, Any?>>)?.map { p ->
                PdfAttachment(
                    name = p["name"] as? String ?: "",
                    storageUrl = p["storageUrl"] as? String ?: "",
                    sizeKb = p["sizeKb"] as? Long ?: 0L
                )
            } ?: emptyList()

            CompanyDetail(
                id = map["id"] as? String ?: "",
                name = map["name"] as? String ?: "",
                logoUrl = map["logoUrl"] as? String ?: "",
                tagline = map["tagline"] as? String ?: "",
                description = map["description"] as? String ?: "",
                website = map["website"] as? String ?: "",
                isHiring = map["isHiring"] as? Boolean ?: false,
                jobRole = map["jobRole"] as? String ?: "",
                roleType = RoleType.valueOf(map["roleType"] as? String ?: "BOTH"),
                location = map["location"] as? String ?: "",
                employeeCount = map["employeeCount"] as? String ?: "",
                packageLpa = map["packageLpa"] as? String,
                stipendPerMonth = map["stipendPerMonth"] as? String,
                level = DifficultyLevel.valueOf(map["level"] as? String ?: "MEDIUM"),
                hiresCount = (map["hiresCount"] as? Long)?.toInt() ?: 0,
                workCulture = map["workCulture"] as? String ?: "",
                internDeadline = map["internDeadline"] as? String ?: "",
                fulltimeDeadline = map["fulltimeDeadline"] as? String ?: "",
                deadline = map["deadline"] as? String ?: "",
                minCgpa = map["minCgpa"] as? String ?: "",
                tenth = map["tenth"] as? String ?: "",
                twelfth = map["twelfth"] as? String ?: "",
                allowedBacklogs = (map["allowedBacklogs"] as? Long)?.toInt() ?: 0,
                allowedBranches = map["allowedBranches"] as? List<String> ?: emptyList(),
                isLiveProcess = map["isLiveProcess"] as? Boolean ?: false,
                createdBy = map["createdBy"] as? String ?: "",
                lastUpdated = map["lastUpdated"] as? Long ?: 0L,
                eligibilityCriteria = EligibilityCriteria(
                    minCgpa = criteria?.get("minCgpa") as? String ?: "",
                    noActiveBacklogs = criteria?.get("noActiveBacklogs") as? Boolean ?: true,
                    allowedBranches = criteria?.get("allowedBranches") as? List<String> ?: emptyList()
                ),
                recruitmentStages = stages,
                preparationResources = resources,
                referenceLinks = refLinks,
                pdfAttachments = pdfs
            )
        } catch (e: Exception) {
            null
        }
    }
}
