package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import timber.log.Timber
import java.util.concurrent.TimeUnit
import android.Manifest
import androidx.annotation.RequiresApi

class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            // Check if Android version allows background start
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Schedule WorkManager task instead
                scheduleLocationWorker(context)
            } else {
                // Proceed only if permissions are granted
                if (hasRequiredPermissions(context)) {
                    val serviceIntent = Intent(context, LocationForegroundService::class.java)
                    ContextCompat.startForegroundService(context, serviceIntent)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun hasRequiredPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.FOREGROUND_SERVICE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun scheduleLocationWorker(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LocationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}