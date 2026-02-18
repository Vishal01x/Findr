package com.exa.android.reflekt.loopit.presentation.main.Map.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel

/**
 * Reusable component to display active filters as dismissible chips
 * Used in both MapScreen2_0 and UserListScreen
 */
@Composable
fun FilterChipsComponent(
    viewModel: LocationViewModel,
    modifier: Modifier = Modifier,
    showLocationChip: Boolean = true,
    onClearLocation: (() -> Unit)? = null
) {
    val selectedRoles = viewModel.selectedRoles.value
    val radius = viewModel.radius.value
    val hasLocation = onClearLocation != null && showLocationChip
    
    if (selectedRoles.isNotEmpty() || radius != 5f || hasLocation) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Location chip (if applicable)
            if (hasLocation) {
                item {
                    SuggestionChip(
                        onClick = { onClearLocation?.invoke() },
                        label = { Text("Custom Location") },
                        icon = {
                            Icon(
                                Icons.Default.MyLocation,
                                contentDescription = "Clear location",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            }
            
            // Radius chip (if not default 5km)
            if (radius != 5f) {
                item {
                    SuggestionChip(
                        onClick = { viewModel.radius.value = 5f },
                        label = { Text("${radius.toInt()} km") },
                        icon = {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = "Reset radius",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            iconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                }
            }
            
            // Role chips
            items(selectedRoles.toList()) { role ->
                SuggestionChip(
                    onClick = { viewModel.removeRole(role) },
                    label = { Text(role) },
                    icon = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove $role",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}
