package com.exa.android.reflekt.loopit.presentation.main.profile.components.setting

import android.content.Context
import android.util.Log
import com.exa.android.reflekt.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exa.android.reflekt.loopit.core.env.AppCollections
import com.exa.android.reflekt.loopit.core.network.Response
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    // Appearance
    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    private val _themeColor = MutableStateFlow(ThemeColor.DEFAULT)
    val themeColor: StateFlow<ThemeColor> = _themeColor.asStateFlow()

    // Privacy
    private val _privacyEnabled = MutableStateFlow(true)
    val privacyEnabled: StateFlow<Boolean> = _privacyEnabled.asStateFlow()

    // App Info
    private val _appVersion = MutableStateFlow("${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    // Data & Storage
    private val _cacheSize = MutableStateFlow("15.2 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    // Authentication states
    private val _passwordChangeSuccess = MutableStateFlow(false)
    val passwordChangeSuccess: StateFlow<Boolean> = _passwordChangeSuccess.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError.asStateFlow()

    private val _accoutDeleteError = MutableStateFlow<String?>(null)
    val accountDeleteError: StateFlow<String?> = _accoutDeleteError.asStateFlow()

    private val _accoutDeleteSuccess =  MutableStateFlow(false)
    val accountDeleteSucsess: StateFlow<Boolean> = _accoutDeleteSuccess.asStateFlow()

    private val _emailUpdateSuccess = MutableStateFlow(false)
    val emailUpdateSuccess: StateFlow<Boolean> = _emailUpdateSuccess.asStateFlow()

    private val _emailUpdateError = MutableStateFlow<String?>(null)
    val emailUpdateError: StateFlow<String?> = _emailUpdateError.asStateFlow()

    // Strict mode: current password re-verification (used by both change-password and update-email flows)
    private val _currentPasswordVerified = MutableStateFlow(false)
    val currentPasswordVerified: StateFlow<Boolean> = _currentPasswordVerified.asStateFlow()

    private val _verifyingCurrentPassword = MutableStateFlow(false)
    val verifyingCurrentPassword: StateFlow<Boolean> = _verifyingCurrentPassword.asStateFlow()

    private val _currentPasswordError = MutableStateFlow<String?>(null)
    val currentPasswordError: StateFlow<String?> = _currentPasswordError.asStateFlow()

    private val _changingPassword = MutableStateFlow(false)
    val changingPassword: StateFlow<Boolean> = _changingPassword.asStateFlow()

    // Update email: verification-link flow (mirrors the sign up / login email verification)
    private val _emailVerificationSent = MutableStateFlow(false)
    val emailVerificationSent: StateFlow<Boolean> = _emailVerificationSent.asStateFlow()

    private val _updatingEmail = MutableStateFlow(false)
    val updatingEmail: StateFlow<Boolean> = _updatingEmail.asStateFlow()

    fun clearPasswordError() {
        _passwordChangeError.value = null
    }

    fun clearEmailError() {
        _emailUpdateError.value = null
    }

    /** Resets the strict-mode current-password verification, e.g. when a dialog is (re)opened. */
    fun resetCurrentPasswordVerification() {
        _currentPasswordVerified.value = false
        _currentPasswordError.value = null
        _passwordChangeSuccess.value = false
    }

    /** Resets the update-email flow, e.g. when the dialog is (re)opened. */
    fun resetEmailUpdateFlow() {
        _emailVerificationSent.value = false
        _emailUpdateError.value = null
        _emailUpdateSuccess.value = false
        resetCurrentPasswordVerification()
    }

    /** Step 1 of strict mode: re-authenticate with the current password before allowing any change. */
    fun verifyCurrentPassword(currentPassword: String) {
        viewModelScope.launch {
            _verifyingCurrentPassword.value = true
            _currentPasswordError.value = null
            try {
                val user = auth.currentUser
                val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
                user?.reauthenticate(credential)?.await()
                _currentPasswordVerified.value = true
            } catch (e: Exception) {
                _currentPasswordVerified.value = false
                _currentPasswordError.value = "Current password is incorrect"
            } finally {
                _verifyingCurrentPassword.value = false
            }
        }
    }


    init {
        viewModelScope.launch {
           // loadPreferences()
            loadLocationModePref()
        }
    }

    fun loadLocationModePref(){
        // Load the saved value when ViewModel is created
        viewModelScope.launch {
            dataStore.data
                .map { preferences ->
                    preferences[PreferencesKeys.PRIVACY_ENABLED] ?: true
                }
                .collect { enabled ->
                    _privacyEnabled.value = enabled
                }
        }
    }

    fun setPrivacyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.PRIVACY_ENABLED] = enabled
            }
        }
    }


    private fun loadPreferences() {
        viewModelScope.launch {
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .collect { preferences ->
                    _darkMode.value = preferences[DARK_THEME] ?: false
                    val themeOrdinal = preferences[THEME_COLOR] ?: 0
                    _themeColor.value = ThemeColor.fromOrdinal(themeOrdinal.coerceAtLeast(0))
                }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            _darkMode.value = enabled
            dataStore.edit { preferences ->
                preferences[DARK_THEME] = enabled
            }
            ThemeManager.setDarkTheme(enabled)
        }
    }

    fun setThemeColor(color: ThemeColor) {
        viewModelScope.launch {
            _themeColor.value = color
            dataStore.edit { preferences ->
                preferences[THEME_COLOR] = color.ordinal
            }
            ThemeManager.setThemeColor(color)
        }
    }

