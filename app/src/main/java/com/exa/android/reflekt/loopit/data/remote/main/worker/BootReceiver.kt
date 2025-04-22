package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Timber.tag("BootReceiver").d("Boot completed. Starting location service.")

            val serviceIntent = Intent(context, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)
        }
    }
}
