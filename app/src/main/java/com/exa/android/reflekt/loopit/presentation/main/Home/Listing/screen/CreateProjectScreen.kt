package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.CreateProjectViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onProjectCreated: () -> Unit
) {
    val state by viewModel.state
    val scrollState = rememberScrollState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProjectCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create New Project", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },


                actions = {
                    if (state.canSubmit) {
                        TextButton(
                            onClick = { viewModel.createProject() },
                            enabled = state.canSubmit,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Post")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.canSubmit) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.createProject() },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Create") },
                    text = { Text("Create Project") },
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Project Title Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Project Title",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = viewModel::onTitleChange,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter project title") },
                            singleLine = true,
                            isError = state.titleError != null,
                            supportingText = {
                                state.titleError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                    }
                }

                // Project Description Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = viewModel::onDescriptionChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 120.dp),
                            placeholder = { Text("Describe your project in detail") },
                            isError = state.descriptionError != null,
                            supportingText = {
                                state.descriptionError?.let {
                                    Text(it, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            shape = MaterialTheme.shapes.small
                        )
                    }
                }

                var showRolesDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Roles Needed (${state.selectedRoles.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = false,
                                onClick = { showRolesDialog = true },
                                label = { Text("Add Roles") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add roles",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.selectedRoles.isEmpty()) {
                            Text(
                                text = "No roles selected yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedRoles.forEach { role ->
                                    ElevatedAssistChip(
                                        onClick = { viewModel.onRoleRemoved(role) },
                                        label = { Text(role) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove role",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            trailingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        elevation = AssistChipDefaults.assistChipElevation(2.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                                    )
                                }
                            }
                        }
                    }
                }

                // Project Tags Section - Enhanced
                var showTagsDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tags (${state.selectedTags.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilterChip(
                                selected = false,
                                onClick = { showTagsDialog = true },
                                label = { Text("Add Tags") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add tags",
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.selectedTags.isEmpty()) {
                            Text(
                                text = "No tags selected yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.selectedTags.forEach { tag ->
                                    ElevatedAssistChip(
                                        onClick = { viewModel.onTagRemoved(tag) },
                                        label = { Text(tag) },
                                        trailingIcon = {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove tag",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                            trailingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        ),
                                        elevation = AssistChipDefaults.assistChipElevation(2.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))

                                    )
                                }
                            }
                        }
                    }
                }

                // Dialogs
                if (showRolesDialog) {
                    RoleSelectionDialog(
                        availableRoles = state.availableRoles,
                        selectedRoles = state.selectedRoles,
                        onRoleSelected = viewModel::onRoleAdded,
                        onRoleDeselected = viewModel::onRoleRemoved,
                        onNewRoleCreated = viewModel::onNewRoleCreated,
                        onDismiss = { showRolesDialog = false }
                    )
                }

                if (showTagsDialog) {
                    TagSelectionDialog(
                        availableTags = state.availableTags,
                        selectedTags = state.selectedTags,
                        onTagSelected = viewModel::onTagAdded,
                        onTagDeselected = viewModel::onTagRemoved,
                        onNewTagCreated = viewModel::onNewTagCreated,
                        onDismiss = { showTagsDialog = false }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionDialog(
    availableRoles: List<String>,
    selectedRoles: Set<String>,
    onRoleSelected: (String) -> Unit,
    onRoleDeselected: (String) -> Unit,
    onNewRoleCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newRole by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Roles") },
        text = {
            Column {
                // New role input field
                OutlinedTextField(
                    value = newRole,
                    onValueChange = { newRole = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Create new role") },
                    singleLine = true,
                    trailingIcon = {
                        if (newRole.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onNewRoleCreated(newRole)
                                    newRole = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add role")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Existing roles list
                LazyColumn {
                    items(availableRoles.size) {
                        val role =availableRoles[it]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedRoles.contains(role)) {
                                        onRoleDeselected(role)
                                    } else {
                                        onRoleSelected(role)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedRoles.contains(role),
                                onCheckedChange = null // handled by row click
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(role, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionDialog(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit,
    onTagDeselected: (String) -> Unit,
    onNewTagCreated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newTag by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Tags") },
        text = {
            Column {
                // New tag input field
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Create new tag") },
                    singleLine = true,
                    trailingIcon = {
                        if (newTag.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onNewTagCreated(newTag)
                                    newTag = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add tag")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Existing tags list
                LazyColumn {
                    items(availableTags.size) {
                        val tag =availableTags[it]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedTags.contains(tag)) {
                                        onTagDeselected(tag)
                                    } else {
                                        onTagSelected(tag)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedTags.contains(tag),
                                onCheckedChange = null // handled by row click
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tag, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}