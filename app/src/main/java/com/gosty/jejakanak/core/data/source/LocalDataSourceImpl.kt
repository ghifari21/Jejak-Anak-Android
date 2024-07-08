package com.gosty.jejakanak.core.data.source

import com.gosty.jejakanak.BuildConfig
import com.gosty.jejakanak.helpers.SharedPreferenceHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSourceImpl @Inject constructor(
    private val sharedPref: SharedPreferenceHelper
) : LocalDataSource {
    override fun getUserRole(): String {
        return sharedPref.getValueString(BuildConfig.ROLE_KEY) ?: ""
    }

    override fun setUserRole(role: String) {
        sharedPref.putValueString(BuildConfig.ROLE_KEY, role)
    }
}