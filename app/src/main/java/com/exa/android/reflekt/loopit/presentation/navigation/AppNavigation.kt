package com.exa.android.reflekt.loopit.presentation.navigation


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
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
//            // Clewar the intent to avoid re-navigation on recomposition
//            activity?.intent = Intent(activity, MainActivity::class.java)
        }
    }

    val currentDestination = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(

        //modifier = Modifier
            //.fillMaxSize(),
            //.consumeWindowInsets(PaddingValues()), // Optional to manually control insets
            //.padding(WindowInsets.systemBars.asPaddingValues()),


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
            modifier = Modifier.padding(paddingValues)//.navigationBarsPadding()
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
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) MainRoute.ROOT else AuthRoute.ROOT,
        modifier = modifier
    ) {
        authNavGraph(navController)
        mainAppNavGraph(context,navController, locationViewModel)
    }
}


