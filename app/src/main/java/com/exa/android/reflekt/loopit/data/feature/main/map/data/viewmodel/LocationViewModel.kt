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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

@OptIn(FlowPreview::class)
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    // ---------- Filter state (Compose-observable) ----------

    val radius = mutableStateOf(5f)
    val selectedRoles = mutableStateOf(setOf<String>())
    val minRating = mutableStateOf(0f)

    // View mode and list view states
    val viewMode = mutableStateOf(ViewMode.MAP)
    val sortOrder = mutableStateOf(SortOrder.DISTANCE)

    val selectedYear = mutableStateOf<Set<String>>(emptySet())
    val selectedBranch = mutableStateOf<String?>(null)

    // ---------- Search query as StateFlow so we can debounce it ----------

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> get() = _searchQuery

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ---------- Location tracking ----------

    // Last confirmed location used for the GeoFire query
    private val _queryLocation = MutableStateFlow<LatLng?>(null)
    val queryLocation: StateFlow<LatLng?> get() = _queryLocation

    // State to hold the user-selected place location
    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation

    // ---------- Raw user list from GeoFire (live, no filters) ----------

    val userLocations: StateFlow<List<profileUser>> get() = locationRepository.userLocations

    // ---------- Debounced radius & location → triggers GeoFire re-query ----------

    // We debounce radius changes so slider dragging doesn't hammer Firebase
    private val _radiusFlow = MutableStateFlow(5f)

    init {
        // Keep _radiusFlow in sync with the Compose mutableState
        // (We drive the query from _radiusFlow, not directly from the State)
        // Debounce radius: wait 400ms after last slider move before querying
        _radiusFlow
            .debounce(400)
            .distinctUntilChanged()
            .combine(_queryLocation) { r, loc ->
                Pair(r, loc)
            }
            .onEach { (r, loc) ->
                if (loc != null) {
                    locationRepository.queryNearbyUsers(r.toDouble(), loc)
                }
            }
            .launchIn(viewModelScope)

        // Debounce search query for name/email search
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotEmpty()) {
                    viewModelScope.launch {
                        locationRepository.searchUsersByNameOrEmail(query)
                    }
                } else {
                    // Search cleared — re-run the geo query to restore map users
                    val loc = _queryLocation.value ?: _selectedLocation.value
                    if (loc != null) {
                        locationRepository.queryNearbyUsers(radius.value.toDouble(), loc)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Called once we know the user's current GPS position.
     * Sets the query location and triggers the initial GeoFire fetch.
     */
    fun onLocationObtained(latLng: LatLng) {
        if (_queryLocation.value == null) {
            // First time: set the location and trigger query immediately (no debounce wait)
            _queryLocation.value = latLng
            _radiusFlow.value = radius.value
            locationRepository.queryNearbyUsers(radius.value.toDouble(), latLng)
            Timber.tag("GeoFire").d("Initial location obtained: $latLng")
        }
        // Subsequent calls (GPS updates) — just update _queryLocation;
        // the combine above will re-query after debounce
    }

    /**
     * Updates the radius value for visual display only (slider drag).
     * Does NOT trigger a GeoFire re-query.
     */
    fun updateRadius(newRadius: Float) {
        radius.value = newRadius
        // Do NOT push to _radiusFlow here — query only fires on applyRadius()
    }

    /**
     * Applies the current radius and triggers a GeoFire re-query immediately.
     * Call this when the user taps the Apply button.
     */
    fun applyRadius() {
        _radiusFlow.value = radius.value
        // Force immediate query without waiting for debounce
        val loc = _queryLocation.value ?: _selectedLocation.value ?: return
        locationRepository.queryNearbyUsers(radius.value.toDouble(), loc)
    }

    fun setSelectedLocation(latLng: LatLng) {
        _selectedLocation.value = latLng
        _queryLocation.value = latLng
        _radiusFlow.value = radius.value
        locationRepository.queryNearbyUsers(radius.value.toDouble(), latLng)
        Timber.tag("SearchScreen").d("Selected Location: $latLng")
    }

    fun selectLocation(selectedPlace: String, context: Context) {
        viewModelScope.launch {
            val geocoder = Geocoder(context)
            val addresses = withContext(Dispatchers.IO) {
                geocoder.getFromLocationName(selectedPlace, 1)
            }
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val latLng = LatLng(address.latitude, address.longitude)
                setSelectedLocation(latLng)
            }
        }
    }

    // ---------- Derived: filtered + sorted list (reads Compose State directly) ----------
    // This is intentionally a regular function (called inside a composable) so that
    // Compose tracks reads of mutableStateOf values and recomposes automatically.

    fun getFilteredAndSortedUsers(centerLocation: LatLng?): List<profileUser> {
        var users = userLocations.value

        // Apply search query filter (local fallback when search results overlap)
        val query = _searchQuery.value
        if (query.isNotEmpty()) {
            users = users.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                        user.role.contains(query, ignoreCase = true) ||
                        user.email.contains(query, ignoreCase = true)
            }
        }

        // Apply year filter
        if (selectedYear.value.isNotEmpty()) {
            users = users.filter { it.year in selectedYear.value }
        }

        // Apply branch filter
        if (selectedBranch.value != null) {
            val branch = selectedBranch.value!!
            users = users.filter {
                it.branch?.contains(branch, ignoreCase = true) == true ||
                        it.role.contains(branch, ignoreCase = true)
            }
        }

        // Apply role filter
        if (selectedRoles.value.isNotEmpty()) {
            val targetRoles = selectedRoles.value
                .map { it.trim().lowercase() }
                .filter { it.isNotEmpty() }
            if (targetRoles.isNotEmpty()) {
                users = users.filter { user ->
                    val userRoles = user.role
                        .split(",")
                        .map { it.trim().lowercase() }
                        .filter { it.isNotEmpty() }
                    userRoles.any { userRole ->
                        targetRoles.any { targetRole ->
                            userRole == targetRole ||
                                    userRole.contains(targetRole) ||
                                    targetRole.contains(userRole)
                        }
                    }
                }
            }
        }

        // Apply minRating filter
        if (minRating.value > 0f) {
            users = users.filter { it.rating >= minRating.value }
        }

        // Sort
        return when (sortOrder.value) {
            SortOrder.DISTANCE -> {
                if (centerLocation != null) {
                    users.sortedBy { user ->
                        calculateDistance(
                            centerLocation.latitude, centerLocation.longitude,
                            user.lat, user.lng
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

    // ---------- Filter helpers ----------

    fun toggleYear(year: String) {
        val current = selectedYear.value.toMutableSet()
        if (current.contains(year)) current.remove(year) else current.add(year)
        selectedYear.value = current
    }

    fun setBranch(branch: String?) {
        selectedBranch.value = branch
    }

    fun addRole(role: String) {
        selectedRoles.value = selectedRoles.value + role
        Timber.tag("SearchScreen").d("Selected Roles: ${selectedRoles.value}")
    }

    fun removeRole(role: String) {
        selectedRoles.value = selectedRoles.value - role
    }

    fun clearAllFilters() {
        selectedRoles.value = setOf()
        selectedYear.value = emptySet()
        selectedBranch.value = null
        minRating.value = 0f
        updateRadius(5f)
    }

    fun setViewMode(mode: ViewMode) {
        viewMode.value = mode
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    // ---------- Location updates (saving own position) ----------

    fun startLocationUpdates(userId: String?, context: Context) {
        locationRepository.startLocationUpdates(userId, context)
    }

    fun startLocationUpdates(userId: String) {
        locationRepository.startLocationUpdates(userId)
    }

    fun stopLocationUpdates() {
        locationRepository.stopLocationUpdates()
    }

    // ---------- User profile ----------

    val userProfile: StateFlow<profileUser> get() = locationRepository.userProfiles

    fun getUserProfile(userId: String) {
        locationRepository.getUserProfile(userId)
    }

    // ---------- Requested user locations ----------

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

    // ---------- Roles ----------

    private val _roleSuggestions = MutableStateFlow<List<String>>(emptyList())
    val roleSuggestions: StateFlow<List<String>> get() = _roleSuggestions

    fun fetchAllRoles() {
        if (_roleSuggestions.value.isNotEmpty()) return
        viewModelScope.launch {
            locationRepository.fetchRolesFromFirestore()
            _roleSuggestions.value = locationRepository.roles.value
        }
    }

    // ---------- Distance helper ----------

    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return earthRadius * c
    }

    // ---------- Legacy shims (keep callers compiling) ----------

    fun fetchUserLocations(role: String, radius: Double, location: LatLng, minRating: Float) {
        locationRepository.queryNearbyUsers(radius, location)
    }

    fun fetchAllNearbyUsers(radius: Double, location: LatLng) {
        locationRepository.queryNearbyUsers(radius, location)
    }

    fun clearUserLocations() {
        locationRepository.clearUserLocations()
    }

    override fun onCleared() {
        locationRepository.clearRequestedUserLocations()
        super.onCleared()
    }
}