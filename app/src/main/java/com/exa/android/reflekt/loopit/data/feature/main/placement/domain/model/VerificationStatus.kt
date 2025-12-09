package com.exa.android.reflekt.loopit.data.feature.main.placement.domain.model

enum class VerificationStatus(val displayName: String) {
    NOT_VERIFIED("Not Verified"),
    SAFE("Safe"),
    SUSPICIOUS("Suspicious");

    companion object {
        fun fromString(value: String): VerificationStatus {
            return entries.find { it.name == value } ?: NOT_VERIFIED
        }
    }
}
