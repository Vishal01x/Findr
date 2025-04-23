package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.CreateProjectViewModel
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.CreateProjectState
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun CreateProjectScreen(
    viewModel: CreateProjectViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onProjectCreated: () -> Unit
) {
    val state by viewModel.state
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    var showFab by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        var lastOffset = 0
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                showFab = offset <= lastOffset || offset <= 10
                lastOffset = offset
            }
    }
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProjectCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(
                        "Create Project",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 10.dp),
                actions = {
                    if (state.canSubmit) {
                        FilledTonalButton(
                            onClick = { viewModel.createProject() },
                            enabled = state.canSubmit && !state.isLoading,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Save,
                                    contentDescription = "Create",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text("Post")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.canSubmit) {
                AnimatedVisibility(
                    visible = showFab,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.createProject() },
                        icon = {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    Icons.Filled.CreateNewFolder,
                                    contentDescription = "Create",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        text = {
                            Text(
                                "Create Project",
                                style = MaterialTheme.typography.titleSmall
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 12.dp
                        ),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(paddingValues),
            state = listState
        ) {
            item {
                when {
                    state.isLoading -> FullScreenLoading()
                    state.error != null -> ErrorState(error = state.error, onRetry = {})
                    else -> CreateProjectContent(
                        state = state,
                        viewModel = viewModel,
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateProjectContent(
    state: CreateProjectState,
    viewModel: CreateProjectViewModel,
    context: Context
) {
    var showRolesDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        ProjectTitleSection(state, viewModel)
        ProjectDescriptionSection(state, viewModel)
        RolesSection(state, viewModel, showRolesDialog) { showRolesDialog = it }
        TagsSection(state, viewModel, showTagsDialog) { showTagsDialog = it }
    }

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

// Reusable Components (Same as EditScreen)

@Composable
private fun ProjectTitleSection(state: CreateProjectState, viewModel: CreateProjectViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Project Title",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Filled.Create, null, tint = MaterialTheme.colorScheme.outline)
                },
                placeholder = { Text("Amazing Project...", style = MaterialTheme.typography.bodyLarge) },
                isError = state.titleError != null,
                supportingText = {
                    state.titleError?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.large,
                textStyle = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ProjectDescriptionSection(state: CreateProjectState, viewModel: CreateProjectViewModel) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Description",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = state.description,
                onValueChange = viewModel::onDescriptionChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                leadingIcon = {
                    Icon(Icons.Filled.Notes, null, tint = MaterialTheme.colorScheme.outline)
                },
                placeholder = { Text("Describe your project vision...") },
                isError = state.descriptionError != null,
                supportingText = {
                    state.descriptionError?.let {
                        Text(it, style = MaterialTheme.typography.labelSmall)
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                ),
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RolesSection(
    state: CreateProjectState,
    viewModel: CreateProjectViewModel,
    showDialog: Boolean,
    onDialogChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge) // 👈 clip content to match card shape
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = Icons.Outlined.Engineering,
                title = "Required Roles",
                count = state.selectedRoles.size,
                buttonText = "Add Roles",
                buttonColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onDialogChange(true) }
            )

            Spacer(Modifier.height(16.dp))

            if (state.selectedRoles.isEmpty()) {
                EmptyState(message = "No roles selected yet", icon = Icons.Outlined.PersonAdd)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.selectedRoles.forEach { role ->
                        ElevatedAssistChip(
                            onClick = { viewModel.onRoleRemoved(role) },
                            label = { Text(role, style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Work,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            ),
                            elevation = AssistChipDefaults.assistChipElevation(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    state: CreateProjectState,
    viewModel: CreateProjectViewModel,
    showDialog: Boolean,
    onDialogChange: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(20.dp)
        ) {
            SectionHeader(
                icon = Icons.Outlined.Tag,
                title = "Project Tags",
                count = state.selectedTags.size,
                buttonText = "Add Tags",
                buttonColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onDialogChange(true) }
            )

            Spacer(Modifier.height(16.dp))

            if (state.selectedTags.isEmpty()) {
                EmptyState(message = "No tags selected yet", icon = Icons.Outlined.LocalOffer)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.selectedTags.forEach { tag ->
                        ElevatedAssistChip(
                            onClick = { viewModel.onTagRemoved(tag) },
                            label = {
                                Text(tag, style = MaterialTheme.typography.labelLarge)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Label,
                                    null,
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                labelColor = MaterialTheme.colorScheme.onPrimary,
                                leadingIconContentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                            ),
                            elevation = AssistChipDefaults.assistChipElevation(4.dp)
                        )
                    }
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