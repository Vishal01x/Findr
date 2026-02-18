package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents a registration for an event
 */
data class EventRegistration(
    @DocumentId val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfilePic: String? = null,
    val status: RegistrationStatus = RegistrationStatus.REGISTERED,
    @ServerTimestamp val registeredAt: Timestamp? = null,
    val attendanceConfirmed: Boolean = false
) {
    fun isRegistered() = status == RegistrationStatus.REGISTERED
    fun isCancelled() = status == RegistrationStatus.CANCELLED
    fun isAttended() = status == RegistrationStatus.ATTENDED
}

/**
 * Status of an event registration
 */
enum class RegistrationStatus(val displayName: String) {
    REGISTERED("Registered"),
    CANCELLED("Cancelled"),
    ATTENDED("Attended");
    
    companion object {
        fun fromDisplayName(name: String): RegistrationStatus? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}
