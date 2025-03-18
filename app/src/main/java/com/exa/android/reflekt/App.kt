package com.exa.android.reflekt

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        app = this
    }


    companion object {

        @JvmStatic
        lateinit var app: MyApp

    }
}