package com.exa.android.reflekt.loopit.presentation.main.post.components

import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*
/*
/**
 * Section showing pending enrollment requests (visible only to post author)
 */
@Composable
fun PendingRequestsSection(
    post: Post,
    onAccept: (roleId: String, userId: String) -> Unit,
    onReject: (roleId: String, userId: String) -> Unit,
    onViewProfile: (userId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Only show to post author
    if (post.createdBy != "current_user") return
    
    val totalPending = post.totalPendingRequests
    if (totalPending == 0) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(Spacing.medium)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Pending Requests",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Badge(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = totalPending.toString(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.medium))
        
        // Requests grouped by role
        post.pendingRequests.forEach { (roleId, requests) ->
            if (requests.isNotEmpty()) {
                val role = post.rolesNeeded.find { it.id == roleId }
                role?.let {
                    RolePendingRequestsCard(
                        roleName = it.name,
                        requests = requests,
                        onAccept = { userId -> onAccept(roleId, userId) },
                        onReject = { userId -> onReject(roleId, userId) },
                        onViewProfile = onViewProfile
                    )
                    Spacer(modifier = height(Spacing.medium))
                }
            }
        }
    }
}

/**
 * Card showing pending requests for a specific role
 */
@Composable
fun RolePendingRequestsCard(
    roleName: String,
    requests: List<EnrollmentRequest>,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onViewProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Role name header
            Text(
                text = "$roleName Requests (${requests.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
            
            // Individual requests
            requests.forEach { request ->
                EnrollmentRequestItem(
                    request = request,
                    onAccept = { onAccept(request.userId) },
                    onReject = { onReject(request.userId) },
                    onViewProfile = { onViewProfile(request.userId) }
                )
            }
        }
    }
}

/**
 * Individual enrollment request item
 */
@Composable
fun EnrollmentRequestItem(
    request: EnrollmentRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onViewProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithNoIndication(onClick = onViewProfile)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = request.userImageUrl ?: "https://ui-avatars.com/api/?name=${request.userName}",
                    contentDescription = request.userName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                
                Column {
                    Text(
                        text = request.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    if (request.userCourse != null && request.userYear != null) {
                        Text(
                            text = "${request.userYear} • ${request.userCourse}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = "Time",
                            modifier = Modifier.size(11.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = com.exa.android.reflekt.loopit.core.utils.formatTimeAgo(request.requestedAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reject button
                IconButton(
                    onClick = onReject,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Reject",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                // Accept button
                FilledIconButton(
                    onClick = onAccept,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
*/