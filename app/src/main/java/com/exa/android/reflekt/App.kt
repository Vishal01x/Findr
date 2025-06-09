package com.exa.android.reflekt

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorkerFactory
import javax.inject.Inject
import androidx.work.Configuration

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Places.initialize(applicationContext, this.getString(R.string.PLACE_API_KEY))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        app = this

        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()


    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_CHAT,
                    "Chat Messages",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Incoming chat messages and replies"
                    enableLights(true) // blink LED
                    lightColor = ContextCompat.getColor(this@MyApp, R.color.notification_color)
                },
                NotificationChannel(
                    CHANNEL_PROJECTS,
                    "Project Updates",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Project-related notifications"
                },
                NotificationChannel(
                    CHANNEL_SOCIAL,
                    "Social Activity",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Likes, comments, and profile views"
                },
                NotificationChannel(
                    CHANNEL_PROFILE,
                    "Profile Activity",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Alerts when someone views, rates, or verifies your profile"
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System Updates",
                    NotificationManager.IMPORTANCE_MIN
                ).apply {
                    description = "App updates and maintenance"
                }
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannels(channels)
        }
    }


    companion object {
        @JvmStatic
        lateinit var app: MyApp

        const val CHANNEL_CHAT = "channel_chat"
        const val CHANNEL_PROJECTS = "channel_projects"
        const val CHANNEL_SOCIAL = "channel_social"
        const val CHANNEL_PROFILE = "channel_profile"
        const val CHANNEL_SYSTEM = "channel_system"
    }
}
