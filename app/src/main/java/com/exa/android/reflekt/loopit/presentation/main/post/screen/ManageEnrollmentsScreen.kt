package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.theme.Spacing
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ManageEnrollmentsScreen(
    onBackClick: () -> Unit,
    viewModel: ManageEnrollmentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show messages
    LaunchedEffect(state.successMessage, state.error) {
        state.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Enrollments") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.post == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            state.post?.let { post ->
                val roles = post.projectRoles
                val pagerState = rememberPagerState(pageCount = { roles.size })
                val scope = rememberCoroutineScope()
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Role Tabs
                    if (roles.size > 1) {
                        ScrollableTabRow(
                            selectedTabIndex = pagerState.currentPage,
                            edgePadding = 0.dp
                        ) {
                            roles.forEachIndexed { index, role ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                    text = { 
                                        Text(
                                            text = role.roleName,
                                            maxLines = 1
                                        ) 
                                    }
                                )
                            }
                        }
                    }
                    
                    // Pager for each role
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val role = roles[page]
                        val enrollments = state.enrollmentsByRole[role.id] ?: emptyList()
                        
                        RoleEnrollmentsList(
                            role = role,
                            enrollments = enrollments,
                            onAccept = { enrollmentId ->
                                viewModel.acceptEnrollment(enrollmentId, role.id)
                            },
                            onReject = { enrollmentId ->
                                viewModel.rejectEnrollment(enrollmentId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleEnrollmentsList(
    role: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole,
    enrollments: List<RoleEnrollment>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
    val pending = enrollments.filter { it.status == EnrollmentStatus.PENDING }
    val accepted = enrollments.filter { it.status == EnrollmentStatus.ACCEPTED }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        // Role Info
        item {
            RoleInfoCard(role)
        }
        
        // Pending Requests
        if (pending.isNotEmpty()) {
            item {
                Text(
                    text = "Pending Requests (${pending.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(pending) { enrollment ->
                EnrollmentRequestCard(
                    enrollment = enrollment,
                    onAccept = { onAccept(enrollment.id) },
                    onReject = { onReject(enrollment.id) },
                    showActions = true
                )
            }
        }
        
        // Accepted Members
        if (accepted.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(Spacing.medium))
                Text(
                    text = "Accepted Members (${accepted.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(accepted) { enrollment ->
                EnrollmentRequestCard(
                    enrollment = enrollment,
                    onAccept = {},
                    onReject = {},
                    showActions = false
                )
            }
        }
        
        if (pending.isEmpty() && accepted.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No enrollments yet")
                }
            }
        }
    }
}

@Composable
private fun RoleInfoCard(role: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Text(
                text = role.roleName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            role.technology?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.small))
            LinearProgressIndicator(
                progress = { (role.positionsFilled.toFloat() / role.positionsNeeded).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "${role.positionsFilled}/${role.positionsNeeded} positions filled",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EnrollmentRequestCard(
    enrollment: RoleEnrollment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    showActions: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar
                val avatarUrl = enrollment.userProfilePic
                if (avatarUrl != null) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = enrollment.userName.firstOrNull()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Column {
                    Text(
                        text = enrollment.userName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatTimeAgo(enrollment.requestedAt?.toDate() ?: Date()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (showActions) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    IconButton(
                        onClick = onReject,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Icon(Icons.Default.Close, "Reject", tint = MaterialTheme.colorScheme.error)
                    }
                    IconButton(
                        onClick = onAccept,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Check, "Accept", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Accepted",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun formatTimeAgo(date:Date): String {
    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24
    
    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}
