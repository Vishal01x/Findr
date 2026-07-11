package com.exa.android.reflekt.loopit.core.env

import android.content.Context
import android.content.SharedPreferences

object EnvironmentManager {

    enum class Environment { PRODUCTION, STAGING }

    private const val PREF_NAME = "app_environment"
    private const val KEY_ENV = "current_env"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var current: Environment
        get() = if (prefs.getString(KEY_ENV, Environment.PRODUCTION.name) == Environment.STAGING.name)
            Environment.STAGING else Environment.PRODUCTION
        set(value) {
            // commit() is synchronous — ensures the write survives process.killProcess() called right after
            prefs.edit().putString(KEY_ENV, value.name).commit()
        }

    val isStaging: Boolean get() = current == Environment.STAGING
}
