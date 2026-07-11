package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementAdminRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.UploadState
import com.exa.android.reflekt.loopit.presentation.main.placement.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── State ────────────────────────────────────────────────────────────────────

data class AddCompanyFormState(
    // Step navigation
    val currentStep: Int = 0,           // 0..5
    val totalSteps: Int = 6,

    // Step 0 — Basic Info
    val name: String = "",
    val tagline: String = "",
    val description: String = "",
    val website: String = "",
    val location: String = "",
    val employeeCount: String = "",
    val jobRole: String = "",
    val roleType: RoleType = RoleType.BOTH,
    val isLiveProcess: Boolean = false,

    // Step 1 — Eligibility
    val minCgpa: String = "",
    val tenth: String = "",
    val twelfth: String = "",
    val allowedBacklogs: String = "0",
    val allowedBranches: List<String> = emptyList(),

    // Step 2 — Package & Difficulty
    val packageLpa: String = "",
    val stipendPerMonth: String = "",
    val level: DifficultyLevel = DifficultyLevel.MEDIUM,
    val hiresCount: String = "",

    // Step 3 — Recruitment Process
    val recruitmentStages: List<RecruitmentStage> = emptyList(),

    // Step 4 — Prep & Resources
    val prepGuide: String = "",
    val referenceLinks: List<ReferenceLink> = emptyList(),
    val pdfAttachments: List<PdfAttachment> = emptyList(),
    val isPdfUploading: Boolean = false,
    val pdfUploadProgress: Int = 0,

    // Step 5 — Culture & Contacts
    val workCulture: String = "",
    val internDeadline: String = "",
    val fulltimeDeadline: String = "",
    val placementContacts: List<PlacementContact> = emptyList(),

    // Submission
    val isSubmitting: Boolean = false,
    val submitError: String? = null,
    val submitSuccess: Boolean = false
)

sealed class AddCompanyFormEvent {
    // Navigation
    object NextStep : AddCompanyFormEvent()
    object PrevStep : AddCompanyFormEvent()

    // Step 0
    data class NameChanged(val v: String) : AddCompanyFormEvent()
    data class TaglineChanged(val v: String) : AddCompanyFormEvent()
    data class DescriptionChanged(val v: String) : AddCompanyFormEvent()
    data class WebsiteChanged(val v: String) : AddCompanyFormEvent()
    data class LocationChanged(val v: String) : AddCompanyFormEvent()
    data class EmployeeCountChanged(val v: String) : AddCompanyFormEvent()
    data class JobRoleChanged(val v: String) : AddCompanyFormEvent()
    data class RoleTypeChanged(val v: RoleType) : AddCompanyFormEvent()
    data class IsLiveChanged(val v: Boolean) : AddCompanyFormEvent()

    // Step 1
    data class MinCgpaChanged(val v: String) : AddCompanyFormEvent()
    data class TenthChanged(val v: String) : AddCompanyFormEvent()
    data class TwelfthChanged(val v: String) : AddCompanyFormEvent()
    data class BacklogsChanged(val v: String) : AddCompanyFormEvent()
    data class BranchToggled(val branch: String) : AddCompanyFormEvent()

    // Step 2
    data class PackageChanged(val v: String) : AddCompanyFormEvent()
    data class StipendChanged(val v: String) : AddCompanyFormEvent()
    data class LevelChanged(val v: DifficultyLevel) : AddCompanyFormEvent()
    data class HiresCountChanged(val v: String) : AddCompanyFormEvent()

    // Step 3
    data class StageAdded(val stage: RecruitmentStage) : AddCompanyFormEvent()
    data class StageRemoved(val index: Int) : AddCompanyFormEvent()

    // Step 4
    data class PrepGuideChanged(val v: String) : AddCompanyFormEvent()
    data class RefLinkAdded(val link: ReferenceLink) : AddCompanyFormEvent()
    data class RefLinkRemoved(val index: Int) : AddCompanyFormEvent()
    data class PdfSelected(val uri: Uri, val name: String, val companyId: String) : AddCompanyFormEvent()

