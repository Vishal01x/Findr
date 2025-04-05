package com.exa.android.reflekt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exa.android.reflekt.loopit.util.application.MyLifecycleObserver
import com.exa.android.reflekt.loopit.util.application.NetworkCallbackReceiver
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.presentation.navigation.AppNavigation
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import dagger.hilt.android.AndroidEntryPoint
import com.exa.android.reflekt.loopit.data.remote.main.meeting.MeetingRoomTheme
import com.exa.android.reflekt.loopit.presentation.main.test.MediaScreen
import com.exa.android.reflekt.loopit.util.clearAllNotifications

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavController  // Define navController at class level

    val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val curUser = userViewModel.curUser
        curUser?.let {
            val lifecycleObserver = MyLifecycleObserver(userViewModel, it)
            lifecycle.addObserver(lifecycleObserver)
        }

        setContent {
            MeetingRoomTheme {
                updateStatus(this)
                //App()
                MediaScreen()
            }
        }
        clearAllNotifications(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
//        val deepLinkUri = intent.data
//        deepLinkUri?.let {
//            val userId = it.lastPathSegment // Extract userId from notification
//            if (!userId.isNullOrEmpty()) {
//                navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
//            }
//        }
    }

    @Composable
    fun App() {
        val viewModel: UserViewModel = hiltViewModel()
        val curUser = viewModel.curUser
        val navController = rememberNavController()  // Assign instance to class property

        //val intent = Intent()
        val senderId = intent?.data?.lastPathSegment

        AppNavigation(navController, !curUser.isNullOrEmpty(), senderId)
    }
}

@Composable
fun GlobalScreen(modifier: Modifier = Modifier) {
    
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    
}



@Composable
fun updateStatus(context: Context) {
    val viewModel: UserViewModel = hiltViewModel()
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    DisposableEffect(Unit) {
        val callback = NetworkCallbackReceiver { connected ->
            viewModel.observeUserConnectivity()
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}


@Composable
fun OnBackPressed(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current

    BackHandler(true) {
        if (currentRoute == HomeRoute.ChatList.route) {
            (context as? Activity)?.finish()
        } else {
            navController.popBackStack()
        }
    }
}
