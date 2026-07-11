package com.exa.android.reflekt.loopit.data.feature.main.suggestion

data class SuggestedUser(
    val userId: String,
    val name: String,
    val profileImageUrl: String?,
    val headline: String,
    val skills: List<String>,       // their top skills
    val matchedSkills: List<String>,// overlap with current user
    val points: Int                 // engagement score
)
