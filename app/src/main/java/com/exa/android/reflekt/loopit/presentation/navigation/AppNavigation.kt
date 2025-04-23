package com.exa.android.reflekt.loopit.presentation.navigation


import android.content.Intent
import androidx.activity.compose.LocalActivity
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
import com.exa.android.reflekt.MainActivity
import com.exa.android.reflekt.loopit.presentation.main.Home.Listing.component.ProjectCard
import com.exa.android.reflekt.loopit.presentation.main.Home.Map.MapScreen
import com.exa.android.reflekt.loopit.presentation.main.profile.components.header.Profile
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.CustomBottomNavigationBar
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MapInfo
import com.exa.android.reflekt.loopit.presentation.navigation.component.ProfileRoute
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
    val activity = LocalActivity.current

    LaunchedEffect(otherUserId) {
        if (!otherUserId.isNullOrBlank()) {
            navController.navigate(HomeRoute.ChatDetail.createRoute(otherUserId)) {
//                popUpTo(HomeRoute.ChatList.route) {
//                    inclusive = false
//                }
                launchSingleTop = true
            }
//            // Clear the intent to avoid re-navigation on recomposition
//            activity?.intent = Intent(activity, MainActivity::class.java)
        }
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        bottomBar = {
            if (currentDestination == HomeRoute.ChatList.route ||
                currentDestination == ProfileRoute.UserProfile.route ||
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


