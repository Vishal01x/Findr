package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.google.firebase.Timestamp

/**
 * Event Details Dialog Component
 * Dialog for adding/editing event details
 */
@Composable
fun EventDetailsDialog(
    eventDetails: EventDetails?,
    onDismiss: () -> Unit,
    onSave: (EventDetails) -> Unit
) {
    var eventTime by remember { mutableStateOf(eventDetails?.eventTime ?: "") }
    var location by remember { mutableStateOf(eventDetails?.location ?: "") }
    var venue by remember { mutableStateOf(eventDetails?.venue ?: "") }
    var capacity by remember { mutableStateOf(eventDetails?.capacity?.toString() ?: "") }
    var isVirtual by remember { mutableStateOf(eventDetails?.isVirtual ?: false) }
    var meetingLink by remember { mutableStateOf(eventDetails?.meetingLink ?: "") }
    
    // For simplicity, using current timestamp as placeholder for date
    // In a real app, you'd use a DatePicker
    val eventDate = remember { eventDetails?.eventDate ?: Timestamp.now() }
    
    val isValid = if (isVirtual) {
        meetingLink.isNotBlank()
    } else {
        location.isNotBlank()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Event Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Virtual event toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Virtual Event")
                    Switch(
                        checked = isVirtual,
                        onCheckedChange = { isVirtual = it }
                    )
                }
                
                // Event time
                OutlinedTextField(
                    value = eventTime,
                    onValueChange = { eventTime = it },
                    label = { Text("Event Time") },
                    placeholder = { Text("e.g., 1Jan, 2:00 PM") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                if (isVirtual) {
                    // Meeting link for virtual events
                    OutlinedTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = { Text("Meeting Link *") },
                        placeholder = { Text("https://meet.google.com/...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // Location for physical events
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location *") },
                        placeholder = { Text("City, State") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = { Text("Venue") },
                        placeholder = { Text("Venue name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                
                // Capacity
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity (Optional)") },
                    placeholder = { Text("Max attendees") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                
//                Text(
//                    text = "Note: Date picker will be added in future update. Using current date for now.",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val details = EventDetails(
                        eventDate = eventDate,
                        eventTime = eventTime.trim().ifBlank { null },
                        location = location.trim().ifBlank { null },
                        venue = venue.trim().ifBlank { null },
                        capacity = capacity.toIntOrNull(),
                        isVirtual = isVirtual,
                        meetingLink = meetingLink.trim().ifBlank { null }
                    )
                    onSave(details)
                },
                enabled = isValid
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
