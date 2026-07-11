package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

import com.exa.android.reflekt.loopit.presentation.main.placement.model.BatchStatistics

data class CompanyBatchStats(
    val batchYear: Int,
    val totalHired: Int,
    val averagePackage: Double
)

data class PlacementAnalyticsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0 = Overall Trends, 1 = Company Insights
    
    // Overall Trends Data
    val overallStats: List<BatchStatistics> = emptyList(),

    // Company Insights Data
    val searchQuery: String = "",
    val searchedCompany: String? = null,
    val companyStats: List<CompanyBatchStats> = emptyList(),
    val isSearchingCompany: Boolean = false,
    val availableCompanies: List<String> = emptyList()
)

sealed class PlacementAnalyticsEvent {
    data class TabSelected(val tabIndex: Int) : PlacementAnalyticsEvent()
    data class SearchQueryChanged(val query: String) : PlacementAnalyticsEvent()
    data class SearchCompany(val companyName: String) : PlacementAnalyticsEvent()
}

sealed class PlacementAnalyticsSideEffect {
    data class ShowToast(val message: String) : PlacementAnalyticsSideEffect()
}
