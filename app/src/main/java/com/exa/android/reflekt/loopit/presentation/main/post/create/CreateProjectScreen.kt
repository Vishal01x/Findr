package com.exa.android.reflekt.loopit.presentation.main.post.create

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.presentation.main.placement.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectScreen(
    postId: String? = null,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToEditor: (String) -> Unit = {},
    editorResult: String? = null,
    onEditorResultConsumed: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(CreatePostEvent.OnPostTypeSelected(PostType.PROJECT))
        if (postId != null) viewModel.onEvent(CreatePostEvent.OnLoadPost(postId))
    }

    LaunchedEffect(state.postCreatedSuccessfully) {
        if (state.postCreatedSuccessfully) onPostCreated()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                CreatePostUiEvent.PopBack -> onBack()
                is CreatePostUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    // Consume description returned from MarkdownEditorScreen
    LaunchedEffect(editorResult) {
        editorResult?.let {
            viewModel.onEvent(CreatePostEvent.OnDescriptionChanged(it))
            onEditorResultConsumed()
        }
    }

    var showMediaSheet by remember { mutableStateOf(false) }
    var showLinkInput  by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.onEvent(CreatePostEvent.OnImagesSelected(uris))
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onEvent(CreatePostEvent.OnImagesSelected(listOf(it))) }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(
                CreatePostEvent.OnFilesSelected(
                    uris.map { it to (it.lastPathSegment ?: "document") }
                )
            )
        }
    }

    BackHandler { viewModel.onEvent(CreatePostEvent.OnBackPressed) }

    androidx.compose.material3.Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = LiquidColors.BgBase,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
    Box(modifier = Modifier.padding(innerPadding).fillMaxSize().background(LiquidColors.BgBase)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ── Top bar ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LiquidColors.GlassSurface)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { viewModel.onEvent(CreatePostEvent.OnBackPressed) }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
                }
                Text(
                    text = if (state.isEditMode) "Edit Project" else "Create Project",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.width(70.dp)) // balance
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // ── Scrollable form ────────────────────────────────────────────
            val scrollState = rememberScrollState()
            val coroutineScope = rememberCoroutineScope()
            var showValidationErrors by remember { mutableStateOf(false) }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Hero
                Column {
                    Text(
                        "Let's build something.",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Share your idea and find the perfect teammates.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Project Title
                ProjectFormField(label = "Project Title") {
                    ProjectInputField(
                        value = state.title,
                        onValueChange = { 
                            viewModel.onEvent(CreatePostEvent.OnTitleChanged(it))
                            if (showValidationErrors) showValidationErrors = false
                        },
                        placeholder = "e.g. AI Study Buddy App",
                        leadingIcon = Icons.Outlined.RocketLaunch,
                        isError = showValidationErrors && state.title.isBlank(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                // Description
                ProjectFormField(label = "Description") {
                    DescriptionTapField(
                        text = state.description,
                        placeholder = "What is the goal? Who is it for? Tap to write…",
                        onTap = { onNavigateToEditor(state.description) }
                    )
                }

                // Roles section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.GroupAdd, null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                "Looking For",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                        ProjectRolesSection(
                            roles = state.projectRoles,
                            onAddRole = { viewModel.onEvent(CreatePostEvent.OnShowAddRoleDialog) },
                            onEditRole = { viewModel.onEvent(CreatePostEvent.OnEditRole(it)) },
                            onRemoveRole = { viewModel.onEvent(CreatePostEvent.OnRemoveRole(it)) }
                        )
                    }
                }

                // ── Media previews (images/video) ─────────────────────────────────
                if (state.selectedImages.isNotEmpty()) {
                    SharedMediaPreviewRow(
                        images = state.selectedImages,
                        onRemove = { viewModel.onEvent(CreatePostEvent.OnRemoveImage(it)) }
                    )
                }

                // ── File chips ────────────────────────────────────────────────
                if (state.selectedFiles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.selectedFiles.forEach { file ->
                            SharedFileChip(
                                name = file.fileName,
                                onRemove = { viewModel.onEvent(CreatePostEvent.OnRemoveFile(file.id)) }
                            )
                        }
                    }
                }

                // ── Link input ──────────────────────────────────────────────────
                AnimatedVisibility(
                    visible = showLinkInput,
                    enter = slideInVertically { -20 } + fadeIn(),
                    exit = slideOutVertically { -20 } + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LiquidColors.GlassSurface)
                            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Link, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = state.currentLinkInput,
                            onValueChange = { viewModel.onEvent(CreatePostEvent.OnLinkInputChanged(it)) },
                            placeholder = { Text("Paste URL here…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = projectFieldColors(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.onEvent(CreatePostEvent.OnAddLink)
                                showLinkInput = false
                            })
                        )
                        if (state.currentLinkInput.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.onEvent(CreatePostEvent.OnAddLink)
                                showLinkInput = false
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, null,
                                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // ── Link preview card ────────────────────────────────────────────
                AnimatedVisibility(
                    visible = state.links.isNotEmpty() &&
                            (state.linkPreviewTitle != null || state.isLoadingLinkPreview),
                    enter = slideInVertically { 20 } + fadeIn(),
                    exit = fadeOut()
                ) {
                    SharedLinkPreviewCard(
                        isLoading = state.isLoadingLinkPreview,
                        title = state.linkPreviewTitle,
                        description = state.linkPreviewDescription,
                        imageUrl = state.linkPreviewImageUrl,
                        domain = state.linkPreviewDomain,
                        url = state.links.firstOrNull() ?: "",
                        onDismiss = { viewModel.onEvent(CreatePostEvent.OnRemoveLink(state.links.first())) }
                    )
                }

                if (state.links.isNotEmpty() && state.linkPreviewTitle == null && !state.isLoadingLinkPreview) {
                    state.links.forEach { link ->
                        SharedLinkChip(
                            url = link,
                            onRemove = { viewModel.onEvent(CreatePostEvent.OnRemoveLink(link)) }
                        )
                    }
                }

                // ── ADD TO YOUR POST ─────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "ADD TO YOUR POST",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SharedAddMediaButton(
                            icon = Icons.Outlined.Image,
                            label = "Photo / Video",
                            subLabel = "Upload media",
                            modifier = Modifier.weight(1f),
                            onClick = { showMediaSheet = true }
                        )
                        SharedAddMediaButton(
                            icon = Icons.Outlined.Link,
                            label = "Add Link",
                            subLabel = "Attach URL",
                            modifier = Modifier.weight(1f),
                            onClick = { showLinkInput = !showLinkInput }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // ── Footer actions ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LiquidColors.GlassSurface)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { 
                        if (state.canSubmit()) {
                            viewModel.onEvent(CreatePostEvent.OnCreatePost) 
                        } else {
                            showValidationErrors = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                    enabled = !state.isUploading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(if (state.isEditMode) "Update Project" else "Post Project", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward, null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
            }
        }
    }

    // ── Media picker bottom sheet ──────────────────────────────────────────────
    if (showMediaSheet) {
        SharedMediaPickerSheet(
            onDismiss = { showMediaSheet = false },
            onPickImages = {
                showMediaSheet = false
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickVideo = {
                showMediaSheet = false
                videoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onPickFiles = {
                showMediaSheet = false
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        )
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (state.showAddRoleDialog) {
        AddRoleDialog(
            role = state.editingRole,
            onDismiss = { viewModel.onEvent(CreatePostEvent.OnDismissAddRoleDialog) },
            onSave = { viewModel.onEvent(CreatePostEvent.OnSaveRole(it)) }
        )
    }

    if (state.showDiscardDialog) {
        DiscardDialog(
            title = "Discard project?",
            message = "You have unsaved changes. Are you sure you want to discard this project?",
            onConfirm = { viewModel.onEvent(CreatePostEvent.OnConfirmDiscard) },
            onDismiss = { viewModel.onEvent(CreatePostEvent.OnCancelDiscard) }
        )
    }

    state.error?.let { error ->
        LaunchedEffect(error) {
            // Error is shown, dismiss after a moment
        }
    }
    } // end Scaffold
}

// ── Shared helpers ─────────────────────────────────────────────────────────

@Composable
private fun ProjectFormField(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        content()
    }
}

@Composable
private fun ProjectInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
        leadingIcon = { Icon(leadingIcon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        isError = isError,
        colors = projectFieldColors(),
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )
}

@Composable
private fun projectFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
