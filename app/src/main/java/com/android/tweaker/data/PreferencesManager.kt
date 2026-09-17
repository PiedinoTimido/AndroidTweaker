package com.android.tweaker.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("android_tweaker_prefs", Context.MODE_PRIVATE)

    var isDisclaimerAccepted: Boolean
        get() = prefs.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
        set(value) = prefs.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, value).apply()

    var lastAdbPort: String
        get() = prefs.getString(KEY_ADB_PORT, "5555") ?: "5555"
        set(value) = prefs.edit().putString(KEY_ADB_PORT, value).apply()

    var lastPairPort: String
        get() = prefs.getString(KEY_PAIR_PORT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PAIR_PORT, value).apply()

    var lastPairCode: String
        get() = prefs.getString(KEY_PAIR_CODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PAIR_CODE, value).apply()

    var selectedLanguageCode: String
        get() = prefs.getString(KEY_APP_LANGUAGE, com.android.tweaker.model.AppLanguage.getSystemDefault().code)
            ?: com.android.tweaker.model.AppLanguage.getSystemDefault().code
        set(value) = prefs.edit().putString(KEY_APP_LANGUAGE, value).apply()

    companion object {
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
        private const val KEY_ADB_PORT = "last_adb_port"
        private const val KEY_PAIR_PORT = "last_pair_port"
        private const val KEY_PAIR_CODE = "last_pair_code"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}

