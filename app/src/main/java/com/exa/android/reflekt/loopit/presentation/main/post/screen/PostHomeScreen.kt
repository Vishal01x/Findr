package com.exa.android.reflekt.loopit.presentation.main.post.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.presentation.main.post.components.CommentBottomSheet
import com.exa.android.reflekt.loopit.presentation.main.post.components.ImageViewer
import com.exa.android.reflekt.loopit.presentation.main.post.components.PostCard
import com.exa.android.reflekt.loopit.presentation.main.post.components.TopHeaderBar
import com.exa.android.reflekt.loopit.theme.Spacing
import com.google.firebase.auth.FirebaseAuth

/**
 * Home screen with posts feed and interactions
 * Integrated with Firestore via PostHomeViewModel
 */
@Composable
fun PostHomeScreen(
    viewModel: PostHomeViewModel = hiltViewModel(),
    onPostClick: (String) -> Unit = {},
    onUserClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = modifier.fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            //Top header

            TopHeaderBar(
                searchQuery = state.searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                selectedFilter = state.selectedFilter,
                onFilterSelected = viewModel::onFilterSelected
            )

            // Posts Feed
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    state.isLoading && state.posts.isEmpty() -> {
                        // Loading State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    state.error != null -> {
                        // Error State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(Spacing.small)
                            ) {
                                Text(
                                    text = state.error ?: "An error occurred",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Button(onClick = { /* Retry */ }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }

                    state.posts.isEmpty() -> {
                        // Empty State
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No posts found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        // Posts List
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                top = Spacing.small,
                                bottom = 80.dp // Space for bottom nav if present
                            )
                        ) {
                            items(
                                items = state.posts,
                                key = { it.id }
                            ) { post ->
                                PostCard(
                                    post = post,
                                    currentUserId = currentUserId,
                                    onPostClick = { onPostClick(post.id) },
                                    onLikeClick = {
                                        viewModel.onLikeClick(
                                            post.id,
                                            post.likes.contains(currentUserId)
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
                            }
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
}
