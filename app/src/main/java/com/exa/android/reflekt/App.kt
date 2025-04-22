package com.exa.android.reflekt

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import android.content.Context
import android.content.Intent
import android.os.Build
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
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    companion object {
        @JvmStatic
        lateinit var app: MyApp
    }
}