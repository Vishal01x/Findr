package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.exa.android.reflekt.loopit.data.remote.main.MapDataSource.FirebaseDataSource
import com.firebase.geofire.GeoLocation
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CompletableDeferred

@HiltWorker
class LocationWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val locationHelper: LocationHelper,
    private val firebaseDataSource: FirebaseDataSource
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = inputData.getString("USER_ID") ?: return Result.failure()

        val location = locationHelper.getCurrentLocation(applicationContext)
        return if (location != null) {
            val geoLocation = GeoLocation(location.latitude, location.longitude)
            val result = CompletableDeferred<Result>()
            firebaseDataSource.saveUserLocation(userId, geoLocation) { _, error ->
                result.complete(if (error == null) Result.success() else Result.retry())
            }
            result.await()
        } else {
            Result.retry()
        }
    }
}
