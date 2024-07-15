package com.gosty.jejakanak.core.di

import android.content.Context
import androidx.credentials.CredentialManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.gosty.jejakanak.core.data.source.FirebaseDataSource
import com.gosty.jejakanak.core.data.source.FirebaseDataSourceImpl
import com.gosty.jejakanak.core.data.source.LocalDataSource
import com.gosty.jejakanak.core.data.source.LocalDataSourceImpl
import com.gosty.jejakanak.helpers.SharedPreferenceHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    @Singleton
    fun provideFirebaseDataSource(
        database: FirebaseDatabase,
        auth: FirebaseAuth,
        crashlytics: FirebaseCrashlytics
    ): FirebaseDataSource = FirebaseDataSourceImpl(database, auth, crashlytics)

    @Provides
    @Singleton
    fun provideSharedPreferenceHelper(@ApplicationContext context: Context): SharedPreferenceHelper =
        SharedPreferenceHelper(context)

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)

    @Provides
    @Singleton
    fun provideLocalDataSource(
        sharedPreferenceHelper: SharedPreferenceHelper,
        credentialManager: CredentialManager
    ): LocalDataSource =
        LocalDataSourceImpl(sharedPreferenceHelper, credentialManager)
}