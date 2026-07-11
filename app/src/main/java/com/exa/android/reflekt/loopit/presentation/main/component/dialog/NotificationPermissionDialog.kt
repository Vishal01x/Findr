package com.exa.android.reflekt.loopit.presentation.main.component.dialog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors

// ─────────────────────────────────────────────────────────────────────────────
// NotificationPermissionHandler
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Drop-in helper composable. Place near the top of the host Composable.
 *
 * All Compose hooks are called unconditionally (no early returns before hooks).
 * Logic is gated purely through state variables.
 *
 * Usage:
 * ```
 * var requestNotifPermission by remember { mutableStateOf(false) }
 * NotificationPermissionHandler(trigger = requestNotifPermission) {
 *     requestNotifPermission = false
 * }
 * // Then set requestNotifPermission = true on like / comment / post action.
 * ```
 */
@Composable
fun NotificationPermissionHandler(
    trigger: Boolean,
    onHandled: () -> Unit
) {
    val context = LocalContext.current

    // ── State (all declared unconditionally) ──────────────────────────────────
    var showSettingsDialog by remember { mutableStateOf(false) }
    // Tracks the last trigger value we launched the OS dialog for, so we
    // don't refire on every recomposition.
    var lastLaunchedTriggerVersion by remember { mutableStateOf(0) }
    var triggerVersion by remember { mutableStateOf(0) }

    // Bump internal version whenever the external trigger flips to true
    LaunchedEffect(trigger) {
        if (trigger) {
            triggerVersion++
        }
    }

    // ── Permission launcher (always registered) ───────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            // Permission denied — show the Settings-redirect dialog
            showSettingsDialog = true
        }
        onHandled()
    }

    // ── Gate: only run when a new trigger version has arrived ─────────────────
    LaunchedEffect(triggerVersion) {
        if (triggerVersion == 0) return@LaunchedEffect          // not yet triggered
        if (triggerVersion == lastLaunchedTriggerVersion) return@LaunchedEffect  // already handled

        lastLaunchedTriggerVersion = triggerVersion

        // On Android < 13 notifications are granted at install time — nothing to do
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onHandled()
            return@LaunchedEffect
        }

        val isGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (isGranted) {
            // Already granted — nothing to show
            onHandled()
        } else {
            // Launch OS permission dialog
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ── Settings-redirect dialog ──────────────────────────────────────────────
    if (showSettingsDialog) {
        NotificationPermissionDialog(
            onDismiss = { showSettingsDialog = false },
            onEnable = {
                showSettingsDialog = false
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NotificationPermissionDialog  ("Don't miss out on anything!")
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Shown when POST_NOTIFICATIONS is denied.
 * Guides the user to Settings > App > Notifications.
 *
 *  - [Not now]  outlined rectangular button
 *  - [Enable]   primary-colour filled button → opens app notification settings
 */
@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onEnable: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.85f
            )
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = LiquidColors.GlassBorder,
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                // Subtle top glow strip
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    LiquidColors.Primary.copy(alpha = 0.08f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Bell icon ────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        LiquidColors.Primary.copy(alpha = 0.25f),
                                        LiquidColors.Primary.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        LiquidColors.Primary.copy(alpha = 0.6f),
                                        LiquidColors.Primary.copy(alpha = 0.15f)
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = null,
                            tint = LiquidColors.Primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Headline ─────────────────────────────────────────────
                    Text(
                        text = "Don't miss out on anything!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = LiquidColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    // ── Subtitle ─────────────────────────────────────────────
                    Text(
                        text = "Stay in the loop with chat messages, post likes, comments, and more.",
                        fontSize = 14.sp,
                        color = LiquidColors.TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    // ── Settings path hint pill ───────────────────────────────
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(LiquidColors.Primary.copy(alpha = 0.07f))
                            .border(
                                1.dp,
                                LiquidColors.Primary.copy(alpha = 0.20f),
                                RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PathSegment("Settings")
                        PathSeparator()
                        PathSegment("Permissions")
                        PathSeparator()
                        PathSegment("Notifications")
                    }

                    Spacer(Modifier.height(28.dp))

                    // ── Buttons ───────────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Not now — outlined rectangular
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = LiquidColors.GlassBorder
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = LiquidColors.TextSecondary
                            )
                        ) {
                            Text(
                                text = "Not now",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }

                        // Enable — primary filled
                        Button(
                            onClick = onEnable,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LiquidColors.Primary,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text(
                                text = "Enable",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Private helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PathSegment(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = LiquidColors.Primary
    )
}

@Composable
private fun PathSeparator() {
    Text(
        text = "›",
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = LiquidColors.Primary.copy(alpha = 0.5f)
    )
}
