package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

data class EligibilityCriteria(
    val cgpa: Double? = null,
    val branches: List<String> = emptyList(),
    val maxBacklogs: Int? = null,
    val otherCriteria: String = ""
)
