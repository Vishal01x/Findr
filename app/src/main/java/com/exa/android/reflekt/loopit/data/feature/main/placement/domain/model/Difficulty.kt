package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    companion object {
        fun fromString(value: String): Difficulty {
            return entries.find { it.name == value } ?: MEDIUM
        }
    }
}
