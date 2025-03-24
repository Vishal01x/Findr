package com.exa.android.reflekt.loopit.presentation.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.exa.android.reflekt.R
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.ChatViewModel
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
import io.getstream.meeting.room.compose.ui.AppScreens
import io.getstream.meeting.room.compose.ui.call.CallScreen
import io.getstream.meeting.room.compose.ui.lobby.LobbyScreen


fun NavGraphBuilder.homeNavGraph(navController: NavHostController) {
    navigation(startDestination = HomeRoute.ChatList.route, route = "home") {
        composable(HomeRoute.ChatList.route) {
            val viewModel : ChatViewModel = hiltViewModel()
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
            arguments = listOf(navArgument("userId"){
                type = NavType.StringType
            })
        ) {backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            DetailChat(navController, userId!!){users->
                navController.navigate(MeetingRoute.LobbyScreen.createRoute(users))
            }
        }

        composable(HomeRoute.SearchScreen.route){
            val viewModel : ChatViewModel = hiltViewModel()
            SearchScreen(navController,viewModel)
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
            )
        }
        /*composable(ChatInfo.ChatMedia.route) { MediaScreen() }
        composable(ChatInfo.MediaVisibility.route) { MediaVisibilityScreen() }
        composable(ChatInfo.BlockUser.route) { BlockUserScreen() }
        composable(Call.VoiceCall.route) { CallScreen() }*/
    }
}

