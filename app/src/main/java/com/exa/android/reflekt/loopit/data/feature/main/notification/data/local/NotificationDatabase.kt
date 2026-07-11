package com.exa.android.reflekt.loopit.data.feature.main.notification.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.exa.android.reflekt.loopit.data.feature.main.notification.domain.model.Notification

@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}
