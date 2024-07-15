package com.gosty.jejakanak.core.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}