//    fun setPrivacyEnabled(enabled: Boolean) {
//        _privacyEnabled.value = enabled
//    }

    /** Step 2 of strict mode: only meant to be called once [currentPasswordVerified] is true. */
    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            _changingPassword.value = true
            try {
                val user = auth.currentUser
                user?.updatePassword(newPassword)?.await()
                _passwordChangeSuccess.value = true
                _passwordChangeError.value = null
            } catch (e: Exception) {
                _passwordChangeError.value = e.message
                _passwordChangeSuccess.value = false
            } finally {
                _changingPassword.value = false
            }
        }
    }

    /**
     * Step 2 of the update-email flow: sends a verification link to the NEW email address
     * (same mechanism as sign-up email verification). The address on the account only changes
     * once the user opens that link, so we don't flip [emailUpdateSuccess] here - callers should
     * poll with [checkEmailUpdateVerification] instead.
     */
    fun requestEmailUpdate(newEmail: String) {
        viewModelScope.launch {
            _updatingEmail.value = true
            try {
                val user = auth.currentUser
                user?.verifyBeforeUpdateEmail(newEmail)?.await()
                _emailVerificationSent.value = true
                _emailUpdateError.value = null
            } catch (e: Exception) {
                _emailUpdateError.value = e.message
                _emailVerificationSent.value = false
            } finally {
                _updatingEmail.value = false
            }
        }
    }

    /** Polls until the account's email actually flips to [newEmail] after the link is clicked. */
    fun checkEmailUpdateVerification(newEmail: String, onVerified: () -> Unit) {
        viewModelScope.launch {
            while (true) {
                auth.currentUser?.reload()?.await()
                if (auth.currentUser?.email?.equals(newEmail, ignoreCase = true) == true) {
                    _emailUpdateSuccess.value = true
                    onVerified()
                    break
                }
                delay(3000)
            }
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cacheDir = context.cacheDir
                deleteDir(cacheDir)

                withContext(Dispatchers.Main) {
                    _cacheSize.value = "0 MB"
                    // Optionally show a toast/snackbar here if needed
                }
            } catch (e: Exception) {
                Log.e("ClearCache", "Error clearing cache", e)
            }
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (child in children!!) {
                val success = deleteDir(File(dir, child))
                if (!success) return false
            }
        }
        return dir?.delete() ?: false
    }

    fun loadCacheSize(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val sizeInBytes = getFolderSize(context.cacheDir)
            val sizeInMB = sizeInBytes.toDouble() / (1024 * 1024)
            val formattedSize = String.format("%.2f MB", sizeInMB)

            withContext(Dispatchers.Main) {
                _cacheSize.value = formattedSize
            }
        }
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        dir.walkBottomUp().forEach {
            if (it.isFile) size += it.length()
        }
        return size
    }

    fun deleteUserAccount(currentPassword: String){
        viewModelScope.launch {
            deleteAccountCompletely(currentPassword)
        }
    }

    suspend fun deleteAccountCompletely(currentPassword: String): Response<Unit> {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser ?: return Response.Error("No user logged in")

        return try {
            // Step 1: Re-authenticate
            val credential = EmailAuthProvider.getCredential(user.email ?: "", currentPassword)
            user.reauthenticate(credential).await()

            val userId = user.uid

            // Step 2: Delete Firestore user document
            FirebaseFirestore.getInstance().collection(AppCollections.USERS)
                .document(userId).delete().await()

//            // Step 3: Delete profile picture in Firebase Storage if used
//            val storageRef = FirebaseStorage.getInstance().reference
//                .child("profile_pictures/$userId.jpg")
//            runCatching { storageRef.delete().await() }

            // Step 4: Delete the user from Firebase Auth
            user.delete().await()

            _accoutDeleteSuccess.value = true
            _accoutDeleteError.value = null
            Response.Success(Unit)

        } catch (e: Exception) {
            _accoutDeleteError.value = e.localizedMessage
            _accoutDeleteSuccess.value = false

            Response.Error(e.localizedMessage ?: "Error deleting account")
        }
    }


    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val THEME_COLOR = intPreferencesKey("theme_color")
    }
}
// PreferencesKeys.kt
object PreferencesKeys {
    val PRIVACY_ENABLED = booleanPreferencesKey("privacy_enabled")
}

