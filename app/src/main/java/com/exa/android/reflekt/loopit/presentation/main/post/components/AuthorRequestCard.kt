package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EnrollmentStatus
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventRegistration
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.RoleEnrollment
import com.exa.android.reflekt.loopit.theme.Spacing

/**
 * Card for displaying enrollment/registration request for author management
 * Shows user info, role/event, message, and accept/reject actions
 */
@Composable
fun AuthorRequestCard(
    enrollment: RoleEnrollment,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onUserClick : () -> Unit,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
                .clickable{
                    onUserClick()
                }
        ) {
            // User Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (enrollment.userProfilePic != null) {
                    AsyncImage(
                        model = enrollment.userProfilePic,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = enrollment.userName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // User Name and Role
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = enrollment.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Applying for:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = enrollment.roleName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Timestamp
                    if (enrollment.requestedAt != null) {
                        Text(
                            text = enrollment.getFormattedTimestamp(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Status Badge or Actions
                if (enrollment.status == EnrollmentStatus.PENDING && !isProcessing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        // Accept Button
                        FilledIconButton(
                            onClick = onAccept,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "Accept",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        // Reject Button
                        FilledIconButton(
                            onClick = onReject,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Reject",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    EnrollmentStatusBadge(status = enrollment.status)
                }
            }
            
            // Message if available
            if (enrollment.message != null && enrollment.message.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.small))
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.small)
                    ) {
                        Text(
                            text = "Message:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = enrollment.message,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card variant for event registration
 */
@Composable
fun AuthorEventRegistrationCard(
    registration: EventRegistration,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    isProcessing: Boolean = false
) {
    // Convert EventRegistration to display with enrollment status
    val enrollmentStatus = when {
        registration.isRegistered() -> EnrollmentStatus.ACCEPTED
        registration.isCancelled() -> EnrollmentStatus.REJECTED
        else -> EnrollmentStatus.PENDING
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (registration.userProfilePic != null) {
                    AsyncImage(
                        model = registration.userProfilePic,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = registration.userName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = registration.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Registered for event",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Right side - status or actions
                if (enrollmentStatus == EnrollmentStatus.PENDING && !isProcessing) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        FilledIconButton(
                            onClick = onAccept,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = "Accept")
                        }
                        
                        FilledIconButton(
                            onClick = onReject,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Reject")
                        }
                    }
                } else if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    EnrollmentStatusBadge(status = enrollmentStatus)
                }
            }
        }
    }
}
