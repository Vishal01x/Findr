package com.exa.android.reflekt.loopit.presentation.navigation

import android.util.Log
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ProjectListViewModel
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.DetailChat
import com.exa.android.reflekt.loopit.presentation.main.Home.ChatDetail.ProfileScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.HomeScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Map.MapScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.SearchScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.ZoomPhoto
import com.exa.android.reflekt.loopit.presentation.navigation.component.ChatInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.MeetingRoute
import com.google.gson.Gson
import com.exa.android.reflekt.loopit.data.remote.main.meeting.call.CallScreen
import com.exa.android.reflekt.loopit.data.remote.main.meeting.lobby.LobbyScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.CreateProjectScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.EditProjectScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.ListedProjectsScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.ProjectDetailScreen
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.screen.RequestedPersonMapScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.util.application.ProjectListEvent


fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {

    navigation(startDestination = HomeRoute.ChatList.route, route = "home") {
        composable(HomeRoute.ChatList.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            val chatList = listOf("Vishal", "Kanhaiya", "Joe Tam", "Holder", "Smith Darklew")
            HomeScreen(navController, viewModel)
        }



        composable(HomeRoute.ZoomImage.route) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getString("imageId")
            val resourceId = imageId?.toIntOrNull() ?: R.drawable.ic_launcher_background
            ZoomPhoto(imageId = resourceId) {
                navController.popBackStack()
            }
        }

        composable(
            HomeRoute.ChatDetail.route,
            arguments = listOf(navArgument("userId") {
                type = NavType.StringType
            }),
            deepLinks = listOf(navDeepLink { uriPattern = "reflekt://chat/{userId}" })
        ) { backStackEntry ->
//            val encodedUserJson = backStackEntry.arguments?.getString("userJson")
//            val user = Gson().fromJson(URLDecoder.decode(encodedUserJson, "UTF-8"), User::class.java)
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            DetailChat(navController, userId) { users ->
                navController.navigate(MeetingRoute.LobbyScreen.createRoute(users))
            }
        }

        composable(HomeRoute.SearchScreen.route) {
            val viewModel: ChatViewModel = hiltViewModel()
            SearchScreen(navController, viewModel)
        }

        composable(
            route = MeetingRoute.LobbyScreen.route,
            arguments = listOf(navArgument("usersJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val usersJson = backStackEntry.arguments?.getString("usersJson") ?: "[]"
            val users = Gson().fromJson(usersJson, Array<String>::class.java).toList()
            LobbyScreen(navController = navController, users = users)
        }

        composable(MeetingRoute.CallScreen.route) {
            CallScreen(navController = navController)
        }

        chatInfoNavGraph(navController)
        mapNavGraph(navController)
        projectNavGraph(navController)
    }
}

fun NavGraphBuilder.chatInfoNavGraph(navController: NavHostController) {
    navigation(startDestination = ChatInfo.ProfileScreen.route, route = "chat") {
        composable(ChatInfo.ProfileScreen.route) {
            ProfileScreen(
//                "fjidjf",
//                onMediaClick = { navController.navigate(ChatInfo.ChatMedia.route) },
//                onCallClick = { navController.navigate(Call.VoiceCall.route) },
//                onMediaVisibilityClick = { navController.navigate(ChatInfo.MediaVisibility.route) },
//                onBlockClick = { navController.navigate(ChatInfo.BlockUser.route) }
            )
        }
        /*composable(ChatInfo.ChatMedia.route) { MediaScreen() }
        composable(ChatInfo.MediaVisibility.route) { MediaVisibilityScreen() }
        composable(ChatInfo.BlockUser.route) { BlockUserScreen() }
        composable(Call.VoiceCall.route) { CallScreen() }*/
    }
}

fun NavGraphBuilder.mapNavGraph(navController: NavHostController) {
    navigation(
        startDestination = MapInfo.MapScreen.route,
        route = "map_graph"
    ) {
        composable(MapInfo.MapScreen.route) {
            MapScreen(
//                "fjidjf",
//                onMediaClick = { navController.navigate(ChatInfo.ChatMedia.route) },
//                onCallClick = { navController.navigate(Call.VoiceCall.route) },
//                onMediaVisibilityClick = { navController.navigate(ChatInfo.MediaVisibility.route) },
//                onBlockClick = { navController.navigate(ChatInfo.BlockUser.route) }
//                openChat = { userId ->
//                    navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
//                }
            )
        }
        /*composable(ChatInfo.ChatMedia.route) { MediaScreen() }
        composable(ChatInfo.MediaVisibility.route) { MediaVisibilityScreen() }
        composable(ChatInfo.BlockUser.route) { BlockUserScreen() }
        composable(Call.VoiceCall.route) { CallScreen() }*/
    }
}


fun NavGraphBuilder.projectNavGraph(navController: NavHostController) {
    navigation(
        startDestination = ProjectRoute.ProjectList.route,
        route = "project_graph"
    ) {
        composable(ProjectRoute.ProjectList.route) {
            ListedProjectsScreen(
                navController = navController,
                onProjectClick = { projectId ->
                    navController.navigate(ProjectRoute.ProjectDetail.createRoute(projectId))
                }
            )
        }

        composable(ProjectRoute.ProjectDetail.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            ProjectDetailScreen(
                projectId = projectId,
                navController = navController
            )
        }

        composable(ProjectRoute.CreateProject.route) {
            val projectViewModel: ProjectListViewModel = hiltViewModel()
            CreateProjectScreen(
                onBack = {
                    navController.popBackStack()
                    projectViewModel.onEvent(ProjectListEvent.Refresh)
                },
                onProjectCreated = {
                    navController.popBackStack()
                    projectViewModel.onEvent(ProjectListEvent.Refresh)
                },
                viewModel = hiltViewModel(),
                navController = navController
            )
        }
        composable(ProjectRoute.EditProject.route) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val projectViewModel: ProjectListViewModel = hiltViewModel()
            EditProjectScreen(
                projectId = projectId,
                onBack = {
                    navController.popBackStack()
                    projectViewModel.onEvent(ProjectListEvent.Refresh)
                },
                onProjectUpdated = {
                    navController.popBackStack()
                    projectViewModel.onEvent(ProjectListEvent.Refresh)
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

