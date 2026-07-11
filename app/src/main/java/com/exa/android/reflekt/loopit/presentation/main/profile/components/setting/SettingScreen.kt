package com.exa.android.reflekt.loopit.presentation.main.profile.components.setting

import com.exa.android.reflekt.library_ui.modifier.clickableWithNoIndication
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.feature.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.data.feature.main.map.data.viewmodel.LocationViewModel
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.openUrl
import com.exa.android.reflekt.loopit.core.utils.showToast
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.AppDialog
import com.exa.android.reflekt.loopit.presentation.main.component.dialog.ConfirmDialog
import kotlinx.coroutines.launch
import timber.log.Timber
import android.Manifest
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.exa.android.reflekt.loopit.theme.GradientPrimaryEnd
import com.exa.android.reflekt.loopit.theme.GradientPrimaryStart
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.firebase.auth.FirebaseAuth
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidColors
import com.exa.android.reflekt.loopit.presentation.main.placement.components.LiquidMeshBackground
import com.exa.android.reflekt.loopit.presentation.main.placement.components.liquidTopBarColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogOutClick: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthVM = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel()

) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // States
    val darkMode by settingsViewModel.darkMode.collectAsState()
    val themeColor by settingsViewModel.themeColor.collectAsState()
    val privacyEnabled by settingsViewModel.privacyEnabled.collectAsState()
    val appVersion by settingsViewModel.appVersion.collectAsState()
    val cacheSize by settingsViewModel.cacheSize.collectAsState()

    // For dialogs
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showUpdateEmailDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var pendingNewEmail by remember { mutableStateOf<String?>(null) }

    // Error states
    val passwordError by settingsViewModel.passwordChangeError.collectAsState()
    val emailError by settingsViewModel.emailUpdateError.collectAsState()
    var canDeleteAccount by remember { mutableStateOf(true) }

    // Strict-mode current-password verification (shared by change-password & update-email)
    val currentPasswordVerified by settingsViewModel.currentPasswordVerified.collectAsState()
    val verifyingCurrentPassword by settingsViewModel.verifyingCurrentPassword.collectAsState()
    val currentPasswordError by settingsViewModel.currentPasswordError.collectAsState()
    val changingPassword by settingsViewModel.changingPassword.collectAsState()
    val passwordChangeSuccess by settingsViewModel.passwordChangeSuccess.collectAsState()

    // Update email: verification-link flow
    val emailVerificationSent by settingsViewModel.emailVerificationSent.collectAsState()
    val updatingEmail by settingsViewModel.updatingEmail.collectAsState()

    val locationPermissionState = rememberPermissionState(permission = Manifest.permission.ACCESS_FINE_LOCATION)
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    LaunchedEffect(cacheSize) {
        settingsViewModel.loadCacheSize(context)
    }

    LaunchedEffect(passwordError) {
        passwordError?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearPasswordError()
        }
    }

    LaunchedEffect(emailError) {
        emailError?.let {
            snackbarHostState.showSnackbar(it)
            settingsViewModel.clearEmailError()
        }
    }

    LaunchedEffect(passwordChangeSuccess) {
        if (passwordChangeSuccess) {
            showChangePasswordDialog = false
            settingsViewModel.resetCurrentPasswordVerification()
            snackbarHostState.showSnackbar("Password changed successfully")
        }
    }

    // Once the verification link is sent, poll until the user actually clicks it in their inbox
    LaunchedEffect(emailVerificationSent, pendingNewEmail) {
        val newEmail = pendingNewEmail
        if (emailVerificationSent && newEmail != null) {
            settingsViewModel.checkEmailUpdateVerification(newEmail) {
                showUpdateEmailDialog = false
                pendingNewEmail = null
                scope.launch {
                    snackbarHostState.showSnackbar("Email address updated successfully")
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = liquidTopBarColors()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            val feedbackFormUrl =
                "https://docs.google.com/forms/d/e/1FAIpQLSeprJ-ajG7DFUWgVHMVy7gglkcScwNDfx_NixnZsFZGXlPmBQ/viewform?usp=sharing"

            item {
                SectionHeader(title = "Feedback")
            }

            item {
                Text(
                    text = "We’d love to hear your thoughts!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }


            item {
                Text(
                    text = "Tell us what you think about our app. Whether it’s a suggestion, something you’d love to see, expectations we should meet, bugs or issues you’ve encountered — we’re here to listen and improve!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }

            item {
                Text(
                    " -> Fill out form",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickableWithNoIndication { openUrl(context, feedbackFormUrl) })
            }

            // Privacy
            item { SectionHeader("Privacy") }

            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Security,
                    title = "Location Mode",
                    checked = privacyEnabled,
                    onCheckedChange = { checked ->
                        settingsViewModel.setPrivacyEnabled(checked)
                        Timber.tag("Geofire").d("Location Mode Enabled: $checked")
                        if (checked) {
                            //locationViewModel.startLocationUpdates(null, context)
                            // Optionally save to persistent storage
                            if (locationPermissionState.status.isGranted) {
                                userId?.let {
                                    locationViewModel.startLocationUpdates(it)
                                }
                            }
                            showToast(context, "Explore & Connect")
                        } else {
                            locationViewModel.stopLocationUpdates()
                            // Optionally save to persistent storage
                            showToast(context, "Now you cannot be visible on Map")

                        }
                    }
                )

            }

            // Account Settings
            item { SectionHeader("Account") }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = {
                        settingsViewModel.resetCurrentPasswordVerification()
                        showChangePasswordDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Update Email Address",
                    onClick = {
                        pendingNewEmail = null
                        settingsViewModel.resetEmailUpdateFlow()
                        showUpdateEmailDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    onClick = {
                        authViewModel.logout()
                        onLogOutClick()
                    }
                )
            }

//            item {
//                SettingsItem(
//                    icon = Icons.AutoMirrored.Filled.Logout,
//                    title = "Delete Account",
//                    onClick = {
//                        if(canDeleteAccount){
//                            settingsViewModel.deleteUserAccount()
//                            onDeleteAccount()
//                        }else {
//                            showToast(context, "This will permanently delete your account")
//                            canDeleteAccount = false
//                        }
//                    }
//                )
//            }


            // Appearance
//            item { SectionHeader("Appearance") }
//
//            item {
//                SwitchSettingsItem(
//                    icon = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
//                    title = "Dark Mode",
//                    checked = darkMode,
//                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
//                )
//            }
//
//            item {
//                SettingsItem(
//                    icon = Icons.Default.Palette,
//                    title = "Theme Color",
//                    value = themeColor.displayName,
//                    onClick = { showThemeDialog = true }
//                )
//            }

            // Data & Storage
            item { SectionHeader("Data & Storage") }

            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Clear Cache",
                    value = cacheSize,
                    onClick = {
                        settingsViewModel.clearCache(context)
                        scope.launch {
                            snackbarHostState.showSnackbar("Cache cleared successfully")
                        }
                    }
                )
            }

            // Help & Support
            item { SectionHeader("Help & Support") }

            item {
                SettingsItem(
                    icon = Icons.Default.Help,
                    title = "Help Center",
                    onClick = { onNavigate("help") }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Terms & Privacy",
                    onClick = { onNavigate("terms") }
                )
            }

            // About
            item { SectionHeader("About") }

            item {
                SettingsItem(
                    icon = Icons.Default.Android,
                    title = "App Version",
                    value = appVersion,
                    onClick = {}
                )
            }
            val playStoreLink =
                "https://play.google.com/store/apps/details?id=com.exa.android.reflekt"
            item {
                SettingsItem(
                    icon = Icons.Default.Update,
                    title = "Update the App",
                    onClick = { openUrl(context, playStoreLink) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(LiquidColors.GlassSurface)
                        .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "A Note from Us",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hey! Findr started with one simple idea — college gets a lot easier when you're " +
                            "not doing it alone.\n\n" +
                            "So we built a place to find your people: teammates for that project due next week, " +
                            "a group for your doubts, seniors who'll give you the real scoop on placements, " +
                            "people to build your next big idea with, or even someone to shoot your event when " +
                            "you're short a photographer. Whatever you need on campus, chances are someone " +
                            "around you can help — you just need to find them.\n\n" +
                            "Findr is our way of bringing that campus close, one connection at a time. Made by " +
                            "students, for students, with a lot of love (and a few too many all-nighters).\n\n" +
                            "— The Findr Team",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }

    // Dialogs
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isVerified = currentPasswordVerified,
            isVerifying = verifyingCurrentPassword,
            isChanging = changingPassword,
            verifyError = currentPasswordError,
            onVerifyCurrentPassword = { current -> settingsViewModel.verifyCurrentPassword(current) },
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { new -> settingsViewModel.changePassword(new) }
        )
    }

    if (showUpdateEmailDialog) {
        UpdateEmailDialog(
            currentEmail = authViewModel.currentUser?.email ?: "",
            isVerified = currentPasswordVerified,
            isVerifying = verifyingCurrentPassword,
            verifyError = currentPasswordError,
            isSendingLink = updatingEmail,
            verificationSent = emailVerificationSent,
            pendingNewEmail = pendingNewEmail,
            onVerifyCurrentPassword = { current -> settingsViewModel.verifyCurrentPassword(current) },
            onDismiss = {
                showUpdateEmailDialog = false
                pendingNewEmail = null
            },
            onRequestEmailUpdate = { newEmail ->
                pendingNewEmail = newEmail
                settingsViewModel.requestEmailUpdate(newEmail)
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = themeColor,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                settingsViewModel.setThemeColor(theme)
                showThemeDialog = false
            }
        )
    }
}

/**
 * Strict mode: the current password must be verified (re-authenticated against Firebase) before
 * the new-password fields even unlock. Mirrors the "verify first, then act" flow used elsewhere.
 */
@Composable
fun ChangePasswordDialog(
    isVerified: Boolean,
    isVerifying: Boolean,
    isChanging: Boolean,
    verifyError: String?,
    onVerifyCurrentPassword: (current: String) -> Unit,
    onDismiss: () -> Unit,
    onChangePassword: (new: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AppDialog(
        onDismissRequest = onDismiss,
        title = "Change Password",
        content = {
            Text(
                text = if (isVerified) "Step 2 of 2 — choose a new password" else "Step 1 of 2 — verify your current password",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password") },
                visualTransformation = PasswordVisualTransformation(),
                enabled = !isVerified,
                trailingIcon = {
                    if (isVerified) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            (verifyError ?: error)?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            if (isVerified) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmText = when {
            !isVerified -> if (isVerifying) "Verifying..." else "Verify Current Password"
            isChanging -> "Changing..."
            else -> "Change Password"
        },
        isLoading = isVerifying || isChanging,
        onConfirm = {
            if (!isVerified) {
                onVerifyCurrentPassword(currentPassword)
                return@AppDialog
            }
            if (newPassword != confirmPassword) {
                error = "Passwords don't match"
            } else if (newPassword.length < 6) {
                error = "Password must be at least 6 characters"
            } else {
                error = null
                onChangePassword(newPassword)
            }
        },
        onDismiss = onDismiss
    )
}

/**
 * Update email: same current-password re-verification as change-password, then sends a
 * verification link to the NEW address (same Firebase mechanism as sign-up email verification).
 * The address only actually changes once that link is opened, so the dialog waits/polls.
 */
@Composable
fun UpdateEmailDialog(
    currentEmail: String,
    isVerified: Boolean,
    isVerifying: Boolean,
    verifyError: String?,
    isSendingLink: Boolean,
    verificationSent: Boolean,
    pendingNewEmail: String?,
    onVerifyCurrentPassword: (current: String) -> Unit,
    onDismiss: () -> Unit,
    onRequestEmailUpdate: (newEmail: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AppDialog(
        onDismissRequest = onDismiss,
        title = "Update Email Address",
        content = {
            when {
                verificationSent && pendingNewEmail != null -> {
                    Text(
                        text = "We've sent a verification link to $pendingNewEmail. Open it to confirm the change — this will update automatically once you do.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Waiting for verification...", style = MaterialTheme.typography.bodySmall)
                    }
                }
                !isVerified -> {
                    Text(
                        text = "Step 1 of 2 — verify your current password",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Current email: $currentEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    verifyError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    Text(
                        text = "Step 2 of 2 — enter your new email address",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("Current email: $currentEmail", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("New Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmText = when {
            verificationSent && pendingNewEmail != null -> "Waiting..."
            !isVerified -> if (isVerifying) "Verifying..." else "Verify Current Password"
            isSendingLink -> "Sending..."
            else -> "Send Verification Link"
        },
        isLoading = isVerifying || isSendingLink || (verificationSent && pendingNewEmail != null),
        showDismissButton = !(verificationSent && pendingNewEmail != null),
        onConfirm = {
            if (verificationSent && pendingNewEmail != null) return@AppDialog
            if (!isVerified) {
                onVerifyCurrentPassword(currentPassword)
                return@AppDialog
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                error = "Please enter a valid email"
            } else if (newEmail.equals(currentEmail, ignoreCase = true)) {
                error = "This is already your current email"
            } else {
                error = null
                onRequestEmailUpdate(newEmail)
            }
        },
        onDismiss = onDismiss
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeColor,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeColor) -> Unit
) {
    AppDialog(
        onDismissRequest = onDismiss,
        title = "Select Theme",
        content = {
            ThemeColor.values().forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickableWithNoIndication { onThemeSelected(theme) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = theme == currentTheme,
                        onClick = { onThemeSelected(theme) }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = theme.displayName)
                }
            }
        },
        confirmText = "Done",
        onConfirm = onDismiss,
        showDismissButton = false,
        onDismiss = onDismiss
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .clickableWithNoIndication(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (title != "Logout" && title != "Delete Account") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (title == "Delete Account") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (title != "Logout") {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SwitchSettingsItem(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(LiquidColors.GlassSurface)
            .border(1.dp, LiquidColors.GlassBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

enum class ThemeColor(
    val color: Color,
    val displayName: String
) {
    DEFAULT(GradientPrimaryStart, "Default"),
    BLUE(Color(0xFF0D47A1), "Blue"),
    GREEN(Color(0xFF3E6E4D), "Green"),
    PURPLE(Color(0xFFB94E41), "Orange");

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeColor {
            return values().getOrElse(ordinal) { DEFAULT }
        }
    }
}

object ThemeManager {
    private val _darkTheme = mutableStateOf(false)
    val darkTheme: State<Boolean> = _darkTheme

    private val _themeColor = mutableStateOf(ThemeColor.DEFAULT)
    val themeColor: State<ThemeColor> = _themeColor

    fun setDarkTheme(enabled: Boolean) {
        _darkTheme.value = enabled
        // Save to SharedPreferences or DataStore
    }

    fun setThemeColor(color: ThemeColor) {
        _themeColor.value = color
        // Save to SharedPreferences or DataStore
    }
}