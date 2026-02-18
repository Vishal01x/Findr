package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Post
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Section showing enrollment/registration options based on post type
 * For PROJECT: Shows roles with enroll buttons
 * For EVENT: Shows event details with register button
 */
@Composable
fun PostActionSection(
    post: Post,
    currentUserId: String,
    onEnrollInRole: (String) -> Unit,
    onRegisterForEvent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val postType = PostType.fromDisplayName(post.type)
    
    // Check if user has already applied/enrolled in project
    val hasAppliedToProject = post.hasUserRequested(currentUserId) || post.isUserEnrolled(currentUserId)
    
    when (postType) {
        PostType.PROJECT -> {
            if (post.projectRoles.isNotEmpty()) {
                ProjectRolesPreview(
                    roles = post.projectRoles.take(2), // Show max 2 roles in preview
                    totalRoles = post.projectRoles.size,
                    onEnrollClick = onEnrollInRole,
                    hasAppliedToProject = hasAppliedToProject,
                    modifier = modifier
                )
            }
        }
        PostType.EVENT -> {
            post.eventDetails?.let { eventDetails ->
                EventPreview(
                    eventDetails = eventDetails,
                    onRegisterClick = onRegisterForEvent,
                    modifier = modifier
                )
            }
        }
        else -> {
            // No special action section for other types
        }
    }
}

/**
 * Preview of project roles with enroll buttons
 */
@Composable
private fun ProjectRolesPreview(
    roles: List<ProjectRole>,
    totalRoles: Int,
    onEnrollClick: (String) -> Unit,
    hasAppliedToProject: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Text(
            text = "Roles Needed",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        roles.forEach { role ->
            RoleCard(
                role = role,
                onEnrollClick = { onEnrollClick(role.id) },
                hasAppliedToProject = hasAppliedToProject
            )
        }
        
        if (totalRoles > 2) {
            Text(
                text = "+${totalRoles - 2} more roles",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = Spacing.small)
            )
        }
    }
}

/**
 * Individual role card with enroll button
 */
@Composable
private fun RoleCard(
    role: ProjectRole,
    onEnrollClick: () -> Unit,
    hasAppliedToProject: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = role.roleName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (!role.technology.isNullOrBlank()) {
                    Text(
                        text = role.technology,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${role.positionsFilled}/${role.positionsNeeded} filled",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (role.positionsFilled >= role.positionsNeeded) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                    
                    if (role.isManuallyClosed) {
                        Text(
                            text = "Closed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Hide button if closed, full, or already applied
            if (!role.isManuallyClosed && role.positionsFilled < role.positionsNeeded && !hasAppliedToProject) {
                Button(
                    onClick = onEnrollClick,
                    modifier = Modifier.padding(start = Spacing.small),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Enroll",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Enroll")
                }
            }
        }
    }
}

/**
 * Event details preview with register button
 */
@Composable
private fun EventPreview(
    eventDetails: com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = "Event",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Event Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                
                eventDetails.eventTime?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                val locationText = if (eventDetails.isVirtual) {
                    "Virtual Event"
                } else {
                    eventDetails.location ?: "Location TBA"
                }
                
                Text(
                    text = locationText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )

                if (eventDetails.isManuallyClosed) {
                     Text(
                        text = "Registration Closed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else if (eventDetails.isFull()) {
                    Text(
                        text = "Event Full",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Hide if closed, full, or registration closed
            if (!eventDetails.isManuallyClosed && eventDetails.isRegistrationOpen()) {
                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier.padding(start = Spacing.small),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Register")
                }
            }
        }
    }
}
