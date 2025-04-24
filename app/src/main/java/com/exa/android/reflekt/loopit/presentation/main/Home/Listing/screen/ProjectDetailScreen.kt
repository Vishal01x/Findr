package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddDisabled
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ProjectListViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.exa.android.reflekt.loopit.util.application.ProjectListState
import com.exa.android.reflekt.loopit.util.model.Project
import com.google.firebase.auth.FirebaseAuth
import io.getstream.video.android.compose.ui.components.avatar.UserAvatar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.min


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
    val context = LocalContext.current

    // Animation states
    val headerHeight = 280.dp
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val scrollOffset = minOf(scrollState.value.toFloat(), headerHeightPx)
    val dynamicElevation = animateDpAsState(
        targetValue = if (scrollOffset > headerHeightPx - 50) 4.dp else 0.dp,
        label = "elevation"
    )

    // Expanded states
    var expandedRoles by rememberSaveable { mutableStateOf(true) }
    var expandedTags by rememberSaveable { mutableStateOf(true) }
    var expandedEnrolled by rememberSaveable { mutableStateOf(false) }
    var expandedRequests by rememberSaveable { mutableStateOf(false) }

    val isOwner = project?.createdBy == currentUserId
    val isEnrolled = currentUserId?.let {
        project?.enrolledPersons?.containsKey(it) == true ||
                project?.requestedPersons?.containsKey(it) == true
    } ?: false



    Log.d("projectdetail", "project: ${project?.enrolledPersons}, $currentUserId, $isEnrolled")
    val view = LocalView.current
    val window = (view.context as Activity).window
    val insetsController = WindowCompat.getInsetsController(window, view)

    // Remember original colors to restore later
    val originalStatusBarColor = remember { window.statusBarColor }
    val originalNavigationBarColor = remember { window.navigationBarColor }

    val statusBarColor = MaterialTheme.colorScheme.primaryContainer.toArgb()
    val navigationBarColor = MaterialTheme.colorScheme.background.toArgb()

    DisposableEffect(Unit) {
        // Set custom colors when entering
        window.statusBarColor = statusBarColor
        window.navigationBarColor = navigationBarColor
        insetsController.isAppearanceLightStatusBars = false // Dark icons for light color
        insetsController.isAppearanceLightNavigationBars = true // Light icons for dark background

        onDispose {
            // Restore original colors when leaving
            window.statusBarColor = originalStatusBarColor
            window.navigationBarColor = originalNavigationBarColor
            insetsController.isAppearanceLightStatusBars =
                ColorUtils.calculateLuminance(originalStatusBarColor) > 0.5

            insetsController.isAppearanceLightNavigationBars =
                ColorUtils.calculateLuminance(originalNavigationBarColor) > 0.5
        }
    }

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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (project != null && currentUserId != null && !isOwner) {
                AnimatedVisibility(
                    visible = !scrollState.canScrollForward,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            if (isEnrolled) {
                                viewModel.withdrawFromProject(project.id)
                            } else {
                                viewModel.enrollInProject(project.id)
                            }
                        },
                        icon = {
                            Icon(
                                if (isEnrolled) Icons.Default.ExitToApp else Icons.Default.PersonAdd,
                                contentDescription = if (isEnrolled) "Withdraw" else "Enroll"
                            )
                        },
                        text = { Text(if (isEnrolled) "Withdraw" else "Enroll Now") },
                        containerColor = if (isEnrolled) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isEnrolled) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (project == null) {
                LoadingErrorState(
                    isLoading = state.isLoading,
                    modifier = Modifier.padding(padding)
                )
            } else {
                CustomScrollLayout(
                    scrollState = scrollState,
                    headerHeight = headerHeight,
                    headerContent = {
                        ProjectHeaderSection(
                            project = project,
                            scrollOffset = scrollOffset,
                            headerHeightPx = headerHeightPx,
                            onAuthorClick = { autherId->
                                navController.navigate(ProfileRoute.UserProfile.createRoute(autherId))
                            }
                        )
                    },
                    content = {
                        ProjectContentSection(
                            project = project,
                            isOwner = isOwner,
                            expandedRoles = expandedRoles,
                            expandedTags = expandedTags,
                            expandedEnrolled = expandedEnrolled,
                            expandedRequests = expandedRequests,
                            onRoleClick = { expandedRoles = !expandedRoles },
                            onTagClick = { expandedTags = !expandedTags },
                            onEnrolledClick = { expandedEnrolled = !expandedEnrolled },
                            onRequestClick = { expandedRequests = !expandedRequests },
                            viewModel = viewModel,
                            navController = navController,
                            context = context,
                            modifier = Modifier.padding(padding)
                        )
                    }
                )
            }

            DynamicTopAppBar(
                scrollState = scrollState,
                headerHeight = headerHeight,
                project = project,
                navController = navController,
                dynamicElevation = dynamicElevation.value
            )
        }
    }
}

