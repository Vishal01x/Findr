package com.exa.android.reflekt.loopit.presentation.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.ProjectCard
import com.exa.android.reflekt.loopit.presentation.main.Home.Map.MapScreen
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.CustomBottomNavigationBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProjectRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.bottomSheet
import io.getstream.video.android.compose.theme.VideoTheme

@Composable
fun AppNavigation(
    navController: NavHostController,
    isLoggedIn: Boolean,
    otherUserId: String? = null
) {
    //OnBackPressed(navController)

    LaunchedEffect(otherUserId) {
        otherUserId?.let {
            navController.navigate(HomeRoute.ChatDetail.createRoute(otherUserId)) {
                popUpTo(HomeRoute.ChatList.route) {
                    inclusive = false
                } // Ensure HomeScreen is in the back stack
                launchSingleTop = true  // Avoid creating duplicate instances
            }

        }
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            if (currentDestination == HomeRoute.ChatList.route ||
                currentDestination == MainRoute.Profile.route ||
                currentDestination == MapInfo.MapScreen.route ||
                currentDestination == ProjectRoute.ProjectList.route
            ) {
                CustomBottomNavigationBar(navController) {
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

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainRoute.ROOT else AuthRoute.ROOT,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainAppNavGraph(context,navController)
    }
}


