package com.exa.android.reflekt.loopit.data.remote.main.Repository

import android.content.Context
import com.exa.android.reflekt.loopit.data.remote.main.MapDataSource.FirebaseDataSource
import com.exa.android.reflekt.loopit.data.remote.main.MapDataSource.LocationDataSource
import com.exa.android.reflekt.loopit.data.remote.main.worker.LocationForegroundService
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import javax.inject.Inject
import com.exa.android.reflekt.loopit.util.model.UserLocation
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.*
import com.google.firebase.database.*
import dagger.hilt.android.scopes.ViewModelScoped
import com.exa.android.reflekt.loopit.util.model.profileUser
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
    private val _userLocations = MutableStateFlow<List<profileUser>>(emptyList())
    val userLocations: StateFlow<List<profileUser>> get() = _userLocations
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    private var currentGeoQuery: GeoQuery? = null

    private val _roles = MutableStateFlow<List<String>>(emptyList())
    val roles: StateFlow<List<String>> get() = _roles

    suspend fun fetchRolesFromFirestore() {
        val roleList = firebaseDataSource.fetchAllRoles()
        _roles.value = roleList
    }


    fun clearUserLocations() {
        _userLocations.value = emptyList()
        currentGeoQuery?.removeAllListeners()
        currentGeoQuery = null
    }

    fun startLocationUpdates(userId: String?, context: Context) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    firebaseDataSource.saveUserLocation(userId?:currentUserId!!, GeoLocation(location.latitude, location.longitude)) { key, error ->
                        if (error != null) {
                            //Timber.tag("GeoFire").e("Error saving location: ${error.message}")
                        } else {
                            // Timber.tag("GeoFire").d("Location saved for $key")
                        }
                    }
                }
            }
        }
        // Timber.tag("GeoFire").d("Starting location updates for user $userId")
        locationDataSource.startLocationUpdates(context, locationRequest, locationCallback)
    }
    fun startLocationUpdates(userId: String) {
        // Timber.tag("GeoFire").d("Starting foreground service for user $userId")
        LocationForegroundService.startService(context, userId)
    }

    fun stopLocationUpdates() {
        LocationForegroundService.stopService(context)
    }

    fun fetchUserLocations(role: String, radius: Double, location: LatLng, minRating: Float) {
        Timber.tag("GeoFire").d("Fetching user locations for role: $role, radius: $radius, location: $location")
        // clearUserLocations()
        val geoQueryListener = object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                firebaseDataSource.fetchUser(key,
                    onSuccess = { user ->
                        // Check if the fetched role matches the provided role
                        // Timber.tag("LocationSearch").d("Match users: $user")
                        if (user != null && user.uid != currentUserId) {
                            val userRoles = user.role.split(",").map { it.trim().lowercase() } .filter { it.isNotEmpty() }
                            val targetRoles = role.split(",").map { it.trim().lowercase() } .filter { it.isNotEmpty() }
                            Timber.tag("LocationSearch").d("Match users: .$userRoles. .$targetRoles.")
                            // Check if any role from userRoles matches any role from targetRoles
                            if (userRoles.isNotEmpty() && targetRoles.isNotEmpty()) {
                                val isMatching = userRoles.any { userRole ->
                                    targetRoles.any { targetRole ->
                                        userRole == targetRole || userRole.contains(targetRole) || targetRole.contains(userRole)
                                    }
                                }
                                val meetsRating = user.rating >= minRating

                                if (isMatching && meetsRating) {
                                    val updatedUser =
                                        user.copy(lat = location.latitude, lng = location.longitude)

                                    // Timber.tag("GeoFire").d("User location fetched: $updatedUser")
                                    _userLocations.value = _userLocations.value + updatedUser
                                }
                            } else {
                                Timber.tag("LocationSearch").d("Skipping user with no roles.")
                            }
                        }
                    },
                    onFailure = { exception ->
                        // Timber.tag("GeoFire").e(exception, "Error fetching role for user $key")
                    }
                )
            }

            override fun onKeyExited(key: String) {
                _userLocations.value = _userLocations.value.filter { it.uid != key }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                _userLocations.value = _userLocations.value.map {
                    if (it.uid == key) it.copy(lat = location.latitude, lng = location.longitude) else it
                }
            }

            override fun onGeoQueryReady() {
                // Timber.tag("GeoFire").d("All initial data loaded.")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                // Timber.tag("GeoFire").e("GeoQuery error: ${error.message}")
            }
        }

        // Query locations with the provided center and radius
        currentGeoQuery = firebaseDataSource.queryLocations(
            GeoLocation(location.latitude, location.longitude),
            radius,
            geoQueryListener
        )
    }

    fun fetchAllNearbyUsers(radius: Double, location: LatLng) {
        Timber.tag("GeoFire").d("Fetching all users within radius: $radius, location: $location")

        val geoQueryListener = object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                firebaseDataSource.fetchUser(key,
                    onSuccess = { user ->
                        // Timber.tag("NearbyUsers").d("Fetched user: $user")
                        if (user != null && user.uid != currentUserId) {
                            val updatedUser = user.copy(lat = location.latitude, lng = location.longitude)
                            // Timber.tag("NearbyUsers").d("User location added: $updatedUser")
                            _userLocations.value = _userLocations.value + updatedUser
                        }
                    },
                    onFailure = { exception ->
                        // Timber.tag("NearbyUsers").e(exception, "Error fetching user $key")
                    }
                )
            }

            override fun onKeyExited(key: String) {
                _userLocations.value = _userLocations.value.filter { it.uid != key }
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                _userLocations.value = _userLocations.value.map {
                    if (it.uid == key) it.copy(lat = location.latitude, lng = location.longitude) else it
                }
            }

            override fun onGeoQueryReady() {
                // Timber.tag("NearbyUsers").d("All nearby users loaded.")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                // Timber.tag("NearbyUsers").e("GeoQuery error: ${error.message}")
            }
        }

        currentGeoQuery = firebaseDataSource.queryLocations(
            GeoLocation(location.latitude, location.longitude),
            radius,
            geoQueryListener
        )
    }


    private val _userProfile = MutableStateFlow<profileUser>(profileUser())
    val userProfiles: StateFlow<profileUser> get() = _userProfile
    fun getUserProfile(userId: String){
        firebaseDataSource.fetchUser(userId,
            onSuccess = { user ->
                if (user != null) {
                    _userProfile.value = user
                }
            },
            onFailure = { exception ->
                // Timber.tag("GeoFire").e(exception, "Error fetching role for user $userId")
            }
        )
    }


    private val _requestedUserLocations = MutableStateFlow<List<profileUser>>(emptyList())
    val requestedUserLocations: StateFlow<List<profileUser>> get() = _requestedUserLocations

    private val locationListeners = mutableMapOf<String, ValueEventListener>()

    fun fetchRequestedUserLocations(userIds: List<String>) {
        val tag = "LocationDirectFetch"
        // Timber.tag(tag).d("Fetching locations directly for users: $userIds")

        // Clear previous data and listeners
        clearRequestedUserLocations()

        userIds.forEach { userId ->
            val listener = firebaseDataSource.listenToUserLocation(userId) { geoLocation ->
                // Timber.tag(tag).d("Location update for $userId: $geoLocation")

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
                    onFailure = { exception ->
                        // Timber.tag(tag).e(exception, "Error fetching user $userId")
                    }
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