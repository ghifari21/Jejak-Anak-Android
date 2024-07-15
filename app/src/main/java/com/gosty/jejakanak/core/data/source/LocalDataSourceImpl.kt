package com.gosty.jejakanak.core.data.source

import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.gosty.jejakanak.BuildConfig
import com.gosty.jejakanak.helpers.SharedPreferenceHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSourceImpl @Inject constructor(
    private val sharedPref: SharedPreferenceHelper,
    private val credentialManager: CredentialManager
) : LocalDataSource {
    override fun getUserRole(): String {
        return sharedPref.getValueString(BuildConfig.ROLE_KEY) ?: ""
    }

    override fun setUserRole(role: String) {
        sharedPref.putValueString(BuildConfig.ROLE_KEY, role)
    }

    override suspend fun removeCredential() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}