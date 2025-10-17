package com.exa.android.reflekt.loopit.core.permission

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.exa.android.reflekt.loopit.core.utils.showToast

@Composable
fun RequestNotificationPermissionIfNeeded(
    isUserLoggedIn: Boolean
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
               // Log.d("Permission", "Notification permission granted")
            } else {
                //Log.e("Permission", "Notification permission denied")
                showToast(context, "You will not get chat notifications, To enable allow notification")
            }
        }
    )

    LaunchedEffect(isUserLoggedIn) {
        if (isUserLoggedIn &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            // Optional: show rationale
//            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
//                AlertDialog.Builder(context)
//                    .setTitle("Allow Notifications")
//                    .setMessage("We need permission to send chat notifications")
//                    .setPositiveButton("Allow") { _, _ ->
//                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                    }
//                    .setNegativeButton("Deny", null)
//                    .show()
//            } else {
//                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
        }
    }
}
