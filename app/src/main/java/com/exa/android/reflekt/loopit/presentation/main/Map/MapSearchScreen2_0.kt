package com.exa.android.reflekt.loopit.presentation.main.Map

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx. compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapSearchScreen2_0(
    navController: NavController,
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val roles by viewModel.roleSuggestions.collectAsState()
    var roleSearchQuery by remember { mutableStateOf("") }
    val filteredRoles = roles.filter { it.contains(roleSearchQuery, ignoreCase = true) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var isRequestingLocation by remember { mutableStateOf(false) }
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    
    val userCount by viewModel.userLocations.collectAsState()
    
    // Collapsible section states
    var isLocationExpanded by remember { mutableStateOf(false) }
    var isRadiusExpanded by remember { mutableStateOf(true) }
    var isRolesExpanded by remember { mutableStateOf(true) }
    
    // Popular roles (you can fetch these or define them)
    val popularRoles = remember { 
        listOf("Software Engineer", "Product Manager", "Designer", "Data Scientist", "Marketing")
    }

    LaunchedEffect(locationPermissionState.status, isRequestingLocation) {
        if (isRequestingLocation) {
            when (locationPermissionState.status) {
                is PermissionStatus.Granted -> {
                    val permissionGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (permissionGranted) {
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    viewModel.setSelectedLocation(LatLng(it.latitude, it.longitude))
                                }
                            }
                        } catch (e: SecurityException) {
                            e.printStackTrace()
                        }
                    }

                    isRequestingLocation = false
                }

                is PermissionStatus.Denied -> {
                    locationPermissionState.launchPermissionRequest()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchAllRoles()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Filters", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.clearAllFilters() }) {
                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                tonalElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Result count
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.People,
                                null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${userCount.size} users found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Icon(Icons.Default.Check, "Apply")
                        Spacer(Modifier.width(8.dp))
                        Text("Apply Filters", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Location Section - Collapsible
            CollapsibleFilterSection(
                title = "Location",
                icon = Icons.Outlined.LocationOn,
                isExpanded = isLocationExpanded,
                onToggle = { isLocationExpanded = !isLocationExpanded },
                badge = if (viewModel.selectedLocation.collectAsState().value != null) "Custom" else null
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = MaterialTheme.shapes.medium
                            )
                    ) {
                        SearchBar(
                            onPlaceSelected = { place ->
                                viewModel.selectLocation(place, context)
                            }
                        )
                    }

                    Button(
                        onClick = { isRequestingLocation = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.MyLocation, "Current Location")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Use Current Location")
                    }
                }
            }

            // Radius Section - Collapsible with quick presets
            CollapsibleFilterSection(
                title = "Search Radius",
                icon = Icons.Filled.Navigation,
                isExpanded = isRadiusExpanded,
                onToggle = { isRadiusExpanded = !isRadiusExpanded },
                badge = "${viewModel.radius.value.toInt()} km"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Quick preset chips
                    Text(
                        "Quick Select",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(listOf(1f, 5f, 10f, 20f, 35f, 50f)) { radius ->
                            FilterChip(
                                selected = viewModel.radius.value == radius,
                                onClick = { viewModel.radius.value = radius },
                                label = { Text("${radius.toInt()} km") },
                                leadingIcon = if (viewModel.radius.value == radius) {
                                    { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                } else null
                            )
                        }
                    }
                    
                    // Value display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "1 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${viewModel.radius.value.toInt()} km",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "50 km",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Slider
                    Slider(
                        value = viewModel.radius.value,
                        onValueChange = { viewModel.radius.value = it },
                        valueRange = 1f..50f,
                        steps = 48,
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Roles Section - Improved with popular roles
            CollapsibleFilterSection(
                title = "Professional Roles",
                icon = Icons.Outlined.Category,
                isExpanded = isRolesExpanded,
                onToggle = { isRolesExpanded = !isRolesExpanded },
                badge = if (viewModel.selectedRoles.value.isEmpty()) "All" else "${viewModel.selectedRoles.value.size}"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Selected Roles Display
                    if (viewModel.selectedRoles.value.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${viewModel.selectedRoles.value.size} role(s) selected",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    TextButton(onClick = { viewModel.selectedRoles.value = setOf() }) {
                                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(viewModel.selectedRoles.value.toList()) { role ->
                                        SuggestionChip(
                                            onClick = { viewModel.removeRole(role) },
                                            label = { Text(role) },
                                            border = BorderStroke(
                                                1.dp,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                            ),
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                labelColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            icon = {
                                                Icon(
                                                    Icons.Default.Close,
                                                    contentDescription = "Remove",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Popular roles quick select
                    if (popularRoles.isNotEmpty()) {
                        Text(
                            "Popular Roles",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(popularRoles) { role ->
                                val isSelected = viewModel.selectedRoles.value.contains(role)
                                SuggestionChip(
                                    onClick = {
                                        if (isSelected) {
                                            viewModel.removeRole(role)
                                        } else {
                                            viewModel.addRole(role)
                                        }
                                    },
                                    label = { Text(role) },
                                    icon = if (isSelected) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                                    } else null,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    // Role Search
                    OutlinedTextField(
                        value = roleSearchQuery,
                        onValueChange = { roleSearchQuery = it },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = if (roleSearchQuery.isNotEmpty()) {
                            {
                                IconButton(onClick = { roleSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        } else null,
                        placeholder = { Text("Search all roles...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        singleLine = true
                    )

                    // Role Grid (improved layout)
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        if (filteredRoles.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.WorkOff,
                                        null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "No roles found",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 140.dp),
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredRoles) { role ->
                                    val isSelected = viewModel.selectedRoles.value.contains(role)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) {
                                                viewModel.removeRole(role)
                                            } else {
                                                viewModel.addRole(role)
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = role,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                if (isSelected) Icons.Default.CheckCircle else Icons.Default.Work,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = if (isSelected) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onSurface,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        border = FilterChipDefaults.filterChipBorder(
                                            borderColor = MaterialTheme.colorScheme.outline,
                                            selectedBorderColor = MaterialTheme.colorScheme.primary,
                                            borderWidth = 1.dp,
                                            selectedBorderWidth = 2.dp,
                                            selected = isSelected,
                                            enabled = true
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsibleFilterSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    badge: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header - clickable to expand/collapse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (badge != null && !isExpanded) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    "Toggle section",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Content - animated visibility
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}
