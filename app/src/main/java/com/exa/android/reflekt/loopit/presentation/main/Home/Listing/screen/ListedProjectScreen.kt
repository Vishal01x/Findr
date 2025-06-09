package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.*
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ProjectListViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.ProjectCard
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.SearchFilterBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.presentation.test.AppHeader
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.exa.android.reflekt.loopit.util.model.Comment
import com.exa.android.reflekt.loopit.util.showToast
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun ListedProjectsScreen(
    navController: NavHostController,
    onProjectClick: (String) -> Unit,
    viewModel: ProjectListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var showFab by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val tooltipState = rememberTooltipState(isPersistent = true)
    var isTooltipShown by remember { mutableStateOf(false) }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { viewModel.onEvent(ProjectListEvent.Refresh) }
    )
    val currentUserId = viewModel.currentUserId

    val curUserDetailsMap by userViewModel.userDetail.collectAsState()
    val curUserDetails = curUserDetailsMap[currentUserId]


    LaunchedEffect(currentUserId) {
        currentUserId?.let{
            userViewModel.getUserDetail(it)
        }
    }

    LaunchedEffect(tooltipState) {
        if (!isTooltipShown) {
            // Wait for UI to settle before showing
            delay(500)
            tooltipState.show()
            // Auto-hide after 5 seconds
            delay(50000)
            tooltipState.dismiss()
            isTooltipShown = true
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                showFab = offset == 0
            }
    }

    BackHandler(enabled = state.showMyProjectsOnly) {
        viewModel.onEvent(ProjectListEvent.ToggleMyProjects)
    }

    Scaffold(
        /*topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.showMyProjectsOnly) "My Activity" else "Explore",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (state.showMyProjectsOnly) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.shadow(elevation = 10.dp),
                actions = {
                    // Filter Status Indicator
                    if (state.selectedRoles.isNotEmpty() || state.selectedTags.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(
                                        text = (state.selectedRoles.size + state.selectedTags.size).toString(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        ) {
                            TooltipBox(
                                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                                tooltip = {
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(0.1f),
                                                shape = MaterialTheme.shapes.medium
                                            )
                                            .padding(horizontal = 6.dp, vertical = 4.dp)
                                    ) {
                                    }
                                },
                                state = tooltipState
                            ) {
                                IconButton(
                                    onClick = {
                                        viewModel.onEvent(ProjectListEvent.ClearFilters)
                                        tooltipState.dismiss()
                                      },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FilterAlt,
                                        contentDescription = "Active Filters",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // My Projects Toggle
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                        tooltip = {
                            if (state.showMyProjectsOnly){
                                TrianglePointerTooltip( "Show all activity")
                            }
                            else{
                                TrianglePointerTooltip("Show only my activity")
                            }

                        },
                        state = tooltipState
                    ) {
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            IconButton(
                                onClick = {
                                    viewModel.onEvent(ProjectListEvent.ToggleMyProjects)
                                    tooltipState.dismiss()
                                },

                            ) {
                                Icon(
                                    imageVector = if (state.showMyProjectsOnly) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "My Projects Filter",
                                    tint = if (state.showMyProjectsOnly) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                }
            )
        },*/
        topBar = { AppHeader(state.showMyProjectsOnly, curUserDetails,onNotificationsClick = {
            showToast(context, "No Notifications Yet")
        }, onProfileClick = {
            viewModel.onEvent(ProjectListEvent.ToggleMyProjects)
            tooltipState.dismiss()
        }) },
        /*floatingActionButton = {
            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(ProjectRoute.CreateProject.route) },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add Project",
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    text = {
                        Text(
                            "New Activity",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }*/
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                //contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
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
                            .padding(vertical = 4.dp),
                        selectedPostType = state.selectedPostType,
                        onTypeSelected = { type ->
                            viewModel.onEvent(ProjectListEvent.SelectPostType(type))
                        },
                        onAddClick = {
                            navController.navigate(ProjectRoute.CreateProject.route)
                        }

                    )
                }

                when {
                    state.isLoading -> {

                        items(5) {
                            ShimmerProjectCard()
                        }

                    }
                    state.error != null -> {
                        item {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = "Error",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = state.error!!,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.loadProjects() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    ) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }

                    state.projects.isEmpty() -> {
                        item {

                            EmptyStateContent(
                                showMyProjectsOnly = state.showMyProjectsOnly,
                                hasFilters = state.searchQuery.isNotBlank() ||
                                        state.selectedRoles.isNotEmpty() ||
                                        state.selectedTags.isNotEmpty(),
                                onClearFilters = { viewModel.onEvent(ProjectListEvent.ClearFilters) }
                            )
                        }
                    }

                    else -> {

                        items(
                            items = state.projects,
                            key = { it.id }
                        ) { project ->
                            if( state.showMyProjectsOnly  || currentUserId != project.createdBy) {

                                ProjectCard(
                                    project = project,
                                    onClick = { navController.navigate("project_detail/${project.id}") },
                                    isEditable = state.showMyProjectsOnly,
                                    onDelete = {
                                        viewModel.onEvent(
                                            ProjectListEvent.DeleteProject(
                                                project.id
                                            )
                                        )
                                    },
                                    onEdit = { navController.navigate("edit_project/${project.id}") },
                                    onEnroll = { viewModel.enrollInProject(project) },
                                    withdraw = { viewModel.withdrawFromProject(project.id) },
                                    onAccept = { userId, userName ->
                                        viewModel.acceptJoinRequest(project, userId, userName)
                                    },
                                    onReject = { userId ->
                                        viewModel.rejectJoinRequest(project, userId)
                                    },
                                    onViewOnMap = { userIds ->
                                        navController.navigate("map_screen/${userIds.joinToString(",")}")
                                    },
                                    currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
                                    modifier = Modifier
                                        .animateItemPlacement()
                                        .fillMaxWidth()
                                        .clipToBounds(),
                                    onAuthorProfileClick = { autherId ->
                                        navController.navigate(
                                            ProfileRoute.UserProfile.createRoute(
                                                autherId
                                            )
                                        )
                                    },
                                    onToggleLike = { projectId ->
                                        viewModel.onEvent(ProjectListEvent.ToggleLike(projectId))
                                    },
                                    onCommentEvent = { event ->
                                        viewModel.onEvent(event)
                                    }
                                )
                            }
                        }

                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun TrianglePointerTooltip(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // Align entire content to end
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }


    }
}


@Composable
fun ShimmerProjectCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shimmerEffect()
        )
    }
}

fun Modifier.shimmerEffect(): Modifier = composed {
    val colors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = LinearEasing
            )
        ), label = ""
    )

    val brush = Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim.value - 500, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    this.background(brush)
}

@Composable
private fun EmptyStateContent(
    showMyProjectsOnly: Boolean,
    hasFilters: Boolean,
    onClearFilters: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Inbox,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Text(
                text = when {
                    showMyProjectsOnly -> "Your project portfolio is empty"
                    hasFilters -> "No projects match your criteria"
                    else -> "No projects available yet"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                textAlign = TextAlign.Center
            )

            if (hasFilters && !showMyProjectsOnly) {
                FilledTonalButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text("Clear Filters")
                }
            }

            if (showMyProjectsOnly) {
                Text(
                    text = "Start by creating your first project!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
private fun UserCommentPreview(
    comment: Comment,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.text,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium
                )

                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, "Edit")
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, "Delete")
                }
            }
            Text(
                text = comment.timestamp.toDate().formatAsTimeAgo(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}