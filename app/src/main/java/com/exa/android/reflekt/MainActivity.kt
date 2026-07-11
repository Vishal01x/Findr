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
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.exa.android.reflekt.loopit.core.env.EnvironmentManager
import com.exa.android.reflekt.loopit.core.env.ShakeDetector
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.debug.EnvironmentSwitcherSheet
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.core.utils.application.MyLifecycleObserver
import com.exa.android.reflekt.loopit.core.network.NetworkCallbackReceiver
import com.exa.android.reflekt.loopit.data.feature.main.chat.data.viewmodel.UserViewModel
import com.exa.android.reflekt.loopit.data.feature.main.map.data.worker.LocationForegroundService
import com.exa.android.reflekt.loopit.data.feature.main.map.data.worker.LocationWorker
import com.exa.android.reflekt.loopit.fcm.DeepLinkHelper
import com.exa.android.reflekt.loopit.presentation.navigation.nav_graph.AppNavigation
import com.exa.android.reflekt.loopit.presentation.navigation.component.HomeRoute
import dagger.hilt.android.AndroidEntryPoint
import com.exa.android.reflekt.loopit.core.notification.clearAllNotifications
import com.exa.android.reflekt.loopit.theme.FindrTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<Intent?>(intent)
    private lateinit var navController: NavController
    private lateinit var requestNotificationPermissionLauncher: ActivityResultLauncher<String>
    private var lifecycleObserver: MyLifecycleObserver? = null
    val authViewModel: AuthVM by viewModels()
    val userViewModel: UserViewModel by viewModels()
    val locationViewModel: LocationViewModel by viewModels()
    private var showEnvSheet by mutableStateOf(false)
//    private lateinit var shakeDetector: ShakeDetector

//    @Inject
//    lateinit var signalInitializer: SignalInitializer

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ObsoleteSdkInt", "BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

//        shakeDetector = ShakeDetector(this) { showEnvSheet = true }

        // WindowCompat.setDecorFitsSystemWindows(window, false)
        val curUser = userViewModel.curUser
        curUser?.let {
            // Initialize Signal Protocol keys
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    signalInitializer.initializeIfNeeded(it)
//                    Log.d("MainActivity", "Signal Protocol initialized for user: $it")
//                } catch (e: Exception) {
//                    Log.e("MainActivity", "Failed to initialize Signal Protocol", e)
//                }
//            }
//
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
            FindrTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    navController = rememberNavController()
                    App(currentIntent)
                    updateStatus(this)
                    if (EnvironmentManager.isStaging) {
                        StagingBadge()
                    }
                    if (showEnvSheet) {
                        EnvironmentSwitcherSheet(onDismiss = { showEnvSheet = false })
                    }
                }
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

    override fun onResume() {
        super.onResume()
//        shakeDetector.start()
    }

    override fun onPause() {
        super.onPause()
//        shakeDetector.stop()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent
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
        val loginState = authViewModel.loginState.value

        val navController = rememberNavController()

        // Handle initial intent when app is cold-started
        LaunchedEffect(Unit) {
            handleDeepLink(navController, this@MainActivity.intent)
        }

        // Handle new intents when app is already running
        LaunchedEffect(currentIntent) {
            if (currentIntent != null) {
                handleDeepLink(navController, currentIntent)
            }
        }

        val senderId = currentIntent?.data?.lastPathSegment

        AppNavigation(
            navController = navController,
            loginState = loginState,
            otherUserId = senderId
        )
    }

    private fun handleDeepLink(navController: NavController, intent: Intent) {
        com.google.firebase.dynamiclinks.FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener { pendingData ->
                val deepLink = pendingData?.link
                if (deepLink != null) {
                    com.exa.android.reflekt.loopit.fcm.DeepLinkHelper.handleDeepLink(navController, deepLink)
                } else {
                    navController.handleDeepLink(intent)
                }
            }
            .addOnFailureListener {
                navController.handleDeepLink(intent)
            }
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
fun StagingBadge() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 8.dp, top = 4.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(androidx.compose.ui.graphics.Color(0xFFFF6B00))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "STAGING",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
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
