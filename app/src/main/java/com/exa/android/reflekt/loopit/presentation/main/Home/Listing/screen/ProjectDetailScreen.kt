package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ProjectListViewModel
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    navController: NavHostController,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val project = state.projects.find { it.id == projectId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val scrollState = rememberScrollState()

    // Animation states
    val animatedVisibility by animateFloatAsState(
        targetValue = if (scrollState.value > 0) 0.9f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    // Expanded states
    var expandedRoles by remember { mutableStateOf(false) }
    var expandedTags by remember { mutableStateOf(false) }
    var expandedEnrolled by remember { mutableStateOf(false) }
    var expandedRequests by remember { mutableStateOf(false) }

    val isOwner = project?.createdBy == currentUserId
    val isEnrolled = currentUserId?.let { project?.enrolledPersons?.containsKey(it) } ?: false

    LaunchedEffect(projectId) {
        if (state.projects.none { it.id == projectId }) {
            viewModel.onEvent(ProjectListEvent.Refresh)
        }
    }


    LaunchedEffect(state.error) {
        state.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Dismiss"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Project Details",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.alpha(animatedVisibility)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (project != null && currentUserId != null && !isOwner) {
                ExtendedFloatingActionButton(
                    onClick = {
                        if (isEnrolled) {
                            viewModel.withdrawFromProject(project.id)
                        } else {
                            val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
                            viewModel.enrollInProject(project.id, userName)
                        }
                    },
                    icon = {
                        Icon(
                            if (isEnrolled) Icons.Default.ExitToApp else Icons.Default.PersonAdd,
                            contentDescription = if (isEnrolled) "Withdraw" else "Enroll"
                        )
                    },
                    text = { Text(if (isEnrolled) "Withdraw" else "Enroll") },
                    containerColor = if (isEnrolled) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (isEnrolled) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        if (project == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Not found",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text("Project not found", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .fillMaxSize()
            ) {
                // Header Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            ),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            project.title,
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Author",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "by ${project.createdByName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Description Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Description",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                project.description,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }

                    // Roles Needed Section
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expandedRoles = !expandedRoles }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Group,
                                contentDescription = "Roles",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Roles Needed (${project.rolesNeeded.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expandedRoles) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expandedRoles) "Collapse roles" else "Expand roles",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = expandedRoles || project.rolesNeeded.size <= 3,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            FlowRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                project.rolesNeeded.forEach { role ->
                                    ElevatedAssistChip(
                                        onClick = {},
                                        label = { Text(role) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ),
                                        elevation = AssistChipDefaults.assistChipElevation(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Tags Section
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expandedTags = !expandedTags }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Tag,
                                contentDescription = "Tags",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tags (${project.tags.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expandedTags) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expandedTags) "Collapse tags" else "Expand tags",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = expandedTags || project.tags.size <= 3,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            FlowRow(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                project.tags.forEach { tag ->
                                    ElevatedAssistChip(
                                        onClick = {},
                                        label = { Text(tag) },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        ),
                                        elevation = AssistChipDefaults.assistChipElevation(4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Enrolled Members Section (visible to owner)
                    if (isOwner) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expandedEnrolled = !expandedEnrolled }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = "Team Members",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Team Members (${project.enrolledPersons.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (expandedEnrolled) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (expandedEnrolled) "Collapse members" else "Expand members",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(
                                visible = expandedEnrolled,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(project.enrolledPersons.toList()) { (userId, userName) ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                elevation = CardDefaults.cardElevation(1.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .background(
                                                                color = if (userId == project.createdBy)
                                                                    MaterialTheme.colorScheme.primaryContainer
                                                                else
                                                                    MaterialTheme.colorScheme.secondaryContainer,
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            userName.take(1).uppercase(),
                                                            style = MaterialTheme.typography.titleLarge,
                                                            color = if (userId == project.createdBy)
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            else
                                                                MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            userName,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        if (userId == project.createdBy) {
                                                            Text(
                                                                "Creator",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            navController.navigate("map_screen/${project.enrolledPersons.keys.joinToString(",")}")
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Map,
                                            contentDescription = "View on map",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("View Team on Map")
                                    }
                                }
                            }
                        }
                    }

                    // Join Requests Section (only for owner)
                    if (isOwner) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { expandedRequests = !expandedRequests }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = "Join Requests",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Join Requests (${project.requestedPersons.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = if (expandedRequests) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (expandedRequests) "Collapse requests" else "Expand requests",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(
                                visible = expandedRequests,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 300.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(project.requestedPersons.toList()) { (userId, userName) ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                elevation = CardDefaults.cardElevation(1.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                                shape = CircleShape
                                                            ),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            userName.take(1).uppercase(),
                                                            style = MaterialTheme.typography.titleLarge,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            userName,
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                    Row {
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.acceptJoinRequest(
                                                                    project.id,
                                                                    userId,
                                                                    userName
                                                                )
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Accept",
                                                                tint = MaterialTheme.colorScheme.primary
                                                            )
                                                        }
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.rejectJoinRequest(
                                                                    project.id,
                                                                    userId
                                                                )
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Reject",
                                                                tint = MaterialTheme.colorScheme.error
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Project Metadata
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        project.createdAt?.toDate()?.let { date ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = "Created at",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Created ${date.formatAsTimeAgo()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Action Buttons (for project owner)
                if (isOwner) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigate("edit_project/${project.id}") },
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Project")
                        }
                        Button(
                            onClick = {
                                viewModel.onEvent(ProjectListEvent.DeleteProject(project.id))
                                navController.popBackStack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

// Extension function for date formatting
fun Date.formatAsTimeAgo(): String {
    val seconds = (System.currentTimeMillis() - this.time) / 1000
    return when {
        seconds < 60 -> "$seconds seconds ago"
        seconds < 3600 -> "${seconds / 60} minutes ago"
        seconds < 86400 -> "${seconds / 3600} hours ago"
        seconds < 2592000 -> "${seconds / 86400} days ago"
        seconds < 31536000 -> "${seconds / 2592000} months ago"
        else -> "${seconds / 31536000} years ago"
    }
}


/*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    navController: NavHostController,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val project = state.projects.find { it.id == projectId }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(projectId) {
        if (state.projects.none { it.id == projectId }) {
            viewModel.onEvent(ProjectListEvent.Refresh)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { error ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = error,
                    actionLabel = "Dismiss"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Project Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        project?.let { project ->
            ProjectCard(
                project = project,
                onClick = {}, // No action needed since we're already on detail screen
                isEditable = project.createdBy == currentUserId,
                onDelete = {
                    viewModel.onEvent(ProjectListEvent.DeleteProject(project.id))
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate("edit_project/${project.id}")
                },
                onEnroll = {
                    val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
                    viewModel.enrollInProject(project.id, userName)
                },
                withdraw = {
                    viewModel.withdrawFromProject(project.id)
                },
                currentUserId = currentUserId,
                modifier = Modifier
                    .padding(padding)
                    .fillMaxWidth()
            )
        } ?: run {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Project not found")
                }
            }
        }
    }
}

 */