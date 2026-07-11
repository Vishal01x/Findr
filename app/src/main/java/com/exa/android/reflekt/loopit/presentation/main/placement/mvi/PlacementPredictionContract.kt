package com.exa.android.reflekt.loopit.presentation.main.placement.mvi

enum class DsaLevel(val label: String) {
    BEGINNER("Beginner"), INTERMEDIATE("Intermediate"), ADVANCED("Advanced"), EXPERT("Expert")
}

data class PredictionResult(
    val companyId: String,
    val companyName: String,
    val packageDisplay: String,
    val role: String,
    val eligibilityStatus: EligibilityStatus,
    val reason: String
)

enum class EligibilityStatus { ELIGIBLE, PREPARE_MORE, NOT_ELIGIBLE }

data class PlacementPredictionState(
    val cgpa: Float = 7.5f,
    val dsaLevel: DsaLevel = DsaLevel.INTERMEDIATE,
    val projectsCount: Int = 2,
    val interests: Set<String> = emptySet(),
    val hasBacklogs: Boolean = false,
    val results: List<PredictionResult>? = null,
    val isLoading: Boolean = false,
    val showResults: Boolean = false
)

sealed class PlacementPredictionEvent {
    data class CgpaChanged(val value: Float) : PlacementPredictionEvent()
    data class DsaLevelChanged(val level: DsaLevel) : PlacementPredictionEvent()
    data class ProjectsCountChanged(val count: Int) : PlacementPredictionEvent()
    data class InterestToggled(val interest: String) : PlacementPredictionEvent()
    data class BacklogsChanged(val has: Boolean) : PlacementPredictionEvent()
    object RunPrediction : PlacementPredictionEvent()
    object ResetResults : PlacementPredictionEvent()
}

sealed class PlacementPredictionSideEffect {
    data class NavigateToCompanyDetail(val companyId: String) : PlacementPredictionSideEffect()
}
