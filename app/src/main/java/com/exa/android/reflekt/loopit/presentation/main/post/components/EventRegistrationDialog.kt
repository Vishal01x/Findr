package com.exa.android.reflekt.loopit.presentation.main.post.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.EventDetails
import com.exa.android.reflekt.loopit.presentation.main.component.textfield.AppTextField
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog for registering for an event
 * Shows event details and allows user to register with optional message
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventRegistrationDialog(
    eventDetails: EventDetails,
    eventTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (message: String) -> Unit,
    isLoading: Boolean = false
) {
    var message by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(Spacing.large)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Register for Event",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary
                    )
                    if (!isLoading) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = LiquidColors.TextSecondary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.medium))
                
                // Event Information Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        Text(
                            text = eventTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Spacing.small),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                        
                        // Date
                        if (eventDetails.eventDate != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
                                        .format(eventDetails.eventDate.toDate()),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Time
                        if (eventDetails.eventTime != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = eventDetails.eventTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        // Location
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (eventDetails.isVirtual) Icons.Rounded.Laptop else Icons.Rounded.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = eventDetails.getLocationDisplay(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        // Capacity
                        if (eventDetails.capacity != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${eventDetails.registeredCount} / ${eventDetails.capacity} registered",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Spacing.large))
                
                // Optional Message
                AppTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = "Message to organizer (Optional)",
                    placeholder = "Any questions or special requirements…",
                    leadingIcon = Icons.Default.Edit,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    enabled = !isLoading,
                )
                
                Spacer(modifier = Modifier.height(Spacing.large))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onSubmit(message) },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && eventDetails.isRegistrationOpen()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Register")
                        }
                    }
                }
                
                // Warning if event is full or deadline passed
                if (!eventDetails.isRegistrationOpen()) {
                    Spacer(modifier = Modifier.height(Spacing.small))
                    Text(
                        text = if (eventDetails.isFull()) "Event is full" else "Registration closed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
