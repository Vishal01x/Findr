package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp

/**
 * Domain model for event-specific details
 * Used when creating event posts to specify event information
 */
data class EventDetails(
    val eventDate: Timestamp? = null,
    val eventTime: String? = null,              // Format: "HH:mm" or "2:00 PM"
    val location: String? = null,               // City/Area
    val venue: String? = null,                  // Specific venue name
    val capacity: Int? = null,                  // Maximum attendees
    val registrationDeadline: Timestamp? = null,
    val isVirtual: Boolean = false,
    val meetingLink: String? = null,            // For virtual events
    val registeredCount: Int = 0,               // Current registrations
    val isManuallyClosed: Boolean = false       // Manual closure by author
) {
    /**
     * Validates if event details have required fields
     */
    fun isValid(): Boolean {
        // At minimum, event should have a date
        if (eventDate == null) return false
        
        // If virtual, must have meeting link
        if (isVirtual && meetingLink.isNullOrBlank()) return false
        
        // If physical, should have location
        if (!isVirtual && location.isNullOrBlank()) return false
        
        return true
    }
    
    /**
     * Checks if event is full
     */
    fun isFull(): Boolean {
        return capacity != null && registeredCount >= capacity
    }
    
    /**
     * Gets remaining capacity
     */
    fun remainingCapacity(): Int? {
        return capacity?.let { (it - registeredCount).coerceAtLeast(0) }
    }
    
    /**
     * Checks if registration is still open
     */
    fun isRegistrationOpen(): Boolean {
        if (isManuallyClosed) return false
        if (isFull()) return false
        
        registrationDeadline?.let { deadline ->
            val now = Timestamp.now()
            if (now.seconds > deadline.seconds) return false
        }
        
        return true
    }
    
    /**
     * Returns formatted location string
     */
    fun getLocationDisplay(): String {
        return if (isVirtual) {
            "Virtual Event"
        } else {
            listOfNotNull(venue, location)
                .filter { it.isNotBlank() }
                .joinToString(", ")
                .ifBlank { "Location TBA" }
        }
    }
}
