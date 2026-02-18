package com.exa.android.reflekt.loopit.presentation.main.post.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.post.components.CommentBottomSheet
import com.exa.android.reflekt.loopit.presentation.main.post.components.ImageViewer
import com.exa.android.reflekt.loopit.presentation.main.post.components.PostCard
import com.exa.android.reflekt.loopit.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPostsScreen(
    onBackClick: () -> Unit,
    onEditPost: (String) -> Unit,
    onPostClick : (String) -> Unit,
    onUserClick : (String) -> Unit,
    viewModel: PostHomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Posts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
             if (state.isLoading && state.posts.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null && state.posts.isEmpty()) {
                 Text(
                    text = state.error ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (state.posts.isEmpty()) {
                 Text(
                    text = "You haven't posted anything yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = Spacing.medium)
                ) {
                    items(state.posts) { post ->
                        if(post.createdBy == viewModel.currentUserId) {
                            Column {
                                PostCard(
                                    post = post,
                                    currentUserId = viewModel.currentUserId,
                                    onPostClick = { onPostClick(post.id) },
                                    onLikeClick = {
                                        viewModel.onLikeClick(
                                            post.id,
                                            post.likes.contains(viewModel.currentUserId)
                                        )
                                    },
                                    onCommentClick = { viewModel.onCommentClick(post.id) },
                                    onRepostClick = { viewModel.onRepostClick(post.id) },
                                    onShareClick = { viewModel.onShareClick(post.id, context) },
                                    onImageClick = { index ->
                                        viewModel.onImageClick(
                                            post.imageUrls,
                                            index,
                                            post.userInfo,
                                            post.description
                                        )
                                    },
                                    onUserClick = {
                                        onUserClick(post.createdBy)
                                    },
                                    onEnrollInRole = { roleId ->
                                        viewModel.enrollInRole(post.id, roleId)
                                    },
                                    onRegisterForEvent = {
                                        viewModel.registerForEvent(post.id)
                                    }
                                )

                                // Edit/Delete actions
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = Spacing.medium,
                                            vertical = Spacing.small
                                        ),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = { onEditPost(post.id) },
                                        modifier = Modifier.padding(end = Spacing.small)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Edit,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Edit")
                                    }

                                    Button(
                                        onClick = { viewModel.onDeleteClick(post) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Delete")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            // Comment Bottom Sheet
            if (state.showCommentSheet) {
                CommentBottomSheet(
                    onDismiss = viewModel::onDismissCommentSheet,
                    postId = state.selectedPostId ?: "",
                    onUserClick = onUserClick
                )
            }


            // Image Viewer with User Info and Caption
            if (state.showImageViewer) {
                ImageViewer(
                    images = state.selectedImages,
                    initialPage = state.selectedImageIndex,
                    user = state.selectedImageUser,
                    caption = state.selectedImageCaption,
                    onDismiss = viewModel::onDismissImageViewer
                )
            }
        }
        
        if (state.showDeleteDialog && state.postToDelete != null) {
            AlertDialog(
                onDismissRequest = viewModel::onDismissDeleteDialog,
                title = { Text("Delete Post") },
                text = { Text("Are you sure you want to delete '${state.postToDelete?.title}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = viewModel::onConfirmDelete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onDismissDeleteDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
