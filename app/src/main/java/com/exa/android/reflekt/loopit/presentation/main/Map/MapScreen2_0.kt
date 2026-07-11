package com.exa.android.reflekt.loopit.presentation.main.Map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.ViewMode
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.presentation.main.Map.components.FilterChipsComponent
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.ConfirmDialog
import com.exa.android.reflekt.loopit.presentation.main.placement.components.GlassCard
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.profile.components.setting.SettingsViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.theme.*
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
import kotlin.math.roundToInt

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
    val viewMode by viewModel.viewMode
    
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

    BackHandler(enabled = true) {
        navController.navigate(com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute.PostList.route) {
            popUpTo(navController.graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Fetch all roles
    LaunchedEffect(Unit) {
        viewModel.fetchAllRoles()
    }

    // Fetch current user's profile
    LaunchedEffect(userId) {
        viewModel.getUserProfile(userId)
    }

    // Clear keyboard focus when leaving this screen (e.g. back navigation)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    DisposableEffect(Unit) {
        onDispose {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
        }
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
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            
            if (viewMode == ViewMode.MAP) {
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

                    (selectedLocation ?: currentLocation)?.let { location ->
                        Circle(
                            center = location,
                            radius = radius * 1000.0,
                            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            strokeColor = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3f
                        )
                    }
                }
                
                // Floating action buttons for MAP mode
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
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp), // Match toggle height
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.MyLocation, "Current Location")
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showMapTypeMenu = !showMapTypeMenu },
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Layers, "Map Type")
                    }

                    if (showMapTypeMenu) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
            } else {
                // LIST VIEW
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(top = 170.dp), // Space for search & filters
                        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (userLocations.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No users found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(userLocations) { user ->
                                if (user.uid != userId) {
                                    UserCard(
                                        user = user,
                                        centerLocation = selectedLocation ?: currentLocation,
                                        viewModel = viewModel,
                                        onUserClick = { selectedUser = user },
                                        onChatClick = { navController.navigate(HomeRoute.ChatDetail.createRoute(user.uid)) },
                                        onProfileClick = { navController.navigate(ProfileRoute.UserProfile.createRoute(user.uid)) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // TOP OVERLAY: Unified Search and Filters
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 8.dp)
            ) {
                UnifiedSearchBar(viewModel, navController)
                QuickFilters(viewModel)
                
                // User Count Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = LiquidColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${userLocations.size} users found",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LiquidColors.TextSecondary
                    )
                }
            }

            // LIST / MAP PILL TOGGLE
            ViewModeToggle(
                viewMode = viewMode,
                onViewModeChange = { viewModel.setViewMode(it) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 100.dp)
            )

            // Modals
            selectedUser?.let { user ->
                ProfileBottomSheet(
                    user = user,
                    openProfile = { navController.navigate(ProfileRoute.UserProfile.createRoute(user.uid)) },
                    openChat = { navController.navigate(HomeRoute.ChatDetail.createRoute(user.uid)) },
                    onDismiss = { selectedUser = null }
                )
            }

            if (showGpsDialog) {
                ConfirmDialog(
                    title = "GPS Required",
                    message = "Please enable GPS for accurate location services.",
                    confirmText = "Enable GPS",
                    dismissText = "Cancel",
                    onConfirm = {
                        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        showGpsDialog = false
                    },
                    onDismiss = { showGpsDialog = false }
                )
            }
            
            if (showPermissionDialog) {
                val permissionStatus = locationPermissionState.status
                val isPermanentlyDenied = !permissionStatus.shouldShowRationale && !permissionStatus.isGranted

                ConfirmDialog(
                    title = "Permission Required",
                    message = if (isPermanentlyDenied)
                        "Location permission was permanently denied. Please enable it in app settings."
                    else
                        "Location permission is needed to show nearby users.",
                    confirmText = if (isPermanentlyDenied) "Open Settings" else "Grant Permission",
                    dismissText = "Cancel",
                    showDismissButton = !isPermanentlyDenied,
                    onConfirm = {
                        showPermissionDialog = false
                        if (isPermanentlyDenied) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    onDismiss = { showPermissionDialog = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSearchBar(viewModel: LocationViewModel, navController: NavController) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.setSearchQuery(it) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search by name or email...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(LiquidColors.Primary.copy(alpha=0.15f))
                    .clickable { navController.navigate("searchScreen2_0") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Tune, "Filters", tint = LiquidColors.Primary, modifier = Modifier.size(16.dp))
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFilters(viewModel: LocationViewModel) {
    val selectedYear by viewModel.selectedYear
    val selectedBranch by viewModel.selectedBranch
    val radius by viewModel.radius
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val context = LocalContext.current
    
    var showLocationSheet by remember { mutableStateOf(false) }
    var showRadiusSheet by remember { mutableStateOf(false) }
    var showYearSheet by remember { mutableStateOf(false) }
    var showBranchSheet by remember { mutableStateOf(false) }
    
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Location filter
        item {
            FilterChip(
                selected = selectedLocation != null,
                onClick = { showLocationSheet = true },
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp)) }
            )
        }
        
        // Radius filter
        item {
            FilterChip(
                selected = radius != 5f,
                onClick = { showRadiusSheet = true },
                label = { Text("Radius: ${radius.toInt()}km") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) }
            )
        }
        
        // Year filter
        item {
            FilterChip(
                selected = selectedYear.isNotEmpty(),
                onClick = { showYearSheet = true },
                label = { Text(if (selectedYear.isNotEmpty()) "Year ${selectedYear.joinToString(",")}" else "College Year") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) }
            )
        }
        
        // Branch chip
        item {
            FilterChip(
                selected = selectedBranch != null,
                onClick = { showBranchSheet = true },
                label = { Text(selectedBranch?.uppercase() ?: "Branch") },
                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, modifier = Modifier.size(16.dp)) }
            )
        }
    }
    
    if (showLocationSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showLocationSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Search Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                ) {
                    com.exa.android.reflekt.loopit.presentation.main.Map.SearchBar(
                        onPlaceSelected = { place ->
                            viewModel.selectLocation(place, context)
                            showLocationSheet = false
                        }
                    )
                }
            }
        }
    }

    if (showRadiusSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showRadiusSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Search Radius", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("1 km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${radius.toInt()} km", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("50 km", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Slider(
                    value = radius,
                    onValueChange = { viewModel.radius.value = it },
                    valueRange = 1f..50f,
                    steps = 48,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf(1f, 5f, 10f, 20f, 35f, 50f)) { r ->
                        FilterChip(
                            selected = radius == r,
                            onClick = { viewModel.radius.value = r },
                            label = { Text("${r.toInt()} km") }
                        )
                    }
                }
            }
        }
    }

    if (showYearSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showYearSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("College Year", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("1", "2", "3", "4")) { year ->
                        FilterChip(
                            selected = selectedYear.contains(year),
                            onClick = { viewModel.toggleYear(year) },
                            label = { Text("Year $year") }
                        )
                    }
                }
            }
        }
    }

    if (showBranchSheet) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.surface,
            onDismissRequest = { showBranchSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Branch", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("cse", "ec", "me", "iot", "aiml")) { branch ->
                        FilterChip(
                            selected = selectedBranch == branch,
                            onClick = { 
                                viewModel.setBranch(if (selectedBranch == branch) null else branch)
                                showBranchSheet = false
                            },
                            label = { Text(branch.uppercase()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewModeToggle(
    viewMode: ViewMode,
    onViewModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.padding(bottom = 16.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val mapSelected = viewMode == ViewMode.MAP
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (mapSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onViewModeChange(ViewMode.MAP) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    "Map", 
                    color = if (mapSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (!mapSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onViewModeChange(ViewMode.LIST) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(
                    "List", 
                    color = if (!mapSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
fun UserCard(
    user: profileUser,
    centerLocation: LatLng?,
    viewModel: LocationViewModel,
    onUserClick: () -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val distance = if (centerLocation != null) {
        viewModel.calculateDistance(
            centerLocation.latitude,
            centerLocation.longitude,
            user.lat,
            user.lng
        )
    } else null
    
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickableWithNoIndication(onClick = onUserClick),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile picture
            if (user.profilePicture.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                ImageUsingCoil(
                    context,
                    user.profilePicture,
                    R.drawable.placeholder,
                    Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = user.role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (distance != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.NearMe,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${distance.roundToInt()} km",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    if (user.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "%.1f".format(user.rating),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
