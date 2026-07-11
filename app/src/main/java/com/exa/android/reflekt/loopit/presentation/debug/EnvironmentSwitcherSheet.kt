package com.exa.android.reflekt.loopit.presentation.debug

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exa.android.reflekt.loopit.core.env.EnvironmentManager
import com.exa.android.reflekt.loopit.core.env.EnvironmentManager.Environment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvironmentSwitcherSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val current = EnvironmentManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Switch Environment",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "App will restart to apply changes",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            EnvOption(
                label = "Production",
                description = "Live Firebase data • Google Sign-In",
                icon = Icons.Default.Cloud,
                accentColor = Color(0xFF1E5FFF),
                isSelected = current == Environment.PRODUCTION,
                onClick = {
                    if (current != Environment.PRODUCTION) {
                        EnvironmentManager.current = Environment.PRODUCTION
                        restartApp(context as Activity)
                    } else {
                        onDismiss()
                    }
                }
            )

            Spacer(Modifier.height(12.dp))

            EnvOption(
                label = "Staging",
                description = "test_ collections • Email/Password auth",
                icon = Icons.Default.BugReport,
                accentColor = Color(0xFFFF6B00),
                isSelected = current == Environment.STAGING,
                onClick = {
                    if (current != Environment.STAGING) {
                        EnvironmentManager.current = Environment.STAGING
                        restartApp(context as Activity)
                    } else {
                        onDismiss()
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Current: ${current.name}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EnvOption(
    label: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) accentColor else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickableWithNoIndication(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Active",
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun restartApp(activity: Activity) {
    val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName) ?: return
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    activity.startActivity(intent)
    activity.finishAffinity()
    // killProcess is more reliable than Runtime.exit() — avoids race with the new activity launch
    android.os.Process.killProcess(android.os.Process.myPid())
}
