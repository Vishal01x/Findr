package com.exa.android.reflekt.loopit.data.feature.main.notification.domain

import android.util.Log
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationCategory
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.NotificationType
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object InAppNotificationHelper {
    fun sendNotification(
        firestore: FirebaseFirestore,
        recipientId: String,
        type: NotificationType,
        category: NotificationCategory,
        title: String,
        message: String,
        senderName: String? = null,
        senderImageUrl: String? = null,
        projectImageUrl: String? = null,
        senderId: String? = null,
        targetId: String? = null,
        route: String? = null
    ) {
        if (recipientId.isBlank()) return
        
        val notificationId = UUID.randomUUID().toString()
        val notification = Notification(
            id = notificationId,
            userId = recipientId,
            type = type,
            category = category,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            senderName = senderName,
            senderImageUrl = senderImageUrl,
            projectImageUrl = projectImageUrl,
            senderId = senderId,
            targetId = targetId,
            route = route
        )
        
        firestore.collection("users")
            .document(recipientId)
            .collection("notifications")
            .document(notificationId)
            .set(notification)
            .addOnFailureListener { e ->
                Log.e("InAppNotification", "Failed to send notification to $recipientId", e)
            }
    }
}
