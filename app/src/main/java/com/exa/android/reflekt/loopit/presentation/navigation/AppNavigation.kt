package com.exa.android.reflekt.loopit.presentation.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.reflekt.loopit.presentation.navigation.component.CustomBottomNavigationBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import io.getstream.meeting.room.compose.ui.MeetingRoomTheme
import io.getstream.video.android.compose.theme.VideoTheme

@Composable
fun AppNavigation(navController: NavHostController, isLoggedIn: Boolean) {
    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            if (currentDestination == HomeRoute.ChatList.route ||
                currentDestination == MainRoute.Profile.route) {
                CustomBottomNavigationBar(navController) {
                    bottomSheet = true
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) "main_app" else "auth",
            modifier = Modifier.padding(paddingValues).background(VideoTheme.colors.appBackground)
        ) {
            authNavGraph(navController)
            mainAppNavGraph(navController)
        }
    }
}