    // Step 5
    data class WorkCultureChanged(val v: String) : AddCompanyFormEvent()
    data class InternDeadlineChanged(val v: String) : AddCompanyFormEvent()
    data class FulltimeDeadlineChanged(val v: String) : AddCompanyFormEvent()
    data class ContactAdded(val contact: PlacementContact) : AddCompanyFormEvent()
    data class ContactRemoved(val index: Int) : AddCompanyFormEvent()

    // Submit
    object Submit : AddCompanyFormEvent()
    object DismissError : AddCompanyFormEvent()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class AddCompanyFormViewModel @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val adminRepository: PlacementAdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddCompanyFormState())
    val state: StateFlow<AddCompanyFormState> = _state.asStateFlow()

    // One-time effects
    private val _effect = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val effect: SharedFlow<String> = _effect.asSharedFlow()

    fun onEvent(event: AddCompanyFormEvent) {
        when (event) {
            // ── Navigation ─────────────────────────────────────────────────
            AddCompanyFormEvent.NextStep -> {
                val s = _state.value
                if (s.currentStep < s.totalSteps - 1)
                    _state.update { it.copy(currentStep = it.currentStep + 1) }
            }
            AddCompanyFormEvent.PrevStep -> {
                val s = _state.value
                if (s.currentStep > 0)
                    _state.update { it.copy(currentStep = it.currentStep - 1) }
            }

            // ── Step 0 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.NameChanged        -> _state.update { it.copy(name = event.v) }
            is AddCompanyFormEvent.TaglineChanged     -> _state.update { it.copy(tagline = event.v) }
            is AddCompanyFormEvent.DescriptionChanged -> _state.update { it.copy(description = event.v) }
            is AddCompanyFormEvent.WebsiteChanged     -> _state.update { it.copy(website = event.v) }
            is AddCompanyFormEvent.LocationChanged    -> _state.update { it.copy(location = event.v) }
            is AddCompanyFormEvent.EmployeeCountChanged -> _state.update { it.copy(employeeCount = event.v) }
            is AddCompanyFormEvent.JobRoleChanged     -> _state.update { it.copy(jobRole = event.v) }
            is AddCompanyFormEvent.RoleTypeChanged    -> _state.update { it.copy(roleType = event.v) }
            is AddCompanyFormEvent.IsLiveChanged      -> _state.update { it.copy(isLiveProcess = event.v) }

            // ── Step 1 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.MinCgpaChanged  -> _state.update { it.copy(minCgpa = event.v) }
            is AddCompanyFormEvent.TenthChanged    -> _state.update { it.copy(tenth = event.v) }
            is AddCompanyFormEvent.TwelfthChanged  -> _state.update { it.copy(twelfth = event.v) }
            is AddCompanyFormEvent.BacklogsChanged -> _state.update { it.copy(allowedBacklogs = event.v) }
            is AddCompanyFormEvent.BranchToggled   -> {
                _state.update { s ->
                    val current = s.allowedBranches.toMutableList()
                    if (event.branch in current) current.remove(event.branch) else current.add(event.branch)
                    s.copy(allowedBranches = current)
                }
            }

            // ── Step 2 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.PackageChanged   -> _state.update { it.copy(packageLpa = event.v) }
            is AddCompanyFormEvent.StipendChanged   -> _state.update { it.copy(stipendPerMonth = event.v) }
            is AddCompanyFormEvent.LevelChanged     -> _state.update { it.copy(level = event.v) }
            is AddCompanyFormEvent.HiresCountChanged -> _state.update { it.copy(hiresCount = event.v) }

            // ── Step 3 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.StageAdded   -> _state.update { it.copy(recruitmentStages = it.recruitmentStages + event.stage) }
            is AddCompanyFormEvent.StageRemoved -> _state.update {
                it.copy(recruitmentStages = it.recruitmentStages.toMutableList().also { l -> l.removeAt(event.index) })
            }

            // ── Step 4 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.PrepGuideChanged -> _state.update { it.copy(prepGuide = event.v) }
            is AddCompanyFormEvent.RefLinkAdded     -> _state.update { it.copy(referenceLinks = it.referenceLinks + event.link) }
            is AddCompanyFormEvent.RefLinkRemoved   -> _state.update {
                it.copy(referenceLinks = it.referenceLinks.toMutableList().also { l -> l.removeAt(event.index) })
            }
            is AddCompanyFormEvent.PdfSelected -> uploadPdf(event.uri, event.name, event.companyId)

            // ── Step 5 ─────────────────────────────────────────────────────
            is AddCompanyFormEvent.WorkCultureChanged      -> _state.update { it.copy(workCulture = event.v) }
            is AddCompanyFormEvent.InternDeadlineChanged   -> _state.update { it.copy(internDeadline = event.v) }
            is AddCompanyFormEvent.FulltimeDeadlineChanged -> _state.update { it.copy(fulltimeDeadline = event.v) }
            is AddCompanyFormEvent.ContactAdded   -> _state.update { it.copy(placementContacts = it.placementContacts + event.contact) }
            is AddCompanyFormEvent.ContactRemoved -> _state.update {
                it.copy(placementContacts = it.placementContacts.toMutableList().also { l -> l.removeAt(event.index) })
            }

            // ── Submit ─────────────────────────────────────────────────────
            AddCompanyFormEvent.Submit      -> submitForm()
            AddCompanyFormEvent.DismissError -> _state.update { it.copy(submitError = null) }
        }
    }

    private fun uploadPdf(uri: Uri, name: String, companyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isPdfUploading = true, pdfUploadProgress = 0) }
            placementRepository.uploadPdf(uri, companyId.ifBlank { "temp" }, name)
                .collect { state ->
                    when (state) {
                        is UploadState.Progress -> _state.update { it.copy(pdfUploadProgress = state.percent) }
                        is UploadState.Success  -> {
                            _state.update { s ->
                                s.copy(
                                    isPdfUploading = false,
                                    pdfAttachments = s.pdfAttachments + PdfAttachment(
                                        name = name,
                                        storageUrl = state.url,
                                        sizeKb = state.sizeKb
                                    )
                                )
                            }
                            _effect.tryEmit("PDF uploaded: $name")
                        }
                        is UploadState.Error -> {
                            _state.update { it.copy(isPdfUploading = false) }
                            _effect.tryEmit("Upload failed: ${state.message}")
                        }
                    }
                }
        }
    }

    private fun submitForm() {
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true, submitError = null) }
            val s = _state.value
            val detail = CompanyDetail(
                name = s.name,
                tagline = s.tagline,
                description = s.description,
                website = s.website,
                location = s.location,
                employeeCount = s.employeeCount,
                jobRole = s.jobRole,
                roleType = s.roleType,
                isLiveProcess = s.isLiveProcess,
                minCgpa = s.minCgpa,
                tenth = s.tenth,
                twelfth = s.twelfth,
                allowedBacklogs = s.allowedBacklogs.toIntOrNull() ?: 0,
                allowedBranches = s.allowedBranches,
                eligibilityCriteria = EligibilityCriteria(
                    minCgpa = s.minCgpa,
                    noActiveBacklogs = (s.allowedBacklogs.toIntOrNull() ?: 0) == 0,
                    allowedBranches = s.allowedBranches
                ),
                packageLpa = s.packageLpa.takeIf { it.isNotBlank() },
                stipendPerMonth = s.stipendPerMonth.takeIf { it.isNotBlank() },
                level = s.level,
                hiresCount = s.hiresCount.toIntOrNull() ?: 0,
                recruitmentStages = s.recruitmentStages,
                preparationResources = listOfNotNull(
                    if (s.prepGuide.isNotBlank()) PrepResource(
                        title = "Preparation Guide",
                        subtitle = s.prepGuide,
                        icon = PrepResourceIcon.DOCUMENT
                    ) else null
                ),
                referenceLinks = s.referenceLinks,
                pdfAttachments = s.pdfAttachments,
                workCulture = s.workCulture,
                internDeadline = s.internDeadline,
                fulltimeDeadline = s.fulltimeDeadline,
                placementContacts = s.placementContacts,
                isHiring = s.isLiveProcess
            )
            placementRepository.addCompany(detail).fold(
                onSuccess = {
                    _state.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    _effect.tryEmit("✅ Company '${detail.name}' added successfully!")
                },
                onFailure = { e ->
                    _state.update { it.copy(isSubmitting = false, submitError = e.message ?: "Failed to save") }
                }
            )
        }
    }
}
