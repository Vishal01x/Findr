package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacedStudentRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementAdminRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacedStudentState
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacedStudentViewModel @Inject constructor(
    private val repo: PlacedStudentRepository,
    private val adminRepo: PlacementAdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlacedStudentState())
    val state: StateFlow<PlacedStudentState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.getLiveEvents().collect { events ->
                _state.update { it.copy(liveEvents = events) }
            }
        }
    }

    private val _effect = MutableSharedFlow<PlacedStudentSideEffect>(extraBufferCapacity = 8)
    val effect: SharedFlow<PlacedStudentSideEffect> = _effect.asSharedFlow()

    /** Cursor for the next Firestore page — never exposed in UI state (not serializable). */
    private var lastDocument: DocumentSnapshot? = null

    private var studentsJob: Job? = null
    private var debounceJob: Job? = null

    private fun scheduleReload() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(100)
            reloadStudents()
        }
    }

    fun onEvent(event: PlacedStudentEvent) {
        when (event) {
            is PlacedStudentEvent.Load -> load(event.preFilterCompany, event.preFilterBatch)

            is PlacedStudentEvent.BatchSelected -> {
                _state.update { it.copy(selectedBatch = event.batch) }
                scheduleReload()
            }

            is PlacedStudentEvent.TypeSelected -> {
                _state.update { it.copy(selectedType = event.type) }
                scheduleReload()
            }

            is PlacedStudentEvent.ApplyFilters -> {
                _state.update {
                    it.copy(
                        selectedType = event.type,
                        packageRange = event.packageRange,
                        selectedCompanies = event.companies
                    )
                }
                reloadStudents()
            }

            is PlacedStudentEvent.ClearFilters -> {
                _state.update {
                    it.copy(
                        selectedBatch = null,
                        selectedCompany = null,
                        selectedType = null,
                        selectedCompanies = emptyList(),
                        packageRange = 0f..100f
                    )
                }
                reloadStudents()
            }

            // These update state only — actual filtering deferred until ApplyFilters
            is PlacedStudentEvent.PackageRangeChanged -> {
                _state.update { it.copy(packageRange = event.range) }
            }
            is PlacedStudentEvent.CompanyFilterChanged -> {
                _state.update { it.copy(selectedCompany = event.company) }
            }
            is PlacedStudentEvent.ToggleCompanySelection -> {
                _state.update { st ->
                    val current = st.selectedCompanies.toMutableList()
                    if (current.contains(event.company)) current.remove(event.company)
                    else current.add(event.company)
                    st.copy(selectedCompanies = current)
                }
            }

            is PlacedStudentEvent.LoadMore -> loadMoreStudents()

            is PlacedStudentEvent.ToggleFilterSheet -> {
                _state.update { it.copy(showFilterSheet = !it.showFilterSheet) }
            }
            is PlacedStudentEvent.ToggleCompanySearchSheet -> {
                _state.update { it.copy(showCompanySearchSheet = !it.showCompanySearchSheet) }
            }

            is PlacedStudentEvent.OpenLinkedIn -> {
                viewModelScope.launch {
                    if (event.url.isBlank()) {
                        _effect.emit(PlacedStudentSideEffect.ShowToast("No LinkedIn profile available"))
                    } else {
                        _effect.emit(PlacedStudentSideEffect.OpenUrl(event.url))
                    }
                }
            }
            else -> Unit
        }
    }

    private fun load(preFilterCompany: String?, preFilterBatch: Int?) {
        val currentState = _state.value
        // If data is already loaded and we're just revisiting the tab without new filters, don't reload
        if (currentState.students.isNotEmpty() && 
            currentState.selectedCompany == preFilterCompany && 
            currentState.selectedBatch == (preFilterBatch ?: currentState.availableBatches.maxOrNull())) {
            return
        }

        viewModelScope.launch {
            val isAdmin = adminRepo.isCurrentUserAdmin()
            val batches = repo.getAvailableBatches()
            _state.update {
                it.copy(
                    isAdmin = isAdmin,
                    selectedCompany = preFilterCompany,
                    selectedBatch = preFilterBatch ?: batches.maxOrNull(),
                    availableBatches = batches
                )
            }
            reloadStudents()
        }
    }

    private fun reloadStudents() {
        studentsJob?.cancel()
        val s = _state.value

        // Fetch stats as one-shot
        viewModelScope.launch {
            val stats = repo.getBatchStatistics(s.selectedBatch)
            if (stats != null) {
                _state.update { st ->
                    st.copy(
                        totalPlaced = stats.totalPlaced,
                        avgPackageLpa = stats.averagePackage,
                        topPackageLpa = stats.highestPackage,
                        topPackageDisplay = if (stats.highestPackage > 0) "${stats.highestPackage} LPA" else "",
                        topStudentName = stats.topStudents.firstOrNull()?.name ?: "",
                        topCompanyName = stats.topStudents.firstOrNull()?.company ?: stats.topCompany,
                        packageDistribution = stats.packageDistribution
                    )
                }
            } else {
                _state.update { st ->
                    st.copy(
                        totalPlaced = 0, avgPackageLpa = 0.0, topPackageLpa = 0.0,
                        topPackageDisplay = "", topStudentName = "", topCompanyName = "",
                        packageDistribution = emptyMap()
                    )
                }
            }
        }

        // Fetch first page
        studentsJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, students = emptyList(), canLoadMore = false) }
            lastDocument = null
            try {
                val page = repo.fetchStudentsPage(
                    batchYear         = s.selectedBatch,
                    company           = s.selectedCompany,
                    type              = s.selectedType,
                    isAdmin           = s.isAdmin,
                    packageRange      = s.packageRange,
                    selectedCompanies = s.selectedCompanies
                )
                lastDocument = page.lastDocument
                _state.update { st ->
                    st.copy(isLoading = false, students = page.students, canLoadMore = page.hasMore)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadMoreStudents() {
        val s = _state.value
        if (s.isLoadingMore || !s.canLoadMore || lastDocument == null) return

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            try {
                val page = repo.fetchStudentsPage(
                    batchYear         = s.selectedBatch,
                    company           = s.selectedCompany,
                    type              = s.selectedType,
                    isAdmin           = s.isAdmin,
                    packageRange      = s.packageRange,
                    selectedCompanies = s.selectedCompanies,
                    afterDocument     = lastDocument
                )
                if (page.lastDocument != null) lastDocument = page.lastDocument
                _state.update { st ->
                    st.copy(
                        isLoadingMore = false,
                        students = st.students + page.students,
                        canLoadMore = page.hasMore
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        studentsJob?.cancel()
        debounceJob?.cancel()
    }
}
