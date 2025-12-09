package com.exa.android.reflekt.loopit.data.feature.main.map.data.worker

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

        sharedPreferences = try {
            EncryptedSharedPreferences.create(
                context,
                "secure_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Handle corrupted encrypted preferences (common after app reinstall or keystore reset)
            // Delete the corrupted file and recreate
            when (e) {
                is java.security.GeneralSecurityException,
                is javax.crypto.AEADBadTagException -> {
                    // Delete corrupted preferences file
                    context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .clear()
                        .commit()
                    
                    // Delete the actual encrypted file
                    context.deleteSharedPreferences("secure_prefs")
                    
                    // Recreate with fresh encryption
                    EncryptedSharedPreferences.create(
                        context,
                        "secure_prefs",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    )
                }
                else -> throw e
            }
        }
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