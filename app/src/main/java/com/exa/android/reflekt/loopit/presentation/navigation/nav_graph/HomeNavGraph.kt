package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph

import android.util.Log
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.DetailChat
import com.exa.android.reflekt.loopit.presentation.main.chat.HomeScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.MapScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ChatInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.MeetingRoute
import com.exa.android.reflekt.loopit.presentation.main.Map.RequestedPersonMapScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.MapSearchScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.MapScreen2_0
import com.exa.android.reflekt.loopit.presentation.main.Map.MapSearchScreen2_0
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.ChatMediaScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreatePostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.PostTypeSelectionScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreateProjectScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreateEventScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreatePostViewModel
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreateSharePostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreatePollScreen
import androidx.compose.runtime.remember
import com.exa.android.reflekt.loopit.presentation.main.post.create.MarkdownEditorScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.collectAsState
import com.exa.android.reflekt.loopit.presentation.main.post.screen.AuthorManagementScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.MyPostsScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostHomeScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.main.post.screen.UserSearchScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.ShortlistedUsersScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType


fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    navigation(startDestination = HomeRoute.ChatList.route, route = "home") {
        composable(HomeRoute.ChatList.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            val chatList = listOf("Vishal", "Kanhaiya", "Joe Tam", "Holder", "Smith Darklew")
            HomeScreen(navController,viewModel)
        }
        composable(HomeRoute.ZoomImage.route) { backStackEntry ->
            /*
            val imageId = backStackEntry.arguments?.getString("imageId")
            val resourceId = imageId?.toIntOrNull() ?: R.drawable.ic_launcher_background
            ZoomPhoto(imageId = resourceId) {
                navController.popBackStack()
            }

             */
        }

        composable(
            HomeRoute.ChatDetail.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("eventTitle") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(navDeepLink { uriPattern = "findr://chat/{userId}" })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val eventTitle = backStackEntry.arguments?.getString("eventTitle")?.takeIf { it.isNotBlank() }
            DetailChat(navController, userId, eventTitle) { users ->
                navController.navigate(MeetingRoute.LobbyScreen.createRoute(users))
            }
        }

        chatInfoNavGraph(navController)
    }
}

fun NavGraphBuilder.chatInfoNavGraph(navController: NavHostController) {
    navigation(startDestination = ChatInfo.ChatMedia.route, route = "chat") {
        composable(ChatInfo.ChatMedia.route) {
            ChatMediaScreen()
        }
    }
}

fun NavGraphBuilder.mapNavGraph(navController: NavHostController, locationViewModel: LocationViewModel) {
    navigation(
        startDestination = MapInfo.MapScreen2.route, // Reverted to 2.0 version
        route = "map_graph"
    ) {
        // New MapScreen 2.0 (default)
        composable(MapInfo.MapScreen2.route) {
            MapScreen2_0(
                navController = navController,
                viewModel = locationViewModel
            )
        }
        
        // New MapSearchScreen 2.0
        composable(MapInfo.MapSearchScreen2.route) {
            MapSearchScreen2_0(
                navController = navController,
                viewModel = locationViewModel
            )
        }
        
        // User List Screen
        // Legacy MapScreen (for backward compatibility)
        composable(MapInfo.MapScreen.route) {
            MapScreen(
                navController,
                viewModel = locationViewModel
                /*                "fjidjf",
//                onMediaClick = { navController.navigate(ChatInfo.ChatMedia.route) },
//                onCallClick = { navController.navigate(Call.VoiceCall.route) },
//                onMediaVisibilityClick = { navController.navigate(ChatInfo.MediaVisibility.route) },
//                onBlockClick = { navController.navigate(ChatInfo.BlockUser.route) }
//                openChat = { userId ->
//                    navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
//                }*/
            )
        }

        // Legacy search screen (for backward compatibility)
        composable("searchScreen") {
            MapSearchScreen(
                viewModel = locationViewModel,
                onApplyFilters = {
                    navController.popBackStack()
                }
            )
        }
        
        // New search screen 2.0 - also accessible via old route for backward compatibility
        composable("searchScreen2_0") {
            MapSearchScreen2_0(
                navController = navController,
                viewModel = locationViewModel
            )
        }
        
        // User list screen - also accessible via simple route

//        // New Bottom Sheet Map Screen (Experimental)
//        composable(MapInfo.MapScreenBottomSheet.route) {
//            MapScreenBottomSheet(
//                navController = navController,
//                viewModel = locationViewModel
//            )
//        }
        
        // User list screen - also accessible via simple route
    }
}


