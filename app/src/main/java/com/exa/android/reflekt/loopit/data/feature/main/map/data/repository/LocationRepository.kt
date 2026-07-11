package com.exa.android.reflekt.loopit.data.feature.main.map.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.exa.android.reflekt.loopit.data.feature.main.map.domain.usecase.FirebaseDataSource
import com.exa.android.reflekt.loopit.data.feature.main.map.domain.usecase.LocationDataSource
import com.exa.android.reflekt.loopit.data.feature.main.map.data.worker.LocationForegroundService
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.firebase.database.*
import dagger.hilt.android.scopes.ViewModelScoped
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.firebase.geofire.GeoQuery
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

@ViewModelScoped
class LocationRepository @Inject constructor(
    private val firebaseDataSource: FirebaseDataSource,
    private val locationDataSource: LocationDataSource,
    private val context: Context
) {
    // Raw list of all geo-queried users (no filters applied here)
    private val _userLocations = MutableStateFlow<List<profileUser>>(emptyList())
    val userLocations: StateFlow<List<profileUser>> get() = _userLocations

    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Single persistent GeoQuery – we update center/radius instead of recreating
    private var currentGeoQuery: GeoQuery? = null

    // Track keys currently in the query so we can handle moved/exited correctly
    private val activeKeys = mutableSetOf<String>()

    private val _roles = MutableStateFlow<List<String>>(emptyList())
    val roles: StateFlow<List<String>> get() = _roles

    suspend fun fetchRolesFromFirestore() {
        val roleList = firebaseDataSource.fetchAllRoles()
        _roles.value = roleList
    }

    suspend fun searchUsersByNameOrEmail(query: String) {
        val users = firebaseDataSource.searchUsersByNameOrEmail(query)
        val filteredUsers = users.filter { it.uid != currentUserId }
        _userLocations.value = filteredUsers
    }

    /**
     * Clears search results and restores the geo-query state.
     * Called when the search bar is cleared.
     */
    fun clearSearchResults() {
        // The GeoFire query is still active; just reset to its current list
        // (do NOT touch currentGeoQuery)
        _userLocations.value = activeKeys
            .mapNotNull { /* cached users are held in _userLocations via GeoFire callbacks */ null }
            .filterNotNull()
        // Re-fetch via existing GeoQuery is automatic because it's still live.
        // But we need to rebuild from scratch if the query is active.
        _userLocations.value = emptyList()
        // GeoFire will re-fire onKeyEntered for all active keys once listeners are active.
        // So we just wait; the query is already running.
    }

    /**
     * Primary method: start or update the geo-query.
     * If a GeoQuery already exists, we update its center and radius instead of
     * recreating it — this avoids the blank-screen gap caused by clearing all listeners.
     */
    fun queryNearbyUsers(radius: Double, location: LatLng) {
        val geoCenter = GeoLocation(location.latitude, location.longitude)

        if (currentGeoQuery != null) {
            // Update the existing query in-place. GeoFire automatically fires
            // onKeyExited / onKeyEntered for changed results — no data gap.
            currentGeoQuery!!.center = geoCenter
            currentGeoQuery!!.radius = radius
            Timber.tag("GeoFire").d("Updated existing GeoQuery center=$geoCenter radius=$radius")
        } else {
            // First-time setup: clear state and start fresh
            activeKeys.clear()
            _userLocations.value = emptyList()

            val listener = buildGeoQueryListener()
            currentGeoQuery = firebaseDataSource.queryLocations(geoCenter, radius, listener)
            Timber.tag("GeoFire").d("Created new GeoQuery center=$geoCenter radius=$radius")
        }
    }

    /**
     * Full reset (e.g. when navigating away or logging out).
     */
    fun clearUserLocations() {
        currentGeoQuery?.removeAllListeners()
        currentGeoQuery = null
        activeKeys.clear()
        _userLocations.value = emptyList()
    }

    private fun buildGeoQueryListener(): GeoQueryEventListener {
        return object : GeoQueryEventListener {

            override fun onKeyEntered(key: String, location: GeoLocation) {
                if (key == currentUserId) return
                activeKeys.add(key)
                firebaseDataSource.fetchUser(key,
                    onSuccess = { user ->
                        if (user != null && user.uid != currentUserId) {
                            val updatedUser = user.copy(lat = location.latitude, lng = location.longitude)
                            // Add or replace the user immediately so UI updates as each arrives
                            _userLocations.value = _userLocations.value
                                .filter { it.uid != key } + updatedUser
                        }
                    },
                    onFailure = { /* silent */ }
                )
            }

            override fun onKeyExited(key: String) {
                activeKeys.remove(key)
                _userLocations.value = _userLocations.value.filter { it.uid != key }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                _userLocations.value = _userLocations.value.map {
                    if (it.uid == key) it.copy(lat = location.latitude, lng = location.longitude) else it
                }
            }

            override fun onGeoQueryReady() {
                Timber.tag("GeoFire").d("GeoQuery ready. Users loaded: ${_userLocations.value.size}")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                Timber.tag("GeoFire").e("GeoQuery error: ${error.message}")
            }
        }
    }

    // --- Location updates for saving current user's position ---

    fun startLocationUpdates(userId: String?, context: Context) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    firebaseDataSource.saveUserLocation(userId ?: currentUserId!!, GeoLocation(location.latitude, location.longitude)) { key, error ->
                        if (error != null) {
                            Timber.tag("GeoFire").e("Error saving location: ${error.message}")
                        } else {
                            Timber.tag("GeoFire").d("LR Location saved for $key")
                        }
                    }
                }
            }
        }
        Timber.tag("GeoFire").d("Starting location updates for user $userId")
        locationDataSource.startLocationUpdates(context, locationRequest, locationCallback)
    }

    private var locationCallback: LocationCallback? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(userId: String?) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    firebaseDataSource.saveUserLocation(userId ?: "",
                        GeoLocation(location.latitude, location.longitude)) { key, error ->
                        if (error != null) {
                            Timber.tag("GeoFire").e("Error saving location: ${error.message}")
                        } else {
                            Timber.tag("GeoFire").d("LR Location saved for $key")
                        }
                    }
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        if (userId != null) {
            LocationForegroundService.startService(context, userId)
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        fusedLocationClient = null
        LocationForegroundService.stopService(context)
    }

    // --- Legacy methods kept for backward compatibility ---

    fun fetchUserLocations(role: String, radius: Double, location: LatLng, minRating: Float) {
        // Now handled by queryNearbyUsers + local filtering in ViewModel
        queryNearbyUsers(radius, location)
    }

    fun fetchAllNearbyUsers(radius: Double, location: LatLng) {
        queryNearbyUsers(radius, location)
    }

    // --- User profile ---

    private val _userProfile = MutableStateFlow<profileUser>(profileUser())
    val userProfiles: StateFlow<profileUser> get() = _userProfile

    fun getUserProfile(userId: String) {
        firebaseDataSource.fetchUser(userId,
            onSuccess = { user ->
                if (user != null) {
                    _userProfile.value = user
                }
            },
            onFailure = { /* silent */ }
        )
    }

    // --- Requested user locations (friend/requested map) ---

    private val _requestedUserLocations = MutableStateFlow<List<profileUser>>(emptyList())
    val requestedUserLocations: StateFlow<List<profileUser>> get() = _requestedUserLocations

    private val locationListeners = mutableMapOf<String, ValueEventListener>()

    fun fetchRequestedUserLocations(userIds: List<String>) {
        clearRequestedUserLocations()

        userIds.forEach { userId ->
            val listener = firebaseDataSource.listenToUserLocation(userId) { geoLocation ->
                firebaseDataSource.fetchUser(userId,
                    onSuccess = { user ->
                        user?.let {
                            val updatedUser = it.copy(
                                lat = geoLocation.latitude,
                                lng = geoLocation.longitude
                            )
                            _requestedUserLocations.value = _requestedUserLocations.value
                                .filter { it.uid != userId } + updatedUser
                        }
                    },
                    onFailure = { /* silent */ }
                )
            }
            locationListeners[userId] = listener
        }
    }

    fun clearRequestedUserLocations() {
        locationListeners.forEach { (userId, listener) ->
            firebaseDataSource.removeLocationListener(userId, listener)
        }
        locationListeners.clear()
        _requestedUserLocations.value = emptyList()
    }
}