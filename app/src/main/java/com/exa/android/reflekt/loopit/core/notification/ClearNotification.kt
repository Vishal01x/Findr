package com.exa.android.reflekt.loopit.core.notification

import android.app.NotificationManager
import android.content.Context


fun clearChatNotifications(context: Context, chatId: String) {
    // Get SharedPreferences using context
    val sharedPreferences = context.getSharedPreferences("chat_notifications", Context.MODE_PRIVATE)
    sharedPreferences.edit().remove(chatId).apply() // Remove the chatId from SharedPreferences means all unread message associated with this chat will be removed

    // Get NotificationManager and cancel the notification
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancel(chatId.hashCode()) // Remove notifications for this chat

//    val manager = NotificationManagerCompat.from(context)
//    manager.activeNotifications.forEach {
//        if (it.notification.group == "group_chat_${chatId}") {
//            manager.cancel(it.id)
//        }
//    }

}


fun clearAllNotifications(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll() // Remove notifications for this app
}
