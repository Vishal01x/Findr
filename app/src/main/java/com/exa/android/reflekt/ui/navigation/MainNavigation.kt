package com.exa.android.reflekt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.exa.android.reflekt.GlobalScreen
import com.exa.android.reflekt.ProfileScreen
import com.exa.android.reflekt.model.ChatUser
import com.exa.android.reflekt.ui.chat.ChatScreen
import com.exa.android.reflekt.ui.home.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Global : Screen("global")
    object Profile : Screen("profile")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
}

@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val users = listOf(
        ChatUser(
            id = "1",
            name = "Alice Smith",
            imageUrl = "",
            lastMessage = "Hey, are you coming tomorrow?",
            unreadCount = 2,
            isOnline = true,
            lastSeen = ""
        ),
        ChatUser(
            id = "2",
            name = "Bob Johnson",
            imageUrl = "",
            lastMessage = "Thanks for the help!",
            unreadCount = 0,
            isOnline = false,
            lastSeen = ""
        )
    )
    val chat = ChatUser(
        id = "1",
        name = "John Doe",
        imageUrl = "",
        lastMessage = "Hello!",
        unreadCount = 2,
        isOnline = true,
        lastSeen = ""
    )
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onUserClicked = { user ->
                    navController.navigate(Screen.Chat.createRoute(user.id))
                },
                onNewChatClicked = { /* Handle new chat */ },
                users = users
            )
        }

        composable(Screen.Global.route) {
            GlobalScreen() // Implement similar to HomeScreen
        }

        composable(Screen.Profile.route) {
            ProfileScreen() // Implement profile UI
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            // Fetch user from repository
            ChatScreen(
                user = chat,
                onBackPressed = { navController.popBackStack() },
                onSendMessage = { message ->
                    // Handle message sending
                }
            )
        }
    }
}