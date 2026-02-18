package com.exa.android.reflekt.loopit.presentation.main.post.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive event details card with all information
 */
@Composable
fun EventDetailsCard(
    eventDetails: EventDetails,
    isRegistered: Boolean = false,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.medium, vertical = Spacing.small),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = "Event",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Event Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            
            // Date & Time
            eventDetails.eventDate?.let { date ->
                EventDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = formatDate(date.toDate())
                )
            }
            
            eventDetails.eventTime?.let { time ->
                EventDetailRow(
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    value = time
                )
            }
            
            // Location
            if (eventDetails.isVirtual) {
                EventDetailRow(
                    icon = Icons.Default.Videocam,
                    label = "Format",
                    value = "Virtual Event"
                )
                
                eventDetails.meetingLink?.let { link ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                                context.startActivity(intent)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.small),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Meeting Link",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Join Meeting",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Open",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            } else {
                eventDetails.venue?.let { venue ->
                    EventDetailRow(
                        icon = Icons.Default.Place,
                        label = "Venue",
                        value = venue
                    )
                }
                
                eventDetails.location?.let { location ->
                    EventDetailRow(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = location
                    )
                }
            }
            
            // Capacity
            eventDetails.capacity?.let { capacity ->
                val remaining = eventDetails.remainingCapacity() ?: 0
                EventDetailRow(
                    icon = Icons.Default.People,
                    label = "Capacity",
                    value = "${eventDetails.registeredCount}/$capacity registered"
                )
                
                // Progress bar
                LinearProgressIndicator(
                    progress = { (eventDetails.registeredCount.toFloat() / capacity).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (eventDetails.isFull()) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                
                if (remaining > 0) {
                    Text(
                        text = "$remaining spots remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = "Event is full",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Registration Deadline
            eventDetails.registrationDeadline?.let { deadline ->
                EventDetailRow(
                    icon = Icons.Default.Schedule,
                    label = "Registration Deadline",
                    value = formatDate(deadline.toDate())
                )
            }
            
            // Register Button
            val buttonText = when {
                isRegistered -> "Already Registered"
                eventDetails.isFull() -> "Event Full"
                !eventDetails.isRegistrationOpen() -> "Registration Closed"
                else -> "Register for Event"
            }
            
            val buttonEnabled = !isRegistered && eventDetails.isRegistrationOpen() && !eventDetails.isFull()
            
            val buttonIcon = if (isRegistered) Icons.Default.CheckCircle else Icons.Default.PersonAdd
            
            Button(
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = "Register",
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

@Composable
private fun EventDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return formatter.format(date)
}
