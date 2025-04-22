package com.exa.android.reflekt.loopit.data.remote.main.MapDataSource
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import javax.inject.Inject
import android.Manifest
import timber.log.Timber

class LocationDataSource @Inject constructor(
    private val locationProvider: FusedLocationProviderClient
) {
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(
        context: Context,
        locationRequest: LocationRequest,
        callback: LocationCallback
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.   permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            Timber.tag("Location").d("Permission granted")
            locationProvider.requestLocationUpdates(locationRequest, callback, Looper.getMainLooper())
        } else {
            Timber.tag("Location").e("Permission not granted")
        }
    }

    fun stopLocationUpdates(callback: LocationCallback) {
        locationProvider.removeLocationUpdates(callback)
    }
}