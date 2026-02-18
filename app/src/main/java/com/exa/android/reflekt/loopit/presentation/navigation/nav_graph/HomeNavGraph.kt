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
import com.exa.android.reflekt.loopit.presentation.main.Map.UserListScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.ChatMediaScreen
import com.exa.android.reflekt.loopit.presentation.main.post.create.CreatePostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.AuthorManagementScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.EditPostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.MyPostsScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostHomeScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute


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
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "findr://chat/{userId}" })
        ) { backStackEntry ->
//            val encodedUserJson = backStackEntry.arguments?.getString("userJson")
//            val user = Gson().fromJson(URLDecoder.decode(encodedUserJson, "UTF-8"), User::class.java)
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DetailChat(navController, userId) { users ->
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
        composable(MapInfo.UserListScreen.route) {
            UserListScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }
        
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
        composable("userListScreen") {
            UserListScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }

//        // New Bottom Sheet Map Screen (Experimental)
//        composable(MapInfo.MapScreenBottomSheet.route) {
//            MapScreenBottomSheet(
//                navController = navController,
//                viewModel = locationViewModel
//            )
//        }
        
        // User list screen - also accessible via simple route
        composable("userListScreen") {
            UserListScreen(
                navController = navController,
                viewModel = locationViewModel
            )
        }
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
                }
            )
        }

        // Post Detail Screen
        composable(
            PostRoute.PostDetail.route,
            deepLinks = listOf(navDeepLink { uriPattern = "findr://project/{projectId}" })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("projectId") ?: ""
            PostDetailScreen(
                postId,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = { postId ->
                    navController.navigate(PostRoute.EditPost.createRoute(postId))
                },
                onManageRequestsClick = { postId ->
                    navController.navigate(PostRoute.AuthorManagement.createRoute(postId))
                },
                onUserClick = {userId->
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
                }

            )
        }

        // Create Post Screen
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
        
        // My Posts Screen
        composable(PostRoute.MyPosts.route) {
            MyPostsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onEditPost = { postId ->
                    navController.navigate(PostRoute.EditPost.createRoute(postId))
                },
                onPostClick = {postId->
                    navController.navigate(PostRoute.PostDetail.createRoute(postId))
                },
                onUserClick = { userId ->
                    // Navigate to user profile when implemented
                    navController.navigate(ProfileRoute.UserProfile.createRoute(userId))
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

        
        // Edit Post Screen
        composable(PostRoute.EditPost.route) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            EditPostScreen(
                postId = postId,
                onBackClick = {
                    navController.popBackStack()
                },
                onPostUpdated = {
                    navController.popBackStack()
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

