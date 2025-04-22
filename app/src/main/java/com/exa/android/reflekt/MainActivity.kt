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
import android.app.AlertDialog
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import com.exa.android.reflekt.loopit.presentation.navigation.AppNavigation
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import dagger.hilt.android.AndroidEntryPoint
import com.exa.android.reflekt.loopit.util.clearAllNotifications
import com.google.firebase.auth.FirebaseAuth
import io.getstream.meeting.room.compose.ui.AppTheme
import timber.log.Timber
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var navController: NavController  // Define navController at class level

    private var lifecycleObserver: MyLifecycleObserver? = null
    val userViewModel: UserViewModel by viewModels()
    val locationViewModel: LocationViewModel by viewModels()
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @SuppressLint("ObsoleteSdkInt", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val curUser = userViewModel.curUser
        curUser?.let {
            lifecycleObserver = MyLifecycleObserver(
                viewModel=userViewModel,
                userId=it,
                locationViewModel=locationViewModel,
                context = this
            )
            lifecycle.addObserver(lifecycleObserver!!)
            checkAndRequestLocationPermissions(it)
        }
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



        setContent {
            AppTheme (
                darkTheme = false, // ✅ Force light theme
                dynamicColor = false // ✅ Optional: prevent Material You theme changes
            ) {
              updateStatus(this)
               App()
            }
        }
        clearAllNotifications(this)
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
                LocationForegroundService.startService(applicationContext, userId)
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
//        val deepLinkUri = intent.data
//        deepLinkUri?.let {
//            val userId = it.lastPathSegment // Extract userId from notification
//            if (!userId.isNullOrEmpty()) {
//                navController.navigate(HomeRoute.ChatDetail.createRoute(userId))
//            }
//        }
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
    fun App() {
        val viewModel: AuthVM = hiltViewModel()
        val isLoggedIn = viewModel.loginState.value.loginSuccess
        val navController = rememberNavController()  // Assign instance to class property

        //val intent = Intent()
        val senderId = intent?.data?.lastPathSegment

        AppNavigation(navController, isLoggedIn, senderId)
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
