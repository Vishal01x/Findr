package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ProjectListViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.ProjectCard
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.SearchFilterBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListedProjectsScreen(
    navController: NavHostController,
    onProjectClick: (String) -> Unit,
    viewModel: ProjectListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showFab by remember { mutableStateOf(true) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                showFab = offset == 0 || listState.firstVisibleItemScrollOffset < 0
            }
    }
    // Handle errors
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
    BackHandler(enabled = state.showMyProjectsOnly) {
        viewModel.onEvent(ProjectListEvent.ToggleMyProjects)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Project List") },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(ProjectListEvent.ToggleMyProjects) }) {
                        Icon(
                            imageVector = if (state.showMyProjectsOnly) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "My Projects"
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(ProjectListEvent.Refresh) }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },

        floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(ProjectRoute.CreateProject.route) },
                    icon = { Icon(Icons.Filled.Add, "Add Project") },
                    text = { Text("New Project") }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Search and Filter Bar
            SearchFilterBar(
                searchQuery = state.searchQuery,
                onSearchChange = { viewModel.onEvent(ProjectListEvent.SearchQueryChanged(it)) },
                selectedRoles = state.selectedRoles,
                availableRoles = state.availableRoles,
                onRoleSelected = { viewModel.onEvent(ProjectListEvent.RoleSelected(it)) },
                onRoleDeselected = { viewModel.onEvent(ProjectListEvent.RoleDeselected(it)) },
                selectedTags = state.selectedTags,
                availableTags = state.availableTags,
                onTagSelected = { viewModel.onEvent(ProjectListEvent.TagSelected(it)) },
                onTagDeselected = { viewModel.onEvent(ProjectListEvent.TagDeselected(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Project List
            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.projects.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = "No projects",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (state.showMyProjectsOnly) {
                                    "You haven't created any projects yet"
                                } else {
                                    if (state.searchQuery.isNotBlank() ||
                                        state.selectedRoles.isNotEmpty() ||
                                        state.selectedTags.isNotEmpty()) {
                                        "No matching projects found"
                                    } else {
                                        "No projects available"
                                    }
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            if (!state.showMyProjectsOnly &&
                                (state.searchQuery.isNotBlank() ||
                                        state.selectedRoles.isNotEmpty() ||
                                        state.selectedTags.isNotEmpty())) {
                                TextButton(
                                    onClick = {
                                        viewModel.onEvent(ProjectListEvent.ClearFilters)
                                    }
                                ) {
                                    Text("Clear all filters")
                                }
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.projects.size) {
                            val project = state.projects[it]
                            ProjectCard(
                                project = project,
                                onClick = { navController.navigate("project_detail/${project.id}")  },
                                isEditable = state.showMyProjectsOnly,
                                onDelete = {
                                    viewModel.onEvent(ProjectListEvent.DeleteProject(project.id))
                                },
                                onEdit = {
                                    // Navigate to edit screen
                                    navController.navigate("edit_project/${project.id}")
                                },
                                onEnroll = {
                                    // You might want to get the current user's name from somewhere
                                    val userName = "Current User Name" // Replace with actual user name
                                    viewModel.enrollInProject(project.id, userName)
                                },
                                withdraw = {
                                    viewModel.withdrawFromProject(project.id)
                                },
                                onAccept = { userId, userName ->
                                    viewModel.acceptJoinRequest(project.id, userId, userName)
                                },
                                onReject = { userId ->
                                    viewModel.rejectJoinRequest(project.id, userId)
                                },
                                onViewOnMap = { userIds ->
                                    navController.navigate("map_screen/${userIds.joinToString(",")}")
                                },
                                currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }
            }
        }
    }
}