package com.gosty.jejakanak.helpers

import android.content.Context
import com.gosty.jejakanak.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferenceHelper @Inject constructor(
    private val context: Context
) {
    private val sharedPref =
        context.getSharedPreferences(BuildConfig.SHARED_PREF, Context.MODE_PRIVATE)
    private val editor = sharedPref.edit()

    fun putValueBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getValueBoolean(key: String): Boolean {
        return sharedPref.getBoolean(key, true)
    }

    fun putValueString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

    fun getValueString(key: String): String? {
        return sharedPref.getString(key, null)
    }

    fun putValueInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getValueInt(key: String): Int {
        return sharedPref.getInt(key, 0)
    }

    fun clear() {
        editor.clear().apply()
    }
}