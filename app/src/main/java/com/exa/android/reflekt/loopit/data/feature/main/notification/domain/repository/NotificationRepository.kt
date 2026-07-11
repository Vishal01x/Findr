package com.exa.android.reflekt.loopit.data.feature.main.notification.domain.repository

import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notification>>
    suspend fun syncNotificationsRealTime()
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead()
    fun getUnreadCount(): Flow<Int>
    suspend fun acceptInvitation(notificationId: String, postId: String): Result<Unit>
    suspend fun rejectInvitation(notificationId: String): Result<Unit>
}
