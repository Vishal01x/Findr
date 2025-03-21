package com.exa.android.reflekt

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exa.android.reflekt.loopit.application.MyLifecycleObserver
import com.exa.android.reflekt.loopit.application.NetworkCallbackReceiver
import com.exa.android.reflekt.loopit.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.mvvm.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.AppNavigation
import com.exa.android.reflekt.loopit.presentation.navigation.component.AuthRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import com.exa.android.reflekt.loopit.presentation.navigation.component.MainRoute
import com.exa.android.reflekt.ui.navigation.MainNavigation
import com.exa.android.reflekt.ui.theme.ReflektTheme
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.meeting.room.compose.ui.MeetingRoomTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    val userViewModel : UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()

        val curUser = userViewModel.curUser

        curUser?.let {
            val lifecycleObserver = MyLifecycleObserver(userViewModel, it)
            lifecycle.addObserver(lifecycleObserver)
        }

        setContent {
            MeetingRoomTheme{
                updateStatus(this)
                App()
               // MainNavigation()
            }
        }
    }
}

@Composable
fun GlobalScreen() {
    // Implement similar to HomeScreen with different data
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Global Connections", style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun ProfileScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Add profile UI components
        Text("Profile Screen", style = MaterialTheme.typography.titleLarge)
    }
}


@Composable
fun updateStatus(context : Context) {
    val viewModel : UserViewModel = hiltViewModel()
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    DisposableEffect(Unit) {
        val callback = NetworkCallbackReceiver{connected->
            viewModel.observeUserConnectivity()
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}

@Composable
fun App() {

//    val link = intent?.data?.toString()  // Extract deep link here
//    val email = intent?.getStringExtra("email")  // Extract email if needed
//
//    link?.let {
//        // Verify the sign-in link when the app is opened from a deep link
//        if (email != null) {
//            viewModel.verifySignInLink(email, it)
//        }

    val viewModel: AuthVM = hiltViewModel()
    val isLoggedIn = viewModel.authStatus.collectAsState().equals(true)
    val navController = rememberNavController()
    OnBackPressed(navController = navController)
    AppNavigation(navController, isLoggedIn)
}


@Composable
fun OnBackPressed(navController: NavController) {
    // Handle back press based on the current screen
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // Observe the current destination route
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current
    //Log.d("currentBackStackEntry->onBack", currentRoute.toString())

    // Listen for the back press event
    BackHandler {
        when (currentRoute) {
            MainRoute.Profile.route -> { // it helps to get rid of loops for home and profile screen
                navController.navigate(HomeRoute.ChatList.route) {
                    popUpTo(HomeRoute.ChatList.route) { inclusive = true }
                }
            }
            HomeRoute.ChatList.route -> {
                // Close the app only if we are on the Home screen
                (context as? Activity)?.finish()
            }
            AuthRoute.Login.route -> {
                // Allow default back button behavior for login screen (closing app)
                (context as? Activity)?.finish()
            }
            else -> {
                // If on other screens, navigate back normally
                navController.popBackStack()
            }
        }
    }

}