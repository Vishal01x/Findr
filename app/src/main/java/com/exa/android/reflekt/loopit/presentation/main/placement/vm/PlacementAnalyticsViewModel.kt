package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacedStudentRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.*
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacementAnalyticsViewModel @Inject constructor(
    private val repo: PlacedStudentRepository,
    private val placementRepo: PlacementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementAnalyticsState())
    val state: StateFlow<PlacementAnalyticsState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PlacementAnalyticsSideEffect>()
    val effect: SharedFlow<PlacementAnalyticsSideEffect> = _effect.asSharedFlow()

    private var overallJob: Job? = null
    private var companyJob: Job? = null
    private var listJob: Job? = null

    private var searchDebounceJob: Job? = null

    init {
        loadOverallStats()
        loadCompanies()
    }

    private fun loadCompanies() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            placementRepo.getCompaniesForList().collect { companies ->
                _state.update { it.copy(availableCompanies = companies.map { c -> c.name }) }
            }
        }
    }

    fun onEvent(event: PlacementAnalyticsEvent) {
        when (event) {
            is PlacementAnalyticsEvent.TabSelected -> {
                _state.update { it.copy(selectedTab = event.tabIndex) }
            }
            is PlacementAnalyticsEvent.SearchQueryChanged -> {
                val query = event.query.uppercase()
                _state.update { it.copy(searchQuery = query) }
                
                searchDebounceJob?.cancel()
                if (query.trim().length >= 3) {
                    searchDebounceJob = viewModelScope.launch {
                        delay(200)
                        onEvent(PlacementAnalyticsEvent.SearchCompany(query))
                    }
                } else if (query.trim().isEmpty()) {
                    _state.update { it.copy(isSearchingCompany = false, searchedCompany = "") }
                }
            }
            is PlacementAnalyticsEvent.SearchCompany -> {
                val query = event.companyName.trim()
                if (query.isNotBlank()) {
                    val actualCompany = _state.value.availableCompanies.firstOrNull {
                        it.equals(query, ignoreCase = true)
                    } ?: query
                    _state.update { it.copy(searchedCompany = actualCompany, isSearchingCompany = true) }
                    loadCompanyStats(actualCompany)
                }
            }
        }
    }

    private fun loadOverallStats() {
        overallJob?.cancel()
        overallJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            // One-shot fetch — batch statistics docs are pre-computed and change
            // infrequently, so a live listener is wasteful here.
            val stats = repo.getAllBatchStatistics()
            _state.update { it.copy(overallStats = stats, isLoading = false) }
        }
    }

    private fun loadCompanyStats(company: String) {
        companyJob?.cancel()
        companyJob = viewModelScope.launch {
            // Push company filter to Firestore (server-side whereEqualTo) instead of
            // downloading all students and filtering in memory. Requires an index on
            // the "company" field — see firestore.indexes.json.
            repo.getFilteredStudents(company = company).collect { list ->
                val grouped = list.groupBy { it.batchYear }
                val stats = grouped.map { (year, students) ->
                    val totalHired = students.size
                    val avg = if (totalHired > 0) students.sumOf { it.packageLpa } / totalHired else 0.0
                    CompanyBatchStats(year, totalHired, avg)
                }.sortedBy { it.batchYear }

                _state.update {
                    it.copy(
                        companyStats = stats,
                        isSearchingCompany = false
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        overallJob?.cancel()
        companyJob?.cancel()
        listJob?.cancel()
    }
}
