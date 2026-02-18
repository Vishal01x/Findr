package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import java.util.UUID

/**
 * Domain model for a project role requirement
 * Used when creating project posts to specify team needs
 */
data class ProjectRole(
    val id: String = UUID.randomUUID().toString(),
    val roleName: String = "",              // e.g., "Frontend Developer"
    val technology: String? = null,          // e.g., "React", "Kotlin"
    val positionsNeeded: Int = 1,
    val positionsFilled: Int = 0,
    val description: String? = null,
    val isManuallyClosed: Boolean = false
) {
    /**
     * Validates if the role has required fields
     */
    fun isValid(): Boolean {
        return roleName.isNotBlank() && positionsNeeded > 0
    }
    
    /**
     * Checks if all positions are filled
     */
    fun isFilled(): Boolean {
        return positionsFilled >= positionsNeeded
    }
    
    /**
     * Gets remaining positions
     */
    fun remainingPositions(): Int {
        return (positionsNeeded - positionsFilled).coerceAtLeast(0)
    }

    /**
     * Checks if application is open for this role
     */
    fun isApplicationOpen(): Boolean {
        return !isManuallyClosed && !isFilled()
    }
    
    /**
     * Returns a formatted display string for the role
     * e.g., "Frontend Developer (React) - 2 positions"
     */
    fun getDisplayText(): String {
        val techPart = technology?.let { " ($it)" } ?: ""
        val positionsPart = if (positionsNeeded > 1) {
            " - $positionsNeeded positions"
        } else {
            " - 1 position"
        }
        return "$roleName$techPart$positionsPart"
    }
}
