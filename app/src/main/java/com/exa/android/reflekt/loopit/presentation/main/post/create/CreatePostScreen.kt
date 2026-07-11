package com.exa.android.reflekt.loopit.presentation.main.post.create

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.placement.components.liquidTopBarColors
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.presentation.main.post.create.components.*
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.NotificationPermissionHandler

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

    // Notification permission trigger — fires when user publishes a post
    var notifPermissionTrigger by remember { mutableStateOf(false) }
    NotificationPermissionHandler(trigger = notifPermissionTrigger) {
        notifPermissionTrigger = false
    }

    // Handle successful post creation
    LaunchedEffect(state.postCreatedSuccessfully) {
        if (state.postCreatedSuccessfully) {
            notifPermissionTrigger = true
            onPostCreated()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect {event->
            when(event){
                CreatePostUiEvent.PopBack -> onBack()
                is CreatePostUiEvent.ShowSnackbar -> {} // Handled by individual create screens or ignored here
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

    Box(modifier = Modifier.fillMaxSize().background(LiquidColors.BgBase)) {
        Scaffold(
        containerColor = Color.Transparent,
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
                },
                colors = liquidTopBarColors()
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

            // Anonymous toggle
            AnonymousToggleRow(
                isAnonymous = state.isAnonymous,
                onToggle = { viewModel.onEvent(CreatePostEvent.OnToggleAnonymous) }
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
}

@Composable
private fun AnonymousToggleRow(
    isAnonymous: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (isAnonymous)
        LiquidColors.Primary.copy(alpha = 0.5f)
    else
        LiquidColors.GlassBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isAnonymous) LiquidColors.Primary.copy(alpha = 0.08f)
                else LiquidColors.GlassSurface
            )
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Incognito avatar preview
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isAnonymous) LiquidColors.Primary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isAnonymous) Icons.Rounded.VisibilityOff else Icons.Rounded.Person,
                contentDescription = null,
                tint = if (isAnonymous) LiquidColors.Primary else LiquidColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Post Anonymously",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isAnonymous) LiquidColors.Primary else LiquidColors.TextPrimary
            )
            Text(
                text = if (isAnonymous)
                    "Your name and photo will be hidden from everyone"
                else
                    "Others will see your name and profile photo",
                style = MaterialTheme.typography.bodySmall,
                color = LiquidColors.TextSecondary
            )
        }

        Switch(
            checked = isAnonymous,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = LiquidColors.Primary
            )
        )
    }
}
