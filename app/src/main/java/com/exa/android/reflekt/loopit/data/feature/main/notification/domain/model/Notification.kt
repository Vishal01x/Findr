package com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    VIEW, MESSAGE, COMMENT, ENROLL_REQUEST, ENROLL_ACCEPT, ENROLL_REJECT, POST, EVENT, ALERT, INVITATION
}

enum class NotificationCategory {
    ALL, PLACEMENTS, SOCIAL, EVENTS
}

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String = "",
    val userId: String = "",
    val type: NotificationType = NotificationType.ALERT,
    val category: NotificationCategory = NotificationCategory.ALL,
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val senderName: String? = null,
    val senderImageUrl: String? = null,
    val projectImageUrl: String? = null,
    val senderId: String? = null,
    val targetId: String? = null,
    val route: String? = null
) {
    // Required empty constructor for Firestore
    constructor() : this("", "", NotificationType.ALERT, NotificationCategory.ALL, "", "", System.currentTimeMillis(), false, null, null, null, null, null, null)
}