fun NavGraphBuilder.postNavGraph(navController: NavHostController) {
    navigation(
        startDestination = PostRoute.PostList.route,
        route = "post_graph"
    ) {
        composable(PostRoute.PostList.route) {
            // Use the new PostHomeScreen with Hilt-injected ViewModel
            PostHomeScreen(
                onPostClick = { postId ->
                    navController.navigate(PostRoute.PostDetail.createRoute(postId))
                },
                onUserClick = { userId ->
                    // Navigate to user profile when implemented
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                },
                onNotificationClick = {
                    navController.navigate(MainRoute.Notification.route)
                }
            )
        }

        // Post Detail Screen
        composable(
            route = PostRoute.PostDetail.route,
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType },
                navArgument("scrollTo") { type = NavType.StringType; defaultValue = "" }
            ),
            deepLinks = listOf(
                // FDL link= parameter domain
                navDeepLink { uriPattern = "https://reflekt.com/project/{projectId}" },
                navDeepLink { uriPattern = "https://reflekt.com/project/{projectId}?scrollTo={scrollTo}" },
                // FCM custom scheme
                navDeepLink { uriPattern = "findr://project/{projectId}" },
                // Firebase Hosting fallback
                navDeepLink { uriPattern = "https://social-media-ab0cc.web.app/post/{projectId}" },
                navDeepLink { uriPattern = "https://social-media-ab0cc.web.app/post/{projectId}?scrollTo={scrollTo}" },
                navDeepLink { uriPattern = "https://findr.app/post/{projectId}" },
                navDeepLink { uriPattern = "https://findr.app/post/{projectId}?scrollTo={scrollTo}" }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("projectId") ?: ""
            val scrollTo = backStackEntry.arguments?.getString("scrollTo") ?: ""
            PostDetailScreen(
                postId = postId,
                scrollTo = scrollTo,
                onBackClick = { navController.popBackStack() },
                onEditClick = { id, postType ->
                    val route = when {
                        postType == PostType.PROJECT.displayName -> PostRoute.CreateProject.createRoute(id)
                        postType == PostType.EVENT.displayName -> PostRoute.CreateEvent.createRoute(id)
                        postType == PostType.POLL.displayName -> PostRoute.CreatePoll.createRoute(id)
                        else -> PostRoute.CreateSharePost.createRoute(id)
                    }
                    navController.navigate(route)
                },
                onManageRequestsClick = { id -> navController.navigate(PostRoute.AuthorManagement.createRoute(id)) },
                onUserClick = { userId -> navController.navigate(ProfileRoute.UserProfile.createRoute(userId)) },
                onChatClick = { userId, eventTitle ->
                    navController.navigate(HomeRoute.ChatDetail.createRoute(userId, eventTitle))
                },
                onInviteClick = { id, title ->
                    navController.navigate(PostRoute.UserSearch.createRoute(id, title))
                },
                onViewAllCollaborators = { id, title ->
                    navController.navigate(PostRoute.ShortlistedUsers.createRoute(id, "project", title))
                },
                onViewAllParticipants = { id, title ->
                    navController.navigate(PostRoute.ShortlistedUsers.createRoute(id, "event", title))
                }
            )
        }

        // User Search / Invite Screen
        composable(
            route = PostRoute.UserSearch.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            UserSearchScreen(
                onBackClick = { navController.popBackStack() },
                onUserSelected = { userId ->
                    navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
                },
                onProfileClick = { userId ->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                }
            )
        }

        composable(
            route = PostRoute.ShortlistedUsers.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "project" },
                navArgument("title") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            ShortlistedUsersScreen(
                onBackClick = { navController.popBackStack() },
                onUserClick = { userId -> if (userId.isNotBlank()) navController.navigate(ProfileRoute.UserProfile.createRoute(userId)) }
            )
        }

        // Legacy Create Post Screen (kept for backward compat)
        composable(PostRoute.CreatePost.route) {
            CreatePostScreen(
                onBack = {
                    navController.popBackStack()
                },
                onPostCreated = {
                    navController.popBackStack()
                }
            )
        }

        // ── New Create Post Selection ─────────────────────────────────────
        composable(PostRoute.CreatePostSelection.route) {
            PostTypeSelectionScreen(
                onStartProject = {
                    navController.navigate(PostRoute.CreateProject.createRoute()) {
                        launchSingleTop = true
                    }
                },
                onOrganizeEvent = {
                    navController.navigate(PostRoute.CreateEvent.createRoute()) {
                        launchSingleTop = true
                    }
                },
                onSharePost = {
                    navController.navigate(PostRoute.CreateSharePost.createRoute()) {
                        launchSingleTop = true
                    }
                },
                onCreatePoll = {
                    navController.navigate(PostRoute.CreatePoll.createRoute()) {
                        launchSingleTop = true
                    }
                },
                onDismiss = {
                    navController.popBackStack()
                }
            )
        }

        // ── Create Project ────────────────────────────────────────────────
        composable(
            route = PostRoute.CreateProject.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("post_graph")
            }
            val viewModel: CreatePostViewModel = hiltViewModel(parentEntry)
            val editorResult = backStackEntry.savedStateHandle
                .getStateFlow<String?>("md_result", null)
                .collectAsState().value
            CreateProjectScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.navigate(PostRoute.PostList.route) {
                        popUpTo("post_graph") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditor = { text ->
                    backStackEntry.savedStateHandle["md_input"] = text
                    navController.navigate(PostRoute.MarkdownEditor.route)
                },
                editorResult = editorResult,
                onEditorResultConsumed = {
                    backStackEntry.savedStateHandle.remove<String>("md_result")
                },
                viewModel = viewModel
            )
        }

        // ── Create Event ──────────────────────────────────────────────────
        composable(
            route = PostRoute.CreateEvent.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("post_graph")
            }
            val viewModel: CreatePostViewModel = hiltViewModel(parentEntry)
            val editorResult = backStackEntry.savedStateHandle
                .getStateFlow<String?>("md_result", null)
                .collectAsState().value
            CreateEventScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.navigate(PostRoute.PostList.route) {
                        popUpTo("post_graph") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditor = { text ->
                    backStackEntry.savedStateHandle["md_input"] = text
                    navController.navigate(PostRoute.MarkdownEditor.route)
                },
                editorResult = editorResult,
                onEditorResultConsumed = {
                    backStackEntry.savedStateHandle.remove<String>("md_result")
                },
                viewModel = viewModel
            )
        }

        // ── Create Share Post ─────────────────────────────────────────────
        composable(
            route = PostRoute.CreateSharePost.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("post_graph")
            }
            val viewModel: CreatePostViewModel = hiltViewModel(parentEntry)
            val editorResult = backStackEntry.savedStateHandle
                .getStateFlow<String?>("md_result", null)
                .collectAsState().value
            CreateSharePostScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.navigate(PostRoute.PostList.route) {
                        popUpTo("post_graph") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditor = { text ->
                    backStackEntry.savedStateHandle["md_input"] = text
                    navController.navigate(PostRoute.MarkdownEditor.route)
                },
                editorResult = editorResult,
                onEditorResultConsumed = {
                    backStackEntry.savedStateHandle.remove<String>("md_result")
                },
                viewModel = viewModel
            )
        }

        // ── Create Poll ───────────────────────────────────────────────────
        composable(
            route = PostRoute.CreatePoll.route,
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry("post_graph")
            }
            val viewModel: CreatePostViewModel = hiltViewModel(parentEntry)
            val editorResult = backStackEntry.savedStateHandle
                .getStateFlow<String?>("md_result", null)
                .collectAsState().value
            CreatePollScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onPostCreated = {
                    navController.navigate(PostRoute.PostList.route) {
                        popUpTo("post_graph") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToEditor = { text ->
                    backStackEntry.savedStateHandle["md_input"] = text
                    navController.navigate(PostRoute.MarkdownEditor.route)
                },
                editorResult = editorResult,
                onEditorResultConsumed = {
                    backStackEntry.savedStateHandle.remove<String>("md_result")
                },
                viewModel = viewModel
            )
        }

        // ── Markdown Editor ───────────────────────────────────────────────
        composable(PostRoute.MarkdownEditor.route) {
            val initialText = navController.previousBackStackEntry
                ?.savedStateHandle?.get<String>("md_input") ?: ""
            MarkdownEditorScreen(
                initialText = initialText,
                onSave = { result ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle?.set("md_result", result)
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        
        // My Posts Screen
        composable(
            route = PostRoute.MyPosts.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val targetUserId = backStackEntry.arguments?.getString("userId")
            MyPostsScreen(
                targetUserId = targetUserId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditPost = { postId, postType ->
                    val route = when {
                        postType == com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType.PROJECT.displayName ->
                            PostRoute.CreateProject.createRoute(postId)
                        postType == com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType.EVENT.displayName ->
                            PostRoute.CreateEvent.createRoute(postId)
                        postType == com.exa.android.reflekt.loopit.data.feature.main.post.domain.model.PostType.POLL.displayName ->
                            PostRoute.CreatePoll.createRoute(postId)
                        else -> PostRoute.CreateSharePost.createRoute(postId)
                    }
                    navController.navigate(route)
                },
                onPostClick = { postId ->
                    navController.navigate(PostRoute.PostDetail.createRoute(postId))
                },
                onUserClick = { userId ->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                },
                onCreatePost = {
                    navController.navigate(PostRoute.CreatePostSelection.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Enrollment Management Screen
        composable(PostRoute.EnrollmentManagement.route) { backStackEntry ->
            com.exa.android.reflekt.loopit.presentation.main.post.screen.EnrollmentManagementScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Manage Enrollments Screen
        composable(PostRoute.ManageEnrollments.route) { backStackEntry ->
            com.exa.android.reflekt.loopit.presentation.main.post.screen.ManageEnrollmentsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Manage Registrations Screen
        composable(PostRoute.ManageRegistrations.route) { backStackEntry ->
            com.exa.android.reflekt.loopit.presentation.main.post.screen.ManageRegistrationsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Author Management Screen (New comprehensive management panel)
        composable(
            route = PostRoute.AuthorManagement.route,
            arguments = listOf(navArgument("postId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            AuthorManagementScreen(
                postId = postId,
                onBackClick = {
                    navController.popBackStack()
                },
                onUserClick = {userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                }
            )
        }

        // Map Screen
        composable("map_screen/{userIds}") { backStackEntry ->
            val userIds = backStackEntry.arguments?.getString("userIds") ?: ""
            RequestedPersonMapScreen(
                userIds = userIds,
                navController = navController,
//                openChat = {otherUserId->
//                    navController.navigate(HomeRoute.ChatDetail.createRoute(otherUserId))
//                }
            )
        }
    }
}

