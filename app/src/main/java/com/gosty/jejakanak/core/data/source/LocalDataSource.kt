package com.gosty.jejakanak.core.data.source

interface LocalDataSource {
    fun getUserRole(): String

    fun setUserRole(role: String)

    suspend fun removeCredential()
}