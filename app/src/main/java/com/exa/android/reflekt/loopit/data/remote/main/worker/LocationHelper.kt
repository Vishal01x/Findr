package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocationHelper @Inject constructor() {

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context): Location? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        var lastLocation: Location? = null

        val latch = CountDownLatch(1)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                lastLocation = location
                latch.countDown()
            }
            .addOnFailureListener { latch.countDown() }

        latch.await(3, TimeUnit.SECONDS)
        return lastLocation
    }
}
