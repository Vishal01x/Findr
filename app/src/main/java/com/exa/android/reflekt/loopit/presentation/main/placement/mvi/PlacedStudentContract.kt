package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

import com.exa.android.reflekt.loopit.presentation.main.placement.model.PlacedStudent

// ─── State ────────────────────────────────────────────────────────────────────
data class PlacedStudentState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val students: List<PlacedStudent> = emptyList(),      // filtered + sorted result
    val availableBatches: List<Int> = emptyList(),
    val selectedBatch: Int? = null,                       // null = All batches
    val selectedCompany: String? = null,                  // null = All companies
    val selectedType: String? = null,                     // null=All | "FTE" | "Intern"
    val packageRange: ClosedFloatingPointRange<Float> = 0f..100f,
    val selectedCompanies: List<String> = emptyList(),
    val isAdmin: Boolean = false,
    val showFilterSheet: Boolean = false,
    val showCompanySearchSheet: Boolean = false,
    val error: String? = null,
    // Stats computed from current filter
    val totalPlaced: Int = 0,
    val avgPackageLpa: Double = 0.0,
    val topPackageLpa: Double = 0.0,
    val topPackageDisplay: String = "",
    val topStudentName: String = "",
    val topCompanyName: String = "",
    val liveEvents: List<com.exa.android.reflekt.loopit.presentation.main.placement.model.LiveEventModel> = emptyList(),
    val packageDistribution: Map<String, Int> = emptyMap()
)

// ─── Events ───────────────────────────────────────────────────────────────────
sealed class PlacedStudentEvent {
    /** Initial load — pass optional pre-filters from navigation args */
    data class Load(
        val preFilterCompany: String? = null,
        val preFilterBatch: Int? = null
    ) : PlacedStudentEvent()

    data class BatchSelected(val batch: Int?) : PlacedStudentEvent()
    data class CompanyFilterChanged(val company: String?) : PlacedStudentEvent()
    data class TypeSelected(val type: String?) : PlacedStudentEvent()
    data class PackageRangeChanged(val range: ClosedFloatingPointRange<Float>) : PlacedStudentEvent()
    data class ToggleCompanySelection(val company: String) : PlacedStudentEvent()
    object ClearFilters : PlacedStudentEvent()
    object ToggleFilterSheet : PlacedStudentEvent()
    object ToggleCompanySearchSheet : PlacedStudentEvent()
    data class OpenLinkedIn(val url: String) : PlacedStudentEvent()
    object LoadMore : PlacedStudentEvent()
    data class ApplyFilters(
        val type: String?,
        val packageRange: ClosedFloatingPointRange<Float>,
        val companies: List<String>
    ) : PlacedStudentEvent()
}

// ─── Side Effects ─────────────────────────────────────────────────────────────
sealed class PlacedStudentSideEffect {
    data class OpenUrl(val url: String) : PlacedStudentSideEffect()
    data class ShowToast(val message: String) : PlacedStudentSideEffect()
}
