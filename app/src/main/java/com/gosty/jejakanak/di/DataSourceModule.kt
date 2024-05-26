package com.gosty.jejakanak.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.gosty.jejakanak.data.source.FirebaseDataSource
import com.gosty.jejakanak.data.source.FirebaseDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
}