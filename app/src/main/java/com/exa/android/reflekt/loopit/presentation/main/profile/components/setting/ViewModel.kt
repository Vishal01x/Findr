package com.exa.android.reflekt.loopit.presentation.main.profile.components.setting

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    private val _appVersion = MutableStateFlow("1.0.0")
    val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    // Data & Storage
    private val _cacheSize = MutableStateFlow("15.2 MB")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    // Authentication states
    private val _passwordChangeSuccess = MutableStateFlow(false)
    val passwordChangeSuccess: StateFlow<Boolean> = _passwordChangeSuccess.asStateFlow()

    private val _passwordChangeError = MutableStateFlow<String?>(null)
    val passwordChangeError: StateFlow<String?> = _passwordChangeError.asStateFlow()

    private val _emailUpdateSuccess = MutableStateFlow(false)
    val emailUpdateSuccess: StateFlow<Boolean> = _emailUpdateSuccess.asStateFlow()

    private val _emailUpdateError = MutableStateFlow<String?>(null)
    val emailUpdateError: StateFlow<String?> = _emailUpdateError.asStateFlow()

    fun clearPasswordError() {
        _passwordChangeError.value = null
    }

    fun clearEmailError() {
        _emailUpdateError.value = null
    }


    init {
        viewModelScope.launch {
            loadPreferences()
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

    fun setPrivacyEnabled(enabled: Boolean) {
        _privacyEnabled.value = enabled
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                val credential = EmailAuthProvider.getCredential(
                    user?.email ?: "",
                    currentPassword
                )
                user?.reauthenticate(credential)?.await()
                user?.updatePassword(newPassword)?.await()
                _passwordChangeSuccess.value = true
                _passwordChangeError.value = null
            } catch (e: Exception) {
                _passwordChangeError.value = e.message
                _passwordChangeSuccess.value = false
            }
        }
    }

    fun updateEmail(newEmail: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                user?.updateEmail(newEmail)?.await()
                _emailUpdateSuccess.value = true
                _emailUpdateError.value = null
            } catch (e: Exception) {
                _emailUpdateError.value = e.message
                _emailUpdateSuccess.value = false
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // Implement actual cache clearing logic
            _cacheSize.value = "0 MB"
        }
    }

    companion object {
        private val DARK_THEME = booleanPreferencesKey("dark_theme")
        private val THEME_COLOR = intPreferencesKey("theme_color")
    }
}