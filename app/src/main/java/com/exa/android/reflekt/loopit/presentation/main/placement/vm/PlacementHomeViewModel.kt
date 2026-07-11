package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacedStudentRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementAdminRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementDataSeeder
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementHomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacementHomeViewModel @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val adminRepository: PlacementAdminRepository,
    private val seeder: PlacementDataSeeder,
    private val studentRepository: PlacedStudentRepository
) : ViewModel() {

    // ─── State ────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(PlacementHomeState())
    val state: StateFlow<PlacementHomeState> = _state.asStateFlow()

    // ─── One-time side-effects ────────────────────────────────────────────────
    private val _effect = Channel<PlacementHomeSideEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        onEvent(PlacementHomeEvent.LoadData)
    }

    // ─── Intent Handler ───────────────────────────────────────────────────────
    fun onEvent(event: PlacementHomeEvent) {
        when (event) {
            is PlacementHomeEvent.LoadData              -> loadData()
            is PlacementHomeEvent.CompanyClicked        -> sendEffect(PlacementHomeSideEffect.NavigateToCompanyDetail(event.id))
            is PlacementHomeEvent.AddCompanyClicked     -> sendEffect(PlacementHomeSideEffect.NavigateToAddCompany)
            is PlacementHomeEvent.ViewAllCompaniesClicked -> sendEffect(PlacementHomeSideEffect.NavigateToAllCompanies)
            is PlacementHomeEvent.ViewTimelineClicked   -> sendEffect(PlacementHomeSideEffect.NavigateToTimeline)
            is PlacementHomeEvent.ViewAllAlumniClicked  -> sendEffect(PlacementHomeSideEffect.NavigateToAlumni)
            is PlacementHomeEvent.AlumniBatchSelected   -> _state.update { it.copy(alumniSelectedBatch = event.batch) }
            is PlacementHomeEvent.SeedData              -> seedAllData()
            is PlacementHomeEvent.DismissSeedResult     -> _state.update { it.copy(seedResult = null) }
        }
    }

    // ─── Private ──────────────────────────────────────────────────────────────
    private fun loadData() {
        viewModelScope.launch {
            // Check admin status
            val isAdmin = adminRepository.isCurrentUserAdmin()
            _state.update { it.copy(isAdmin = isAdmin) }
        }
        viewModelScope.launch {
            // Stream company summaries for the home screen (no heavy nested arrays).
            // Full detail is loaded only when the user navigates to a company.
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                placementRepository.getCompaniesForList().collect { companiesList ->
                    _state.update { it.copy(companies = companiesList, isLoading = false) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load companies") }
            }
        }
    }

    private fun sendEffect(effect: PlacementHomeSideEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    // ─── Seed Data (Admin) ────────────────────────────────────────────────────
    private fun seedAllData() {
        if (_state.value.isSeeding) return
        _state.update { it.copy(isSeeding = true, seedResult = null) }
        viewModelScope.launch {
            val result = seeder.seedAll()
            _state.update {
                it.copy(
                    isSeeding = false,
                    seedResult = if (result.isSuccess)
                        "✅ Seeded 11 companies with batch reports & feedback!"
                    else
                        "❌ Seed failed: ${result.exceptionOrNull()?.message}"
                )
            }
        }
    }
}
