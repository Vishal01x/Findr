package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph

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
import com.exa.android.reflekt.loopit.data.feature.main.post.data.viewmodel.PostListViewModel
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.DetailChat
import com.exa.android.reflekt.loopit.presentation.main.chat.HomeScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.MapScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ChatInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.MeetingRoute
import com.exa.android.reflekt.loopit.presentation.main.post.screen.CreatePostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.EditPostScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.ListedPostsScreen
import com.exa.android.reflekt.loopit.presentation.main.post.screen.PostDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.RequestedPersonMapScreen
import com.exa.android.reflekt.loopit.presentation.main.Map.MapSearchScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.core.network.PostListEvent
import com.exa.android.reflekt.loopit.presentation.main.chat.ChatDetail.component.ChatMediaScreen


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
        startDestination = MapInfo.MapScreen.route,
        route = "map_graph"
    ) {
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

        composable("searchScreen") {
            MapSearchScreen(
                viewModel = locationViewModel,
                onApplyFilters = {
                    navController.popBackStack()
                }
            )
        }
    }
}


fun NavGraphBuilder.projectNavGraph(navController: NavHostController) {
    //homeNavGraph(navController)
    navigation(
        startDestination = ProjectRoute.ProjectList.route,
        route = "project_graph"
    ) {
        composable(ProjectRoute.ProjectList.route) {
            val chatViewModel : ChatViewModel = hiltViewModel()
            ListedPostsScreen(
                navController = navController,
                onProjectClick = { projectId ->
                    navController.navigate(ProjectRoute.ProjectDetail.createRoute(projectId))
                }
            )
        }

        composable(ProjectRoute.ProjectDetail.route,
                deepLinks = listOf(navDeepLink { uriPattern = "findr://project/{projectId}" })
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            PostDetailScreen(
                projectId = projectId,
                navController = navController
            )
        }

        composable(ProjectRoute.CreateProject.route) {
            val projectViewModel: PostListViewModel = hiltViewModel()
            CreatePostScreen(
                onBack = {
                    navController.popBackStack()
                    projectViewModel.onEvent(PostListEvent.Refresh)
                },
                onProjectCreated = {
                    navController.popBackStack()
                    projectViewModel.onEvent(PostListEvent.Refresh)
                },
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(ProjectRoute.EditProject.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val projectViewModel: PostListViewModel = hiltViewModel()
            EditPostScreen(
                projectId = projectId,
                onBack = {
                    navController.popBackStack()
                    projectViewModel.onEvent(PostListEvent.Refresh)
                },
                onProjectUpdated = {
                    navController.popBackStack()
                    projectViewModel.onEvent(PostListEvent.Refresh)
                },
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        // In your navigation graph:
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

