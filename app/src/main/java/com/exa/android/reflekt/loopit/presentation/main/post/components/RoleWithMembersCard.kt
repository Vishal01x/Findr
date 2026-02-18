package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.Role

/**
 * Display a role card with enrolled members and locations
 */
@Composable
fun RoleWithMembersCard(
    role: Role,
    enrolledUserIds: List<String>,
    userDataMap: Map<String, UserDisplayData>, // userId -> UserDisplayData
    locations: List<String>,
    isPending: Boolean = false,
    canEnroll: Boolean = true,
    onEnrollClick: (() -> Unit)? = null,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Role Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = role.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (role.technologies.isNotEmpty()) {
                        Text(
                            text = role.technologies.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Fill Status Badge
                Badge(
                    containerColor = if (enrolledUserIds.size >= role.count)
                        MaterialTheme.colorScheme.errorContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${enrolledUserIds.size}/${role.count}",
                        color = if (enrolledUserIds.size >= role.count)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Enrolled User Avatars
            if (enrolledUserIds.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(enrolledUserIds) { userId ->
                        val userData = userDataMap[userId]
                        if (userData != null) {
                            EnrolledUserAvatar(
                                userId = userId,
                                userName = userData.name,
                                userImageUrl = userData.imageUrl,
                                onClick = { onUserClick(userId) }
                            )
                        }
                    }
                    
                    // Empty slot indicator if not full and has enroll action
                    if (enrolledUserIds.size < role.count && onEnrollClick != null) {
                        item {
                            EmptySlotIndicator(onClick = onEnrollClick)
                        }
                    }
                }
                
                // Location Chips
                if (locations.isNotEmpty()) {
                    LocationChipsRow(locations = locations.distinct())
                }
            } else if (onEnrollClick != null) {
                // Empty state - show enroll button
                Text(
                    text = "No members yet. Be the first to join!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Enroll Button (for non-authors)
            if (onEnrollClick != null && canEnroll) {
                Button(
                    onClick = onEnrollClick,
                    enabled = !isPending && enrolledUserIds.size < role.count,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isPending) Icons.Rounded.Schedule else Icons.Rounded.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isPending -> "Request Pending"
                            enrolledUserIds.size >= role.count -> "Role Full"
                            else -> "Request to Join"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Simple data class for user display
 */
data class UserDisplayData(
    val name: String,
    val imageUrl: String?,
    val course: String? = null,
    val year: String? = null
)
