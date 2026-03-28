package com.exa.android.reflekt.loopit.presentation.main.post.create

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.presentation.main.post.create.components.LinkInputSection

/**
 * Main Create Post Screen
 * Allows users to create different types of posts with rich content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    // Handle successful post creation
    LaunchedEffect(state.postCreatedSuccessfully) {
        if (state.postCreatedSuccessfully) {
            onPostCreated()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {event->
            when(event){
                CreatePostUiEvent.PopBack -> onBack()
            }

        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(CreatePostEvent.OnImagesSelected(uris))
        }
    }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            // TODO: Extract file names from URIs
            val filesWithNames = uris.map { uri ->
                uri to (uri.lastPathSegment ?: "document")
            }
            viewModel.onEvent(CreatePostEvent.OnFilesSelected(filesWithNames))
        }
    }

    // Handle back press
    BackHandler {
        viewModel.onEvent(CreatePostEvent.OnBackPressed)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onEvent(CreatePostEvent.OnBackPressed) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.onEvent(CreatePostEvent.OnCreatePost) },
                        enabled = state.canSubmit()
                    ) {
                        Text("Post")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Post Type Selector
            PostTypeSelector(
                selectedType = state.selectedPostType,
                onTypeSelected = { viewModel.onEvent(CreatePostEvent.OnPostTypeSelected(it)) }
            )

            // Title and Description
            TitleDescriptionInput(
                title = state.title,
                description = state.description,
                onTitleChange = { viewModel.onEvent(CreatePostEvent.OnTitleChanged(it)) },
                onDescriptionChange = { viewModel.onEvent(CreatePostEvent.OnDescriptionChanged(it)) }
            )

            // Media Attachments
            MediaAttachmentSection(
                selectedImages = state.selectedImages,
                onAddImages = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onRemoveImage = { viewModel.onEvent(CreatePostEvent.OnRemoveImage(it)) }
            )

            // File Attachments
            FileAttachmentSection(
                selectedFiles = state.selectedFiles,
                onAddFiles = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/msword",
                            "*/*"
                        )
                    )
                },
                onRemoveFile = { viewModel.onEvent(CreatePostEvent.OnRemoveFile(it)) }
            )

            // Link Input
            LinkInputSection(
                links = state.links,
                currentInput = state.currentLinkInput,
                onInputChange = { viewModel.onEvent(CreatePostEvent.OnLinkInputChanged(it)) },
                onAddLink = { viewModel.onEvent(CreatePostEvent.OnAddLink) },
                onRemoveLink = { viewModel.onEvent(CreatePostEvent.OnRemoveLink(it)) }
            )

            // Project Roles (only for PROJECT type)
            if (state.selectedPostType == PostType.PROJECT) {
                ProjectRolesSection(
                    roles = state.projectRoles,
                    onAddRole = { viewModel.onEvent(CreatePostEvent.OnShowAddRoleDialog) },
                    onEditRole = { viewModel.onEvent(CreatePostEvent.OnEditRole(it)) },
                    onRemoveRole = { viewModel.onEvent(CreatePostEvent.OnRemoveRole(it)) }
                )
            }

            // Event Details (only for EVENT type)
            if (state.selectedPostType == PostType.EVENT) {
                EventDetailsSection(
                    eventDetails = state.eventDetails,
                    onEditDetails = { viewModel.onEvent(CreatePostEvent.OnShowEventDetailsDialog) }
                )
            }

            // Upload Progress
        }

        // Dialogs
        if (state.showAddRoleDialog) {
            AddRoleDialog(
                role = state.editingRole,
                onDismiss = { viewModel.onEvent(CreatePostEvent.OnDismissAddRoleDialog) },
                onSave = { viewModel.onEvent(CreatePostEvent.OnSaveRole(it)) }
            )
        }

        if (state.showEventDetailsDialog) {
            EventDetailsDialog(
                eventDetails = state.eventDetails,
                onDismiss = { viewModel.onEvent(CreatePostEvent.OnDismissEventDetailsDialog) },
                onSave = { viewModel.onEvent(CreatePostEvent.OnSaveEventDetails(it)) }
            )
        }

        if (state.showDiscardDialog) {
            com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog(
                title = "Discard post?",
                message = "You have unsaved changes. Are you sure you want to discard this post?",
                discardText = "Discard",
                keepText = "Keep editing",
                onDiscard = { viewModel.onEvent(CreatePostEvent.OnConfirmDiscard) },
                onKeep = { viewModel.onEvent(CreatePostEvent.OnCancelDiscard) }
            )
        }

        // Error Snackbar
        state.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.onEvent(CreatePostEvent.OnDismissError) }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }

        // Full screen loader
        if (state.isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(.5f)).clickable(false){}, // Semi-transparent background , // Block clicks
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Uploading Post...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
