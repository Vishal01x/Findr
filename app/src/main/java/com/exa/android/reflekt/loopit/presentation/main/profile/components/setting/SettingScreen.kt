package com.exa.android.reflekt.loopit.presentation.main.profile.components.setting

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exa.android.reflekt.loopit.data.remote.authentication.vm.AuthVM
import com.exa.android.reflekt.loopit.presentation.main.profile.components.extra_card.openUrl
import com.exa.android.reflekt.loopit.theme.Purple40
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onLogOutClick : () -> Unit,
    modifier: Modifier = Modifier,
    authViewModel: AuthVM = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),

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

    // Error states
    val passwordError by settingsViewModel.passwordChangeError.collectAsState()
    val emailError by settingsViewModel.emailUpdateError.collectAsState()

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


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Account Settings
            item { SectionHeader("Account") }

            item {
                SettingsItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    onClick = { showChangePasswordDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Email,
                    title = "Update Email Address",
                    onClick = { showUpdateEmailDialog = true }
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

            val feedbackFormUrl = "https://docs.google.com/forms/d/e/1FAIpQLSeprJ-ajG7DFUWgVHMVy7gglkcScwNDfx_NixnZsFZGXlPmBQ/viewform?usp=sharing"

            item {
                SectionHeader(title = "Feedback")
            }

            item {
                Text(
                    text = "We’d love to hear your thoughts!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onTertiary
                )
            }


            item {
                Text(
                    text = "Tell us what you think about our app. Whether it's a suggestion, something you'd love to see, expectations we should meet, bugs or issues you've encountered — we're here to listen and improve!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiary,
                    lineHeight = 20.sp
                )
            }

            item {
                Text(" -> Fill out form", modifier = Modifier.clickable { openUrl(context, feedbackFormUrl) })
            }

            // Privacy
            item { SectionHeader("Privacy") }

            item {
                SwitchSettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Mode",
                    checked = privacyEnabled,
                    onCheckedChange = { settingsViewModel.setPrivacyEnabled(it) }
                )
            }

            // Appearance
            item { SectionHeader("Appearance") }

            item {
                SwitchSettingsItem(
                    icon = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    title = "Dark Mode",
                    checked = darkMode,
                    onCheckedChange = { settingsViewModel.setDarkMode(it) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme Color",
                    value = themeColor.displayName,
                    onClick = { showThemeDialog = true }
                )
            }

            // Data & Storage
            item { SectionHeader("Data & Storage") }

            item {
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Clear Cache",
                    value = cacheSize,
                    onClick = {
                        settingsViewModel.clearCache()
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
            val updateLink = "https://drive.google.com/drive/u/1/folders/12zo8h7J_Utjk7OPgXldpaRBLsJoXKaLr"
            item {
                SettingsItem(
                    icon = Icons.Default.Update,
                    title = "Update the App",
                    onClick = { openUrl(context, updateLink) }
                )
            }
        }
    }

    // Dialogs
    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onChangePassword = { current, new ->
                settingsViewModel.changePassword(current, new)
                showChangePasswordDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Password changed successfully")
                }
            }
        )
    }

    if (showUpdateEmailDialog) {
        UpdateEmailDialog(
            currentEmail = authViewModel.currentUser?.email ?: "",
            onDismiss = { showUpdateEmailDialog = false },
            onUpdateEmail = { newEmail ->
                settingsViewModel.updateEmail(newEmail)
                showUpdateEmailDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("Email updated successfully")
                }
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

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onChangePassword: (current: String, new: String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPassword != confirmPassword) {
                        error = "Passwords don't match"
                    } else if (newPassword.length < 6) {
                        error = "Password must be at least 6 characters"
                    } else {
                        onChangePassword(currentPassword, newPassword)
                    }
                }
            ) {
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun UpdateEmailDialog(
    currentEmail: String,
    onDismiss: () -> Unit,
    onUpdateEmail: (String) -> Unit
) {
    var newEmail by remember { mutableStateOf(currentEmail) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Email Address") },
        text = {
            Column {
                Text("Current email: $currentEmail")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = newEmail,
                    onValueChange = { newEmail = it },
                    label = { Text("New Email Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                        error = "Please enter a valid email"
                    } else {
                        onUpdateEmail(newEmail)
                    }
                }
            ) {
                Text("Update Email")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeColor,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeColor) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            Column {
                ThemeColor.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
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
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
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
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if(title != "Logout" )MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        if(title != "Logout") {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
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
            .padding(vertical = 12.dp, horizontal = 8.dp),
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
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

enum class ThemeColor(
    val color: Color,
    val displayName: String
) {
    DEFAULT(Purple40, "Default"),
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