package com.exa.android.reflekt

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
import android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE
import android.app.AlertDialog
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.LocationViewModel
import com.exa.android.reflekt.loopit.util.application.MyLifecycleObserver
import com.exa.android.reflekt.loopit.util.application.NetworkCallbackReceiver
import com.exa.android.reflekt.loopit.data.remote.main.ViewModel.UserViewModel
import com.exa.android.reflekt.loopit.data.remote.main.worker.LocationForegroundService
import com.exa.android.reflekt.loopit.data.remote.main.worker.LocationWorker
import com.exa.android.reflekt.loopit.fcm.DeepLinkHelper
import com.exa.android.reflekt.loopit.presentation.navigation.AppNavigation
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import dagger.hilt.android.AndroidEntryPoint
import com.exa.android.reflekt.loopit.util.clearAllNotifications
import io.getstream.meeting.room.compose.ui.AppTheme
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<Intent?>(intent)
    private lateinit var navController: NavController  // Define navController at class level
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>
    private var lifecycleObserver: MyLifecycleObserver? = null
    val userViewModel: UserViewModel by viewModels()
    val locationViewModel: LocationViewModel by viewModels()

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ObsoleteSdkInt", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // WindowCompat.setDecorFitsSystemWindows(window, false)
        val curUser = userViewModel.curUser
        curUser?.let {
            lifecycleObserver = MyLifecycleObserver(
                viewModel = userViewModel,
                userId = it,
                locationViewModel = locationViewModel,
                context = this
            )
            lifecycle.addObserver(lifecycleObserver!!)
            checkAndRequestLocationPermissions(it)
        }
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val packageName = packageName
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                } catch (e: Exception) {
                    // fallback to app settings if the above doesn't work
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        }

         */

//        requestNotificationPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted: Boolean ->
//            if (isGranted) {
//               // Log.d("Permission", "Notification permission granted")
//                // requestSystemAlertPermission() // If needed
//            } else {
//             //   Log.e("Permission", "Notification permission denied")
//            }
//        }

        // Then call this function to request it
        //requestNotificationPermissionIfNeeded()


        setContent {
            AppTheme(
                darkTheme = false, // ✅ Force light theme
                dynamicColor = false // ✅ Optional: prevent Material You theme changes
            ) {
                navController = rememberNavController()
                App(currentIntent)
                updateStatus(this)
            }
        }
        clearAllNotifications(this)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Show custom rationale if needed before requesting
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    AlertDialog.Builder(this)
                        .setTitle("Notification Permission")
                        .setMessage("We need this permission to send you important updates.")
                        .setPositiveButton("Allow") { _, _ ->
                            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else {
                    requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }


    @SuppressLint("InlinedApi")
    private fun checkAndRequestLocationPermissions(userId: String) {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.FOREGROUND_SERVICE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requiredPermissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
        }

        when {
            hasAllPermissions(requiredPermissions) -> {
                // Permissions already granted
                if (isAppInForeground()) {
                    LocationForegroundService.startService(applicationContext, userId)
                }
                scheduleLocationWorker(userId)
            }

            shouldShowPermissionRationale(requiredPermissions) -> {
                showPermissionRationaleDialog(userId)
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    requiredPermissions.toTypedArray(),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun isAppInForeground(): Boolean {
        val appProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(appProcessInfo)
        return appProcessInfo.importance == IMPORTANCE_FOREGROUND ||
                appProcessInfo.importance == IMPORTANCE_VISIBLE
    }

    @SuppressLint("InlinedApi")
    private fun showPermissionRationaleDialog(userId: String) {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Needed")
            .setMessage("This app needs location permissions to provide location-based services")
            .setPositiveButton("OK") { _, _ ->
                val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.FOREGROUND_SERVICE_LOCATION
                    )
                } else {
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.FOREGROUND_SERVICE
                    )
                }

                ActivityCompat.requestPermissions(
                    this,
                    requiredPermissions,
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()
            .show()
    }

    private fun scheduleLocationWorker(userId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        val inputData = workDataOf("USER_ID" to userId)

        val locationWorkRequest = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "LocationTracking",
            ExistingPeriodicWorkPolicy.UPDATE,
            locationWorkRequest
        )
    }


    private fun hasAllPermissions(permissions: List<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun shouldShowPermissionRationale(permissions: List<String>): Boolean {
        return permissions.any {
            ActivityCompat.shouldShowRequestPermissionRationale(this, it)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent // Update the state

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                userViewModel.curUser?.let { userId ->
                    LocationForegroundService.startService(applicationContext, userId)
                    scheduleLocationWorker(userId)
                }
            } else {
                Toast.makeText(
                    this,
                    "Location features will be limited without permissions",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @Composable
    fun App(currentIntent: Intent?) {
        val viewModel: AuthVM = hiltViewModel()
        val isLoggedIn = viewModel.loginState.value.loginSuccess
        val navController = rememberNavController()  // Assign instance to class property
        //val currentIntent = rememberUpdatedState(LocalActivity.current).value?.intent

//        // Handle initial intent when app is cold-started
//        LaunchedEffect(Unit) {
//            navController.handleDeepLink(this@MainActivity.currentIntent)
//        }
//
//        // Handle new intents when app is already running
//        LaunchedEffect(currentIntent) {
//            navController.handleDeepLink(currentIntent)
//        }

        // Handle initial intent when app is cold-started
        LaunchedEffect(Unit) {
            handleDeepLink(navController, this@MainActivity.intent)
        }

        // Handle new intents when app is already running
        LaunchedEffect(currentIntent) {
            if (currentIntent != null) {
                //handleDeepLink(navController, currentIntent)
                handleDeepLink(navController,currentIntent)
            }
        }

        val senderId = currentIntent?.data?.lastPathSegment
        //val intent = Intent()
        //val senderId = intent?.data?.lastPathSegment


        AppNavigation(navController, isLoggedIn, senderId)
    }

    private fun handleDeepLink(navController: NavController, intent: Intent) {
//        intent.data?.let { uri ->
//            if (uri.toString().startsWith("findr://chat")) {
//                val userId = uri.lastPathSegment ?: return@let
//                navController.navigate(HomeRoute.ChatDetail.createRoute(userId)) {
//                    popUpTo(HomeRoute.ChatList.route) { inclusive = false }
//                    launchSingleTop = true
//                }
//            }
//        }

        navController.handleDeepLink(intent) // since deep link navigation is slow keep direct
    }


    private fun handleDeepLinkNav(navController: NavController,intent: Intent) {
        intent.data?.let { uri ->
            DeepLinkHelper.handleDeepLink(navController, uri)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.N)
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
