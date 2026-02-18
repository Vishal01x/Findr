package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType
import com.exa.android.reflekt.loopit.presentation.main.post.components.*
import com.exa.android.reflekt.loopit.presentation.test.RolesSection

/**
 * Screen for editing existing posts
 * Reuses components from CreatePostScreen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    postId: String,
    onBackClick: () -> Unit,
    onPostUpdated: () -> Unit,
    viewModel: EditPostViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle successful update
    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onPostUpdated()
        }
    }

    // Show error messages
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.savePost() },
                        enabled = !state.isUploading && state.title.isNotBlank() && state.description.isNotBlank()
                    ) {
                        if (state.isUploading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Update")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (state.isLoading && state.originalPost == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.error != null && state.originalPost == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Error loading post",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Post Type (Read-only)
                Text(
                    text = "Post Type: ${state.type}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                // Title
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::updateTitle,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Description
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::updateDescription,
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 8
                )

                // Tags - Read-only display, edit via manual input if needed
                if (state.tags.isNotEmpty()) {
                    Text(
                        text = "Tags (${state.tags.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.tags.forEach { tag ->
                            TagChip(tag = tag)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Links - Using LinkInputSection from create components
                var linkInput by remember { mutableStateOf("") }
                LinkInputSection(
                    links = state.links,
                    currentInput = linkInput,
                    onInputChange = { linkInput = it },
                    onAddLink = {
                        viewModel.addLink(linkInput)
                        linkInput = ""
                    },
                    onRemoveLink = viewModel::removeLink
                )

                // Images (Read-only for now - show existing)
                if (state.imageUrls.isNotEmpty()) {
                    Text(
                        text = "Images (${state.imageUrls.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.imageUrls.forEach { url ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = url.takeLast(30),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { viewModel.removeImage(url) }) {
                                Text("Remove")
                            }
                        }
                    }
                }

                // Files (Read-only for now - show existing)
                if (state.fileUrls.isNotEmpty()) {
                    Text(
                        text = "Files (${state.fileUrls.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    state.fileUrls.forEach { url ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = url.takeLast(30),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(onClick = { viewModel.removeFile(url) }) {
                                Text("Remove")
                            }
                        }
                    }
                }

                // Project Roles (if PROJECT type)
//                if (state.type == PostType.PROJECT.displayName) {
//                    ProjectRoleSection(
//                        roles = state.projectRoles,
//                        onAddRole = {},  // TODO: implement role dialog
//                        onEditRole = {},
//                        onRemoveRole = {}
//                    )
//                }

                // Event Details (if EVENT type)
                if (state.type == PostType.EVENT.displayName) {
                    EventDetailsSection(
                        eventDetails = state.eventDetails,
                        onEditDetails = viewModel::updateEventDetails,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Loading overlay
        if (state.isUploading && state.originalPost != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Uploading...")
                }
            }
        }
    }
}
