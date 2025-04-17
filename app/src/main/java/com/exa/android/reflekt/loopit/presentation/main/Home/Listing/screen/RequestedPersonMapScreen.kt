package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.LatLng
import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import com.exa.android.reflekt.loopit.presentation.main.Home.Map.CustomMapMarker
import com.exa.android.reflekt.loopit.presentation.main.Home.Map.ProfileBottomSheet
import com.google.accompanist.permissions.isGranted
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.rememberCameraPositionState
import profileUser
import timber.log.Timber

@SuppressLint("UnrememberedMutableState", "MissingPermission")
@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RequestedPersonMapScreen(
    userIds: String, // Comma-separated list of user IDs
    navController: NavHostController,
    openChat : (String) -> Unit,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val userLocations by viewModel.requestedUserLocations.collectAsState()
    val userIdList = remember(userIds) { userIds.split(",") }

    // Location and UI states
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedUser by remember { mutableStateOf<profileUser?>(null) }

    // Permission and location clients
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val cameraPositionState = rememberCameraPositionState()
    val tag="locationGeofence"
    // Fetch enrolled users' locations
    LaunchedEffect(userIdList) {
        if (userIdList.isNotEmpty()) {
            Timber.tag("locationGeofence").d("Fetching requested user locations: $userIdList")
            viewModel.fetchRequestedUserLocations(userIdList)
        }
    }
    // Location permission handling
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        currentLocation = LatLng(it.latitude, it.longitude)
                        cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation!!, 12f)
                    }
                }
                .addOnFailureListener { e ->
                    Timber.tag(tag).e(e, "Error getting current location")
                }
        } else {
            locationPermissionState.launchPermissionRequest()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Members") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionState.status.isGranted)
            ) {

                Log.d(tag, "userLocations: $userLocations")

                userLocations.forEach { user ->
                    CustomMapMarker(
                        imageUrl = "https://i.pinimg.com/originals/b8/5e/9d/b85e9df9e9b75bcce3a767eb894ef153.jpg",
                        fullName = "${user.firstName} ${user.lastName}",
                        location = LatLng(user.lat, user.lng),
                        onClick = {
                            selectedUser = user
                            true
                        }
                    )
                }

            }

            // Profile Bottom Sheet
            selectedUser?.let { user ->
                ProfileBottomSheet(
                    user = user,
                    openChat = {
                        openChat(user.uid)
                    },
                    onDismiss = { selectedUser = null }
                )
            }
        }
    }

    if (!locationPermissionState.status.isGranted) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Permission Required") },
            text = { Text("Location permission is needed to show team members") },
            confirmButton = {
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        )
    }
}