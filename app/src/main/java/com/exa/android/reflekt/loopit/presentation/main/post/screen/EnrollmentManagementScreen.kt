package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentManagementScreen(
    onBackClick: () -> Unit,
    viewModel: EnrollmentManagementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enrollment Requests") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.enrollments.isEmpty()) {
                Text(
                    text = "No enrollment requests yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Spacing.medium)
                ) {
                    val groupedEnrollments = state.enrollments.groupBy { it.status }
                    
                    // Pending Requests
                    groupedEnrollments[EnrollmentStatus.PENDING]?.let { pending ->
                         if (pending.isNotEmpty()) {
                             item {
                                 Text(
                                     text = "Pending Requests",
                                     style = MaterialTheme.typography.titleMedium,
                                     fontWeight = FontWeight.Bold,
                                     modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                                 )
                             }
                             items(pending) { enrollment ->
                                 EnrollmentItem(
                                     enrollment = enrollment,
                                     onAccept = { viewModel.acceptEnrollment(enrollment.id) },
                                     onReject = { viewModel.rejectEnrollment(enrollment.id) }
                                 )
                             }
                         }
                    }

                    // Accepted Requests
                    groupedEnrollments[EnrollmentStatus.ACCEPTED]?.let { accepted ->
                        if (accepted.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Accepted",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                                )
                            }
                            items(accepted) { enrollment ->
                                EnrollmentItem(enrollment = enrollment, onAccept = {}, onReject = {})
                            }
                        }
                    }
                    
                    // Rejected Requests
                    groupedEnrollments[EnrollmentStatus.REJECTED]?.let { rejected ->
                        if (rejected.isNotEmpty()) {
                            item {
                                Text(
                                    text = "Rejected",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
                                )
                            }
                            items(rejected) { enrollment ->
                                EnrollmentItem(enrollment = enrollment, onAccept = {}, onReject = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnrollmentItem(
    enrollment: RoleEnrollment,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                 Text(
                    text = enrollment.userName.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.medium))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = enrollment.userName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Role: ${enrollment.roleId}", // Assuming roleId behaves like a name for now
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                 enrollment.message?.let {
                    Text(
                        text = "\"$it\"",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                         color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (enrollment.status == EnrollmentStatus.PENDING) {
                IconButton(onClick = onAccept) {
                    Icon(Icons.Rounded.Check, contentDescription = "Accept", tint = Color(0xFF4CAF50)) // Green
                }
                IconButton(onClick = onReject) {
                    Icon(Icons.Rounded.Close, contentDescription = "Reject", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                 Text(
                    text = enrollment.status.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when(enrollment.status) {
                        EnrollmentStatus.ACCEPTED -> Color(0xFF4CAF50)
                       else -> MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}
