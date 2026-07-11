package com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

/**
 * Represents a profile view record
 * Tracks when and who viewed a user's profile
 */
data class ProfileView(
    val id: String = "",
    val profileOwnerId: String = "", // The user whose profile was viewed
    val viewerId: String = "", // The user who viewed the profile
    val viewerName: String = "",
    val viewerProfilePic: String? = null,
    @ServerTimestamp val viewedAt: Timestamp? = null
) {
    /**
     * Checks if this view is from today
     */
    fun isToday(): Boolean {
        val now = System.currentTimeMillis()
        val viewTime = viewedAt?.toDate()?.time ?: return false
        val dayInMillis = 24 * 60 * 60 * 1000
        return (now - viewTime) < dayInMillis
    }
    
    /**
     * Gets a formatted time string
     */
    fun getTimeAgo(): String {
        val viewTime = viewedAt?.toDate()?.time ?: return "Unknown"
        return com.exa.android.reflekt.loopit.core.utils.formatTimeAgo(viewTime)
    }
}
