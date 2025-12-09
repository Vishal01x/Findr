package com.exa.android.reflekt.loopit.presentation.main.placement.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementEvent
import com.exa.android.reflekt.loopit.data.feature.main.placement.data.viewmodel.PlacementViewModel
import com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model.*
import com.exa.android.reflekt.loopit.data.feature.main.profile.data.repository.ProfileRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubmitReviewViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    var currentStep by mutableStateOf(0)
        private set

    // Step 1: Company & Basic Info
    var companyName by mutableStateOf("")
    var packageType by mutableStateOf("range") // "range" or "discrete"
    var packageMin by mutableStateOf("")
    var packageMax by mutableStateOf("")
    var discretePackages by mutableStateOf(listOf(""))
    var visitMonth by mutableStateOf("")
    var visitYear by mutableStateOf("")
    var academicYear by mutableStateOf("")

    // Step 2: Eligibility & Stats
    var cgpa by mutableStateOf("")
    var selectedBranches by mutableStateOf(listOf<String>())
    var maxBacklogs by mutableStateOf("")
    var otherCriteria by mutableStateOf("")
    var studentsShortlisted by mutableStateOf("")
    var studentsSelected by mutableStateOf("")
    var overallDifficulty by mutableStateOf(Difficulty.MEDIUM)

    // Step 3: Rounds
    var rounds by mutableStateOf(listOf(createEmptyRound(1)))

    // Step 4: Experience & Tips
    var overallExperience by mutableStateOf("")
    var tips by mutableStateOf(listOf(""))

    fun nextStep() {
        if (currentStep < 3) currentStep++
    }

    fun previousStep() {
        if (currentStep > 0) currentStep--
    }

    fun addDiscretePackage() {
        discretePackages = discretePackages + ""
    }

    fun updateDiscretePackage(index: Int, value: String) {
        discretePackages = discretePackages.toMutableList().apply {
            if (index < size) this[index] = value
        }
    }

    fun removeDiscretePackage(index: Int) {
        if (discretePackages.size > 1) {
            discretePackages = discretePackages.filterIndexed { i, _ -> i != index }
        }
    }

    fun toggleBranch(branch: String) {
        selectedBranches = if (selectedBranches.contains(branch)) {
            selectedBranches - branch
        } else {
            selectedBranches + branch
        }
    }

    fun addRound() {
        rounds = rounds + createEmptyRound(rounds.size + 1)
    }

    fun removeRound(index: Int) {
        if (rounds.size > 1) {
            rounds = rounds.filterIndexed { i, _ -> i != index }
                .mapIndexed { i, round -> round.copy(roundNumber = i + 1) }
        }
    }

    fun updateRound(index: Int, round: Round) {
        rounds = rounds.toMutableList().apply {
            if (index < size) this[index] = round
        }
    }

    fun addTip() {
        tips = tips + ""
    }

    fun updateTip(index: Int, value: String) {
        tips = tips.toMutableList().apply {
            if (index < size) this[index] = value
        }
    }

    fun removeTip(index: Int) {
        if (tips.size > 1) {
            tips = tips.filterIndexed { i, _ -> i != index }
        }
    }

    private fun createEmptyRound(number: Int) = Round(
        roundNumber = number,
        roundType = RoundType.APTITUDE,
        difficulty = Difficulty.MEDIUM,
        specificTopics = emptyList(),
        commonTopics = emptyList(),
        keyPoints = emptyList(),
        description = ""
    )

    fun canProceedToNextStep(): Boolean {
        return when (currentStep) {
            0 -> companyName.isNotBlank() && visitMonth.isNotBlank() &&
                 visitYear.isNotBlank() && academicYear.isNotBlank() &&
                 (packageType == "discrete" || (packageMin.isNotBlank() && packageMax.isNotBlank()))
            1 -> studentsShortlisted.isNotBlank() && studentsSelected.isNotBlank()
            2 -> rounds.isNotEmpty()
            3 -> overallExperience.isNotBlank()
            else -> false
        }
    }

    fun submitReview(placementViewModel: PlacementViewModel) {
        viewModelScope.launch {
            try {
                val currentUserId = auth.currentUser?.uid ?: return@launch
                val userProfile = profileRepository.getUserProfile(currentUserId)
                
                val review = buildReview(
                    userId = currentUserId,
                    userName = userProfile.name.ifBlank { "Anonymous" }
                )
                
                placementViewModel.onEvent(PlacementEvent.SubmitReview(review))
            } catch (e: Exception) {
                // Error will be handled by PlacementViewModel
            }
        }
    }

    private fun buildReview(userId: String, userName: String): PlacementReview {
        val packageInfo = if (packageType == "discrete") {
            Package.Discrete(discretePackages.mapNotNull { it.toDoubleOrNull() })
        } else {
            Package.Range(
                packageMin.toDoubleOrNull() ?: 0.0,
                packageMax.toDoubleOrNull() ?: 0.0
            )
        }

        return PlacementReview(
            companyName = companyName.trim(),
            packageInfo = packageInfo,
            visitDate = Timestamp.now(),
            month = visitMonth.trim(),
            year = visitYear.toIntOrNull() ?: 2024,
            academicYear = academicYear.trim(),
            eligibilityCriteria = EligibilityCriteria(
                cgpa = cgpa.toDoubleOrNull(),
                branches = selectedBranches,
                maxBacklogs = maxBacklogs.toIntOrNull(),
                otherCriteria = otherCriteria.trim()
            ),
            studentsShortlisted = studentsShortlisted.toIntOrNull() ?: 0,
            studentsSelected = studentsSelected.toIntOrNull() ?: 0,
            rounds = rounds,
            overallExperience = overallExperience.trim(),
            tips = tips.filter { it.isNotBlank() }.map { it.trim() },
            difficulty = overallDifficulty,
            submittedBy = userId,
            submittedByName = userName,
            submittedAt = Timestamp.now()
        )
    }

    fun reset() {
        currentStep = 0
        companyName = ""
        packageType = "range"
        packageMin = ""
        packageMax = ""
        discretePackages = listOf("")
        visitMonth = ""
        visitYear = ""
        academicYear = ""
        cgpa = ""
        selectedBranches = emptyList()
        maxBacklogs = ""
        otherCriteria = ""
        studentsShortlisted = ""
        studentsSelected = ""
        overallDifficulty = Difficulty.MEDIUM
        rounds = listOf(createEmptyRound(1))
        overallExperience = ""
        tips = listOf("")
    }
}
