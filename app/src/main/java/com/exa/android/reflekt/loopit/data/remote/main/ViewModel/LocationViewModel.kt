package com.exa.android.reflekt.loopit.data.remote.main.ViewModel

import android.content.Context
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.data.remote.main.Repository.LocationRepository
import com.exa.android.reflekt.loopit.util.model.profileUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    val userLocations: StateFlow<List<profileUser>> get() = locationRepository.userLocations

    fun startLocationUpdates(userId: String?, context: Context) {
        locationRepository.startLocationUpdates(userId, context)
    }

    fun clearUserLocations() {
        locationRepository.clearUserLocations()
    }

    fun fetchUserLocations(role: String, radius: Double, location: LatLng) {
        viewModelScope.launch {
            clearUserLocations()
            locationRepository.fetchUserLocations(role, radius, location)
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


    override fun onCleared() {
        locationRepository.clearRequestedUserLocations()
        super.onCleared()
    }
}