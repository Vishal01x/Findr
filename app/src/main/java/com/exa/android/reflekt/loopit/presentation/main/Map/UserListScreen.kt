package com.exa.android.reflekt.loopit.presentation.main.Map

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.SortOrder
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.ViewMode
import com.exa.android.reflekt.loopit.data.feature.main.profile.domain.model.profileUser
import com.exa.android.reflekt.loopit.presentation.main.Map.components.FilterChipsComponent
import com.exa.android.reflekt.loopit.presentation.main.component.ImageUsingCoil
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(
    navController: NavController,
    viewModel: LocationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val currentLocation = selectedLocation // You may want to pass actual current location
    
    val filteredUsers = remember(
        viewModel.userLocations.collectAsState().value,
        viewModel.searchQuery.value,
        viewModel.sortOrder.value,
        currentLocation
    ) {
        viewModel.getFilteredAndSortedUsers(currentLocation)
    }
    
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<profileUser?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Users in Area",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "${filteredUsers.size} users found",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    // Sort button
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.Sort, "Sort")
                    }
                    
                    // Filter button
                    IconButton(onClick = { navController.navigate("searchScreen2_0") }) {
                        Icon(Icons.Default.FilterList, "Filters")
                    }
                    
                    // Map view button
                    IconButton(onClick = {
                        viewModel.setViewMode(ViewMode.MAP)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Map, "Map View")
                    }
                    
                    // Sort menu
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Distance") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DISTANCE)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.NearMe, "Distance")
                            },
                            trailingIcon = {
                                if (viewModel.sortOrder.value == SortOrder.DISTANCE) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, "Name")
                            },
                            trailingIcon = {
                                if (viewModel.sortOrder.value == SortOrder.NAME) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Rating") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.RATING)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Star, "Rating")
                            },
                            trailingIcon = {
                                if (viewModel.sortOrder.value == SortOrder.RATING) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = viewModel.searchQuery.value,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or role...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Search")
                },
                trailingIcon = {
                    if (viewModel.searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            
            // Active filters
            FilterChipsComponent(
                viewModel = viewModel,
                showLocationChip = selectedLocation != null,
                onClearLocation = {
                    viewModel.setSelectedLocation(LatLng(0.0, 0.0)) // Reset to current location
                }
            )
            
            // User list
            if (filteredUsers.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            "No users found",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Try adjusting your filters or search area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(onClick = { navController.navigate("searchScreen2_0") }) {
                            Icon(Icons.Default.FilterList, "Filters")
                            Spacer(Modifier.width(8.dp))
                            Text("Adjust Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            centerLocation = currentLocation,
                            viewModel = viewModel,
                            onUserClick = {
                                selectedUser = user
                            },
                            onChatClick = {
                                navController.navigate(HomeRoute.ChatDetail.createRoute(user.uid))
                            },
                            onProfileClick = {
                                navController.navigate(ProfileRoute.UserProfile.createRoute(user.uid))
                            }
                        )
                    }
                }
            }
        }
        
        // Profile bottom sheet
        selectedUser?.let { user ->
            ProfileBottomSheet(
                user = user,
                openProfile = { navController.navigate(ProfileRoute.UserProfile.createRoute(user.uid)) },
                openChat = { navController.navigate(HomeRoute.ChatDetail.createRoute(user.uid)) },
                onDismiss = { selectedUser = null }
            )
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
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onUserClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
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
            
            // Action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onChatClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        contentDescription = "Chat",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
