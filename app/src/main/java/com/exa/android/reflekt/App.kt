package com.exa.android.reflekt

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import android.content.Context

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Places.initialize(applicationContext, this.getString(R.string.PLACE_API_KEY))
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree()) // Logs only in Debug mode
        }
        app = this
    }


    companion object {

        @JvmStatic
        lateinit var app: MyApp

    }
}