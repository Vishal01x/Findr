package com.exa.android.reflekt.loopit.data.remote.main.worker

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceHelper @Inject constructor(context: Context) {

    // Using EncryptedSharedPreferences for security
    private val sharedPreferences: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveUserId(userId: String) {
        sharedPreferences.edit().putString("CURRENT_USER_ID", userId).apply()
    }

    fun getUserId(): String? {
        return sharedPreferences.getString("CURRENT_USER_ID", null)
    }

    fun clearUserId() {
        sharedPreferences.edit().remove("CURRENT_USER_ID").apply()
    }
    fun setLocationUpdatesRunning(userId: String, isRunning: Boolean) {
        sharedPreferences.edit().putBoolean("LOCATION_UPDATES_$userId", isRunning).apply()
    }

    fun getLocationUpdatesRunning(userId: String): Boolean {
        return sharedPreferences.getBoolean("LOCATION_UPDATES_$userId", false)
    }
}