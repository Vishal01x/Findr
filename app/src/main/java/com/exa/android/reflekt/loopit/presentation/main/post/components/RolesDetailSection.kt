package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.ProjectRole
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Detailed section showing all project roles with full information
 */
@Composable
fun RolesDetailSection(
    roles: List<ProjectRole>,
    enrollmentStatuses: Map<String, EnrollmentStatus> = emptyMap(),
    hasAppliedToProject: Boolean = false,
    onEnrollClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (roles.isEmpty()) return
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Text(
            text = "Project Roles (${roles.size})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        roles.forEach { role ->
            DetailedRoleCard(
                role = role,
                enrollmentStatus = enrollmentStatuses[role.id] ?: EnrollmentStatus.NOT_ENROLLED,
                hasAppliedToProject = hasAppliedToProject,
                onEnrollClick = { onEnrollClick(role.id) }
            )
        }
    }
}

/**
 * Detailed role card with full information
 */
@Composable
private fun DetailedRoleCard(
    role: ProjectRole,
    enrollmentStatus: EnrollmentStatus,
    hasAppliedToProject: Boolean = false,
    onEnrollClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFilled = role.positionsFilled >= role.positionsNeeded
    val fillPercentage = (role.positionsFilled.toFloat() / role.positionsNeeded).coerceIn(0f, 1f)
    
    // Determine button state
    val isEnrolled = enrollmentStatus == EnrollmentStatus.ACCEPTED
    val isPending = enrollmentStatus == EnrollmentStatus.PENDING
    
    // If user has applied to ANY role, they can't enroll in others
    val buttonEnabled = !isFilled && !hasAppliedToProject && enrollmentStatus == EnrollmentStatus.NOT_ENROLLED
    
    val buttonText = when {
        isEnrolled -> "Enrolled"
        isPending -> "Pending"
        hasAppliedToProject -> "Already Applied"
        isFilled -> "Role Filled"
        else -> "Enroll in Role"
    }
    
    val buttonIcon = when {
        enrollmentStatus == EnrollmentStatus.ACCEPTED -> Icons.Default.CheckCircle
        enrollmentStatus == EnrollmentStatus.PENDING -> Icons.Default.Schedule
        isFilled -> Icons.Default.Block
        else -> Icons.Default.PersonAdd
    }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Role Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = role.roleName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    role.technology?.let { tech ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Technology",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = tech,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                // Status Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFilled) 
                        MaterialTheme.colorScheme.errorContainer 
                    else 
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isFilled) "FILLED" else "OPEN",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFilled) 
                            MaterialTheme.colorScheme.onErrorContainer 
                        else 
                            MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // Description
            role.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Positions Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Positions",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${role.positionsFilled}/${role.positionsNeeded} filled",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                if (!isFilled) {
                    Text(
                        text = "${role.remainingPositions()} spots left",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Progress Bar
            LinearProgressIndicator(
                progress = { fillPercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (isFilled) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            
            // Enroll Button
            Button(
                onClick = onEnrollClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = "Enroll",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
