package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
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
    
    val eventDate = remember { eventDetails?.eventDate ?: Timestamp.now() }
    
    val isValid = if (isVirtual) meetingLink.isNotBlank() else location.isNotBlank()
    
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
                AppTextField(
                    value = eventTime,
                    onValueChange = { eventTime = it },
                    label = "Event Time",
                    placeholder = "e.g., 1 Jan, 2:00 PM",
                    leadingIcon = Icons.Default.Event,
                )
                
                if (isVirtual) {
                    AppTextField(
                        value = meetingLink,
                        onValueChange = { meetingLink = it },
                        label = "Meeting Link *",
                        placeholder = "https://meet.google.com/...",
                        leadingIcon = Icons.Default.Link,
                        isError = meetingLink.isBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                } else {
                    AppTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = "Location *",
                        placeholder = "City, State",
                        leadingIcon = Icons.Default.LocationOn,
                        isError = location.isBlank(),
                    )
                    
                    AppTextField(
                        value = venue,
                        onValueChange = { venue = it },
                        label = "Venue",
                        placeholder = "Venue name",
                        leadingIcon = Icons.Default.Place,
                    )
                }
                
                // Capacity
                AppTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = "Capacity (Optional)",
                    placeholder = "Max attendees",
                    leadingIcon = Icons.Default.People,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
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
