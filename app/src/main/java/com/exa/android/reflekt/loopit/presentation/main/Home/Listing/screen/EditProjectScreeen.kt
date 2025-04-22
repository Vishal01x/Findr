package com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Engineering
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProjectState
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.EditProjectViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.RequestedMember


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun EditProjectScreen(
    projectId: String,
    viewModel: EditProjectViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onProjectUpdated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
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

    LaunchedEffect(projectId) { viewModel.loadProject(projectId) }
    LaunchedEffect(state.isSuccess) { if (state.isSuccess) onProjectUpdated() }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {

                    Text(
                        "Edit Project",
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                modifier = Modifier.shadow(elevation = 10.dp),
                actions = {
                    if (state.canSubmit) {
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            FilledTonalButton(
                                onClick = { viewModel.updateProject() },
                                enabled = state.canSubmit && !state.isLoading,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
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
                                        contentDescription = "Save",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text("Save")
                            }
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
                        onClick = { viewModel.updateProject() },
                        icon = {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 3.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            } else {
                                Icon(
                                    Icons.Filled.SaveAs,
                                    contentDescription = "Save",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        },
                        text = {
                            Text(
                                "Save Changes",
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
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
                    state.isLoading && !state.isInitialLoadComplete -> FullScreenLoading()
                    state.error != null -> ErrorState(
                        error = state.error,
                        onRetry = { viewModel.loadProject(projectId) })

                    else -> EditProjectContent(
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
private fun EditProjectContent(
    state: EditProjectState,
    viewModel: EditProjectViewModel,
    context: Context
) {
    var showRolesDialog by remember { mutableStateOf(false) }
    var showTagsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
        if (state.requestedMembers.isNotEmpty()) EnrolledMembersSection(state)
        DangerZoneSection { showDeleteConfirmation = true }
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

    if (showDeleteConfirmation) {
        DeleteConfirmationDialog(
            onConfirm = {
                viewModel.deleteProject()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun ProjectTitleSection(state: EditProjectState, viewModel: EditProjectViewModel) {
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
private fun ProjectDescriptionSection(state: EditProjectState, viewModel: EditProjectViewModel) {
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
                    modifier = Modifier.size(24.dp))
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
    state: EditProjectState,
    viewModel: EditProjectViewModel,
    showDialog: Boolean,
    onDialogChange: (Boolean) -> Unit
) {
    Box{
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(), // Let card stretch to parent
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp), // 👈 Ensure proper elevation
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
                    buttonText = "Manage Roles",
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
                                label = {
                                    Text(role, style = MaterialTheme.typography.labelLarge)
                                },
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
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(
    state: EditProjectState,
    viewModel: EditProjectViewModel,
    showDialog: Boolean,
    onDialogChange: (Boolean) -> Unit
) {
    Box{
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(), // Let card stretch to parent
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp), // 👈 Ensure proper elevation
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
                    icon = Icons.Outlined.Tag,
                    title = "Project Tags",
                    count = state.selectedTags.size,
                    buttonText = "Manage Tags",
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
                                label = { Text(tag, style = MaterialTheme.typography.labelLarge) },
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
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String,
    count: Int,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$count selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        FilterChip(
            selected = false,
            onClick = onClick,
            modifier = Modifier
                .height(36.dp)
                .padding(horizontal = 2.dp),
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = buttonColor,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                borderWidth = 1.dp,
                enabled = true,
                selected = false
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}



@Composable
private fun EnrolledMembersSection(state: EditProjectState) {
    ElevatedCard(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Enrolled Members",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "${state.requestedMembers.size} participants",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.requestedMembers.size) {
                    val member = state.requestedMembers[it]
                    MemberListItem(member = member)
                }
            }
        }
    }
}

@Composable
private fun MemberListItem(member:  RequestedMember) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceBright),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Text(
                    text = member.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DangerZoneSection(onDeleteClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(16.dp))
                Text(
                    "Danger Zone",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "This action cannot be undone. All project data and associated information will be permanently deleted.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = onDeleteClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("Delete Project Permanently")
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Filled.Warning,
                null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp))
        },
        title = {
            Text(
                "Confirm Deletion",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    "Are you absolutely sure you want to delete this project? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "All of the following will be permanently removed:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(4.dp))
                Text("• Project details and documentation")
                Text("• Team member associations")
                Text("• All related tasks and updates")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete Forever", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontWeight = FontWeight.Medium)
            }
        },
        containerColor = Color.White.copy(alpha = 0.9f)
    )
}

@Composable
fun FullScreenLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun EmptyState(message: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}



@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class)
@Composable
fun EditProjectScreenn(
    projectId: String,
    viewModel: EditProjectViewModel = hiltViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onProjectUpdated: () -> Unit
) {
    // Collect state from ViewModel
    val state by viewModel.state.collectAsState()
    val scrollState = rememberScrollState()

    // Load project data when screen is first displayed
    LaunchedEffect(projectId) {
        viewModel.loadProject(projectId)
    }

    // Handle successful update
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onProjectUpdated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Project",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                            onClick = { viewModel.updateProject() },
                            enabled = state.canSubmit && !state.isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (state.canSubmit) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.updateProject() },
                    icon = {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    },
                    text = { Text("Save Changes") },
                    shape = MaterialTheme.shapes.medium,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    expanded = !state.isLoading
                )
            }
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                state.isLoading && !state.isInitialLoadComplete -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    ErrorState(
                        error = state.error,
                        onRetry = { viewModel.loadProject(projectId) }
                    )
                }
                else -> {
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

                        // Roles Needed Section
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
                                        label = { Text("Edit Roles") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit roles",
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
                                        text = "No roles selected",
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
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        var showTagsDialog by remember { mutableStateOf(false) }

                        // Tags Section
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
                                        label = { Text("Edit Tags") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit tags",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.selectedTags.isEmpty()) {
                                    Text(
                                        text = "No tags selected",
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
                                                border = BorderStroke(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Enrolled Members Section (Read-only)
                        if (state.requestedMembers.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.medium,
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Enrolled Members (${state.requestedMembers.size})",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyColumn(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 200.dp)
                                    ) {
                                        items(state.requestedMembers.size) {
                                            val member=state.requestedMembers[it]
                                            ListItem(
                                                headlineContent = { Text(member.name) },
                                                leadingContent = {
                                                    Icon(
                                                        Icons.Default.Person,
                                                        contentDescription = "Member",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Danger Zone Section
                        var showDeleteConfirmation by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Danger Zone",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { showDeleteConfirmation = true },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Text("Delete Project")
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

                        if (showDeleteConfirmation) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirmation = false },
                                title = { Text("Delete Project?") },
                                text = { Text("Are you sure you want to delete this project? This action cannot be undone.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteProject()
                                            showDeleteConfirmation = false
                                        },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Text("Delete")
                                    }
                                },
                                dismissButton = {
                                    TextButton(
                                        onClick = { showDeleteConfirmation = false }
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ErrorState(
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error ?: "An unknown error occurred",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}