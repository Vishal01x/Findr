package com.exa.android.reflekt.loopit.presentation.main.placement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.PlacementRepository
import com.exa.android.reflekt.loopit.presentation.main.placement.model.DifficultyLevel
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.DsaLevel
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.EligibilityStatus
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionEvent
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionSideEffect
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PlacementPredictionState
import com.exa.android.reflekt.loopit.presentation.main.placement.mvi.PredictionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlacementPredictionViewModel @Inject constructor(
    private val placementRepository: PlacementRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PlacementPredictionState())
    val state: StateFlow<PlacementPredictionState> = _state.asStateFlow()

    private val _effect = Channel<PlacementPredictionSideEffect>(Channel.BUFFERED)
    val effect: Flow<PlacementPredictionSideEffect> = _effect.receiveAsFlow()

    fun onEvent(event: PlacementPredictionEvent) {
        when (event) {
            is PlacementPredictionEvent.CgpaChanged ->
                _state.update { it.copy(cgpa = event.value) }

            is PlacementPredictionEvent.DsaLevelChanged ->
                _state.update { it.copy(dsaLevel = event.level) }

            is PlacementPredictionEvent.ProjectsCountChanged ->
                _state.update { it.copy(projectsCount = event.count) }

            is PlacementPredictionEvent.InterestToggled -> _state.update { current ->
                val updated = current.interests.toMutableSet()
                if (event.interest in updated) updated.remove(event.interest) else updated.add(event.interest)
                current.copy(interests = updated)
            }

            is PlacementPredictionEvent.BacklogsChanged ->
                _state.update { it.copy(hasBacklogs = event.has) }

            PlacementPredictionEvent.RunPrediction -> runPrediction()

            PlacementPredictionEvent.ResetResults ->
                _state.update { it.copy(showResults = false, results = null) }
        }
    }

    /**
     * Called from the screen when the user taps "View Details →" on a result card.
     * Emits a side effect that the screen collects and routes to [onNavigateToCompanyDetail].
     */
    fun onCompanyClicked(companyId: String) {
        viewModelScope.launch {
            _effect.send(PlacementPredictionSideEffect.NavigateToCompanyDetail(companyId))
        }
    }

    private fun runPrediction() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val current = _state.value
            try {
                // One-shot snapshot — take the first emission from the live companies flow.
                val companies = placementRepository.getCompanies().first()

                val results = companies.map { company ->
                    // Resolve CGPA cutoff: prefer eligibilityCriteria.minCgpa (cleaner numeric
                    // string) then fall back to cgpaCutoff ("7.5+") or minCgpa ("8.0 and above").
                    val cutoffStr = company.eligibilityCriteria.minCgpa.takeIf { it.isNotBlank() }
                        ?: company.cgpaCutoff.takeIf { it.isNotBlank() }
                        ?: company.minCgpa
                    val cutoffRaw = cutoffStr
                        .replace("+", "")
                        .replace("and above", "", ignoreCase = true)
                        .replace(" ", "")
                        .trim()
                    val cutoff = cutoffRaw.toFloatOrNull() ?: 0f

                    val userCgpa = current.cgpa
                    val backlogViolation = current.hasBacklogs && company.eligibilityCriteria.noActiveBacklogs
                    val cgpaMet = userCgpa >= cutoff
                    val cgpaAlmost = userCgpa >= (cutoff - 0.5f) && userCgpa < cutoff
                    val isHardCompany = company.level == DifficultyLevel.HARD
                    val dsaWeak = current.dsaLevel == DsaLevel.BEGINNER ||
                            current.dsaLevel == DsaLevel.INTERMEDIATE

                    val status: EligibilityStatus
                    val reason: String

                    when {
                        backlogViolation -> {
                            status = EligibilityStatus.NOT_ELIGIBLE
                            reason = "No active backlogs required — you have active backlogs"
                        }
                        // CGPA qualifies but it's a hard company and DSA needs work
                        cgpaMet && isHardCompany && dsaWeak -> {
                            status = EligibilityStatus.PREPARE_MORE
                            reason = "CGPA meets cutoff, but strengthen DSA to clear ${company.name}'s rounds"
                        }
                        cgpaMet -> {
                            status = EligibilityStatus.ELIGIBLE
                            reason = if (cutoff > 0f)
                                "CGPA: ${String.format("%.1f", cutoff)} required ✓ (you have ${String.format("%.1f", userCgpa)})"
                            else
                                "No minimum CGPA cutoff — you qualify"
                        }
                        // Within 0.5 of the cutoff
                        cgpaAlmost -> {
                            status = EligibilityStatus.PREPARE_MORE
                            reason = "CGPA: ${String.format("%.1f", cutoff)} required (you have ${String.format("%.1f", userCgpa)}) — almost there!"
                        }
                        else -> {
                            status = EligibilityStatus.NOT_ELIGIBLE
                            reason = "CGPA: ${String.format("%.1f", cutoff)} required (you have ${String.format("%.1f", userCgpa)})"
                        }
                    }

                    val packageDisplay = company.packageLpa ?: company.stipendPerMonth ?: "—"

                    PredictionResult(
                        companyId = company.id,
                        companyName = company.name,
                        packageDisplay = packageDisplay,
                        role = company.jobRole,
                        eligibilityStatus = status,
                        reason = reason
                    )
                }.sortedBy { result ->
                    when (result.eligibilityStatus) {
                        EligibilityStatus.ELIGIBLE -> 0
                        EligibilityStatus.PREPARE_MORE -> 1
                        EligibilityStatus.NOT_ELIGIBLE -> 2
                    }
                }

                _state.update { it.copy(isLoading = false, results = results, showResults = true) }
            } catch (e: Exception) {
                // Surface loading=false so the CTA button becomes tappable again.
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        _effect.close()
    }
}
