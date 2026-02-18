package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

/**
 * Enrollment status for project roles and event registrations
 */
enum class EnrollmentStatus(val displayName: String) {
    NOT_ENROLLED("Not Enrolled"),  // User has not enrolled yet
    PENDING("Pending"),            // Enrollment request is pending approval
    ACCEPTED("Accepted"),          // Enrollment request has been accepted
    REJECTED("Rejected");          // Enrollment request has been rejected
    
    companion object {
        /**
         * Get status from display name
         */
        fun fromDisplayName(name: String): EnrollmentStatus? {
            return values().find { it.displayName.equals(name, ignoreCase = true) }
        }
        
        /**
         * Check if status allows new enrollment
         */
        fun canEnroll(status: EnrollmentStatus?): Boolean {
            return status == null || status == NOT_ENROLLED || status == REJECTED
        }
        
        /**
         * Check if status is in progress
         */
        fun isInProgress(status: EnrollmentStatus?): Boolean {
            return status == PENDING || status == ACCEPTED
        }
    }
}