@Composable
private fun LoadingErrorState(isLoading: Boolean, modifier: Modifier = Modifier) {
    if (isLoading) {
        // Show loading spinner
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Show error message
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Error loading project. Please try again.")
        }
    }
}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun CustomScrollLayout(
    scrollState: ScrollState,
    headerHeight: Dp,
    headerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(modifier = Modifier.verticalScroll(scrollState)) {
            // Header Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                headerContent()
            }

            // Main Content
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DynamicTopAppBar(
    scrollState: ScrollState,
    headerHeight: Dp,
    project: Project?,
    navController: NavHostController,
    dynamicElevation: Dp
) {
    val headerHeightPx = with(LocalDensity.current) { headerHeight.toPx() }
    val scrollOffset = minOf(scrollState.value.toFloat(), headerHeightPx)
    val titleOpacity = animateFloatAsState(
        targetValue = if (scrollOffset > headerHeightPx - 50) 1f else 0f,
        label = "titleOpacity"
    )

    TopAppBar(
        title = {
            if (project != null && titleOpacity.value > 0.1f) {
                Text(
                    project.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(titleOpacity.value)
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = CircleShape
                )
            ) {
                Icon(
                    Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            scrolledContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .graphicsLayer {
                translationY = -scrollOffset.toFloat() / 2
                alpha = titleOpacity.value
            }
            .shadow(dynamicElevation),
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun ProjectHeaderSection(
    project: Project,
    scrollOffset: Float,
    headerHeightPx: Float,
    onAuthorClick: (String)->Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
    ) {
        // Parallax background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .graphicsLayer {
                    translationY = scrollOffset * 0.5f
                    alpha = 1 - (scrollOffset / headerHeightPx)
                }
                .background(
                    brush = Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.primaryContainer,
                        1.0f to MaterialTheme.colorScheme.secondaryContainer
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                project.title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                maxLines = 2,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .offset(y = (-scrollOffset * 0.2f).dp)
            )

            ProjectStatusChip(project = project)

            Spacer(modifier = Modifier.height(16.dp))

            AuthorSection(
                project = project,
                onAuthorClick = { autherId->
                    onAuthorClick(autherId)
                }
            )
        }
    }
}

@Composable
private fun ProjectContentSection(
    project: Project,
    isOwner: Boolean,
    expandedRoles: Boolean,
    expandedTags: Boolean,
    expandedEnrolled: Boolean,
    expandedRequests: Boolean,
    onRoleClick: () -> Unit,
    onTagClick: () -> Unit,
    onEnrolledClick: () -> Unit,
    onRequestClick: () -> Unit,
    viewModel: ProjectListViewModel,
    navController: NavHostController,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        DescriptionSection(project.description)

        Spacer(modifier = Modifier.height(24.dp))

        ProjectChipsSection(
            roles = project.rolesNeeded,
            tags = project.tags,
            expandedRoles = expandedRoles,
            expandedTags = expandedTags,
            onRoleClick = onRoleClick,
            onTagClick = onTagClick
        )

        if (isOwner) {
            TeamMembersSection(
                members = project.enrolledPersons,
                creatorId = project.createdBy,
                expanded = expandedEnrolled,
                onHeaderClick = onEnrolledClick,
                onMemberClick = { userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                }
            )

            JoinRequestsSection(
                requests = project.requestedPersons,
                expanded = expandedRequests,
                onHeaderClick = onRequestClick,
                onAccept = { userId, userName ->
                    viewModel.acceptJoinRequest(project.id, userId, userName)
                },
                onReject = { userId ->
                    viewModel.rejectJoinRequest(project.id, userId)
                },
                onViewMap = {
                    if (project.requestedPersons.isNotEmpty()) {
                        navController.navigate("map_screen/${project.requestedPersons.keys.joinToString(",")}")
                    } else {
                        Toast.makeText(context, "No requested members", Toast.LENGTH_SHORT).show()
                    }
                },
                onRequestProfileClick = { userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                }
            )
        }

        ProjectMetadata(project = project)

        if (isOwner) {
            ProjectManagementButtons(
                project = project,
                onEdit = { navController.navigate("edit_project/${project.id}") },
                onDelete = {
                    viewModel.onEvent(ProjectListEvent.DeleteProject(project.id))
                    navController.popBackStack()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ProjectStatusChip(project: Project) {
    ElevatedAssistChip(
        onClick = {},
        label = { Text("Active") }, // Add actual status from project model
        colors = AssistChipDefaults.elevatedAssistChipColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.onSecondary
        ),
        leadingIcon = {
            Icon(
                Icons.Default.Circle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    )
}

@Composable
private fun AuthorSection(project: Project, onAuthorClick: (String) -> Unit = {}) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onAuthorClick(project.createdBy) }
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    project.createdByName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                "Created by",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                project.createdByName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DescriptionSection(description: String) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            "About the Project",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (!expanded) {
            Text(
                "Read more",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { expanded = true }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ProjectChipsSection(
    roles: List<String>,
    tags: List<String>,
    expandedRoles: Boolean,
    expandedTags: Boolean,
    onRoleClick: () -> Unit,
    onTagClick: () -> Unit
) {
    Column {
        // Roles Section
        SectionHeader(
            title = "Roles Needed",
            expanded = expandedRoles,
            onClick = onRoleClick
        )

        AnimatedVisibility(visible = expandedRoles) {
            FlowRow(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roles.forEach { role ->
                    AssistChip(
                        onClick = { /* Handle role chip click */ },
                        label = { Text(role) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    )
                }
            }
        }

        // Tags Section
        SectionHeader(
            title = "Project Tags",
            expanded = expandedTags,
            onClick = onTagClick
        )

        AnimatedVisibility(visible = expandedTags) {
            FlowRow(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    FilterChip(
                        selected = false,
                        onClick = { /* Handle tag chip click */ },
                        label = { Text(tag) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Tag,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TeamMembersSection(
    members: Map<String, String>,
    creatorId: String,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    onMemberClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Team Members • ${members.size}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    if (members.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.PersonOff,
                            message = "No members yet",
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(150.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(members.entries.size) {
                                val (userId, userName) = members.entries.elementAt(it)
                                MemberCard(
                                    name = userName,
                                    isCreator = userId == creatorId,
                                    role = if (userId == creatorId) "Creator" else "Member",
                                    onMemberClick = {
                                        onMemberClick(userId)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun JoinRequestsSection(
    requests: Map<String, String>,
    expanded: Boolean,
    onHeaderClick: () -> Unit,
    onAccept: (String, String) -> Unit,
    onReject: (String) -> Unit,
    onViewMap: () -> Unit,
    onRequestProfileClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onHeaderClick() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Join Requests • ${requests.size}",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Requests List
            AnimatedVisibility(visible = expanded) {
                Column {
                    requests.entries.forEach { (userId, userName) ->
                        RequestCard(
                            name = userName,
                            onAccept = { onAccept(userId, userName) },
                            onReject = { onReject(userId) },
                            modifier = Modifier.padding(vertical = 8.dp),
                            onProfileClick = { onRequestProfileClick(userId) }
                        )
                    }

                    if(requests.isNotEmpty()) {
                        ElevatedButton(
                            onClick = onViewMap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.elevatedButtonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Requests on Map")
                        }
                    }
                }
            }


            if (requests.isEmpty() && expanded) {
                EmptyState(
                    icon = Icons.Default.PersonAddDisabled,
                    message = "No pending requests",
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}


// New Components
@Composable
private fun MemberCard(
    name: String,
    isCreator: Boolean,
    role: String,
    onMemberClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onMemberClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isCreator) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = "Creator",
                    tint = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}


@Composable
private fun RequestCard(
    name: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        onProfileClick()
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            OutlinedButton(
                onClick = onAccept,
                modifier = Modifier
                    .height(45.dp)
                    .width(55.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                contentPadding = PaddingValues(4.dp)
            ) {

                Text("Accept", style = MaterialTheme.typography.labelSmall)

            }

            Spacer(modifier = Modifier.width(8.dp))

            // Reject Button (Vertical)
            OutlinedButton(
                onClick = onReject,
                modifier = Modifier
                    .height(45.dp)
                    .width(55.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                contentPadding = PaddingValues(4.dp)
            ) {

                Text("Reject", style = MaterialTheme.typography.labelSmall)

            }
        }
    }
}




@Composable
private fun EmptyState(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProjectMetadata(project: Project) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp)
    ) {
        Text(
            "Project Metadata",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append("Project ID: ")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    append(project.id)
                }
            },
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append("Creation Date: ")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    append(project.createdAt?.toDate()?.formatAsTimeAgo() ?: "")
                }
            },
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    append("Members: ")
                }
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    append(project.enrolledPersons.size.toString())
                }
            },
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun ProjectManagementButtons(
    project: Project,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        OutlinedButton(
            onClick = onEdit,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit Project")
        }

        FilledTonalButton(
            onClick = onDelete,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
            Spacer(modifier = Modifier.width(4.dp))
            Text("Delete Project")
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
