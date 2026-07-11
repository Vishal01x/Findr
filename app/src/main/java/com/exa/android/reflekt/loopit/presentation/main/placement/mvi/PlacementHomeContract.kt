package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

import com.exa.android.reflekt.loopit.presentation.main.placement.model.CompanyDetail

// ─── State ────────────────────────────────────────────────────────────────────
data class PlacementHomeState(
    val isLoading: Boolean = true,
    val companies: List<CompanyDetail> = emptyList(),
    val isAdmin: Boolean = false,
    val error: String? = null,
    val isSeeding: Boolean = false,    // true while bulk seed is running
    val seedResult: String? = null,    // success/error message after seeding
    val alumniSelectedBatch: Int? = null  // selected batch on home alumni preview
)

// ─── Intent / Events ──────────────────────────────────────────────────────────
sealed class PlacementHomeEvent {
    object LoadData                          : PlacementHomeEvent()
    data class CompanyClicked(val id: String): PlacementHomeEvent()
    object AddCompanyClicked                 : PlacementHomeEvent()
    object ViewAllCompaniesClicked           : PlacementHomeEvent()
    object ViewTimelineClicked               : PlacementHomeEvent()
    object ViewAllAlumniClicked              : PlacementHomeEvent()
    data class AlumniBatchSelected(val batch: Int?) : PlacementHomeEvent()
    object SeedData                          : PlacementHomeEvent()  // Admin: bulk seed
    object DismissSeedResult                 : PlacementHomeEvent()
}

// ─── Side Effects ─────────────────────────────────────────────────────────────
sealed class PlacementHomeSideEffect {
    data class NavigateToCompanyDetail(val companyId: String) : PlacementHomeSideEffect()
    object NavigateToAddCompany                               : PlacementHomeSideEffect()
    object NavigateToAllCompanies                             : PlacementHomeSideEffect()
    object NavigateToTimeline                                 : PlacementHomeSideEffect()
    object NavigateToAlumni                                   : PlacementHomeSideEffect()
    data class ShowToast(val message: String)                 : PlacementHomeSideEffect()
}
