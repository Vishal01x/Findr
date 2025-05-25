package com.exa.android.reflekt.loopit.presentation.navigation


import android.content.Intent
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
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
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


