package com.exa.android.reflekt.loopit.util.application

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.util.CurChatManager.activeChatId
import android.Manifest
import com.exa.android.reflekt.loopit.data.remote.main.worker.LocationForegroundService

class MyLifecycleObserver(
    private val viewModel: UserViewModel,
    private val locationViewModel: LocationViewModel,
    private val userId: String,
    private val context: Context
) : LifecycleObserver {


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        viewModel.updateOnlineStatus(userId, true)
        val requiredPermissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        ) + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        } else {
            emptyList()
        }

        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }) {
            LocationForegroundService.startService(context.applicationContext, userId)
        }
    }

//    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
//    fun onAppPause() {
//        viewModel.updateOnlineStatus(userId, true)
//    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackground() {
        viewModel.updateOnlineStatus(userId, false)
        viewModel.setTypingStatus(userId, "") // when user while typing click home button then decompose will not be called
        //activeChatId = null // update it in detail chat using observer in that
        // that let the status typing but it should be offline
    }
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        locationViewModel.stopLocationUpdates()
    }
    private fun hasLocationPermissions(): Boolean {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}