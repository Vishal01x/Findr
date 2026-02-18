package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.presentation.main.post.components.AuthorRequestCard
import com.exa.android.reflekt.loopit.theme.Spacing
/**
 * Comprehensive screen for post authors to manage enrollment/registration requests
 * Features tabs for Pending, Accepted, and Rejected requests
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorManagementScreen(
    postId: String,
    onBackClick: () -> Unit,
    onUserClick : (String) -> Unit,
    viewModel: AuthorManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Load data when screen appears
    LaunchedEffect(postId) {
        viewModel.loadEnrollments(postId)
    }
    
    // Show snackbar for messages
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Manage Requests")
                        Text(
                            text = "${state.pendingCount} pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Pending")
                            if (state.pendingCount > 0) {
                                Badge {
                                    Text("${state.pendingCount}")
                                }
                            }
                        }
                    }
                )
                
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Accepted")
                            if (state.acceptedCount > 0) {
                                Text(
                                    text = "${state.acceptedCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
                
                Tab(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rejected")
                            if (state.rejectedCount > 0) {
                                Text(
                                    text = "${state.rejectedCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                )
            }
            
            // Content for selected tab
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading && state.getAllEnrollments().isEmpty() -> {
                        // Loading state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    state.error != null -> {
                        // Error state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                Text(
                                    text = state.error ?: "An error occurred",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(onClick = { viewModel.loadEnrollments(postId) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    
                    else -> {
                        // List content based on selected tab
                        val enrollments = when (state.selectedTab) {
                            0 -> state.pendingEnrollments
                            1 -> state.acceptedEnrollments
                            2 -> state.rejectedEnrollments
                            else -> emptyList()
                        }
                        
                        if (enrollments.isEmpty()) {
                            // Empty state
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                                ) {
                                    Icon(
                                        Icons.Rounded.SearchOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when (state.selectedTab) {
                                            0 -> "No pending requests"
                                            1 -> "No accepted requests"
                                            2 -> "No rejected requests"
                                            else -> "No requests"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // List of requests
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(Spacing.medium),
                                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
                            ) {
                                items(
                                    items = enrollments,
                                ) { enrollment ->
                                    AuthorRequestCard(
                                        enrollment = enrollment,
                                        onAccept = { viewModel.acceptEnrollment(enrollment.id) },
                                        onReject = { viewModel.rejectEnrollment(enrollment.id) },
                                        onUserClick =  { onUserClick(enrollment.userId) },
                                        isProcessing = state.processingIds.contains(enrollment.id)
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
