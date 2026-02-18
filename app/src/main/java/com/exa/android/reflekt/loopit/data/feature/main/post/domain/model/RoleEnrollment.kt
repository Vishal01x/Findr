package com.exa.android.reflekt.loopit.data.feature.main.post.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents an enrollment request for a specific project role
 */
data class RoleEnrollment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val roleId: String = "",
    val roleName: String = "",                  // Display name of the role
    val userId: String = "",
    val userName: String = "",
    val userProfilePic: String? = null,
    val status: EnrollmentStatus = EnrollmentStatus.PENDING,
    @ServerTimestamp val requestedAt: Timestamp? = null,
    @ServerTimestamp val respondedAt: Timestamp? = null,
    val message: String? = null // Optional message from user
) {
    fun isPending() = status == EnrollmentStatus.PENDING
    fun isAccepted() = status == EnrollmentStatus.ACCEPTED
    fun isRejected() = status == EnrollmentStatus.REJECTED
    
    /**
     * Get formatted timestamp string
     */
    fun getFormattedTimestamp(): String {
        val timestamp = respondedAt ?: requestedAt
        timestamp?.let {
            val now = System.currentTimeMillis()
            val diff = now - it.toDate().time
            val hours = diff / (1000 * 60 * 60)
            val days = hours / 24
            
            return when {
                days > 0 -> "${days}d ago"
                hours > 0 -> "${hours}h ago"
                else -> "Recently"
            }
        }
        return ""
    }
}

/**
 * Status of an enrollment request
 */
//enum class EnrollmentStatus(val displayName: String) {
//    PENDING("Pending"),
//    ACCEPTED("Accepted"),
//    REJECTED("Rejected"),
//    NOT_ENROLLED("Not Enrolled");
//
//
//    companion object {
//        fun fromDisplayName(name: String): EnrollmentStatus? {
//            return values().find { it.displayName.equals(name, ignoreCase = true) }
//        }
//    }
//}
