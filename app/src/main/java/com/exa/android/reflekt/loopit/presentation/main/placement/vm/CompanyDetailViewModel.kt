package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacedStudentRepository
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyBatchStats
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyDetailEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyDetailSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyDetailState
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.CompanyDetailTab
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.EligibilityResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompanyDetailViewModel @Inject constructor(
    private val placementRepository: PlacementRepository,
    private val placedStudentRepository: PlacedStudentRepository
) : ViewModel() {

    private var batchStatsJob: Job? = null

    // ─── State ────────────────────────────────────────────────────────────────
    private val _state = MutableStateFlow(CompanyDetailState())
    val state: StateFlow<CompanyDetailState> = _state.asStateFlow()

    // ─── Side-Effects (one-time events) ───────────────────────────────────────
    private val _effect = Channel<CompanyDetailSideEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    // ─── Intent Handler ───────────────────────────────────────────────────────
    fun onEvent(event: CompanyDetailEvent) {
        when (event) {
            is CompanyDetailEvent.LoadCompany   -> loadCompany(event.companyId)
            is CompanyDetailEvent.TabSelected   -> onTabSelected(event.tab)
            is CompanyDetailEvent.ToggleBookmark -> toggleBookmark()
            is CompanyDetailEvent.PrimaryActionTapped -> onPrimaryAction()
            is CompanyDetailEvent.ViewTasksTapped -> sendEffect(CompanyDetailSideEffect.NavigateToTaskList)
            is CompanyDetailEvent.ViewAllAlumniTapped -> sendEffect(CompanyDetailSideEffect.NavigateToAlumniList)
            is CompanyDetailEvent.OpenLinkedIn  -> sendEffect(CompanyDetailSideEffect.OpenUrl(event.url))
            is CompanyDetailEvent.OpenEmail     -> sendEffect(CompanyDetailSideEffect.OpenUrl("mailto:${event.email}"))
            is CompanyDetailEvent.OpenResource  -> sendEffect(CompanyDetailSideEffect.OpenUrl(event.url))
            is CompanyDetailEvent.ToggleShareSheet -> _state.update { it.copy(showShareSheet = true) }
            is CompanyDetailEvent.PrepareShare -> prepareShare(event.context)
            is CompanyDetailEvent.ExecuteShare -> executeShare(event.context)
            is CompanyDetailEvent.CloseShareSheet -> _state.update { it.copy(showShareSheet = false, shareData = null) }
            is CompanyDetailEvent.NavigateBack  -> sendEffect(CompanyDetailSideEffect.NavigateBack)
            is CompanyDetailEvent.ViewPlacedStudents -> sendEffect(CompanyDetailSideEffect.NavigateToPlacedStudents(event.batchYear))
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────
    private fun loadCompany(companyId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            placementRepository.getCompanyById(companyId).fold(
                onSuccess = { company ->
                    if (company != null) {
                        _state.update { it.copy(isLoading = false, company = company) }
                        loadBatchPlacementStats(company.name)
                    } else {
                        _state.update { it.copy(isLoading = false, error = "Company not found") }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load company") }
                }
            )
        }
    }

    private fun loadBatchPlacementStats(companyName: String) {
        batchStatsJob?.cancel()
        batchStatsJob = viewModelScope.launch {
            placedStudentRepository.getFilteredStudents(company = companyName).collect { students ->
                val stats = students.groupBy { it.batchYear }
                    .map { (year, batch) ->
                        val totalHired = batch.size
                        val avg = if (totalHired > 0) batch.sumOf { it.packageLpa } / totalHired else 0.0
                        CompanyBatchStats(year, totalHired, avg)
                    }
                    .sortedBy { it.batchYear }
                _state.update { it.copy(batchPlacementStats = stats) }
            }
        }
    }

    private fun onTabSelected(tab: CompanyDetailTab) {
        _state.update { it.copy(selectedTab = tab) }
    }

    private fun toggleBookmark() {
        // TODO: persist bookmark state
        sendEffect(CompanyDetailSideEffect.ShowToast("Saved to your drive list"))
    }

    private fun onPrimaryAction() {
        val company = _state.value.company ?: return
        viewModelScope.launch {
            // If eligibility has not been checked yet, navigate to eligibility form; otherwise register
            if (company.isEligible == null) {
                sendEffect(CompanyDetailSideEffect.NavigateToEligibilityForm)
            } else {
                sendEffect(CompanyDetailSideEffect.ShowToast("Registered for Drive!"))
            }
        }
    }

    private fun checkEligibility() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingEligibility = true) }
            delay(800) // simulate check
            // TODO: real eligibility check against user profile
            val isEligible = true
            val reasons = if (!isEligible) listOf("CGPA below cutoff") else emptyList()
            _state.update {
                it.copy(
                    isCheckingEligibility = false,
                    eligibilityResult = EligibilityResult(isEligible, reasons)
                )
            }
            sendEffect(
                CompanyDetailSideEffect.ShowToast(
                    if (isEligible) "✅ You are eligible!" else "❌ Not eligible: ${reasons.joinToString()}"
                )
            )
        }
    }

    private fun sendEffect(effect: CompanyDetailSideEffect) {
        viewModelScope.launch { _effect.send(effect) }
    }

    private fun prepareShare(context: android.content.Context) {
        val company = _state.value.company ?: return
        viewModelScope.launch {
            val shareData = com.exa.android.reflekt.loopit.core.utils.ShareUtils.prepareCompanyShareData(context, company)
            _state.update { it.copy(shareData = shareData) }
        }
    }

    private fun executeShare(context: android.content.Context) {
        val data = _state.value.shareData
        if (data != null) {
            viewModelScope.launch {
                com.exa.android.reflekt.loopit.core.utils.ShareUtils.executeShare(context, data)
                _state.update { it.copy(showShareSheet = false, shareData = null) }
            }
        }
    }
}
