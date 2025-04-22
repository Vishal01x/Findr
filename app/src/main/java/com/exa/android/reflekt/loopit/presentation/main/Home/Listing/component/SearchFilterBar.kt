package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component

import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exa.android.reflekt.loopit.util.model.Project
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import com.google.firebase.Timestamp
import io.getstream.meeting.room.compose.ui.AppTheme

/*
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRoles: Set<String>,
    availableRoles: List<String>,
    onRoleSelected: (String) -> Unit,
    onRoleDeselected: (String) -> Unit,
    selectedTags: Set<String>,
    availableTags: List<String>,
    onTagSelected: (String) -> Unit,
    onTagDeselected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRolesFilter by remember { mutableStateOf(false) }
    var showTagsFilter by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search projects...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = showRolesFilter || selectedRoles.isNotEmpty(),
                onClick = { showRolesFilter = !showRolesFilter },
                label = { Text("Roles") },
                leadingIcon = if (selectedRoles.isNotEmpty()) {
                    {
                        BadgedBox(
                            badge = { Badge { Text(selectedRoles.size.toString()) } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                } else null
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilterChip(
                selected = showTagsFilter || selectedTags.isNotEmpty(),
                onClick = { showTagsFilter = !showTagsFilter },
                label = { Text("Tags") },
                leadingIcon = if (selectedTags.isNotEmpty()) {
                    {
                        BadgedBox(
                            badge = { Badge { Text(selectedTags.size.toString()) } }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    }
                } else null
            )
        }

        // Dropdown filters
        AnimatedVisibility(visible = showRolesFilter) {
            Column {
                Text("Select Roles", style = MaterialTheme.typography.labelMedium)
                FlowRow {
                    availableRoles.forEach { role ->
                        FilterChip(
                            selected = selectedRoles.contains(role),
                            onClick = {
                                if (selectedRoles.contains(role)) {
                                    onRoleDeselected(role)
                                } else {
                                    onRoleSelected(role)
                                }
                            },
                            label = { Text(role) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }

        AnimatedVisibility(visible = showTagsFilter) {
            Column {
                Text("Select Tags", style = MaterialTheme.typography.labelMedium)
                FlowRow {
                    availableTags.forEach { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag),
                            onClick = {
                                if (selectedTags.contains(tag)) {
                                    onTagDeselected(tag)
                                } else {
                                    onTagSelected(tag)
                                }
                            },
                            label = { Text(tag) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
        }
    }
}

 */

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRoles: Set<String>,
    availableRoles: List<String>,
    onRoleSelected: (String) -> Unit,
    onRoleDeselected: (String) -> Unit,
    selectedTags: Set<String>,
    availableTags: List<String>,
    onTagSelected: (String) -> Unit,
    onTagDeselected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRolesFilter by remember { mutableStateOf(false) }
    var showTagsFilter by remember { mutableStateOf(false) }
    val animationSpec = remember { tween<Float>(durationMillis = 300) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .graphicsLayer {
                clip = false
            }
    ) {
        // Enhanced Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,  // Increased elevation
                    shape = MaterialTheme.shapes.large,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large
                )
                .padding(1.dp),
            placeholder = { Text("Search projects...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(35.dp)
                )
            },
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Chips Row
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Roles Filter Chip
            FilterChip(
                selected = showRolesFilter || selectedRoles.isNotEmpty(),
                onClick = { showRolesFilter = !showRolesFilter },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledBorderColor = Color.Transparent,
                    selected = showRolesFilter || selectedRoles.isNotEmpty(),
                    enabled = true
                ),
                label = {
                    Text(
                        "Roles",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedRoles.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    if (selectedRoles.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ) { Text(selectedRoles.size.toString()) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = if(showRolesFilter) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // Tags Filter Chip
            FilterChip(
                selected = showTagsFilter || selectedTags.isNotEmpty(),
                onClick = { showTagsFilter = !showTagsFilter },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledBorderColor = Color.Transparent,
                    selected = showTagsFilter || selectedTags.isNotEmpty(),
                    enabled = true
                ),
                label = {
                    Text(
                        "Tags",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selectedTags.isNotEmpty()) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    if (selectedTags.isNotEmpty()) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ) { Text(selectedTags.size.toString()) }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = if(showTagsFilter) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }

        // Animated Filter Sections
        AnimatedVisibility(
            visible = showRolesFilter,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Select Roles",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedRoles.isNotEmpty()) {
                            TextButton(onClick = { selectedRoles.forEach(onRoleDeselected) }) {
                                Text("Clear All", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableRoles.forEach { role ->
                            SuggestionChip(
                                onClick = {
                                    if (selectedRoles.contains(role)) {
                                        onRoleDeselected(role)
                                    } else {
                                        onRoleSelected(role)
                                    }
                                },
                                label = { Text(role) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedRoles.contains(role)) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    labelColor = if (selectedRoles.contains(role)) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    disabledBorderColor = Color.Transparent,
                                    selected = showTagsFilter || selectedTags.isNotEmpty(),
                                    enabled = true
                                ),
                                icon = {
                                    Icon(
                                        imageVector = if (selectedRoles.contains(role)) Icons.Filled.Check else Icons.Outlined.WorkOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (selectedRoles.contains(role)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showTagsFilter,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Select Tags",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedTags.isNotEmpty()) {
                            TextButton(onClick = { selectedTags.forEach(onTagDeselected) }) {
                                Text("Clear All", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {
                                    if (selectedTags.contains(tag)) {
                                        onTagDeselected(tag)
                                    } else {
                                        onTagSelected(tag)
                                    }
                                },
                                label = { Text(tag) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedTags.contains(tag)) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    labelColor = if (selectedTags.contains(tag)) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    disabledBorderColor = Color.Transparent,
                                    selected = showTagsFilter || selectedTags.isNotEmpty(),
                                    enabled = true
                                ),
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTags.contains(tag)) Icons.Filled.Check else Icons.Filled.Label,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectCardd(
    project: Project,
    onClick: () -> Unit,
    isEditable: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onEnroll: (() -> Unit)? = null,
    withdraw: (() -> Unit)? = null,
    onAccept: (String, String) -> Unit,
    onReject: (String) -> Unit,
    onViewOnMap: (List<String>) -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedRoles by remember { mutableStateOf(false) }
    var expandedTags by remember { mutableStateOf(false) }
    var expandedRequests by remember { mutableStateOf(false) }
    var expandedEnrolled by remember { mutableStateOf(false) }

    var isEnrolled by remember(project) {
        mutableStateOf(currentUserId?.let { project.requestedPersons.containsKey(it) } ?: false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val cardElevation by animateDpAsState(
        targetValue = if (interactionSource.collectIsPressedAsState().value) 8.dp else 2.dp,
        animationSpec = tween(durationMillis = 150)
    )
    val context = LocalContext.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        interactionSource = interactionSource
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                //Edit project
                if (isEditable) {
                    Row {
                        IconButton(
                            onClick = { onEdit?.invoke() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Project",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Project",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (expandedRoles || expandedTags || expandedRequests) 10 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Roles Section
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expandedRoles = !expandedRoles }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "Roles",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Roles Needed (${project.rolesNeeded.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (expandedRoles) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expandedRoles) "Collapse roles" else "Expand roles",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = expandedRoles,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        project.rolesNeeded.forEach { role ->
                            ElevatedAssistChip(
                                onClick = {},
                                label = { Text(role) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                elevation = AssistChipDefaults.assistChipElevation(4.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Section
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { expandedTags = !expandedTags }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Tags",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tags (${project.tags.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = if (expandedTags) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expandedTags) "Collapse tags" else "Expand tags",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(
                    visible = expandedTags,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        project.tags.forEach { tag ->
                            ElevatedAssistChip(
                                onClick = {},
                                label = { Text(tag) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                ),
                                elevation = AssistChipDefaults.assistChipElevation(4.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                }
            }

            // Requested Members Section
            if (isEditable) {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedRequests = !expandedRequests }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Enrolled",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Join Requests (${project.requestedPersons.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expandedRequests) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expandedRequests) "Collapse members" else "Expand members",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // requested Persons
                    AnimatedVisibility(
                        visible = expandedRequests,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            project.requestedPersons.forEach { (userId, userName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = if (userId == project.createdBy)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = userName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row {
                                        IconButton(
                                            onClick = { onAccept(userId, userName) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Accept",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = { onReject(userId) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Reject",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                            FilledTonalButton(
                                onClick = {
                                    if (project.requestedPersons.isNotEmpty()) {
                                        onViewOnMap(project.requestedPersons.keys.toList())
                                    }else{
                                        Toast.makeText(context, "No requested members", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = project.requestedPersons.isNotEmpty(),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (project.requestedPersons.isNotEmpty())
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Icon(
                                    Icons.Default.Map,
                                    contentDescription = "View on map",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Request on Map")
                            }
                        }
                    }
                }
            }

            if (isEditable ) {
                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { expandedEnrolled = !expandedEnrolled }
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "Team Members",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Team Members (${project.enrolledPersons.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = if (expandedEnrolled) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expandedEnrolled) "Collapse members" else "Expand members",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AnimatedVisibility(
                        visible = expandedEnrolled,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            project.enrolledPersons.forEach { (userId, userName) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                color = if (userId == project.createdBy)
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = userName.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = userName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        if (userId == project.createdBy) {
                                            Text(
                                                text = "Creator",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Author and Date
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Posted by ${project.createdByName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    project.createdAt?.toDate()?.let { date ->
                        Text(
                            text = date.formatAsTimeAgo(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Enroll/Withdraw Button
                if (!isEditable && currentUserId != null && currentUserId !=project.createdBy) {
                    FilledTonalButton(
                        onClick = {
                            if (isEnrolled) {
                                withdraw?.invoke()
                                // isEnrolled = false
                            } else {
                                onEnroll?.invoke()
                                // isEnrolled = true
                            }
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isEnrolled)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                MaterialTheme.colorScheme.primaryContainer,
                            contentColor = if (isEnrolled)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isEnrolled)
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isEnrolled) Icons.Default.ExitToApp else Icons.Default.PersonAdd,
                            contentDescription = if (isEnrolled) "Withdraw" else "Enroll",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEnrolled) "Withdraw" else "Enroll")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableSection(
    icon: ImageVector,
    title: String,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        animationSpec = tween(300)
    )

    Column {
        Row(
            modifier = Modifier
                .clickable { onExpandChange(!expanded) }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Filled.ExpandLess,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotation),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            content()
        }
    }
}
@Composable
private fun RoleChip(role: String) {
    AssistChip(
        onClick = {},
        label = { Text(role) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = MaterialTheme.shapes.medium,
        leadingIcon = {
            Icon(
                Icons.Outlined.Work,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    )
}
@Composable
private fun TagChip(tag: String) {
    SuggestionChip(
        onClick = {},
        label = { Text(tag) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            labelColor = MaterialTheme.colorScheme.onSecondary
        ),
        shape = MaterialTheme.shapes.medium,
        icon = {
            Icon(
                Icons.Filled.Label,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    )
}

@Composable
private fun UserAvatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    ),
                    center = Offset.Unspecified,
                    radius = 100f
                ),
                shape = CircleShape
            )
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    isEditable: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onEnroll: (() -> Unit)? = null,
    withdraw: (() -> Unit)? = null,
    onAccept: (String, String) -> Unit,
    onReject: (String) -> Unit,
    onViewOnMap: (List<String>) -> Unit,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedRoles by remember { mutableStateOf(false) }
    var expandedTags by remember { mutableStateOf(false) }
    var expandedRequests by remember { mutableStateOf(false) }
    var expandedEnrolled by remember { mutableStateOf(false) }

    var isEnrolled by remember(project) {
        mutableStateOf(currentUserId?.let { project.requestedPersons.containsKey(it) } ?: false)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val cardElevation by animateDpAsState(
        targetValue = if (interactionSource.collectIsPressedAsState().value) 8.dp else 6.dp,
        animationSpec = tween(durationMillis = 150), label = ""
    )
    val context = LocalContext.current

    val shape = MaterialTheme.shapes.extraLarge
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


        Card(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,  // Increased elevation
                pressedElevation = 10.dp  // Increased from 8.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.onPrimary
            ),
            interactionSource = interactionSource
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = (-0.02).em
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    //Edit project
                    if (isEditable) {
                        Row {
                            IconButton(
                                onClick = { onEdit?.invoke() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Project",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            IconButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Project",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                var showFullDescription by remember { mutableStateOf(false) }
                Column {
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    var maxLines by remember { mutableStateOf(3) }

                    Text(
                        text = project.description,
                        style = typography.bodyLarge,
                        color = colors.onSurface,
                        maxLines = maxLines,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { textLayoutResult = it }
                    )

                    if ((textLayoutResult?.lineCount ?: 0) > 3 && !showFullDescription) {
                        TextButton(
                            onClick = { showFullDescription = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Read more", style = typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Roles Section
                ExpandableSection(
                    icon = Icons.Filled.Group,
                    title = "Roles Needed (${project.rolesNeeded.size})",
                    expanded = expandedRoles,
                    onExpandChange = { expandedRoles = it },
                    content = {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            project.rolesNeeded.forEach { role ->
                                RoleChip(role = role)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Section
                ExpandableSection(
                    icon = Icons.Filled.Tag,
                    title = "Tags (${project.tags.size})",
                    expanded = expandedTags,
                    onExpandChange = { expandedTags = it },
                    content = {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            project.tags.forEach { tag ->
                                TagChip(tag = tag)
                            }
                        }
                    }
                )

                // Requested Members Section
                if (isEditable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expandedRequests = !expandedRequests }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAddAlt1,
                                contentDescription = "Enrolled",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Join Requests (${project.requestedPersons.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expandedRequests) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expandedRequests) "Collapse members" else "Expand members",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // requested Persons
                        AnimatedVisibility(
                            visible = expandedRequests,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                project.requestedPersons.forEach { (userId, userName) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp).background(
                                                color = MaterialTheme.colorScheme.primary.copy(0.2f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = userName.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = userName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Row {
                                            IconButton(
                                                onClick = { onAccept(userId, userName) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Accept",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            IconButton(
                                                onClick = { onReject(userId) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Reject",
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        if (project.requestedPersons.isNotEmpty()) {
                                            onViewOnMap(project.requestedPersons.keys.toList())
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "No requested members",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    enabled = project.requestedPersons.isNotEmpty(),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (project.requestedPersons.isNotEmpty())
                                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = "View on map",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("View Request on Map")
                                }
                            }
                        }
                    }
                }

                if (isEditable) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { expandedEnrolled = !expandedEnrolled }
                                .padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = "Team Members",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Team Members (${project.enrolledPersons.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (expandedEnrolled) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (expandedEnrolled) "Collapse members" else "Expand members",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(
                            visible = expandedEnrolled,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                project.enrolledPersons.forEach { (userId, userName) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp).background(
                                                color = MaterialTheme.colorScheme.primary.copy(0.2f),
                                                shape = RoundedCornerShape(16.dp)
                                            )
                                            .clip(RoundedCornerShape(16.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = userName.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = userName,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            if (userId == project.createdBy) {
                                                Text(
                                                    text = "Creator",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Footer Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Author Info
                    UserAvatar(
                        name = project.createdByName,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.createdByName,
                            style = typography.labelLarge,
                            color = colors.onSurface
                        )
                        project.createdAt?.toDate()?.let { date ->
                            Text(
                                text = date.formatAsTimeAgo(),
                                style = typography.labelMedium,
                                color = colors.outline
                            )
                        }
                    }

                    // Enroll/Withdraw Button
                    if (!isEditable && currentUserId != null && currentUserId != project.createdBy) {
                        FilledTonalButton(
                            onClick = {
                                if (isEnrolled) {
                                    withdraw?.invoke()
                                } else {
                                    onEnroll?.invoke()
                                }
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isEnrolled)
                                    MaterialTheme.colorScheme.errorContainer
                                else
                                    MaterialTheme.colorScheme.primaryContainer,
                                contentColor = if (isEnrolled)
                                    MaterialTheme.colorScheme.onErrorContainer
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isEnrolled)
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                else
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Icon(
                                imageVector = if (isEnrolled) Icons.Filled.ExitToApp
                                else Icons.Filled.PersonAdd,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEnrolled) "Withdraw" else "Enroll")
                        }
                    }
                }
            }
        }

}

/*
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit,
    isEditable: Boolean = false,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onEnroll: (() -> Unit)? = null,
    withdraw: (() -> Unit)? = null,
    currentUserId: String?,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedRoles by remember { mutableStateOf(false) }
    var expandedTags by remember { mutableStateOf(false) }
    var expandedEnrolled by remember { mutableStateOf(false) }

    val isEnrolled = currentUserId?.let { project.enrolledPersons.containsKey(it) } ?: false

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Project") },
            text = { Text("Are you sure you want to delete this project?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (isEditable) {
                    Row {
                        IconButton(
                            onClick = { onEdit?.invoke() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Project",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Project",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(12.dp))

            // Roles chips section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expandedRoles = !expandedRoles }
            ) {
                Text(
                    text = "Roles Needed:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (expandedRoles) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expandedRoles) "Collapse roles" else "Expand roles",
                    modifier = Modifier.size(16.dp)
                )
            }

            if (expandedRoles) {
                FlowRow {
                    project.rolesNeeded.forEach { role ->
                        AssistChip(
                            onClick = {},
                            label = { Text(role) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            } else {
                Text(
                    text = "${project.rolesNeeded.size} roles",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags chips section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { expandedTags = !expandedTags }
            ) {
                Text(
                    text = "Tags:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(
                    imageVector = if (expandedTags) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expandedTags) "Collapse tags" else "Expand tags",
                    modifier = Modifier.size(16.dp))
            }

            if (expandedTags) {
                FlowRow {
                    project.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            } else {
                Text(
                    text = "${project.tags.size} tags",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }

            // Enrolled persons section (only visible in My Projects)
            if (isEditable && project.enrolledPersons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { expandedEnrolled = !expandedEnrolled }
                ) {
                    Text(
                        text = "Enrolled Persons:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Icon(
                        imageVector = if (expandedEnrolled) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expandedEnrolled) "Collapse enrolled" else "Expand enrolled",
                        modifier = Modifier.size(16.dp))
                }

                if (expandedEnrolled) {
                    Column {
                        project.enrolledPersons.forEach { (userId, userName) ->
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${project.enrolledPersons.size} enrolled",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Posted by ${project.createdByName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)

                Spacer(modifier = Modifier.weight(1f))

                // Enroll/Withdraw button (only visible if not editable and user is logged in)
                if (!isEditable && currentUserId != null) {
                    Button(
                        onClick = {
                            if (isEnrolled) {
                                withdraw?.invoke()
                            } else {
                                onEnroll?.invoke()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isEnrolled) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text=if (isEnrolled) "Withdraw" else "Enroll",
                            color = if (isEnrolled) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = project.createdAt?.toDate()?.formatAsTimeAgo() ?: "",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

 */

private fun Date.formatAsTimeAgo(): String {
    val now = System.currentTimeMillis()
    val diff = now - this.time

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = Devices.PIXEL_4)
@Composable
fun ProjectCardPreview() {
    val sampleProject = Project(
        id = "1",
        title = "Sample Project Title",
        description = "This is a sample project description that should be long enough to demonstrate the read more functionality in the preview.",
        rolesNeeded = listOf("Designer", "Developer", "Project Manager"),
        tags = listOf("Android", "Kotlin", "Compose"),
        requestedPersons = mapOf("user1" to "John Doe", "user2" to "Jane Smith"),
        enrolledPersons = mapOf("creator1" to "Alice Wonderland"),
        createdBy = "creator1",
        createdByName = "Alice Wonderland",
        createdAt = Timestamp.now()
    )

    MaterialTheme {
        ProjectCard(
            project = sampleProject,
            onClick = {},
            isEditable = true,
            onDelete = {},
            onEdit = {},
            onEnroll = {},
            withdraw = {},
            onAccept = { _, _ -> },
            onReject = { _ -> },
            onViewOnMap = { _ -> },
            currentUserId = "creator1",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, device = Devices.PIXEL_4, name = "Non-Editable Preview")
@Composable
fun ProjectCardNonEditablePreview() {
    val sampleProject = Project(
        id = "2",
        title = "Another Project",
        description = "Shorter description for non-editable preview",
        rolesNeeded = listOf("QA Engineer"),
        tags = listOf("Testing", "Automation"),
        requestedPersons = emptyMap(),
        enrolledPersons = mapOf("creator2" to "Bob Builder"),
        createdBy = "creator2",
        createdByName = "Bob Builder",
        createdAt = Timestamp.now()
    )

    MaterialTheme {
        ProjectCard(
            project = sampleProject,
            onClick = {},
            isEditable = false,
            onEnroll = {},
            withdraw = {},
            onAccept = { _, _ -> },
            onReject = { _ -> },
            onViewOnMap = { _ -> },
            currentUserId = "user3",
            modifier = Modifier.padding(16.dp)
        )
    }
}