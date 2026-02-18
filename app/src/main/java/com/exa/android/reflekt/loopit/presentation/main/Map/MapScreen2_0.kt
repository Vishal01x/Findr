package com.exa.android.reflekt.loopit.presentation.main.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.exa.android.reflekt.loopit.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.ViewMode
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.presentation.main.Map.components.FilterChipsComponent
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.SettingsViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.*
import timber.log.Timber

/**
 * Enhanced Map Screen 2.0 with improved UI/UX
 * Features:
 * - New Map theme
 * - Map type selector (Normal, Satellite, Terrain, Hybrid)
 * - Enhanced geofence visualization
 * - Filter chips at top
 * - List view toggle
 * - Modern Material 3 design
 */
@SuppressLint("UnrememberedMutableState", "MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen2_0(
    navController: NavController,
    viewModel: LocationViewModel = hiltViewModel(),
    settingViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return
    val userLocations by viewModel.userLocations.collectAsState()

    // Location and UI states
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    val radius by viewModel.radius
    val selectedRoles by viewModel.selectedRoles
    val minRating by viewModel.minRating
    
    var selectedUser by rememberSaveable(stateSaver = profileUser.Saver) {
        mutableStateOf<profileUser?>(null)
    }
    
    // Map type state
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var showMapTypeMenu by remember { mutableStateOf(false) }

    // Permission and location clients
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()

    var showPermissionDialog by remember { mutableStateOf(true) }

    // Observe the selected location from the ViewModel
    val selectedLocation by viewModel.selectedLocation.collectAsState()

    // GPS check
    val locationManager = remember {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }
    var isGpsEnabled by remember { mutableStateOf(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) }
    var showGpsDialog by remember { mutableStateOf(false) }

    // Fetch all roles
    LaunchedEffect(Unit) {
        viewModel.fetchAllRoles()
    }

    // Fetch current user's profile
    LaunchedEffect(userId) {
        viewModel.getUserProfile(userId)
    }

    // Update map when filters or location change
    LaunchedEffect(
        locationPermissionState.status,
        currentLocation,
        selectedRoles,
        radius,
        selectedLocation,
        minRating
    ) {
        if (locationPermissionState.status.isGranted && currentLocation != null) {
            val rolesAsString = selectedRoles.joinToString(",")
            val location = selectedLocation ?: currentLocation!!
            val zoomLevel = getZoomLevel(radius)
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(location, zoomLevel),
                durationMs = 500
            )
            Timber.tag("GeoFire").d("Fetching user locations for role: $rolesAsString $radius $location")
            if (rolesAsString.isNotEmpty()) {
                viewModel.fetchUserLocations(
                    role = rolesAsString,
                    radius = radius.toDouble(),
                    location = location,
                    minRating = minRating
                )
            } else {
                viewModel.fetchAllNearbyUsers(
                    radius = radius.toDouble(),
                    location = selectedLocation ?: currentLocation!!
                )
            }
        }
    }

    val locationUpdateCheck by settingViewModel.privacyEnabled.collectAsState()

    // Location permission handling
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            showPermissionDialog = false
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            showGpsDialog = !isGpsEnabled
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag("GeoFire").e(e, "Error getting current location")
                }

            // Start location updates for the authenticated user
            Timber.tag("GeoFire").d("Map Screen Location updates started: $locationUpdateCheck")
            if (locationUpdateCheck) {
                viewModel.startLocationUpdates(userId)
            }
        } else {
            showPermissionDialog = true
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationUpdateCheck) {
        if (locationUpdateCheck && locationPermissionState.status.isGranted) {
            viewModel.startLocationUpdates(userId)
        } else {
            viewModel.stopLocationUpdates()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize()) {
        // Google Map
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionState.status.isGranted,
                mapType = mapType,
                mapStyleOptions = MapStyleOptions.loadRawResourceStyle(
                    context,
                    if (isDarkTheme) R.raw.map_style_dark else R.raw.map_style_light
                )
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false
            )
        ) {
            // User markers
            if (userLocations.isNotEmpty()) {
                userLocations.forEach { user ->
                    if (user.uid != userId) {
                        CustomMapMarker(
                            imageUrl = user.profilePicture,
                            fullName = user.name,
                            location = LatLng(user.lat, user.lng),
                            onClick = {
                                selectedUser = user
                                true
                            }
                        )
                    }
                }
            }

            // Geofence circle
            (selectedLocation ?: currentLocation)?.let { location ->
                Circle(
                    center = location,
                    radius = radius * 1000.0, // Convert km to meters
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3f
                )
            }
        }

        // Enhanced top card with better information display
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 16.dp, end = 16.dp)
        ) {
            var isStatsExpanded by remember { mutableStateOf(false) }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Main header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // User count with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.People,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "${userLocations.size}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                GradientPurpleStart,
                                                GradientPurpleMid,
                                                GradientPurpleEnd
                                            )
                                        )
                                    ),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "users found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        
                        // Action buttons row
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Stats expand/collapse
                            FilledTonalIconButton(
                                onClick = { isStatsExpanded = !isStatsExpanded },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (isStatsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    "Toggle stats",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // Filter button
                            FilledTonalIconButton(
                                onClick = { navController.navigate("searchScreen2_0") },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    "Filters",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            // List view button
                            FilledIconButton(
                                onClick = {
                                    viewModel.setViewMode(ViewMode.LIST)
                                    navController.navigate("userListScreen")
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    Icons.Default.List,
                                    "List View",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    // Expandable stats section
                    if (isStatsExpanded) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Radius stat
                            StatItem(
                                icon = Icons.Default.Navigation,
                                label = "Radius",
                                value = "${radius.toInt()} km"
                            )
                            
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            
                            // Roles stat
                            StatItem(
                                icon = Icons.Default.Work,
                                label = "Roles",
                                value = if (selectedRoles.isEmpty()) "All" else "${selectedRoles.size}"
                            )
                            
                            VerticalDivider(
                                modifier = Modifier.height(40.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            
                            // Location stat
                            StatItem(
                                icon = Icons.Default.LocationOn,
                                label = "Location",
                                value = if (selectedLocation != null) "Custom" else "Current"
                            )
                        }
                    }
                    
//                    // Active filters chips (always visible if filters are active)
//                    if (selectedRoles.isNotEmpty() || radius != 5f || selectedLocation != null) {
//                        Spacer(Modifier.height(12.dp))
//                        FilterChipsComponent(
//                            viewModel = viewModel,
//                            showLocationChip = selectedLocation != null,
//                            onClearLocation = {
//                                currentLocation?.let { viewModel.setSelectedLocation(it) }
//                            },
//                            modifier = Modifier.padding(0.dp) // Remove extra padding in this context
//                        )
//                    }
                }
            }
        }

        // Floating action buttons
        // Current location button (top-left)
        FloatingActionButton(
            onClick = {
                if (locationPermissionState.status.isGranted) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            val newLocation = LatLng(it.latitude, it.longitude)
                            currentLocation = newLocation
                            viewModel.setSelectedLocation(newLocation)
                        }
                    }
                } else {
                    locationPermissionState.launchPermissionRequest()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .padding(top = 120.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.MyLocation, "Current Location")
        }

        // Map type selector (bottom-right)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(
                onClick = { showMapTypeMenu = !showMapTypeMenu },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Layers, "Map Type")
            }

            // Map type menu
            if (showMapTypeMenu) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        MapTypeButton("Normal", mapType == MapType.NORMAL) {
                            mapType = MapType.NORMAL
                            showMapTypeMenu = false
                        }
                        MapTypeButton("Satellite", mapType == MapType.SATELLITE) {
                            mapType = MapType.SATELLITE
                            showMapTypeMenu = false
                        }
                        MapTypeButton("Terrain", mapType == MapType.TERRAIN) {
                            mapType = MapType.TERRAIN
                            showMapTypeMenu = false
                        }
                        MapTypeButton("Hybrid", mapType == MapType.HYBRID) {
                            mapType = MapType.HYBRID
                            showMapTypeMenu = false
                        }
                    }
                }
            }
        }

        // Profile Bottom Sheet
        selectedUser?.let { user ->
            ProfileBottomSheet(
                user = user,
                openProfile = { navController.navigate(ProfileRoute.UserProfile.createRoute(user.uid)) },
                openChat = { navController.navigate(HomeRoute.ChatDetail.createRoute(user.uid)) },
                onDismiss = { selectedUser = null }
            )
        }

        // GPS Dialog
        if (showGpsDialog) {
            AlertDialog(
                onDismissRequest = { showGpsDialog = false },
                title = { Text("GPS Required") },
                text = { Text("Please enable GPS for accurate location services") },
                confirmButton = {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        showGpsDialog = false
                    }) {
                        Text("Enable GPS")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showGpsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    }

    // Permission Dialog
    if (showPermissionDialog) {
        val permissionStatus = locationPermissionState.status
        val isPermanentlyDenied = !permissionStatus.shouldShowRationale && !permissionStatus.isGranted

        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = {
                if (isPermanentlyDenied) {
                    Text("Location permission was permanently denied. Please enable it in app settings.")
                } else {
                    Text("Location permission is needed to show nearby users.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        if (isPermanentlyDenied) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    }
                ) {
                    Text(if (isPermanentlyDenied) "Open Settings" else "Grant Permission")
                }
            },
            dismissButton = {
                if (!isPermanentlyDenied) {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun MapTypeButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Text(text)
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
