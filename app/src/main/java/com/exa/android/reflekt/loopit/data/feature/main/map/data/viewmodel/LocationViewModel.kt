package com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.feature.main.map.data.repository.LocationRepository
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


// View mode for list/map toggle
enum class ViewMode {
    MAP, LIST
}

// Sort order for list view
enum class SortOrder {
    DISTANCE, NAME, RATING
}

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val radius = mutableStateOf(5f)
    val selectedRoles = mutableStateOf(setOf<String>())
    val minRating = mutableStateOf(0f)
    
    // View mode and list view states
    val viewMode = mutableStateOf(ViewMode.MAP)
    val sortOrder = mutableStateOf(SortOrder.DISTANCE)
    val searchQuery = mutableStateOf("")

    fun addRole(role: String) {
        selectedRoles.value = selectedRoles.value + role
        Timber.tag("SearchScreen").d("Selected Roles: ${selectedRoles.value}")
    }

    fun removeRole(role: String) {
        selectedRoles.value = selectedRoles.value - role
    }
    
    fun clearAllFilters() {
        selectedRoles.value = setOf()
        minRating.value = 0f
        radius.value = 5f
    }
    
    fun setViewMode(mode: ViewMode) {
        viewMode.value = mode
    }
    
    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }
    
    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    val userLocations: StateFlow<List<profileUser>> get() = locationRepository.userLocations

    fun startLocationUpdates(userId: String?, context: Context) {
        locationRepository.startLocationUpdates(userId, context)
    }

    fun clearUserLocations() {
        locationRepository.clearUserLocations()
    }

    fun fetchUserLocations(role: String, radius: Double, location: LatLng, minRating: Float) {
        viewModelScope.launch {
            clearUserLocations()
            locationRepository.fetchUserLocations(role, radius, location, minRating)
        }
    }

    fun fetchAllNearbyUsers(radius: Double, location: LatLng) {
        viewModelScope.launch {
            clearUserLocations()
            locationRepository.fetchAllNearbyUsers(radius, location)
        }
    }

    val userProfile: StateFlow<profileUser> get() = locationRepository.userProfiles
    fun getUserProfile(userId: String){
        locationRepository.getUserProfile(userId)
    }

    // State to hold the selected place location as LatLng
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    fun setSelectedLocation(latLng: LatLng){
        _selectedLocation.value=latLng
        Timber.tag("SearchScreen").d("Selected Location: $latLng")
    }
    // Function to geocode the selected place and update the selected location state
    fun selectLocation(selectedPlace: String, context: Context) {
        viewModelScope.launch {
            val geocoder = Geocoder(context)
            val addresses = withContext(Dispatchers.IO) {
                // Perform geocoding on a background thread
                geocoder.getFromLocationName(selectedPlace, 1)
            }
            if (!addresses.isNullOrEmpty()) {
                // Update the selected location in the state
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                _selectedLocation.value = latLng
                // Timber.tag("MapScreen").d("Selected Location: $latLng $selectedPlace $selectedLocation")
            } else {
                // Timber.tag("MapScreen").e("No location found for the selected place.")
            }
        }
    }

    private val _requestedUserLocations = MutableStateFlow<List<profileUser>>(emptyList())
    val requestedUserLocations: StateFlow<List<profileUser>> get() = _requestedUserLocations

    init {
        viewModelScope.launch {
            locationRepository.requestedUserLocations.collect { users ->
                _requestedUserLocations.value = users
            }
        }
    }

    fun fetchRequestedUserLocations(userIds: List<String>) {
        locationRepository.fetchRequestedUserLocations(userIds)
    }
    fun startLocationUpdates(userId: String) {
        locationRepository.startLocationUpdates(userId)
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
    }

    private val _roleSuggestions = MutableStateFlow<List<String>>(emptyList())
    val roleSuggestions: StateFlow<List<String>> get() = _roleSuggestions

    fun fetchAllRoles() {
        viewModelScope.launch {
            locationRepository.fetchRolesFromFirestore()
            _roleSuggestions.value = locationRepository.roles.value
        }
    }
    
    /**
     * Calculate distance between two LatLng points using Haversine formula
     * Returns distance in kilometers
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radius of the earth in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }
    
    /**
     * Get filtered and sorted users for list view
     */
    fun getFilteredAndSortedUsers(centerLocation: LatLng?): List<profileUser> {
        var users = userLocations.value
        
        // Apply search query filter
        if (searchQuery.value.isNotEmpty()) {
            users = users.filter { user ->
                user.name.contains(searchQuery.value, ignoreCase = true) ||
                user.role.contains(searchQuery.value, ignoreCase = true)
            }
        }
        
        // Sort users
        return when (sortOrder.value) {
            SortOrder.DISTANCE -> {
                if (centerLocation != null) {
                    users.sortedBy { user ->
                        calculateDistance(
                            centerLocation.latitude,
                            centerLocation.longitude,
                            user.lat,
                            user.lng
                        )
                    }
                } else {
                    users
                }
            }
            SortOrder.NAME -> users.sortedBy { it.name }
            SortOrder.RATING -> users.sortedByDescending { it.rating }
        }
    }


    override fun onCleared() {
        locationRepository.clearRequestedUserLocations()
        super.onCleared()
    }
}