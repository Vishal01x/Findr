package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent
import com.exa.android.reflekt.loopit.util.model.Comment
import com.exa.android.reflekt.loopit.util.model.PostType
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.Timestamp

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
    selectedPostType: PostType?,
    onTypeSelected: (PostType?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRolesFilter by remember { mutableStateOf(false) }
    var showTagsFilter by remember { mutableStateOf(false) }
    val animationSpec = remember { tween<Float>(durationMillis = 300) }
    var rolesSearchQuery by remember { mutableStateOf("") }
    var tagsSearchQuery by remember { mutableStateOf("") }

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

        PostTypeFilter(
            selectedType = selectedPostType,
            onTypeSelected = onTypeSelected
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
                onClick = {
                    showRolesFilter = !showRolesFilter
                    if (showRolesFilter) showTagsFilter = false
                },
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
                onClick = {
                    showTagsFilter = !showTagsFilter
                    if (showTagsFilter) showRolesFilter = false
                },
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
            /*
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

             */
            FilterSectionCard(
                title = "Select Roles",
                searchQuery = rolesSearchQuery,
                onSearchChange = { rolesSearchQuery = it },
                items = availableRoles.filter { it.contains(rolesSearchQuery, true) },
                selectedItems = selectedRoles,
                onItemSelected = onRoleSelected,
                onItemDeselected = onRoleDeselected,
                clearAll = { selectedRoles.forEach(onRoleDeselected) }
            )
        }

        AnimatedVisibility(
            visible = showTagsFilter,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            /*
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

             */

            FilterSectionCard(
                title = "Select Tags",
                searchQuery = tagsSearchQuery,
                onSearchChange = { tagsSearchQuery = it },
                items = availableTags.filter { it.contains(tagsSearchQuery, true) },
                selectedItems = selectedTags,
                onItemSelected = onTagSelected,
                onItemDeselected = onTagDeselected,
                clearAll = { selectedTags.forEach(onTagDeselected) }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSectionCard(
    title: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    items: List<String>,
    selectedItems: Set<String>,
    onItemSelected: (String) -> Unit,
    onItemDeselected: (String) -> Unit,
    clearAll: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .heightIn(max = 300.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (selectedItems.isNotEmpty()) {
                    TextButton(onClick = clearAll) {
                        Text("Clear All", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search within filter section
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search $title...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(24.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { item ->
                    SuggestionChip(
                        onClick = {
                            if (selectedItems.contains(item)) onItemDeselected(item)
                            else onItemSelected(item)
                        },
                        label = { Text(item) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (selectedItems.contains(item)) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            labelColor = if (selectedItems.contains(item)) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledBorderColor = Color.Transparent,
                            selected = selectedItems.isNotEmpty(),
                            enabled = true
                        ),
                        icon = {
                            Icon(
                                imageVector =  when {
                                    selectedItems.contains(item) -> Icons.Filled.Check
                                    title == "Select Tags" -> Icons.Filled.Label
                                    else -> Icons.Outlined.WorkOutline
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selectedItems.contains(item)) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    )
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
    onAuthorProfileClick: (String) -> Unit,
    onToggleLike: (String) -> Unit, // Added like handler
    onCommentEvent: (ProjectListEvent) -> Unit,
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

    val postType = remember(project.type) {
        PostType.entries.find { it.displayName == project.type } ?: PostType.OTHER
    }

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
            if(project.description.isNotEmpty()) {
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
            }
            if (project.imageUrls.isNotEmpty()) {
                ImageCarousel(project.imageUrls)
            }
            if(project.rolesNeeded.isNotEmpty()) {
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
            }
            if(project.tags.isNotEmpty()) {
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
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Requested Members Section
            if (isEditable && project.requestedPersons.isNotEmpty()) {
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

            if (isEditable && project.enrolledPersons.isNotEmpty()) {
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

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
                    .clickable {
                        onAuthorProfileClick(project.createdBy)
                    }
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
                if (!isEditable  && postType != PostType.POST && postType != PostType.OTHER) {
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
                }else{
                    LikeButton(
                        isLiked = project.likes.contains(currentUserId),
                        onLike = { onToggleLike(project.id) },
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }
        }
    }

}

@Composable
private fun CommentSection(
    project: Project,
    currentUserId: String?,
    onCommentEvent: (ProjectListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        // Existing Comments
        project.comments
            .filter { it.senderId == currentUserId }
            .forEach { comment ->
                // can use UserCommentPreview
                UserCommentItem(
                    comment = comment,
                    onEdit = { newText ->
                        onCommentEvent(
                            ProjectListEvent.UpdateComment(
                                project.id,
                                comment.id,
                                newText
                            )
                        )
                    },
                    onDelete = {
                        onCommentEvent(
                            ProjectListEvent.DeleteComment(
                                project.id,
                                comment.id
                            )
                        )
                    }
                )
            }

        // New Comment Input
        CommentInputField(
            commentText = commentText,
            onCommentChange = { commentText = it },
            onSubmit = {
                if (commentText.isNotBlank()) {
                    onCommentEvent(
                        ProjectListEvent.AddComment(
                            project.id,
                            commentText
                        )
                    )
                    commentText = ""
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentInputField(
    commentText: String,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = commentText,
            onValueChange = onCommentChange,
            label = { Text("Add a comment...") },
            singleLine = false,
            maxLines = 3,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSubmit() })
        )

        IconButton(
            onClick = onSubmit,
            enabled = commentText.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Post comment",
                tint = if (commentText.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UserCommentItem(
    comment: Comment,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(comment.text) }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comment.text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = comment.timestamp.toDate().formatAsTimeAgo(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

            }

            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, "Edit comment")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "Delete comment")
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Comment") },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    label = { Text("Edit comment") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEdit(editedText)
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PostTypeFilter(
    selectedType: PostType?,
    onTypeSelected: (PostType?) -> Unit
) {
    val allTypes = remember { listOf(null) + PostType.entries } // Fixed list creation

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allTypes.forEach { type ->
            val isSelected = type == selectedType
            FilterChip(
                selected = isSelected,
                onClick = { onTypeSelected(if (isSelected) null else type) },
                label = {
                    Text(
                        type?.displayName ?: "All Posts",
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (type != null) {
                    {
                        Icon(
                            type.icon,
                            null,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outline,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    disabledBorderColor = Color.Transparent,
                    enabled = true,
                    selected = isSelected
                )
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ImageCarousel(images: List<String>) {
    val pagerState = rememberPagerState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        HorizontalPager(
            count = images.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) { page ->
            AsyncImage(
                model = images[page],
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceDim),
                error = ColorPainter(MaterialTheme.colorScheme.errorContainer)
            )
        }


        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            activeColor = MaterialTheme.colorScheme.onPrimaryContainer,
            inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            indicatorWidth = 12.dp,
            indicatorHeight = 4.dp,
            spacing = 4.dp
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LikeButton(
    isLiked: Boolean,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconColor by animateColorAsState(
        targetValue = if (isLiked) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "iconColor"
    )

    val textColor by animateColorAsState(
        targetValue = if (isLiked) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 300),
        label = "textColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isLiked) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Box(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = false,
                    color = MaterialTheme.colorScheme.primary
                )
            ) { onLike() }
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated icon
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like",
                    modifier = Modifier
                        .size(24.dp)
                        .scale(scale),
                    tint = iconColor
                )
            }

            // Animated text
            AnimatedContent(
                targetState = isLiked,
                transitionSpec = {
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                }
            ) { liked ->
                Text(
                    text = if (liked) "Liked" else "Like",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

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
            modifier = Modifier.padding(16.dp),
            onAuthorProfileClick = {},
            onToggleLike = {},
            onCommentEvent = {}
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
            modifier = Modifier.padding(16.dp),
            onAuthorProfileClick = {},
            onToggleLike = {},
            onCommentEvent = {}
        )
    }
}