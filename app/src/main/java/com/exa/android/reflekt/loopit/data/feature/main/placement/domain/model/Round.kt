package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

data class Round(
    val roundNumber: Int,
    val roundType: RoundType,
    val difficulty: Difficulty,
    val specificTopics: List<String> = emptyList(),
    val commonTopics: List<String> = emptyList(),
    val keyPoints: List<String> = emptyList(),
    val description: String = ""
)
