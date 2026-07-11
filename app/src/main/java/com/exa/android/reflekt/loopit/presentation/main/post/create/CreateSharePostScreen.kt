package com.exa.android.reflekt.loopit.presentation.main.post.create

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import coil.compose.AsyncImage
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.DiscardDialog
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSharePostScreen(
    postId: String? = null,
    onBack: () -> Unit,
    onPostCreated: () -> Unit,
    onNavigateToEditor: (String) -> Unit = {},
    editorResult: String? = null,
    onEditorResultConsumed: () -> Unit = {},
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showMediaSheet by remember { mutableStateOf(false) }
    var showLinkInput  by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(CreatePostEvent.OnPostTypeSelected(PostType.POST))
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

    val imageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.onEvent(CreatePostEvent.OnImagesSelected(uris))
    }

    val videoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.onEvent(CreatePostEvent.OnImagesSelected(listOf(it))) }
    }

    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onEvent(
                CreatePostEvent.OnFilesSelected(
                    uris.map { it to (it.lastPathSegment ?: "file") }
                )
            )
        }
    }

    BackHandler { viewModel.onEvent(CreatePostEvent.OnBackPressed) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var showValidationErrors by remember { mutableStateOf(false) }

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
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onEvent(CreatePostEvent.OnBackPressed) }) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(LiquidColors.GlassSurface)
                            .border(1.dp, LiquidColors.GlassBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Close, null, tint = LiquidColors.TextPrimary, modifier = Modifier.size(18.dp))
                    }
                }
                Text(
                    if (state.isEditMode) "Share a Post (Edit)" else "Share a Post",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = LiquidColors.TextPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        if (state.title.isNotBlank()) {
                            viewModel.onEvent(CreatePostEvent.OnCreatePost)
                        } else {
                            showValidationErrors = true
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    },
                    enabled = !state.isUploading,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    if (state.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(if (state.isEditMode) "Update" else "Post", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            HorizontalDivider(color = LiquidColors.GlassBorder, thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ── Post as (identity picker) ───────────────────────────
                IdentityPicker(
                    isAnonymous = state.isAnonymous,
                    onSelect = { anon ->
                        if (anon != state.isAnonymous)
                            viewModel.onEvent(CreatePostEvent.OnToggleAnonymous)
                    }
                )

                // ── Title ────────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Title",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { 
                            viewModel.onEvent(CreatePostEvent.OnTitleChanged(it))
                            if (showValidationErrors) showValidationErrors = false
                        },
                        placeholder = { Text("What's this about?", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Outlined.EditNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showValidationErrors && state.title.isBlank(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp)
                    )
                }

                // ── Description (tap → markdown editor) ────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Description",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    DescriptionTapField(
                        text = state.description,
                        placeholder = "What's on your mind? Tap to write…",
                        onTap = { onNavigateToEditor(state.description) }
                    )
                }

                if (state.selectedImages.isNotEmpty()) {
                    SharedMediaPreviewRow(
                        images = state.selectedImages,
                        onRemove = { viewModel.onEvent(CreatePostEvent.OnRemoveImage(it)) }
                    )
                }

                // ── File list ──────────────────────────────────────────────────
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

                // ── Link input (shown when user picks "Add Link") ────────────
                AnimatedVisibility(
                    visible = showLinkInput,
                    enter = slideInVertically { -20 } + fadeIn(),
                    exit = slideOutVertically { -20 } + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Link, null,
                                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = state.currentLinkInput,
                                onValueChange = { viewModel.onEvent(CreatePostEvent.OnLinkInputChanged(it)) },
                                placeholder = { Text("Paste URL here…", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Uri,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    viewModel.onEvent(CreatePostEvent.OnAddLink)
                                    if (state.links.isEmpty()) showLinkInput = false
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
                }

                // ── Link preview card ──────────────────────────────────────
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

                // Show added links as chips (no preview / after preview dismissed)
                if (state.links.isNotEmpty() && state.linkPreviewTitle == null && !state.isLoadingLinkPreview) {
                    state.links.forEach { link ->
                        SharedLinkChip(
                            url = link,
                            onRemove = { viewModel.onEvent(CreatePostEvent.OnRemoveLink(link)) }
                        )
                    }
                }

                // ── Add to post section ─────────────────────────────────────
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

                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Media picker bottom sheet ──────────────────────────────────────────
    if (showMediaSheet) {
        SharedMediaPickerSheet(
            onDismiss = { showMediaSheet = false },
            onPickImages = {
                showMediaSheet = false
                imageLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onPickVideo = {
                showMediaSheet = false
                videoLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
            },
            onPickFiles = {
                showMediaSheet = false
                fileLauncher.launch(arrayOf("*/*"))
            }
        )
    }

    if (state.showDiscardDialog) {
        DiscardDialog(
            title = "Discard post?",
            message = "Are you sure you want to discard this post?",
            onConfirm = { viewModel.onEvent(CreatePostEvent.OnConfirmDiscard) },
            onDismiss = { viewModel.onEvent(CreatePostEvent.OnCancelDiscard) }
        )
    }
    } // end Scaffold
}



// ── Identity picker (two tiles) ────────────────────────────────────────────

@Composable
internal fun IdentityPicker(isAnonymous: Boolean, onSelect: (Boolean) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "POST AS",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            IdentityTile(
                icon = Icons.Outlined.Person,
                title = "Yourself",
                subtitle = "Your real profile",
                selected = !isAnonymous,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(false) }
            )
            IdentityTile(
                icon = Icons.Outlined.PersonOff,
                title = "Anonymous",
                subtitle = "Hidden identity",
                selected = isAnonymous,
                modifier = Modifier.weight(1f),
                onClick = { onSelect(true) }
            )
        }
    }
}

@Composable
private fun IdentityTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (selected) LiquidColors.Primary else LiquidColors.GlassBorder
    val bgColor     = if (selected) LiquidColors.Primary.copy(alpha = 0.10f) else LiquidColors.GlassSurface
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            icon, null,
            tint = if (selected) LiquidColors.Primary else LiquidColors.TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(
                title,
                color = if (selected) LiquidColors.TextPrimary else LiquidColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                subtitle,
                color = if (selected) LiquidColors.TextSecondary else LiquidColors.TextMuted,
                fontSize = 11.sp
            )
        }
    }
}


