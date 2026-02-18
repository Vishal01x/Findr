package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails

/**
 * Event Details Section Component
 * Displays and manages event-specific details
 */
@Composable
fun EventDetailsSection(
    eventDetails: EventDetails?,
    onEditDetails: (EventDetails?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Event Details",
                style = MaterialTheme.typography.titleMedium
            )
            
            FilledTonalButton(
                onClick = {onEditDetails(eventDetails)},
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    if (eventDetails == null) Icons.Default.Event else Icons.Default.Edit,
                    contentDescription = if (eventDetails == null) "Add details" else "Edit details",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (eventDetails == null) "Add Details" else "Edit Details")
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        if (eventDetails == null) {
            Text(
                text = "Add event details like date, time, and location.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    eventDetails.eventDate?.let {
                        DetailRow("Date", it.toDate().toString())
                    }
                    
                    eventDetails.eventTime?.let {
                        DetailRow("Time", it)
                    }
                    
                    if (eventDetails.isVirtual) {
                        DetailRow("Type", "Virtual Event")
                        eventDetails.meetingLink?.let {
                            DetailRow("Meeting Link", it)
                        }
                    } else {
                        DetailRow("Location", eventDetails.getLocationDisplay())
                    }
                    
                    eventDetails.capacity?.let {
                        DetailRow("Capacity", "$it attendees")
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
