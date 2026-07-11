package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

import android.content.Context
import com.exa.android.reflekt.loopit.core.utils.ShareData
import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail

// ─── MVI Contract ─────────────────────────────────────────────────────────────

/** All possible states the Company Detail screen can be in. */
data class CompanyDetailState(
    val isLoading: Boolean = true,
    val company: CompanyDetail? = null,
    val error: String? = null,
    val selectedTab: CompanyDetailTab = CompanyDetailTab.OVERVIEW,
    val isCheckingEligibility: Boolean = false,
    val eligibilityResult: EligibilityResult? = null,
    val showShareSheet: Boolean = false,
    val shareData: ShareData? = null,
    val batchPlacementStats: List<CompanyBatchStats> = emptyList()
)

enum class CompanyDetailTab { OVERVIEW, PROCESS, PREP_GUIDE, ELIGIBILITY, CULTURE, REVIEWS }

data class EligibilityResult(
    val isEligible: Boolean,
    val reasons: List<String> = emptyList()
)

/** All user actions that can be dispatched from the UI. */
sealed class CompanyDetailEvent {
    /** Load company data for the given [companyId]. */
    data class LoadCompany(val companyId: String) : CompanyDetailEvent()
    
    data class ViewPlacedStudents(val batchYear: Int) : CompanyDetailEvent()

    /** User switched to a different tab. */
    data class TabSelected(val tab: CompanyDetailTab) : CompanyDetailEvent()

    /** User tapped the bookmark / save button. */
    object ToggleBookmark : CompanyDetailEvent()

    /** User tapped the share button. */
    object ToggleShareSheet : CompanyDetailEvent()

    /** Start preparing share payload in background. */
    data class PrepareShare(val context: Context) : CompanyDetailEvent()

    /** Fire the system share intent. */
    data class ExecuteShare(val context: Context) : CompanyDetailEvent()

    /** User dismissed the share sheet. */
    object CloseShareSheet : CompanyDetailEvent()

    /** User tapped "Register for Drive" / "Check My Eligibility". */
    object PrimaryActionTapped : CompanyDetailEvent()

    /** User tapped "View Tasks" link in preparation section. */
    object ViewTasksTapped : CompanyDetailEvent()

    /** User tapped "View All" alumni link. */
    object ViewAllAlumniTapped : CompanyDetailEvent()

    /** User wants to contact a placement officer via LinkedIn. */
    data class OpenLinkedIn(val url: String) : CompanyDetailEvent()

    /** User wants to contact a placement officer via email. */
    data class OpenEmail(val email: String) : CompanyDetailEvent()

    /** User tapped a prep resource link. */
    data class OpenResource(val url: String) : CompanyDetailEvent()

    /** Navigate back. */
    object NavigateBack : CompanyDetailEvent()
}

/** One-time side effects that the ViewModel emits to the screen. */
sealed class CompanyDetailSideEffect {
    data class OpenUrl(val url: String) : CompanyDetailSideEffect()
    data class ShowToast(val message: String) : CompanyDetailSideEffect()
    object NavigateBack : CompanyDetailSideEffect()
    object NavigateToTaskList : CompanyDetailSideEffect()
    object NavigateToAlumniList : CompanyDetailSideEffect()
    object NavigateToEligibilityForm : CompanyDetailSideEffect()
    data class NavigateToPlacedStudents(val batchYear: Int) : CompanyDetailSideEffect()
}
