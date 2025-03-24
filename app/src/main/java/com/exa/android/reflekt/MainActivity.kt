package com.exa.android.reflekt

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import com.exa.android.reflekt.loopit.util.application.MyLifecycleObserver
import com.exa.android.reflekt.loopit.util.application.NetworkCallbackReceiver
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
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
               //MainNavigation()
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
    //OnBackPressed(navController = navController)
    AppNavigation(navController, isLoggedIn)
}


@Composable
fun OnBackPressed(navController: NavController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val context = LocalContext.current

    BackHandler(true) {
        if(currentRoute == HomeRoute.ChatList.route){
            (context as? Activity)?.finish()
        }else{
            navController.popBackStack()
        }
    }
}