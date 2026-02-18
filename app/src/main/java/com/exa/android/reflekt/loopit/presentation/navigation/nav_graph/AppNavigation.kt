package com.exa.android.reflekt.loopit.presentation.navigation.nav_graph


import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.ChatViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.CustomBottomNavigationBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.PostRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet

@Composable
fun AppNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    otherUserId: String? = null
) {
    if(isLoggedIn){
        val userViewModel : UserViewModel = hiltViewModel()
        LaunchedEffect(Unit) {
            userViewModel.subscribeTopics()
        }
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentDestination == HomeRoute.ChatList.route ||
                currentDestination == ProfileRoute.UserProfile.route ||
                currentDestination == MapInfo.MapScreen2.route ||
                currentDestination == PostRoute.PostList.route
            ) {

                val chatViewModel : ChatViewModel = hiltViewModel()

                CustomBottomNavigationBar(navController, chatViewModel) {
                    bottomSheet = true
                }
            }
        }
    ) { paddingValues ->
        RootNavGraph(
            navController = navController,
            isLoggedIn = isLoggedIn,
            modifier = Modifier.padding(paddingValues)
        )
    }
}


@Composable
fun RootNavGraph(
    navController: NavHostController,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val locationViewModel : LocationViewModel = hiltViewModel()

    val startDestination = if (isLoggedIn) MainRoute.ROOT else AuthRoute.ROOT

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainAppNavGraph(context, navController, locationViewModel)
    }
}

